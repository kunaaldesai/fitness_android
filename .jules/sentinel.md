## 2024-05-23 - Excessive Logging in Production
**Vulnerability:** HTTP request URLs (including path parameters like User IDs) were being logged in release builds via OkHttp Interceptor using `BASIC` level.
**Learning:** Even `BASIC` logging level exposes the full URL, which can contain PII (e.g. `/users/user_id/...`) or tokens if passed in query params.
**Prevention:** Use `HttpLoggingInterceptor.Level.NONE` for release builds to ensure zero leakage in production logs.

## 2024-05-24 - Missing Code Obfuscation in Release Builds
**Vulnerability:** Release builds were configured with `isMinifyEnabled = false`, exposing source code structure and logic to easy reverse engineering.
**Learning:** Disabling R8/ProGuard makes it trivial for attackers to analyze the app's internal logic, API endpoints, and data models using tools like JADX.
**Prevention:** Always set `isMinifyEnabled = true` and `isShrinkResources = true` for release builds and maintain a correct `proguard-rules.pro` file for dependencies like Retrofit and Serialization.

## 2024-05-25 - Unvalidated Input in User Profiles
**Vulnerability:** The `users.py` backend service accepted arbitrary input types and unlimited string lengths for `firstName`, `lastName`, `bio`, and `imageUrl`, leading to potential DoS and data integrity issues.
**Learning:** Python's dynamic typing allows non-string inputs (like lists or dicts) to bypass naive checks or crash operations like `.capitalize()`. Always validate `isinstance(value, str)` before processing string operations.
**Prevention:** Implement explicit type checks and length limits for all user-generated content fields in API endpoints.
