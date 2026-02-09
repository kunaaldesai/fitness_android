## 2024-05-22 - [Compose: Sorting in Recomposition]
**Learning:** Performing list sorting directly inside a Composable function (or as an argument to `items`) causes the sort to run on every recomposition, even if the data hasn't changed. This is an $O(N \log N)$ operation that can cause dropped frames.
**Action:** Always wrap expensive list operations (sorting, filtering) in `remember` keyed by the source data.

## 2024-05-23 - [Compose: Argument Computation in Lazy Lists]
**Learning:** Computations passed as arguments to Composables inside `items` (e.g., `WorkoutPlanCard(highlight = plan.toHighlight())`) run on every recomposition of the item if not memoized. If `toHighlight` returns a new object instance (data class), it breaks skipping for the child Composable, forcing it to recompose even if the source data hasn't changed.
**Action:** Wrap derived state computations for list items in `remember(item) { item.computeDerivedState() }` to ensure stable arguments and enable skipping.
