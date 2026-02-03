# JULES TO DO LIST
## INTRO
Hello Jules. This is the file where you will be able to see the to do list for scheduled tasks. If there is nothing listed here, then do not do anything for this run. You will also see a bugs section. Fix the bugs if there are bugs listed there.


## TO DO 
### Task 1 (Daily Task)
- Make sure the folder structure and instructions in README.md are up to date. If not, then make a PR and update it.
### Task 2 (Daily Task)
- Validate that automated tests pass and keep mocks aligned with backend shapes.
- Run unit tests: `./gradlew test` (use the Android Studio JBR if `JAVA_HOME` is required).
- Run UI tests only with a device/emulator: `./gradlew connectedAndroidTest`.
- Keep mock payloads in tests consistent with the backend endpoints defined in `workouts.py`, `users.py`, and `workouts_helpers.py` (field names, types, optional values).
- Do not hit live databases or external services; use MockK or local fakes only.
- If a backend field changes, update the test fixtures and assertions first, then re-run the suite.

## BUGS
### Bug 1 (IMPORTANT)
- HTTP 403 pop up on the bottom when opening the homescreen. Usually when scrolling all the way to the bottom. Sometimes when closing the app and reopening.
- Profile page is empty. FULLY redesign the profile page so it looks like the rest of the app. Use Google Stitch to design it, I have it connected in Jules. So you have access to it. And then create it.
