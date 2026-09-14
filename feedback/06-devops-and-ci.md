# DevOps and CI

## No CI at all

Commit `50187fc` deliberately removed the GitHub Actions workflow, citing
Actions billing. I didn't re-add it since that was an explicit choice, but
flagging the trade-off clearly: right now nothing runs `./mvnw verify`,
`npm run lint`, `npm run test:run`, or `npm run build` automatically on any
push or PR. A regression only surfaces when someone runs it locally and
remembers to.

If billing was the actual concern: public GitHub repos get free Actions
minutes, and even a private repo's free tier (2,000 min/month) covers a
`paths`-filtered workflow with dependency caching easily — a full backend
`mvn verify` with Testcontainers plus a frontend lint/test/build run in
roughly 5–8 minutes. If you want, I can build a minimal workflow that only
triggers on changes to `backend/**` or `frontend/**` respectively, so
you're not burning minutes on unrelated changes (like this feedback folder).

Minimum viable pipeline, if/when you want it:
- backend: `./mvnw -B verify` (this includes ArchUnit + Testcontainers)
- frontend: `npm ci && npm run lint && npm run test:run && npm run build`
- optionally: `trivy fs .` for dependency vulnerabilities, `trivy image` on
  both built Docker images

## No Kubernetes manifests

Everything here is `docker-compose.yml`-only. If K8s/OpenShift is an actual
target (your review prompt mentioned it), nothing exists yet — no
Deployment, no Service, no Ingress, no ConfigMap/Secret split, no HPA. I
didn't build this speculatively since I don't know your target platform or
whether you're even planning to deploy there. If you are, the shape I'd
suggest:

- backend Deployment: `readinessProbe` on `/actuator/health/readiness`,
  `livenessProbe` on `/actuator/health/liveness` (both already exist and are
  enabled — `management.health.readinessState.enabled: true` etc. — so this
  is just manifest work, not app work)
- `securityContext.runAsNonRoot: true` (the Dockerfile already runs as a
  non-root `app` user, so this is just asserting what's already true)
- the backend currently writes to `logs/` and `uploads/` (local disk) — if
  you want `readOnlyRootFilesystem: true`, either mount `emptyDir` volumes
  at those paths or switch logging to stdout-only (which the JSON console
  appender already supports — `logback-spring.xml`'s `CONSOLE_JSON` appender
  could be the only sink in a K8s deployment, letting the cluster's log
  collector handle aggregation instead of the app managing log files)
- frontend Deployment: the image is now `nginxinc/nginx-unprivileged` on
  port 8080 (fixed in the last pass), so this is ready for a
  `runAsNonRoot` K8s security policy without further image changes

## Image scanning

No `trivy`/`grype`/`docker scout` anywhere, no SBOM generation. If supply
chain hygiene matters for this project (portfolio piece for a security-
conscious audience, or an actual production target), this is a cheap add
once CI exists — `trivy image admin-api:latest` and `trivy image
frontend:latest` as a CI step, non-blocking at first (report only) so it
doesn't immediately fail the build on a base-image CVE you can't control.

## Frontend image build args

I fixed the immediate problem (the image no longer bakes your local `.env`,
and `docker-compose.yml` now passes `VITE_*` values as build args). But
build-time env baking is inherently awkward for anything beyond local
compose — if you ever want one built image promoted across
dev/staging/prod, "bake at build time" means rebuilding per environment.
The more scalable pattern is a small nginx entrypoint script that writes a
`/config.js` (or similar) from runtime environment variables when the
container starts, and the frontend reads `window.__APP_CONFIG__` instead of
`import.meta.env.VITE_*` for anything that varies per environment. I didn't
do this because it's a real change to how the frontend reads config
(`core/config/env.ts`), not a Docker-only fix, and touches more of the app
than felt appropriate to change unasked.
