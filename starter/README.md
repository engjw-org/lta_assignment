# VRLS2 Take-Home — Starter Repository

This repository is the starting point for the VRLS2 modernisation take-home assignment.

## Quick start (Docker)

Run from the `starter/` directory with Docker running:

```bash
docker compose up --build
```

- Frontend: <http://localhost:4200>
- API: <http://localhost:8080/api/vehicles>

Compose explicitly selects the local `dev` profile and binds both ports to localhost. This profile allows anonymous access for the exercise. Other profiles (including default and mixed `prod,dev`) refuse startup until production authentication and admin authorisation are implemented; supplying credentials alone does not enable production mode.

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
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

**Frontend** (in a separate terminal)
```bash
cd apps/web
npm install
npm start
```

## After code changes: test, build and run

Start each step below from `starter/`. During the TDD red stage, run and review the failing tests and manually commit them before implementation (§8). After implementing the fix, both test suites should pass before rebuilding the application.

### 1. Backend tests

With Java 21 and Maven installed:

```bash
cd apps/api
mvn test
cd ../..
```

Run from `apps/api` so the CSV dataset tests can find `../../data`.

If Java 21 or Maven is not installed locally, use this PowerShell alternative from `starter/`:

```powershell
docker run --rm --mount "type=bind,source=$($PWD.Path),target=/workspace" --workdir /workspace/apps/api maven:3.9-eclipse-temurin-21 mvn -B -ntp "-Dmaven.repo.local=/workspace/apps/api/target/maven-repository" test
```

### 2. Angular tests

Install frontend dependencies first if needed (`npm ci` inside `apps/web`). Google Chrome must be installed for the headless browser tests.

```bash
cd apps/web
npm test -- --progress=false
cd ../..
```

The test script runs `ng test --watch=false --browsers=ChromeHeadless` once and exits. On Windows PowerShell, use `npm.cmd` if script execution policy blocks `npm`. If Chrome is not detected automatically, set its executable path before running the tests:

```powershell
$env:CHROME_BIN = 'C:\Program Files\Google\Chrome\Application\chrome.exe'
```

### 3. Rebuild and restart Docker services

From `starter/`:

```bash
docker compose build
docker compose up -d
docker compose ps
```

Alternatively, combine the build and startup with `docker compose up -d --build`.

Refresh <http://localhost:4200> after both services start. These containers serve built images, so saving source files or only restarting containers does not rebuild the application. Docker builds do not replace the test steps above: the API Dockerfile skips test execution, and the web Dockerfile builds without running Angular tests.

## Data

Sample CSVs live in `data/`. They are loaded at backend startup.

## Assignment

See the assignment brief you were sent by email for full requirements, the IDD/TDD process, and deliverables. Start by reading the brief carefully and filing issues — do not start coding first.
