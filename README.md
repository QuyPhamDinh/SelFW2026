# SelFW2026

Reusable UI, API, and integration test automation framework built with Java 21, Selenium 4, Cucumber, TestNG, and Rest Assured.

## Architecture

Reusable implementation lives in `src/main/java/com/company/framework`:

- `core`: environment configuration, token caching, data factories, utilities, and diagnostics
- `api`: shared Rest Assured specifications, typed models, and clients
- `ui`: thread-safe driver factory/manager and page abstractions
- `integration`: DB, queue, and stub-server contracts ready for system-specific adapters

Tests and business-facing scenarios remain in `src/test`. Cucumber is the primary BDD layer, while TestNG suite XML files support focused smoke, API, integration, and UI regression runs.

## Run locally

Install Java 21 and Maven, then run all scenarios:

```bash
mvn test -Denv=dev
```

Run only tagged scenarios or override configuration without changing source:

```bash
mvn test -Denv=ci -Dcucumber.filter.tags="@ui" -Dbrowser=firefox -Dheadless=true
mvn test -Denv=staging -Dcucumber.filter.tags="@api" -Dapi.base.url=https://jsonplaceholder.typicode.com
```

Supported configuration keys are in `src/test/resources/config.properties`. Environment variables use uppercase names with dots replaced by underscores, for example `API_BASE_URL`. Set `remote.url` to run against Selenium Grid or another remote WebDriver endpoint.

Reports are generated in `target/cucumber-report.html`, `target/cucumber-report.json`, and `target/allure-results`. UI scenarios use a thread-local WebDriver and API scenarios use scenario-local response state, making the runner ready for parallel execution. GitHub Actions workflows are provided for pull requests, main branch pushes, and nightly execution.

## Remote Selenium with Docker and Terraform

The repository includes a Terraform-managed Selenium Standalone Chromium container. The pinned image is multi-architecture and supports Apple Silicon (`linux/arm64`) as well as `amd64`.

Prerequisites:

- Java 21 and Maven
- Docker Desktop
- Terraform 1.5+

Start Selenium Grid:

```bash
terraform -chdir=terraform init
terraform -chdir=terraform validate
terraform -chdir=terraform apply -auto-approve
```

Run UI tests against the Grid:

```bash
SELENIUM_REMOTE_URL=http://localhost:4444 \
mvn -B test -Denv=ci -Dheadless=true -Dcucumber.filter.tags="@ui"
```

Run API-only tests without requiring Selenium:

```bash
mvn -B test -Denv=ci -Dcucumber.filter.tags="@api"
```

Always remove the local container when finished:

```bash
terraform -chdir=terraform destroy -auto-approve
```

`SELENIUM_REMOTE_URL` takes precedence over `remote.url` and enables `RemoteWebDriver`; when it is absent, `DriverFactory` starts a local ChromeDriver or FirefoxDriver. The Selenium container maps Grid port `4444` and receives 2 GB shared memory.

## Jenkins

Create a Pipeline job pointing at this GitHub repository and use the repository `Jenkinsfile`. The job requires Pipeline, Git, JUnit, and Docker Pipeline plugins. The optional Allure Jenkins plugin publishes the generated Allure results. The Jenkins agent must have Java 21, Maven, Docker Desktop/Engine, Terraform, and `curl` available on `PATH`.

The pipeline checks out the source, validates and applies Terraform, waits for `/status` to report readiness, runs smoke, API, and UI-tagged tests, publishes JUnit/Cucumber/screenshots/Allure artifacts, and destroys the Terraform-managed container in `post { always { ... } }`. Concurrent builds are disabled so local Docker resources cannot collide.

For focused local runs, the existing TestNG XML files remain available under `src/test/resources`; the Cucumber TestNG runner is the Maven test entry point and uses `cucumber.filter.tags` for the CI stage boundaries.
