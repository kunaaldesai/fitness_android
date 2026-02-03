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
- The Gradle Daemon says there's a bunch of stuff that's deprecated. Can you fix that? ALL of the functionality should remain the same and the same should also look and feel the same. I just don't want all these errors. It runs fine, but I'm worried about it. I'll paste the example output below:

Executing tasks: [:app:assembleDebug] in project /Users/kun/AndroidStudioProjects/FitnessTracker

Starting Gradle Daemon...
Gradle Daemon started in 681 ms

> Configure project :app
AGPBI: {"kind":"warning","text":"The option setting 'android.usesSdkInManifest.disallowed=false' is deprecated.\nThe current default is 'true'.\nIt will be removed in version 10.0 of the Android Gradle plugin.","sources":[{}]}
AGPBI: {"kind":"warning","text":"The option setting 'android.sdk.defaultTargetSdkToCompileSdkIfUnset=false' is deprecated.\nThe current default is 'true'.\nIt will be removed in version 10.0 of the Android Gradle plugin.","sources":[{}]}
AGPBI: {"kind":"warning","text":"The option setting 'android.enableAppCompileTimeRClass=false' is deprecated.\nThe current default is 'true'.\nIt will be removed in version 10.0 of the Android Gradle plugin.","sources":[{}]}
AGPBI: {"kind":"warning","text":"The option setting 'android.builtInKotlin=false' is deprecated.\nThe current default is 'true'.\nIt will be removed in version 10.0 of the Android Gradle plugin.","sources":[{}]}
AGPBI: {"kind":"warning","text":"The option setting 'android.newDsl=false' is deprecated.\nThe current default is 'true'.\nIt will be removed in version 10.0 of the Android Gradle plugin.","sources":[{}]}
AGPBI: {"kind":"warning","text":"The option setting 'android.r8.optimizedResourceShrinking=false' is deprecated.\nThe current default is 'true'.\nIt will be removed in version 10.0 of the Android Gradle plugin.","sources":[{}]}
AGPBI: {"kind":"warning","text":"The option setting 'android.defaults.buildfeatures.resvalues=true' is deprecated.\nThe current default is 'false'.\nIt will be removed in version 10.0 of the Android Gradle plugin.","sources":[{}]}
AGPBI: {"kind":"warning","text":"The property android.dependency.excludeLibraryComponentsFromConstraints improves project import performance for very large projects. It should be enabled to improve performance.\nTo suppress this warning, add android.generateSyncIssueWhenLibraryConstraintsAreEnabled=false to gradle.properties","sources":[{}]}
AGPBI: {"kind":"warning","text":"The property android.dependency.excludeLibraryComponentsFromConstraints improves project import performance for very large projects. It should be enabled to improve performance.\nTo suppress this warning, add android.generateSyncIssueWhenLibraryConstraintsAreEnabled=false to gradle.properties","sources":[{}]}
AGPBI: {"kind":"warning","text":"The property android.dependency.excludeLibraryComponentsFromConstraints improves project import performance for very large projects. It should be enabled to improve performance.\nTo suppress this warning, add android.generateSyncIssueWhenLibraryConstraintsAreEnabled=false to gradle.properties","sources":[{}]}
AGPBI: {"kind":"warning","text":"The property android.dependency.excludeLibraryComponentsFromConstraints improves project import performance for very large projects. It should be enabled to improve performance.\nTo suppress this warning, add android.generateSyncIssueWhenLibraryConstraintsAreEnabled=false to gradle.properties","sources":[{}]}

> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :app:generateDebugBuildConfig UP-TO-DATE
> Task :app:checkDebugAarMetadata UP-TO-DATE
> Task :app:processDebugNavigationResources UP-TO-DATE
> Task :app:compileDebugNavigationResources UP-TO-DATE
> Task :app:generateDebugResValues UP-TO-DATE
> Task :app:mapDebugSourceSetPaths UP-TO-DATE
> Task :app:generateDebugResources UP-TO-DATE
> Task :app:mergeDebugResources UP-TO-DATE
> Task :app:packageDebugResources UP-TO-DATE
> Task :app:parseDebugLocalResources UP-TO-DATE
> Task :app:createDebugCompatibleScreenManifests UP-TO-DATE
> Task :app:extractDeepLinksDebug UP-TO-DATE
> Task :app:processDebugMainManifest UP-TO-DATE
> Task :app:processDebugManifest UP-TO-DATE
> Task :app:processDebugManifestForPackage UP-TO-DATE
> Task :app:processDebugResources UP-TO-DATE
> Task :app:javaPreCompileDebug UP-TO-DATE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets UP-TO-DATE
> Task :app:compressDebugAssets UP-TO-DATE
> Task :app:checkDebugDuplicateClasses UP-TO-DATE
> Task :app:desugarDebugFileDependencies UP-TO-DATE
> Task :app:mergeExtDexDebug UP-TO-DATE
> Task :app:mergeLibDexDebug UP-TO-DATE
> Task :app:mergeDebugJniLibFolders UP-TO-DATE
> Task :app:mergeDebugNativeLibs UP-TO-DATE
> Task :app:stripDebugDebugSymbols UP-TO-DATE
> Task :app:validateSigningDebug UP-TO-DATE
> Task :app:writeDebugAppMetadata UP-TO-DATE
> Task :app:writeDebugSigningConfigVersions UP-TO-DATE

> Task :app:compileDebugKotlin
w: file:///Users/kun/AndroidStudioProjects/FitnessTracker/app/src/main/java/com/example/fitnesstracker/ui/FitnessScreen.kt:457:46 'val Icons.Rounded.DirectionsRun: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.DirectionsRun.
w: file:///Users/kun/AndroidStudioProjects/FitnessTracker/app/src/main/java/com/example/fitnesstracker/ui/FitnessScreen.kt:479:42 'val Icons.Rounded.DirectionsRun: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.DirectionsRun.
w: file:///Users/kun/AndroidStudioProjects/FitnessTracker/app/src/main/java/com/example/fitnesstracker/ui/FitnessScreen.kt:2412:44 'val Icons.Rounded.ArrowBack: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.ArrowBack.
w: file:///Users/kun/AndroidStudioProjects/FitnessTracker/app/src/main/java/com/example/fitnesstracker/ui/FitnessScreen.kt:2633:52 'val Icons.Rounded.ArrowBack: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.ArrowBack.
w: file:///Users/kun/AndroidStudioProjects/FitnessTracker/app/src/main/java/com/example/fitnesstracker/ui/FitnessScreen.kt:3759:44 'val Icons.Rounded.ArrowBack: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.ArrowBack.
w: file:///Users/kun/AndroidStudioProjects/FitnessTracker/app/src/main/java/com/example/fitnesstracker/ui/FitnessScreen.kt:4241:44 'val Icons.Rounded.ArrowBack: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.ArrowBack.
w: file:///Users/kun/AndroidStudioProjects/FitnessTracker/app/src/main/java/com/example/fitnesstracker/ui/FitnessScreen.kt:4474:44 'val Icons.Rounded.ArrowBack: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.ArrowBack.
w: file:///Users/kun/AndroidStudioProjects/FitnessTracker/app/src/main/java/com/example/fitnesstracker/ui/FitnessScreen.kt:5621:44 'val Icons.Rounded.ArrowBack: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.ArrowBack.

> Task :app:compileDebugJavaWithJavac UP-TO-DATE
> Task :app:processDebugJavaRes UP-TO-DATE
> Task :app:mergeDebugJavaResource UP-TO-DATE
> Task :app:dexBuilderDebug
> Task :app:mergeProjectDexDebug
> Task :app:packageDebug
> Task :app:createDebugApkListingFileRedirect UP-TO-DATE
> Task :app:assembleDebug

BUILD SUCCESSFUL in 19s
37 actionable tasks: 4 executed, 33 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.1.0/userguide/configuration_cache_enabling.html

Build Analyzer results available