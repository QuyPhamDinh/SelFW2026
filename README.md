# SelFW2026

Portable UI and API automation framework built with Java 23, Selenium, Cucumber, TestNG, REST Assured, Docker Compose, and Jenkins.

## Architecture

Docker Compose manages two services:

- `selenium`: pinned, multi-architecture Selenium Standalone Chromium with a readiness healthcheck.
- `test-runner`: the existing Maven/Cucumber/TestNG project in a Java 23 image.

The runner connects to `http://selenium:4444` through the Compose service network. Java and Maven are not required on the host. Local direct execution remains available when developers want ChromeDriver from IntelliJ.

The Maven project targets Java 21 bytecode and runs on Java 23 in the container. The entrypoint applies `-XX:UseSVE=0` only on ARM64 because the selected Java 23 ARM runtime requires that compatibility setting on Apple Silicon Docker Desktop.

## Prerequisites

- Docker Desktop or Docker Engine with Docker Compose v2
- Git

Java, Maven, Terraform, and a locally installed browser are not required for Docker execution. Windows is supported through Docker Desktop Linux containers; native Windows containers are not supported.

## Local Docker execution

Run the complete suite and retain reports:

```bash
mkdir -p target allure-results
chmod -R a+rwX target allure-results
docker compose build test-runner
docker compose up -d --wait selenium
docker compose run --rm --no-deps test-runner
docker compose down --remove-orphans
```

The final `down` command should be run even after a test failure. A portable shell variant that always cleans up is:

```bash
docker compose build test-runner
docker compose up -d --wait selenium
docker compose run --rm --no-deps test-runner; status=$?
docker compose logs selenium > target/selenium.log 2>&1 || true
docker compose down --remove-orphans
exit $status
```

Run focused suites:

```bash
docker compose run --rm --no-deps test-runner \
  -B test -Denv=ci -Dheadless=true -Dcucumber.filter.tags="@ui"

docker compose run --rm --no-deps test-runner \
  -B test -Denv=ci -Dcucumber.filter.tags="@api"
```

Reports are written to `target/` and `allure-results/`:

- Surefire/TestNG XML: `target/surefire-reports/`
- Cucumber HTML/JSON: `target/cucumber-report.html`, `target/cucumber-report.json`
- Failure screenshots: `target/screenshots/`
- Allure raw results: `allure-results/`
- Selenium logs: `target/selenium.log`

## Scheduled synthetic monitoring

`.github/workflows/scheduled-smoke-monitoring.yml` runs the existing Cucumber smoke
scenarios (`@api and @smoke`, then `@ui and @smoke`) every 15 minutes at minutes
7, 22, 37, and 52. It can also be started manually from the repository's
**Actions → Scheduled Smoke Monitoring → Run workflow** menu. The workflow runs
Java, Maven, and Chromium entirely in Docker, waits for Selenium readiness,
uploads Surefire/TestNG, Cucumber, Allure, screenshots, and Docker logs, and
always removes Compose resources. API and UI failures both fail the workflow;
full regression suites are not included.

GitHub scheduling is best-effort and may not start at the exact cron minute.
The schedule can be changed or disabled by editing or disabling this workflow.
Keep the smoke run lightweight because each execution consumes GitHub-hosted
runner minutes. Local Docker validation measured approximately 14 seconds for
the two Maven smoke commands; hosted duration will also include checkout,
image-build, and runner startup overhead. As a planning estimate, multiply the
measured workflow duration by four executions per hour (about 96 per day);
verify actual usage in **Settings → Billing and plans → Actions**.

### GitHub failure email notifications

The workflow does not send email itself. To enable GitHub's built-in notifications,
open **GitHub Settings → Notifications**, locate the **Actions** preferences,
enable email notifications for failed workflow runs, select failure-only
notifications where that option is available, and verify that the account receives
the messages. A workflow cannot change a user's personal notification settings
automatically. For scheduled workflows, recipients also depend on GitHub's
scheduling rules and the user associated with the schedule.

No SMTP server, SendGrid, Gmail API, or other external email service is used.

Compose exposes Selenium at `http://localhost:4444` for diagnostics. Tests must use the service hostname `http://selenium:4444` inside the Compose network.

On Windows PowerShell, use the same Compose commands and omit the Unix `mkdir`/`chmod` commands; Docker creates the bind-mounted directories as needed.

## Local execution without Docker

When Java 21+ and Maven are installed, run the existing local-driver path:

```bash
mvn -B test -Denv=dev
mvn -B test -Denv=ci -Dheadless=true -Dcucumber.filter.tags="@ui"
```

`SELENIUM_REMOTE_URL` takes precedence over `remote.url`. If neither is configured, `DriverFactory` starts local ChromeDriver or FirefoxDriver. For Compose execution, `SELENIUM_REMOTE_URL=http://selenium:4444` selects `RemoteWebDriver`. `DriverManager` remains thread-local, and Cucumber hooks capture failed UI screenshots before quitting each session.

## Jenkins

Create a Pipeline job pointing at this GitHub repository and use the repository `Jenkinsfile`. The Jenkins agent requires only Docker CLI/daemon access and Docker Compose v2; it does not require Java or Maven. Docker Desktop must be running for the Jenkins host user on macOS. Linux agents need permission to access the Docker daemon. Unix shell steps are used, so a Linux Jenkins agent is recommended.

Install the Jenkins **Allure** plugin before running the job: **Manage Jenkins → Plugins → Available plugins → Allure**. Then configure an Allure Commandline installation under **Manage Jenkins → Tools → Allure Commandline installations** (for example, name it `allure`). The pipeline checks out the source, verifies Docker, builds the test-runner image, starts Selenium with `docker compose up -d --wait`, executes Maven inside the container, publishes the generated Allure report, archives Surefire/Cucumber/Allure/screenshots/logs, and runs `docker compose down --remove-orphans` in `post { always { ... } }`. `disableConcurrentBuilds()` and a build-specific `COMPOSE_PROJECT_NAME` isolate Jenkins builds.

After a successful build, open the build page and select **Allure Report** in the left-side build menu. The raw results remain available under **Build Artifacts → allure-results**. The `allure` publisher requires the Jenkins Allure plugin; without it, Jenkins will report `No such DSL method 'allure'`.

## Configuration

Environment files are under `src/test/resources/config/`. System properties and environment variables override them; for example, `API_BASE_URL` overrides `api.base.url`. Select Cucumber scenarios with `-Dcucumber.filter.tags`.

Generated build output and Allure results are ignored by `.gitignore`. No Terraform configuration or Terraform commands are part of the active project.
