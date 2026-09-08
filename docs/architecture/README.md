# System Architecture

## Overview

The platform starts as a two-tier application backed by local infrastructure:

1. A React single-page application presents settlement information to users.
2. A Spring Boot REST API exposes backend capabilities under `/api`.
3. PostgreSQL provides durable relational storage.
4. Kafka provides asynchronous trade and settlement event streams.

During local development, Vite serves the frontend on port `5173` and proxies
requests beginning with `/api` to Spring Boot on port `8080`. This keeps browser
requests same-origin from the application's perspective and avoids development-only
CORS configuration.

## Initial communication flow

```text
Browser -> React/Vite -> GET /api/status -> Spring Boot
Browser <- React UI   <- JSON response  <- Spring Boot
```

The initial endpoint returns the backend service name and health state. It provides
a small end-to-end integration point that can later be replaced or supplemented by
trade capture, validation, enrichment, Kafka processing, and settlement APIs.

## Backend packages

- `config`: application and integration configuration
- `controller`: REST endpoints
- `service`: business orchestration
- `repository`: persistence access
- `entity`: persistence-domain entities
- `dto`: API request and response models
- `kafka`: event producers, consumers, and event models
- `validation`: trade validation rules
- `enrichment`: reference-data enrichment
- `settlement`: settlement workflows
- `exception`: error types and centralized handling

## Frontend folders

- `components`: reusable UI elements
- `pages`: route-level screens
- `services`: backend API access
- `hooks`: reusable React hooks
- `types`: shared type definitions
- `utils`: framework-independent helpers
- `public`: static public assets

## Future deployment

The repository reserves infrastructure directories for Docker, Kafka, and Azure.
Docker Compose provides the complete local topology. The frontend nginx container
proxies `/api` to the backend, while the backend connects to PostgreSQL and Kafka
over the private Compose network. Production infrastructure remains intentionally
separate from this local setup.
