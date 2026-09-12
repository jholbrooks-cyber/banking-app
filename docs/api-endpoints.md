# National Bank of Greece — API & Endpoint Reference

All endpoints, their auth requirements (as designed vs. as implemented), and attack surface notes.

---

## Authentication Endpoints

| Method | Path | Auth Required | Notes |
|---|---|---|---|
| GET | `/login` | No | Renders login form; accepts `?redirect=` param (open redirect) |
| POST | `/login` | No | SQLi in username/password; no rate limit; verbose errors; open redirect |
| GET | `/logout` | No | Clears client cookie only; server session persists |
| GET | `/register` | No | Renders register form with hidden `role` field |
| POST | `/register` | No | Binds `role` from POST body — privilege escalation |
| GET | `/reset-password` | No | Renders reset form |
| POST | `/reset-password` | No | Resets password via secret answer only; no rate limit; no email token |

---

## Dashboard

| Method | Path | Auth Required (actual) | Notes |
|---|---|---|---|
| GET | `/dashboard` | Session check (weak) | Renders accounts list; `th:utext` on fullName |

---

## Account Endpoints

| Method | Path | Auth Required (actual) | Notes |
|---|---|---|---|
| GET | `/accounts/{id}` | Session check | **IDOR** — no ownership check; returns any account |
| GET | `/accounts/{id}/transactions` | Session check | **IDOR** — same; no ownership check |
| GET | `/accounts/balance` | **None** | Completely unauthenticated; returns balance for any `?accountId=` |

---

## Transfer Endpoints

| Method | Path | Auth Required (actual) | Notes |
|---|---|---|---|
| GET | `/transfer` | Session check | Renders form with hidden mass-assignment fields |
| POST | `/transfer` | Session check | No CSRF; no ownership check on `fromAccountId`; accepts negative amounts |
| POST | `/api/transfer` | **None** | REST endpoint — zero auth check; direct transfer |

---

## Profile Endpoints

| Method | Path | Auth Required (actual) | Notes |
|---|---|---|---|
| GET | `/profile` | Session check | Renders profile; stored XSS via `th:utext` on messages |
| POST | `/profile/update` | Session check | Mass assignment — `role` field bindable |
| POST | `/profile/message` | Session check | Stores message body raw — stored XSS |
| GET | `/profile/search` | Session check | Reflected XSS via `?q=`; exposes all user PII |
| GET | `/profile/redirect` | Session check | Open redirect via `?url=` |

---

## Admin Endpoints

| Method | Path | Auth Required (actual) | Notes |
|---|---|---|---|
| GET | `/admin` | `userId != null` (not role check) | Any logged-in user; exposes password hashes |
| GET | `/admin/search` | `userId != null` | Reflected XSS via `?q=`; SQLi |
| GET | `/admin/users` | **None** | JSON dump of all users including hashed passwords |
| GET | `/admin/promote/{id}` | `userId != null` | Promotes user to ADMIN — accessible by any authenticated user |
| GET | `/admin/delete/{id}` | `userId != null` | Deletes any user |

---

## API / REST Endpoints

| Method | Path | Auth Required (actual) | Notes |
|---|---|---|---|
| POST | `/api/xml` | **None** | XXE — `DocumentBuilderFactory` with no entity restrictions |
| GET | `/api/fetch` | **None** | SSRF — fetches any URL via `?url=` param |
| POST | `/api/deserialize` | **None** | RCE via Java deserialization + commons-collections gadget chain |
| POST | `/api/token` | Session check | Issues JWT with `NBGSecretKey2024` |
| GET | `/api/token/validate` | None | Validates JWT; no algorithm pinning |

---

## Infrastructure / Debug Endpoints

| Path | Exposure |
|---|---|
| `/h2-console` | H2 web console — `web-allow-others=true`; full DB access |
| `/actuator` | Lists all actuator endpoints |
| `/actuator/env` | Full environment variables including secrets |
| `/actuator/beans` | Spring bean graph |
| `/actuator/mappings` | All URL mappings |
| `/actuator/heapdump` | JVM heap dump — may contain credentials/session tokens |
| `/actuator/logfile` | Application log — contains SQL queries with credentials |
| `/actuator/shutdown` | `POST` to shut down application |

---

## Default Credentials

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `john.doe` | `password` | USER |
| `jane.smith` | `123456` | USER |

---

## MD5 Hash Reference

| Password | MD5 (no salt) |
|---|---|
| `admin123` | `0192023a7bbd73250516f069df18b500` |
| `password` | `5f4dcc3b5aa765d61d8327deb882cf99` |
| `123456` | `e10adc3949ba59abbe56e057f20f883e` |

---

## Key Headers / Cookies

| Name | Type | Issue |
|---|---|---|
| `NBGSESSION` | Cookie | No `HttpOnly`; no `Secure`; no `SameSite`; value is sequential integer |
| `Authorization: Bearer <jwt>` | Header | Accepted on API endpoints; HS256 with guessable secret |
| CORS | Response header | `Access-Control-Allow-Origin: *` on all endpoints |
