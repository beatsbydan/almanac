# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

A university course-timetabling system (final year project): a Spring Boot (Java 21) REST API in `server/` backed by MySQL, and a React + TypeScript (Vite) SPA in `client/`. The core domain problem is scheduling courses into classrooms/time slots without conflicts and generating `.docx` timetables (Apache POI) per college/user.

## Commands

### Server (`server/`, Maven, Java 21)

```
./mvnw.cmd spring-boot:run          # run the API (port 8080)
./mvnw.cmd test                     # run all tests
./mvnw.cmd test -Dtest=ClassName                     # run one test class
./mvnw.cmd test -Dtest=ClassName#methodName           # run one test method
./mvnw.cmd clean package            # build the jar
```

Requires a `server/.env` (gitignored, not committed) providing `DB_URL`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `SECRET_KEY` (JWT signing key), `MAIL_PASSWORD` (Gmail SMTP app password), `REDIS_ADDRESS` — loaded via `spring.config.import=optional:file:.env[.properties]` in `application.properties`. Needs a running MySQL instance and Redis instance locally. Schema is managed entirely by Liquibase (`spring.liquibase.change-log=classpath:db/migrations/changelog-master.xml`) — do not rely on Hibernate ddl-auto for schema changes; add a new changelog file under `src/main/resources/db/migrations/` (one XML file per table) and include it from `changelog-master.xml`.

Integration tests extend `BaseIntegrationTest` (`@SpringBootTest`, `@Transactional`, MockMvc wired from the real `WebApplicationContext`) — each test's DB writes roll back automatically.

### Client (`client/`, npm, Vite + React 19 + TypeScript)

```
npm install
npm run dev        # vite dev server
npm run build       # tsc -b && vite build
npm run lint
```

Requires `client/.env` with `VITE_APP_LOCAL_URL` (API base URL, e.g. `http://localhost:8080`) and `VITE_APP_PROD_URL`. No test script/framework is configured for the client.

## Server architecture

Layering, outer to inner: `controllers` (thin REST endpoints) → `services/*MgtService` (business orchestration, builds response DTOs, enforces rules) → `db/services/<entity>` (plain CRUD/persistence per entity, one subpackage each for appUser, classroom, college, course, courseCode, courseParticipant, department, generationTracker, level, otp, schedule, scheduleCombination, timetable) → `db/repositories` (Spring Data JPA) → `db/models` (JPA entities). DTOs live under `dtos/request`, `dtos/response`, `dtos/api` (generic response envelope), `dtos/memory` (non-persisted in-memory transfer objects, e.g. slot suggestions).

### Domain model

- `College` → many `Department`; `ClassRoom` belongs to a `College` (or none, meaning university-wide).
- `Department` ↔ `CourseParticipant` many-to-many (which department(s)/level take a course).
- `Course` → many `CourseCode`, many `CourseParticipant`; flags `shared`/`batched`/`general`, plus `expectedClassSize`/`courseUnit`.
- `AppUser` implements Spring `UserDetails`; has a role, one-to-one `Department`, `Level`, `Otp`; one-to-many `Schedule` (as lecturer); `hasSchedule` flag.
- `Schedule` = one concrete class session: day, time period, one `Course`, one lecturer `AppUser`, one `TimeTable`, many `ClassRoom`s, many `ScheduleCombination` rows. `processedForGeneration` marks whether it has been baked into the generated timetable document.
- `ScheduleCombination` = a flattened (day, startTime, endTime, classroomName) occupancy record per classroom per schedule, used purely as a fast conflict-detection index — don't bypass it when creating/updating schedules.
- `TimeTable` = the top-level semester/session container; has `status`, `creationStatus` (whether new schedules can still be added), and a one-to-one `GenerationTracker`.
- `GenerationTracker` holds the current `GenerationStage` enum value driving the cron's phased document generation.

### Schedule creation & conflict prevention

`ScheduleMgtService` validates and persists schedules under **Redisson distributed locks** keyed per `(timetableCode:day:timePeriod:classroom)` to avoid race conditions between concurrent requests. Validation checks, in order: classroom capacity vs. course's expected size, the hard-coded Wednesday 10–12 blackout slot (chapel/assembly), no duplicate `ScheduleCombination` for the same day/time/classroom, and no overlapping schedule for any department/level group tied to the course via `CourseParticipant`. Only after all checks pass are the `Schedule` and its per-classroom `ScheduleCombination` rows written.

`SuggestionService` finds open slots by randomized trial sampling (not exhaustive search): it repeatedly picks a random day/startTime/duration/classroom from the candidate pool, checks it against existing `ScheduleCombination`s, and collects up to 10 valid suggestions within a 10-second budget.

### Timetable document generation (cron-driven)

`TimeTableGenerationCron` runs on a schedule (default every 2 minutes, `project.generation.cron.exp`), guarded by `@SchedulerLock` (ShedLock, backed by a `shedlock` MySQL table, `lockAtLeastFor=5m`/`lockAtMostFor=10m`) so only one node generates at a time in a multi-instance deployment. Each tick, `TimeTableGenerationCronService` advances the active `TimeTable`'s `GenerationTracker.generationStage` (`GenerationStage` enum, e.g. `UWC` → `COCCS`), processing one page (100 rows) of still-unprocessed `Schedule`s scoped to that stage's classrooms/college, writing them into a shared Apache POI `.docx` (one landscape table per day, rows = classrooms, columns = fixed hourly slots), marking each schedule `processedForGeneration=true`, and resuming from the file on disk on the next tick rather than regenerating from scratch. When a stage has no schedules left it advances to the next stage; after the last stage the `TimeTable` is marked done.

A separate, on-demand path (`TimeTableGenerationService`) builds a personalized per-user `.docx` export (different layout) from that user's own schedules (as student/course-rep vs. lecturer), unrelated to the cron-driven general document.

### Auth

Stateless JWT (`jjwt`), no server sessions. `AuthFilter` (`OncePerRequestFilter`) reads the `Authorization: Bearer` header and populates `SecurityContextHolder`; `SecurityConfig` permits `/api/v1/auth/**`, swagger, and a couple of public department endpoints, and requires auth everywhere else, enforced further by `@PreAuthorize` + role constants in `util/AuthorityUtil`. `AuthMgtService` issues three token types: short-lived access token (returned in the body), refresh token and reset token (both set as httpOnly cookies). `/auth/refresh` mints a new access token from the refresh cookie — the client's axios instance (`client/src/configs/Api.ts`) auto-retries a 401 once via this endpoint before logging out. OTP (`db/services/otp`) is used only for email verification and password reset, not for every login — verification/reset tokens are cookie-based and checked against OTP expiry server-side.

### Exceptions

Domain exceptions under `exceptions/` (`NotFoundException`, `DuplicateEntryException`, `UnauthorizedException`, `ForbiddenException`, `BadRequestException`, `MailingException`, `FileGenerationException`, `TimeTableGenerationException`, `ScheduleUpsertException`) are handled centrally by a `@RestControllerAdvice` in `configs/`. Prefer throwing one of these over ad-hoc error handling in services/controllers.

## Client architecture

- `contexts/` + `hooks/useContexts.ts`: `AuthContext` (user/token/role, login/logout/updateToken), `ApiContext` (wraps the axios instance from `configs/Api.ts`), `NavContext`. Access via `useAuth()`, `useApi()`, `useNav()` rather than `useContext` directly.
- `configs/Api.ts`: creates the axios instance; a response interceptor catches 401s and transparently calls `/auth/refresh` (cookie-based) to get a new access token and retries the original request once, logging out on failure.
- `utils/ApiRoutes.ts`: the single source of truth for all backend route paths, grouped by resource (`AUTH`, `CLASS_ROOM`, `TIMETABLE`, `COURSE`, `COLLEGE`, `DEPARTMENT`, `SCHEDULE`) — add new endpoints here rather than inlining path strings.
- `routing/Routes.tsx`: top-level route table. `/login`, `/register`, `/verify-email`, `/verify-otp`, `/reset-password` are public; everything else is nested under `<ReAuth/>` (auth-gate) and then under `<StaffPrivileges/>` or `<AdminPrivileges/>` (`routing/Privileges.tsx`) route guards by role.
- `routing/Permissions.tsx`: component-level (not route-level) role gating — `AdminPermission`, `StaffPermission`, `LecturerPermission`, `StudentPermission`, `NotAdminPermission` wrap children and toggle visibility via `ROLES` from `utils/Constants.ts` (`STUDENT`, `LECTURER`, `ADMINISTRATOR`). Use these to conditionally show UI within an otherwise shared page, and the route guards to block whole pages.
- `routing/routes/app/<resource>/`: one folder per resource (classrooms, colleges, courses, departments, schedules, timetables), each typically with a list view, a `Form.tsx` upsert modal, and shared `Card.tsx`/`UpsertModal.tsx`/`Validations.ts` in `routing/routes/app/`.
- `utils/Constants.ts`: shared enums mirrored from the server — `ROLES`, `DaysOfTheWeek`, `TimePeriods`, `ClassSizes` — keep in sync with server-side `Role`/`Semester`/time-slot assumptions if either side changes.
