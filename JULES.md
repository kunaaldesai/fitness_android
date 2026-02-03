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
- When I scroll down on the homepage and then start to scroll up, it jumps to the top. It should be a smooth scrolling experience. I should be able to scroll all the way down and scroll all the way up. It's like it's loading cards randomly only when it appears on screen. Very odd. Fix this. This happens on the homepage and also the explore page. 