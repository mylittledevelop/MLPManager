# MLPManager

A self-hosted Pterodactyl fleet manager for the MyLittlePlanet Minecraft network.

Runs as a Docker container alongside Pterodactyl Panel and Wings. Handles the full
lifecycle of a Minecraft network — from bootstrapping a blank Pterodactyl installation
to day-to-day fleet control via a simple web UI.

## What It Does

- **Bootstrap** — creates the Pterodactyl location, node, allocations, and all game
  servers from a single `config.yml` definition. Run once on a fresh panel.
- **Apply** — reconciles the running panel state against your config. Idempotent,
  safe to run at any time. Creates missing servers, updates drifted config.
- **Fleet Control** — start, stop, and restart the entire network or individual
  servers from a web UI, respecting startup order (proxy first, backends last).
- **Deploy** — update the git branch for one server, a group, or the entire network
  and trigger a rolling restart.

## Prerequisites

- Docker + Docker Compose on your VPS
- A running Pterodactyl Panel with an admin account
- Two Pterodactyl API keys (Application + Client)
- A GitHub account with access to your server file repositories

## Getting Started

1. Copy the example config and fill in your values:
   ```bash
   cp config.example.yml config.yml
   nano config.yml
   ```

2. Start the manager:
   ```bash
   docker compose up -d
   ```

3. Open the web UI:
   ```
   http://YOUR_VPS_IP:8081
   ```

4. Hit **Apply** to bootstrap your network.

## Configuration

All configuration lives in `config.yml` — see `config.example.yml` for a fully
documented template. The config file covers:

- Panel connection details and API keys
- Node definition (FQDN, port range, resource limits)
- Egg settings (egg ID, Docker image)
- Git credentials and script repository
- Network startup order
- Per-server definitions (type, resources, environment variables)

`config.yml` contains secrets and is excluded from version control via `.gitignore`.
Never commit it.

## Project Structure

```
src/main/java/gg/mylittleplanet/manager/
├── config/          — config.yml POJO mapping
├── ptero/           — Pterodactyl Application + Client API clients
├── apply/           — bootstrap and reconciliation logic
├── fleet/           — start/stop/restart/deploy orchestration
└── web/             — Thymeleaf controllers and REST endpoints
```

## Development

```bash
# Run locally (requires a config.yml in the project root)
./gradlew bootRun

# Build fat JAR
./gradlew bootJar

# Build Docker image
docker build -t mlp-manager .
```

## Tech Stack

- Java 21
- Spring Boot 3.3
- Thymeleaf
- Lombok
- Gradle (Kotlin DSL)