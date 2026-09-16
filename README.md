# 2FA Infrastructure

This project provisions and runs a Multi-Factor Authentication (MFA) stack using Keycloak and PrivacyIDEA. 

The environment is managed via [Docker Compose](https://docs.docker.com/compose/) and orchestrated using [Mise](https://mise.jdx.dev/) tasks. Secrets and certificates are retrieved dynamically from HashiCorp Vault via Ansible.

## Architecture

* **Traefik**: Reverse proxy handling HTTP/HTTPS routing.
* **Keycloak**: Identity and Access Management (IAM) server (version 26+).
* **PrivacyIDEA**: Two Factor Authentication system.
* **MariaDB**: Centralized database backend for both Keycloak and PrivacyIDEA.

## Prerequisites

1. [Docker](https://docs.docker.com/engine/install/) and Docker Compose
2. [Mise](https://mise.jdx.dev/getting-started.html) installed on the host
3. Access to HashiCorp Vault for fetching credentials

## Quick Start

Bring up the entire stack (fetches Vault secrets, generates `.env`, writes certificates, and starts Docker containers):

```bash
mise run start
```

### Access URLs
* **Keycloak**: `https://keycloak-mfa.crosswired.me`
* **PrivacyIDEA**: `https://pi-mfa.crosswired.me`
* **Traefik Dashboard**: `http://localhost:8080`

## Available Tasks

Manage the stack easily using `mise run <task>`:

| Task | Description |
|---|---|
| `start` | Fetch secrets via Ansible, generate `.env` and certs, start Docker stack |
| `stop` | Stop all containers gracefully |
| `down` | Stop and remove containers (keeps volumes intact) |
| `restart` | Restart all containers |
| `logs:keycloak` | check keycloak logs |
| `logs:privacyidea` | check privacyidea logs |
| `logs:mariadb` | check mariadb logs |
| `ps` | List running containers |
| `pull` | Pull latest Docker images |
| `db:export` | Export a full MariaDB SQL dump into `database/mariadb_backup_<timestamp>.tar.gz` |
| `db:restore <file>`| Restore MariaDB databases from a `.tar.gz` backup file |
| `build:keycloak-provider` | Build the Keycloak provider JAR |
| `deploy:keycloak-provider` | Build and deploy the provider JAR into the running Keycloak container |
| `clean` | **DANGER**: Stop stack, delete volumes, DB backups, and remove generated certs |

## Keycloak Provider

The `keycloak-provider/` module contains custom Keycloak identity providers. After making changes:

```bash
# Build only
mise run build:keycloak-provider

# Build and deploy to the running Keycloak container (auto-restarts Keycloak)
mise run deploy:keycloak-provider
```

The provider JAR is mounted into the Keycloak container at `/opt/keycloak/providers` so it's available on container start.

## Backup and Restore

**Export Database**:
```bash
mise run db:export
```
This will create a compressed `.tar.gz` backup inside the `database/` directory.

**Restore Database**:
```bash
mise run db:restore database/mariadb_backup_20260914_123456.tar.gz
```
