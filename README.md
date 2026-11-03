# FarmChainX

FarmChainX is a Spring MVC application for agricultural product traceability. It provides role-based web dashboards, authenticated REST operations, and a public product-journey lookup. Risk analysis is currently deterministic and rule-based; the project does not call or train a machine-learning model.

## Architecture

The application is a layered monolith:

- Thymeleaf MVC controllers render role-oriented pages.
- REST controllers expose JSON APIs and use DTO validation.
- Services own business rules and ownership checks.
- Spring Data JPA repositories persist the domain entities in MySQL.
- Spring Security protects the UI and API with session authentication, CSRF, URL rules, and method-level authorization.
- Flyway applies the versioned schema; Hibernate validates it and does not update production schemas.

## Technology

- Java 21
- Spring Boot 3.5.16, Spring MVC, Spring Security, Spring Data JPA
- Thymeleaf and Bootstrap 5
- MySQL 8.4
- Maven wrapper
- Flyway, HikariCP, Spring Boot Actuator

## Prerequisites

- JDK 21
- MySQL 8 (local development), or Docker Engine with Docker Compose
- PowerShell on Windows (commands below use the repository's `mvnw.cmd`)

## Local setup

Create a database and a least-privilege application user in MySQL. The application does not create databases by default. Configure environment variables in the launching shell or through your local secret manager:

| Variable | Purpose |
|---|---|
| `DB_HOST` | MySQL host (defaults to `localhost`) |
| `DB_PORT` | MySQL port (defaults to `3306`) |
| `DB_NAME` | Schema name (defaults to `farmchainx`) |
| `DB_USERNAME` | Application database user |
| `DB_PASSWORD` | Application database password |
| `DB_SSL_MODE` | MySQL Connector/J SSL mode; local `dev` defaults to `DISABLED` |
| `DB_CREATE_DATABASE` | Optional local-only database creation request (defaults to `false`; production always disables it) |
| `ADMIN_EMAIL` | Email for the initial administrator |
| `ADMIN_PASSWORD` | Password for the initial administrator |
| `SERVER_PORT` | HTTP port (defaults to `8080`) |
| `DB_POOL_MIN_IDLE` / `DB_POOL_MAX_SIZE` | HikariCP pool bounds |

Set all required variables without putting values in source control or command history. On PowerShell, for example:

```powershell
$env:DB_USERNAME = '<database-user>'
$env:DB_PASSWORD = '<database-password>'
$env:ADMIN_EMAIL = '<administrator-email>'
$env:ADMIN_PASSWORD = '<administrator-password>'
$env:DB_NAME = 'farmchainx'
$env:DB_SSL_MODE = 'DISABLED' # local development only
.\mvnw.cmd spring-boot:run
```

The default profile is `dev`. The first run applies `src/main/resources/db/migration/V1__baseline_farmchainx_schema.sql`, validates entity mappings, and creates the configured administrator if its email is absent. If that email already belongs to a non-ADMIN account, startup fails explicitly; the application does not silently promote or overwrite an existing account. Resolve the role through an approved database administration process.

## Database migrations and profiles

- `dev`: local MySQL defaults, bounded connection pool, non-secure local HTTP session cookie.
- `test`: H2 configuration under `src/test/resources/application-test.yml`; Flyway is disabled and the isolated schema is recreated for tests.
- `prod`: requires a pre-created database, `ddl-auto: validate`, Hikari pool settings, HTTP-only/secure/Strict session cookies, and Connector/J `VERIFY_IDENTITY` by default. Configure the production trust chain; do not use `DISABLED` outside local development.

Flyway baselines an existing non-empty schema at version `0` and runs the idempotent initial schema migration. The migration uses `CREATE TABLE IF NOT EXISTS` and does not drop or rewrite existing tables. Back up production data and review every future migration before deployment.

## Docker Compose

Copy `.env.example` to `.env`, replace every placeholder with unique secrets, then run:

```powershell
Copy-Item .env.example .env
# Edit .env using a secure editor; do not commit it.
docker compose up --build -d
docker compose ps
docker compose logs --tail 100 app
```

Compose starts MySQL 8.4 with a persistent `mysql-data` volume, waits for its health check, and then starts the application. The app and MySQL ports bind to localhost by default. Application uploads use a separate persistent volume. The Compose profile disables TLS only for this local container network. For production, provide managed MySQL with TLS and deploy the app with the `prod` profile; do not expose the database port publicly.

Stop the local stack with `docker compose down`. This keeps the data volume. Do not use `docker compose down -v` unless you have explicitly decided to delete local database and upload data.

## Administrator initialization

`ADMIN_EMAIL` and `ADMIN_PASSWORD` are external configuration, not defaults embedded in the application. The initial password is BCrypt-encoded. The initializer creates the account only when the configured email is not present. Existing user records are not overwritten. Use a unique administrator address and rotate the password through your normal operational process.

## Roles

| Role | Main responsibilities |
|---|---|
| `ADMIN` | Manage users and inspect all farms, batches, and administration views |
| `FARMER` | Manage owned farms, products, and batches |
| `DISTRIBUTOR` | Manage assigned transportation and in-transit batch workflows |
| `WAREHOUSE_MANAGER` | Manage warehouse operations and quality inspections |
| `RETAILER` | Review batches at retail |
| `CONSUMER` | Create an account and use consumer-facing tracking |

Public tracking does not require an account. Role checks and owner-scoped service rules remain enforced for protected operations.

## Web UI and API

The web login is session-based at `POST /login` using form fields `email` and `password`. Thymeleaf forms include CSRF tokens. CSRF protection also applies to REST writes; send the session's token as `X-CSRF-TOKEN` (or `_csrf` for form submissions). Fetch a fresh token from an authenticated form page after login, because Spring Security rotates the token during authentication.

REST responses use the `ApiResponse` envelope. Important routes:

| Method | Path | Access |
|---|---|---|
| `POST` | `/api/auth/register` | Public registration; CSRF required |
| `GET` | `/api/auth/me` | Authenticated |
| `POST` | `/api/farms` | `ADMIN`, `FARMER` |
| `GET` | `/api/farms` | `ADMIN`, `FARMER` |
| `POST` / `GET` | `/api/products` | Authenticated; writes require `ADMIN` or `FARMER` |
| `POST` / `GET` | `/api/batches` | Create: `ADMIN`, `FARMER`; list: supply-chain roles |
| `PUT` | `/api/batches/{id}/status?status=IN_TRANSIT` | Supply-chain roles, with service-level transition checks |
| `POST` | `/api/transportation` | `ADMIN`, `DISTRIBUTOR` |
| `POST` | `/api/quality-inspections` | `ADMIN`, `WAREHOUSE_MANAGER` |
| `POST` / `GET` | `/api/batches/{batchId}/events`, `/api/batches/{batchId}/history` | Authenticated supply-chain roles |
| `GET` | `/api/public/track/{trackingId}` | Public |

Sample requests (replace IDs with records created by your user):

```http
POST /api/farms
Content-Type: application/json
X-CSRF-TOKEN: {{csrfToken}}

{"farmName":"North Farm","location":"Punjab","farmSize":12.5,"certification":"Organic"}
```

```http
POST /api/products
Content-Type: application/json
X-CSRF-TOKEN: {{csrfToken}}

{"productName":"Rice","category":"Grain","description":"Example product"}
```

```http
POST /api/batches
Content-Type: application/json
X-CSRF-TOKEN: {{csrfToken}}

{"productId":1,"farmId":1,"quantity":50.0,"harvestDate":"2026-11-03","expiryDate":"2027-05-03"}
```

```http
GET /api/public/track/FCX-<tracking-id>
```

The complete request sequence and remaining payload templates are in [postman/FarmChainX.postman_collection.json](./postman/FarmChainX.postman_collection.json). It contains variable placeholders only; keep credentials in a private, unshared Postman environment.

## Testing

Run the isolated H2 unit and MVC/security suite:

```powershell
.\mvnw.cmd clean verify
```

An opt-in test connects the application context to the configured real MySQL schema, verifies Flyway/schema validation, and persists/reads a temporary user inside a rolled-back transaction:

```powershell
.\mvnw.cmd --define=farmchainx.mysql.integration=true --define=test=MySqlPersistenceIntegrationTest test
```

The MySQL test requires `DB_USERNAME` and `DB_PASSWORD`, a reachable schema, and the same environment variables used by the app. It is not replaced by the H2 suite. Docker-backed image and Compose validation require a running Docker daemon.

The MVC integration test renders all 36 discovered HTML templates: 33 through controller-backed requests, both error pages directly through Thymeleaf, and the shared layout fragment through rendered pages. Browser smoke checks covered login, registration, and public tracking at mobile width; the remaining templates have not had individual browser-based visual review.

## Health and operations

Only `/actuator/health/liveness` and `/actuator/health/readiness` are exposed and permitted without login. Details are suppressed. Readiness includes the database health indicator. Application logging defaults to INFO and does not log database credentials or administrator email/password values.

## Release status

The repository includes an initial schema baseline, environment profiles, and deployment assets. The resolved-dependency CVE scan returned no findings, but GitHub Advisory Database separately lists Spring Framework 6.2.19 as affected by critical CVE-2026-47884 (CVSS 9.8). Exploitation requires `XsltView` and a catch-all view-rendering mapping; neither is present in the current source. The advisory lists a fix in Spring Framework 7.0.9, not the current 6.2 line, so confirm applicability and resolve this framework upgrade/risk decision before release. A clean release claim also requires Docker image/Compose startup, full-template browser review, all-role live login, and end-to-end traceability/API smoke testing.
