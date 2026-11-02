# Local development

## Prerequisites

- Docker Desktop with Compose support
- Java 11 and Maven 3.6.3+ for running the backend outside containers
- Node.js 20.19+ (or 22.12+) and npm 9+

## Start the complete local stack

From the repository root:

```bash
docker compose up --build -d
docker compose ps
```

Compose starts PostgreSQL on `localhost:5432`, Kafka on `localhost:9092`,
Spring Boot on `localhost:8080`, and the frontend on `localhost:5173`. The
`kafka-init` service creates the six application topics after Kafka becomes
healthy. PostgreSQL migrations and reference-data seeds run when the backend
starts.

## Run services without Docker

Start PostgreSQL and Kafka using the connection values in
`backend/src/main/resources/application-local.yml`, then run:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

cd ../frontend
npm install
npm run dev
```

The Vite development server proxies `/api` to `http://localhost:8080`. Set
`VITE_API_PROXY_TARGET` or `VITE_API_BASE_URL` when the backend is hosted
elsewhere. Never place credentials in committed environment files.

## Verify local services

```bash
curl http://localhost:8080/api/status
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/reference-data/instruments
curl http://localhost:8080/api/reference-data/counterparties
```

The frontend build can be checked independently with `npm run build` from
`frontend`. Backend tests require Maven and a working Java 11 environment;
Spark tests require the Python environment described in `spark/requirements.txt`.

## Stop and reset

```bash
docker compose down
docker compose down -v   # also removes local PostgreSQL and Kafka volumes
```

The second command is destructive for local data and should only be used when
a clean database and Kafka log are required.
