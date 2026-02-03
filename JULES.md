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
- After clicking start workout, i dont need to see a pop up saying template loaded locally. Just do it, but dont announce it.
- After i press save on a workout, it still has the pop up. 
- After I press save, "loading workout details" has the loading icon forever. It says loading. Doesn't do anything. 
- The data is saved in the database so why does it not appear in Recent Activity -> See All screen aka the workout history screen
- HTTP 403 when going to homescreen sometimes