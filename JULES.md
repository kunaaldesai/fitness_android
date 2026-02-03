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
- After I press save, "loading workout details" it says could not load details try again immediately. something is wrong here. Doesn't do anything. the data is saved in the database though. so what's the issue? look at the backend workouts.py, workouts_helpers.py, users.py. workouts.py is relevant one. don't edit it. but maybe see if you're not using the get and update endpoints properly?
- The data is saved in the database so why does it not appear in Recent Activity -> See All screen aka the workout history screen. Maybe this is related to the first bug though.
- HTTP 403 pop up on the bottom when opening the homescreen. Usually when scrolling all the way to the bottom. Sometimes when closing the app and reopening.