# VRLS2 Take-Home — Starter Repository

This repository is the starting point for the VRLS2 modernisation take-home assignment.

## Quick start (Docker)

```bash
docker compose up --build
```

- Frontend: <http://localhost:4200>
- API: <http://localhost:8080/api/vehicles>

## Local toolchain

If you prefer running without Docker:

**Prerequisites**
- Java 21
- Maven 3.9 or later
- Node.js 20 or later
- npm 10 or later

**Backend**
```bash
cd apps/api
mvn spring-boot:run
```

**Frontend** (in a separate terminal)
```bash
cd apps/web
npm install
npm start
```

## Data

Sample CSVs live in `data/`. They are loaded at backend startup.

## Assignment

See the assignment brief you were sent by email for full requirements, the IDD/TDD process, and deliverables. Start by reading the brief carefully and filing issues — do not start coding first.
