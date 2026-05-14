# AGENTS.md

## Cursor Cloud specific instructions

### Communication style

Użytkownik uczy się profesjonalnego kodowania — przy każdej zmianie kodu wyjaśniaj **co** zostało zmienione i **dlaczego**, z przykładami "było/jest" oraz opisem zastosowanych wzorców i zasad.

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

Create a `.env` file at the repo root (gitignored). Required variables for local dev:

```
POSTGRES_USER=admin_user
POSTGRES_PASSWORD=postgres
POSTGRES_DB=db1
DB_HOST=localhost
DB_PORT=5431
DB_NAME=db1
DB_USER=admin_user
DB_PASSWORD=postgres
API_PORT=8100
API_ENV=DEV
```

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

### Key gotchas

- The `email` service (separate repo at `../kontenerkiEmail`) is optional. The API catches exceptions if it's unavailable -- invoice-sending features won't work but the app runs fine.
- Tests use H2 in-memory database, so PostgreSQL is not needed for `./gradlew test`.
- The Gradle wrapper downloads Gradle 8.13 on first run, which takes time. Subsequent builds use the cached distribution.
- `--no-daemon` is recommended in ephemeral environments to avoid leftover daemon processes.
