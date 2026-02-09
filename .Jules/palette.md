# Palette's Journal

## 2024-10-26 - Semantic Navigation Tabs
**Learning:** Custom bottom navigation bars built with `clickable` Columns lack semantic meaning for screen readers. They are announced as generic buttons without "selected" state.
**Action:** Use `Modifier.selectable` with `role = Role.Tab` and explicit `selected` state for navigation items. Silence redundant Icon descriptions if a Text label is present.

## 2024-10-27 - Accessible Selection Controls
**Learning:** Custom selection chips using `clickable` lack semantic state (selected/checked) for screen readers.
**Action:** Use `Modifier.selectable` with `Role.RadioButton` for single-select and `Modifier.toggleable` with `Role.Checkbox` for multi-select components.

## 2024-10-28 - Keyboard Navigation in Repetitive Forms
**Learning:** Repetitive numeric input forms (like sets/reps) create friction if users must manually navigate between fields. Default keyboard actions often close the keyboard.
**Action:** Explicitly configure `KeyboardOptions(imeAction = ImeAction.Next)` for intermediate fields and `ImeAction.Done` for the final field to enable seamless data entry flow.
