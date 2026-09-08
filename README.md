# Real-Time Trade Settlement Platform

Initial full-stack foundation using Java 11, Spring Boot, Maven, React, and Vite.

## Prerequisites

- JDK 11 or newer (the Maven compiler targets Java 11)
- Maven 3.6.3 or newer
- Node.js 20.19 or newer (or 22.12 or newer)
- npm 9 or newer

## Run the backend

```bash
cd backend
mvn spring-boot:run
```

The REST API starts at `http://localhost:8080`. Verify it with:

```bash
curl http://localhost:8080/api/status
```

## Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` calls to the Spring Boot server.

## Build

```bash
cd backend
mvn clean verify

cd ../frontend
npm install
npm run build
```

See [docs/architecture/README.md](docs/architecture/README.md) for the system design
and the intended responsibilities of each package.

## Docker local environment

The complete local environment includes PostgreSQL, Kafka, the backend, and the
frontend:

```bash
docker compose up --build -d
docker compose ps
```

Open `http://localhost:5173` after all services become healthy. Detailed profile,
configuration, verification, troubleshooting, and shutdown instructions are in
[docs/deployment/local-development.md](docs/deployment/local-development.md).
