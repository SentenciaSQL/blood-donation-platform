# Phase 1 — Backend Audit Report

**Project:** Blood Donation Platform  
**Branch base:** `complete-dds`  
**Scope:** Existing Spring Boot backend only (no frontend, Docker, or CI yet)

---

## 1. Existing Functionality (Preserved)

| Area | What works today |
|------|------------------|
| Stack | Java 21, Spring Boot 3.5.7, Security 6, JWT (JJWT), JPA/Hibernate, PostgreSQL, Flyway, Bean Validation, springdoc, JaCoCo |
| Roles | `ADMIN`, `DONOR`, `REQUESTER` |
| Auth | `POST /auth/register`, `POST /auth/login` with BCrypt + JWT access token |
| Donors | `GET /donors?bloodType=` (public search by blood type + availability) |
| Blood banks | `POST/GET/GET/{id}/DELETE /blood-banks` (ADMIN for write/delete) |
| Inventory | Nested under banks: `GET/PUT /blood-banks/{bankId}/inventory` |
| Requests | Create (REQUESTER/ADMIN), admin list/get/status/delete |
| Donations | Donor schedule + `/me`, admin status update |
| Notifications | `/me`, admin create, mark seen |
| Domain | `User`, `Donor`, `BloodBank`, `Inventory`, `Request`, `Donation`, `Notification` |
| Migrations | `V1__init`, `V2__add_last_name_column`, `V3__seed_data` |
| Tests | Unit tests for Auth controller, Donation/Inventory/Request/Notification/Jwt services, BloodTypeValidator |

---

## 2. Missing Functionality (Deferred to Later Phases)

- Refresh tokens, `/auth/refresh`, `GET /users/me`
- Full donor profile fields and donor self-service endpoints
- Blood bank city/email/active, update endpoint, filters
- Standalone `/inventory` API, add/remove stock, low-stock
- Extended blood request model (units, urgency enum, statuses, requester `/me`)
- Donation confirm/complete/no-show workflow + inventory side effects
- Blood compatibility engine and matching service
- Notification type, unread-count, read-all, WebSocket, email
- Dashboard/statistics APIs
- Angular frontend, Docker, GitHub Actions
- Rate limiting, production CORS from env

---

## 3. Incomplete Functionality

- Registration accepts `bloodType` but never creates a `Donor` row
- Donation completion does not update eligibility, inventory, or notifications
- Request matching is a manual `matchedDonorUserId` with no compatibility rules
- Repositories often rely on `findAll()` + in-memory filtering
- `BloodTypeValidator` exists but was unused by DTOs/controllers
- `Clock` bean exists but services call `Instant.now()` directly

---

## 4. Security Problems

| Severity | Issue |
|----------|--------|
| Critical | Controllers returned JPA entities; `User.password` could be serialized |
| Critical | Public `/donors` exposed donor + nested user data |
| Critical | Registration allowed client-chosen `ADMIN` role |
| High | Notification mark-seen had no ownership check (IDOR) |
| High | Password validation was `@Size(max = 6)` (max, not min) |
| Medium | No refresh token / revocation |
| Medium | CORS enabled with empty/default config |
| Medium | JWT parse errors swallowed silently in filter |

---

## 5. Validation Problems

- Blood type not validated at API layer (DB check only)
- Request/donation status are free-form strings
- Inventory `unitsAvailable` can become `null` on update
- No `@Future` on donation schedule
- Coordinates / phone not validated

---

## 6. Database Problems

- Entity `User.active` vs migration column `users.enabled` (V1)
- V3 seed inserts into `active`, which does not exist after V1+V2 → fresh migrate fails
- Status/urgency enums in requirements differ from current DB checks (`OPEN/MATCHED/CLOSED`, urgency 1..3)
- Missing indexes/fields planned for later migrations (city, minimum_stock, refresh_tokens, etc.)

---

## 7. Missing Tests

- No MockMvc/security integration tests
- No DonorService / AuthService tests (Auth was controller-only)
- No coverage for exception handler, ownership, role escalation
- `BloodDonationPlatformApplicationTests` fails without env vars / DB
- Target ≥80% service coverage not yet met

---

## 8. Missing Endpoints (vs Requirements)

`/auth/refresh`, `/users/me`, donor `/me` + eligibility/history, blood-bank PUT, inventory CRUD/add/remove/low-stock, request `/me` + cancel, donation confirm/complete/no-show, `/compatibility`, `/requests/{id}/matches`, notification unread/read-all, dashboard stats

---

## 9. Potential Bugs

1. Flyway V3 fails on fresh database (`active` column missing)
2. Password hash leak via entity JSON
3. Null inventory units on update
4. Invalid `requestId` silently ignored when scheduling
5. `orElseThrow()` → generic 500 instead of 404
6. Lazy serialization risk after transaction ends (`BloodBank.inventories`)

---

## 10. Refactor Candidates (Phase 1 addresses foundation)

| Item | Action in Phase 1 |
|------|-------------------|
| Auth logic in controller | Move to `AuthService` |
| Donor controller → repository | Introduce `DonorService` |
| Entity API responses | Introduce response DTOs + mapper |
| No `@RestControllerAdvice` | Add global handler + typed exceptions |
| Schema `enabled`/`active` | New Flyway `V4` (do not edit V1–V3) |
| Weak password validation | Fix min/max size |
| Unused blood type validator | Wire via `@ValidBloodType` |
| Duplicate `spring-boot-starter-test` in pom | Deduplicate |

---

## Phase 1 Implementation Plan

1. Document this audit (this file).
2. Add custom exceptions + consistent `ApiError` JSON via `@RestControllerAdvice`.
3. Add response DTOs; stop returning entities from controllers.
4. Restore Controller → Service → Repository for Auth and Donors.
5. Fix validation (password, blood type) and critical service `orElseThrow` → typed exceptions.
6. Add Flyway `V4__rename_enabled_to_active.sql`.
7. Add test profile defaults so unit/context setup is runnable.
8. Update affected unit tests; run `./mvnw test`.

**Out of scope for Phase 1:** refresh tokens, new domain fields, matching, frontend, Docker/CI, README rewrite.
