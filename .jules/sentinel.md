## 2024-05-23 - Excessive Logging in Production
**Vulnerability:** HTTP request URLs (including path parameters like User IDs) were being logged in release builds via OkHttp Interceptor using `BASIC` level.
**Learning:** Even `BASIC` logging level exposes the full URL, which can contain PII (e.g. `/users/user_id/...`) or tokens if passed in query params.
**Prevention:** Use `HttpLoggingInterceptor.Level.NONE` for release builds to ensure zero leakage in production logs.
