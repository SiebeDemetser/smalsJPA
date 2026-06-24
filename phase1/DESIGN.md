# Phase 1 — Domain Model Design Decisions

## 1. Availability count: denormalized counter vs. derived count

**Choice:** Denormalized `availableCopies` counter on `Book`, protected by a `@Version` optimistic-lock field.

**Why the counter wins here:** Deriving availability requires `SELECT COUNT(*) FROM loans WHERE book_id = ? AND status IN ('ACTIVE','OVERDUE')`. Under normal library load every borrow-page render fires this query, and every borrowing transaction must re-run it inside the same transaction to prevent TOCTOU—meaning two concurrent borrows of the last copy both see count = 1 and both proceed. Fixing that race at the derived-count layer requires `SELECT ... FOR UPDATE` on the book row (pessimistic) or `REPEATABLE READ` isolation (which serializes far more than needed). By keeping a denormalized counter and protecting decrements with `@Version`, a conflicting decrement is detected immediately at commit with `OptimisticLockException`, and the retry logic is contained in one place: `LoanService.borrow()`. The cost we accept is that every borrow and return must update the counter; that update is already happening inside the same transaction that creates or closes the loan, so there is no extra round-trip.

## 2. Money as a value type

**Choice:** `@Embeddable Money` with a `BigDecimal amount` and a `String currency` (3-letter ISO 4217 code). The `add()` method enforces same-currency arithmetic and throws on mismatch.

**Why a raw BigDecimal loses:** `BigDecimal` has no currency dimension. Nothing in the type system prevents `book.getPrice().add(book.getTotalCopies())` or a comparison between a EUR price and a USD price. The compiler cannot catch those bugs, and a runtime `if (!currency.equals(other.currency))` buried inside a service is easily forgotten. By making `Money` a first-class value type, the `add` and `isGreaterThan` methods are the only arithmetic surface and they enforce currency compatibility at call-time rather than at review-time.

## 3. Derived vs. stored member status

**Choice:** `membershipExpiry` (stored date) and `suspendedUntil` (stored nullable date) are the ground truth. `getStatus()` derives `ACTIVE`, `SUSPENDED`, or `EXPIRED` from those two dates at call time.

**Why storing a status flag loses:** A stored `status` column can disagree with the dates the moment time passes midnight. A member whose `membershipExpiry` was yesterday but whose status column still reads `ACTIVE` is a data integrity violation that no constraint can prevent without a scheduled job or a trigger. By storing only the dates and computing the status on demand, the two values literally cannot contradict each other: `EXPIRED` is true by definition once `LocalDate.now().isAfter(membershipExpiry)`. Suspension and reinstatement are still explicit operations (`suspend(until)` / `reinstate()`) because they represent a deliberate librarian action, but their effect is stored as a date, not a flag.

## 4. Loan lifecycle as a state machine

**Choice:** `LoanStatus` enum with values `REQUESTED`, `ACTIVE`, `OVERDUE`, `RETURNED`. Transitions are exposed only through named methods (`activate()`, `markOverdue()`, `markReturned()`). There is no `setStatus()` method.

**Why a free-form status column loses:** If `status` has a public setter, nothing stops a caller from writing `loan.setStatus(RETURNED)` without setting `returnDate`, or from transitioning `REQUESTED → RETURNED` directly, skipping loan-counter updates. The state machine encapsulates every invariant: `markReturned()` throws if the loan is already returned (idempotent protection), sets `returnDate` atomically, and is the only path that ever assigns `RETURNED`. `markOverdue()` only accepts an `ACTIVE` loan, so an already-overdue loan cannot be double-transitioned. The compiler guarantees that no code path reaches a status without going through a method that enforces the precondition.