## 2024-05-22 - [Compose: Sorting in Recomposition]
**Learning:** Performing list sorting directly inside a Composable function (or as an argument to `items`) causes the sort to run on every recomposition, even if the data hasn't changed. This is an $O(N \log N)$ operation that can cause dropped frames.
**Action:** Always wrap expensive list operations (sorting, filtering) in `remember` keyed by the source data.
