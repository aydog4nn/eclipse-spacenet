# Eclipse / SpaceNet

A hackathon project shared as a portfolio example. This full-stack aerospace collaboration platform connects companies and suppliers through organization profiles, capability discovery, project listings, applications and invitations.

This repository is for source-code review and optional local exploration. It is not intended for live deployment. Original hackathon credentials are removed and are not needed to review the project.

## Features

- Company, supplier and administrator roles with JWT authentication
- Organization profiles and technical capability management
- Project discovery and capability-based matching
- Applications, invitations and project messaging via STOMP
- Optional Gemini assistant via WebSocket and SMTP email flows
- React interface with interactive Three.js space scenes

## Stack

Java 21, Spring Boot 4.1, Spring Security, PostgreSQL, Redis, Elasticsearch, MapStruct, React 19, TypeScript, Vite and Three.js.

```text
backend/      Spring Boot API and tests
frontend/     React application and assets
scripts/      Public-source secret checks
```

## Run locally

Requirements: Java 21, Maven, Node.js 22.12+ (Node 24 recommended), Docker Compose and Python 3.

1. Copy `.env.example` to `.env` at the repository root and set a new `DB_PASSWORD`.
2. Copy `backend/.env.example` to `backend/.env`. Use the same database password and generate `JWT_SECRET_KEY` using `openssl rand -base64 32`.
3. Start local infrastructure from the root: `docker compose up -d`.
4. Start the API: `cd backend` then `./mvnw spring-boot:run` (or `mvn spring-boot:run`). Spring imports the local `.env` file.
5. In another terminal: `cd frontend`, `cp .env.example .env`, `npm ci`, `npm run dev`.
6. Open `http://localhost:5173`. API: `http://localhost:8080/api/v1`.

SMTP and Gemini require your own credentials in `backend/.env`. Frontend `VITE_*` values are included in browser assets; put only public URLs there. New accounts require administrator approval. To create the first administrator, temporarily enable bootstrap and provide your own admin identity and strong password in the backend environment, then disable it.

## Validation

```sh
python3 scripts/check_secrets.py
cd backend
mvn test -Dtest=SecurityRegressionTest
cd ../frontend
npm ci
npm run build
npm run lint
```

The original `EclipseApplicationTests` starts the full Spring context and needs running infrastructure plus local credentials. The focused security tests run without external services.

## Public source sharing

Source configuration contains environment references rather than credentials. Local `.env` files, certificates, uploads, logs and database exports are excluded. CI checks the tracked source for common secret formats.

This Compose configuration is for optional local development and binds service ports to loopback. The repository does not include database migrations or real user data.

See [SECURITY.md](SECURITY.md) for the source cleanup and checks.
