# Velocorner deployment guide

## Production release

CircleCI runs the `ci_cd` workflow. The two publish jobs run on branches
matching `/^master.*/`. Use `master` for the normal production release.

1. `build` runs `sbt Compile/compile` and `sbt Test/test` with PostgreSQL 16,
   then passes the checkout and build outputs to the publish jobs.
2. `deploy_web-backend` generates Swagger with `sbt "web-app/swagger"`, builds
   the Play fat JAR with `sbt "web-app/assembly"`, and publishes
   `peregin/velocorner.com:latest` for `linux/arm64`.
3. In parallel, `deploy_web-frontend` builds the Vite app in Docker with Node 24
   and publishes `peregin/velocorner.frontend:latest` for `linux/arm64`. The
   frontend Docker build runs `npm run build`.
4. After both images publish, `restart_stack` SSHes into `velocorner.com` as
   `DEPLOYER_USER` and invokes `/opt/velocorner/deploy.sh`. The production host
   owns that script, the service definitions, and the TLS proxy configuration.
   The CircleCI `peregin` context supplies deployment settings including
   `DOCKERHUB_PASSWORD` and `DEPLOYER_USER`.

The crawler is published separately with `crawler-service/deploy.sh`. That
script assembles the crawler and pushes `peregin/velocorner.crawler:latest` for
ARM64. Each repository `deploy.sh` pushes to Docker Hub. Use a local Docker build
to inspect an image without publishing it.

[CircleCI workflow](.circleci/config.yml) · [Backend image](web-app/Dockerfile) ·
[Frontend image](web-frontend/Dockerfile) · [Crawler publish script](crawler-service/deploy.sh)

## Runtime configuration

- **Ingress.** Serve the public origin over HTTPS. Route `/api/*`, `/docs`,
  `/swagger.json`, `/assets/swagger.json`, and `/assets/swagger-ui/*` to Play
  on port 9000. Send the React routes and frontend assets to Caddy on port 3000.
  Caddy serves the Vite bundle and falls back to `index.html` for client-side
  routes. Keep the Swagger paths specific because both services use `/assets/`.
  [Play routes](web-app/conf/routes) · [Caddy configuration](web-frontend/Caddyfile.live)
- **Backend.** Supply `DB_URL` (a PostgreSQL JDBC URL), `DB_USER`, and
  `DB_PASSWORD` through the runtime environment or private application config.
  Provide `JWT_SECRET`, `STRAVA_API_ID`, and `STRAVA_API_SECRET` for
  authentication. The legacy Play login also uses `STRAVA_API_CALLBACK`. Set
  `play.http.secret.key` in private configuration. Use `CRAWLER_URL`,
  `RATES_URL`, and `WEATHER_URL` for the corresponding integrations. The
  backend runs Flyway migrations when it
  initializes storage. Provision the database first and give the configured
  user the permissions required by the migrations.
  [Configuration keys](data-provider/src/main/scala/velocorner/SecretConfig.scala) ·
  [Storage initialization](web-app/app/controllers/ConnectivitySettings.scala)
- **Frontend.** `VITE_API_HOST` controls API requests and `VITE_WEB_HOST`
  controls the browser origin used for the OAuth callback at `/oauth/strava`.
  Both default to `https://velocorner.com`. Set them before building for a
  different origin. The checked-in Dockerfile uses the defaults. For a custom
  Docker build, pass the values to the Vite build stage. Container runtime
  environment variables cannot change the already-built bundle. If the API
  and frontend have separate origins, use the frontend origin in Play's CORS
  configuration and register the matching OAuth callback.
  [API client](web-frontend/src/service/ApiClient.js) · [OAuth flow](web-frontend/src/service/auth.ts)

## Build locally without publishing

From the repository root, use the project's sbt launcher with JDK 25, Node 24
for a local frontend build, and Docker for container builds.

```sh
sbt "web-app/swagger" "web-app/assembly"
docker build -t velocorner-backend:local web-app
docker build -t velocorner-frontend:local web-frontend
```

To build just the frontend bundle, run `npm ci` and `npm run build` from
`web-frontend/`. Vite writes the bundle to `web-frontend/dist/`.

## Verify a release

Check that both publish jobs and `restart_stack` succeeded. Then run the public
smoke checks.

```sh
curl -fsS https://velocorner.com/ -o /dev/null
curl -fsS https://velocorner.com/api/ping
curl -fsS https://velocorner.com/docs -o /dev/null
curl -fsSL https://velocorner.com/swagger.json -o /dev/null
```

Expect the ping JSON to contain `"status":"ok"`. In a browser, confirm the
homepage loads its assets, open a client-side route directly, and check that
Swagger loads its API definition. For releases that touch login or product
search, exercise the affected flow too.

For a failed release, check the corresponding CircleCI job first. If all images
published, check the production host's container health and logs for Play,
Caddy, and any affected dependency. The backend container probes `/api/ping`.
The frontend container probes `/health`, which currently uses Caddy's SPA
fallback. A healthy frontend container alone doesn't verify API routing.

Both publish jobs write mutable `latest` tags. If a publish job fails, confirm
both tags come from the same release before restarting. If a newer publish
has changed either tag, use a fresh release workflow to publish both images.
To roll back a bad release, publish a revert through the normal workflow
after checking that the earlier application code can use the current database
schema. If a migration is incompatible, coordinate database recovery before
restarting with the older code.
