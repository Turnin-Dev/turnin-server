# Test Case Strategy

Cover every distinct behavior once. No redundant cases, no gaps.

## INCLUDE

- Each distinct code branch and outcome: success paths and each error path.
- Each unique return value and each exception type the target can produce.
- For routes: a separate case per concrete status code (400, 401, 403, 404, ...). Never merge them and never write "4xx".
- Validation: one failing case per independent constraint, with every other input valid so the rejection has only one possible cause. Plus at least one case where everything is valid.
- Boundaries: for each declared limit, the nearest valid and nearest invalid value.
  - Example: max length 2,200 → 2,200 chars is valid, 2,201 is invalid.
  - Example: `x > 0` → 0 is invalid, 1 is valid.
- Branches inside private functions: reach them through the public function with different inputs.

## EXCLUDE

- Input variations inside the same equivalence class (expired 5 days ago vs 10 days ago).
- Collection size variations (1, 2, 3 items) unless the code has explicit size-dependent logic.
- Speculative cases (exotic Unicode, huge payloads) unless the code handles them explicitly.
- Null arguments for non-nullable Kotlin parameters. The compiler already rejects them. Test null only for `T?` parameters.
- Behavior that belongs to another layer. A use case test does not re-test repository SQL; a route test does not re-test use case rules.

## Decision rule

Two cases are **not** duplicates just because they end in the same exception or status. Keep both when each checks a condition that can break independently.

- A deleted account and a blocked account both reject the request with the same exception → keep both.
- Blocked 1 day ago and blocked 30 days ago → one representative is enough.

Ask: "If this case were deleted, could a real regression go unnoticed?" If no, drop it.
