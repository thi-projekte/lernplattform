# Infrastructure Setup

This document describes the supporting infrastructure for the MYnd platform — the database, identity provider, object storage, mail, and the deployment pipeline — for both local development and production. It does **not** cover running the frontend/backend application code itself; for that, see the root [`SETUP.md`](../SETUP.md).

PostgreSQL and Keycloak are fixed components of this stack. Object storage and mail are swappable, since the backend only talks to them through a generic S3 / SMTP interface.

## 1. Local development infrastructure

Local infra is defined in [`docker-compose.dev.yml`](../docker-compose.dev.yml).

```bash
docker compose -f docker-compose.dev.yml up -d
docker compose -f docker-compose.dev.yml ps
```

| Service    | Image                          | Ports                  | Default credentials                | Purpose |
|------------|---------------------------------|-------------------------|-------------------------------------|---------|
| `database` | `pgvector/pgvector:pg18`        | `5432:5432`             | `mynd` / `db_password`              | PostgreSQL for the app (`mynd` DB) and Keycloak (`keycloak` DB, created via `init_db.sql`) |
| `keycloak` | `quay.io/keycloak/keycloak:latest` (`start-dev`) | `8081:8080`  | `admin` / `admin`                   | Identity provider / OIDC |
| `minio`    | `minio/minio:latest`            | `9999:9000` (API), `9001` (console) | `admin` / `admin1234567`            | S3-compatible object storage |
| `maildev`  | `maildev/maildev`               | `1080` (web UI), `1025` (SMTP) | —                                   | Catches outgoing mail in dev, no real delivery |

### PostgreSQL (fixed)

No alternative is supported — the app and Keycloak both require PostgreSQL (the app additionally relies on the `pgvector` extension, hence the `pgvector/pgvector` image).

### Keycloak (fixed)

No alternative is supported. After the container is up, a realm/client/role configuration step is required (frontend client `mynd` with roles `builder`/`learner`, plus a `mynd-backend` service-account client whose secret goes into `mynd-backend/.env` as `KEYCLOAK_BACKEND_CLIENT_SECRET`). See root [`SETUP.md` § Keycloak setup](../SETUP.md#keycloak-setup) for the exact click-path.

### Object storage (MinIO by default)

Open the console at [http://localhost:9001](http://localhost:9001) and create a bucket named `default` (the app reads this name from the `mynd.s3.bucket` property). See root [`SETUP.md` § Minio (S3) setup](../SETUP.md#minio-s3-setup) for the exact click-path.

### Mail (Maildev by default)

Maildev is dev-only — it catches everything sent through `MAILER_HOST`/`MAILER_PORT` and shows it at [http://localhost:1080](http://localhost:1080) without delivering it. In production, point the same `MAILER_*` variables at a real SMTP provider.

## 2. Object storage alternatives

The backend talks to storage purely through an S3-compatible client, so MinIO can be swapped for any S3-compatible provider by changing `S3_HOST`, `S3_HOST_PUBLIC`, `S3_ACCESS_KEY`, `S3_SECRET`, and `S3_REGION`, and creating a bucket named `default` on that provider:

- AWS S3
- Cloudflare R2
- Backblaze B2
- DigitalOcean Spaces
- Wasabi
- Self-hosted alternatives: Garage, SeaweedFS

## 3. Production deployment infrastructure

### Pipeline overview

3. [`deploy.yml`](../.github/workflows/deploy.yml) — runs when a GitHub Release is created: builds and pushes the backend, frontend, and Keycloak images to GHCR (`ghcr.io/thi-projekte/lernplattform-mynd-*:<release-tag>`), then triggers deployment.
4. [`portainer.yml`](../.github/workflows/portainer.yml) — deploys [`portainer/full/docker-compose.yml`](../portainer/full/docker-compose.yml) as a stack named `mynd-lernplattform` via the Portainer API.

The same Portainer-deploy pattern is reused for two optional add-on stacks — Plausible analytics ([`plausible.yml`](../.github/workflows/plausible.yml)) and Bugsink error tracking ([`bugsink.yml`](../.github/workflows/bugsink.yml)) — triggered manually via `workflow_dispatch`; they are not required for the core app.

### External prerequisites

Before `deploy.yml` can succeed, the following must already exist outside this repo:

- A reachable **Portainer** instance with an API token.
- An existing **external** Docker network named `nginx-proxy-manager` on the Portainer host — this repo's stacks attach to it for reverse proxy/TLS termination but do not create it.

### Required GitHub Actions secrets

| Concern | Secret | Purpose |
|---|---|---|
| Portainer | `PORTAINER_URL` | Portainer base URL |
| | `PORTAINER_TOKEN` | Portainer API token |
| | `PORTAINER_ENDPOINT` | Portainer endpoint ID (defaults to `1` if unset) |
| Database | `MYND_DB_PASSWORD` | Password for the app's PostgreSQL user |
| | `KEYCLOAK_DB_PASSWORD` | Password for Keycloak's PostgreSQL user |
| Keycloak | `KEYCLOAK_URL` | Public URL of the Keycloak instance |
| | `KEYCLOAK_ADMIN_USER` | Keycloak bootstrap admin username |
| | `KEYCLOAK_ADMIN_PASSWORD` | Keycloak bootstrap admin password |
| | `MYND_KC_BACKEND_SECRET` | Client secret for the `mynd-backend` service-account client |
| Object storage | `S3_ACCESS_KEY` | Access key for the object storage provider |
| | `S3_SECRET_KEY` | Secret key for the object storage provider |
| | `S3_HOST` | Internal/network-reachable endpoint used by the backend |
| | `S3_HOST_PUBLIC` | Publicly reachable endpoint (e.g. for presigned URLs) |
| Mailer | `MAILER_HOST`, `MAILER_PORT`, `MAILER_USERNAME`, `MAILER_PASSWORD`, `MAILER_FROM` | SMTP connection details for outgoing mail |
| Stripe | `STRIPE_API_KEY` | Stripe secret API key |
| | `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret |
| App / CORS | `CORS_ORIGIN` | Allowed CORS origin for the backend |
| | `FRONTEND_URI` | Public frontend URL, used by the backend (e.g. in emails/links) |
| Frontend build args | `MYND_BACKEND_URL` | Backend URL baked into the frontend build |
| | `PLAUSIBLE_TRACKING_URL`, `PLAUSIBLE_BASE_URL` | Analytics endpoint baked into the frontend build |
| | `SENTRY_DSN` | Error tracking DSN baked into the frontend build |

The same provider swap applies in production: point `S3_HOST`, `S3_HOST_PUBLIC`, `S3_ACCESS_KEY`, and `S3_SECRET_KEY` at an external S3-compatible provider instead of the bundled `s3` (MinIO) service in `portainer/full/docker-compose.yml`.

**NOTE:** If you dont want to use Portainer for deployment, you can also use the `docker-compose.yml` and deploy it with the secrets manually.

## 4. See also

- Root [`SETUP.md`](../SETUP.md) — Keycloak client/role click-path, MinIO bucket creation click-path, and how to run the backend (IntelliJ) and frontend (`npm run dev`) for local development.
- [`docs/architecture/`](./architecture) — overall service architecture diagram.
