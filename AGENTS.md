# AGENTS.md

## Cursor Cloud specific instructions

### Project overview

Kontenerki is a Kotlin/Ktor backend API for managing container and yard rentals. It uses PostgreSQL (via Exposed ORM), JWT authentication, and Koin for DI. See `README.md` for the official feature list and build/run tasks.

### Prerequisites

- **JDK 21** (required by `jvmToolchain(21)` in `build.gradle.kts`)
- **Docker** (for PostgreSQL)
- A local JAR dependency exists at `libs/library-1.0.1.jar` (checked into the repo)

### Running PostgreSQL

Start only the `db` service from docker-compose (the `api` and `email` services reference external repos):

```sh
docker compose up -d db
```

The container exposes PostgreSQL on host port **5431** (mapped to container port 5432). Credentials: user=`admin_user`, password=`postgres`, database=`db1`.

### Environment variables

Copy `.env.example` to `.env` at the repo root (`.env` is gitignored). Put real `GOOGLE_CLIENT_SECRET`, `GOOGLE_REFRESH_TOKEN`, and `KSEF_TOKEN` only in `.env` — not in committed files.

For **Gradle on the host**, use `DB_HOST=localhost` and `DB_PORT=5432` (or `5431` if Docker maps Postgres to 5431).

For **docker compose**, set `DB_PORT=5431` in `.env` for the host mapping; compose overrides `DB_HOST`/`DB_PORT` for the `api` service on the internal network.

The **email** service requires `INTERNAL_API_KEY` (same value as in the API `.env`), plus `GOOGLE_CLIENT_*` and `EMAIL_USER`. Compose sets `API_NAME=api` for container networking. Quote values that contain spaces or `|` (e.g. `JWT_REALM`, `KSEF_TOKEN`).

### Docker Compose (full stack)

```sh
cp .env.example .env   # if missing
docker compose up -d --build
```

API: `http://localhost:8100`. Database from the host: `localhost:${DB_PORT}` (default `5431`).

`docker-compose.dev.yml` also builds `web` and `email` from sibling repos (`../kontenerkiWeb`, `../kontenerkiEmail`).

When `API_ENV=DEV`, the app auto-creates database tables on startup via `SchemaUtils.createMissingTablesAndColumns`.

### Running the application locally

The app reads `DB_HOST` and `DB_PORT` from environment/config. The `application.yaml` defaults `DB_HOST` to `db` (docker service name), so for local Gradle runs override it:

```sh
DB_HOST=localhost DB_PORT=5431 ./gradlew run
```

The API starts on port **8100**.

### Build, test, lint

| Task | Command |
|---|---|
| Build (skip tests) | `./gradlew build -x test` |
| Run tests | `./gradlew test` |
| Run app | `DB_HOST=localhost DB_PORT=5431 ./gradlew run` |

There is no separate lint tool configured; Kotlin compiler warnings serve that role.

### Authentication (dev)

The dev login is hardcoded in `AuthServiceImpl`: email=`ppp`, password=`ppp`. Use `POST /auth/login` with JSON body `{"email":"ppp","password":"ppp"}` to get a JWT. Pass it as `Authorization: Bearer <token>` for authenticated routes.

### KSeF integration

KSeF (Krajowy System e-Faktur) API v2 is integrated under `com.kontenery.ksef` (client → repository → service → `/ksef` routes). Authentication uses a **KSeF system token** (generated in the KSeF portal), not the app JWT.

With **`API_ENV=DEV`**, KSeF defaults to **TEST (sandbox)**: `https://api-test.ksef.mf.gov.pl`, API `v2`, `KSEF_NIP` from `.env` or `8943278612`. Override with `KSEF_ENV` (`TEST` | `DEMO` | `PRODUCTION`) or explicit `KSEF_BASE_URL`. Production URL is blocked when `API_ENV=DEV`.

Add to `.env` (copy from `.env.example`):

```
KSEF_ENV=TEST
KSEF_BASE_URL=https://api-test.ksef.mf.gov.pl
KSEF_API_SUFFIX=v2
KSEF_NIP=8943278612
KSEF_TOKEN=<token from KSeF TEST portal — same NIP as KSEF_NIP>
```

Optional: `KSEF_TOKEN_FILE=/path/to/token.txt` instead of `KSEF_TOKEN`.

`docker compose` sets `DB_HOST=db`, `KSEF_ENV=TEST` and sandbox URL on the `api` service unless `.env` overrides them.

For production: `API_ENV=PROD`, `KSEF_ENV=PRODUCTION`, `KSEF_BASE_URL=https://api.ksef.mf.gov.pl`, and a production token.

JWT-protected endpoints:

- `GET /ksef/login` — authenticate to KSeF and return access token metadata
- `GET /ksef/invoices` — list invoice metadata (`pageOffset`, `pageSize` 10–250, optional `from`/`to` ISO dates, `subjectType` default `Subject1`)
- `POST /ksef/invoices/send` — map `Invoice` (domain DTO) to FA(3) XML and send via KSeF online session
- `POST /ksef/invoices/{invoiceNumber}/send` — load invoice from DB by **invoice number**, then send to KSeF (returns `sessionReferenceNumber`, `invoiceReferenceNumber`, optional `ksefNumber`)

Invoice creation (`POST /invoice/...`) for VAT clients: build invoice in memory → send to KSeF → save to DB with `ksefNumber` → persist KSeF session status after save. Lookup by invoice number: `GET /invoice/{invoiceNumber}/id`.

Mapper: `ksef/mapper/InvoiceToKsefFa3Mapper.kt` (`Invoice` → XML FA v3).

### Key gotchas

- The `email` service (separate repo at `../kontenerkiEmail`) is optional. The API catches exceptions if it's unavailable -- invoice-sending features won't work but the app runs fine.
- Tests use H2 in-memory database, so PostgreSQL is not needed for `./gradlew test`.
- The Gradle wrapper downloads Gradle 8.13 on first run, which takes time. Subsequent builds use the cached distribution.
- `--no-daemon` is recommended in ephemeral environments to avoid leftover daemon processes.
