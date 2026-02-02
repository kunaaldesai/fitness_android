# Palette's Journal

## 2024-10-26 - Semantic Navigation Tabs
**Learning:** Custom bottom navigation bars built with `clickable` Columns lack semantic meaning for screen readers. They are announced as generic buttons without "selected" state.
**Action:** Use `Modifier.selectable` with `role = Role.Tab` and explicit `selected` state for navigation items. Silence redundant Icon descriptions if a Text label is present.
