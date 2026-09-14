# Developer task runner — requires Git Bash (Windows) or any POSIX shell.
# Usage: make <target>     (run `make help` for the list)

.DEFAULT_GOAL := help
MVN := ./mvnw --no-transfer-progress
COMPOSE := docker compose -f ../docker-compose.yml

#  Build & test

.PHONY: build
build: ## Compile and package every module (skip tests)
	$(MVN) package -DskipTests

.PHONY: test-unit
test-unit: ## Unit + architecture tests only (no Docker needed)
	$(MVN) test -Dtest='!*IntegrationTest' -Dsurefire.failIfNoSpecifiedTests=false

.PHONY: test-integration
test-integration: ## Testcontainers integration tests (needs Docker)
	$(MVN) test -Dtest='*IntegrationTest' -Dsurefire.failIfNoSpecifiedTests=false

.PHONY: verify
verify: ## Full build + all tests + architecture checks (what CI runs for the backend)
	$(MVN) verify

.PHONY: check
check: ## Everything CI checks, backend and frontend: verify + lint + test + build
	$(MVN) verify
	cd ../frontend && npm ci --legacy-peer-deps && npm run lint && npm run test:run && npm run build

.PHONY: clean
clean: ## Remove build artefacts from every module
	$(MVN) clean

.PHONY: openapi-update
openapi-update: ## Refresh the committed OpenAPI snapshot after an intentional API change (needs Docker)
	$(MVN) test -pl app -am -Dtest=OpenApiContractIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false -Dopenapi.update=true

.PHONY: sbom
sbom: ## Generate the CycloneDX SBOM (app/target/classes/META-INF/sbom/application.cdx.json); the image and /actuator/sbom carry the same file
	$(MVN) -q generate-resources -pl app -am
	@ls -l app/target/classes/META-INF/sbom/application.cdx.json

.PHONY: image
image: ## Build the admin-api image (Java 25 runtime, layered, CDS-trained, SBOM inside); tag with the git sha
	docker build --build-arg GIT_SHA=$$(git rev-parse --short HEAD) --build-arg BUILD_DATE=$$(date -u +%Y-%m-%dT%H:%M:%SZ) -t admin-api:latest -t admin-api:$$(git rev-parse --short HEAD) .

.PHONY: hooks
hooks: ## Install the pre-push hook that runs verify before every push (ADR 0008)
	git -C .. config core.hooksPath scripts/git-hooks
	@echo "pre-push hook installed: 'make verify' runs before every push (SKIP_VERIFY=1 git push to bypass once)"

#  Run

.PHONY: infra
infra: ## Start Postgres, Keycloak, Redis, RabbitMQ (root compose, profile infra)
	$(COMPOSE) --profile infra up -d

.PHONY: run
run: ## Run the API from source with the dev profile (starts the infra profile itself)
	$(MVN) spring-boot:run -pl app -am -Dspring-boot.run.profiles=dev

.PHONY: up
up: ## Whole stack in containers (profile app)
	$(COMPOSE) --profile app up -d --build

.PHONY: up-scale
up-scale: ## Two API replicas behind nginx on :8080 (profile scale)
	$(COMPOSE) --profile scale up -d --build

.PHONY: observability
observability: ## Grafana :3000, Prometheus :9091, Tempo, OTel collector
	$(COMPOSE) --profile observability up -d

.PHONY: loadtest
loadtest: ## Run the k6 scenario against :8080 (results in deploy/loadtest/results)
	$(COMPOSE) --profile loadtest run --rm k6

.PHONY: down
down: ## Stop every profile
	$(COMPOSE) --profile infra --profile app --profile scale --profile observability --profile loadtest down

#  Help

.PHONY: help
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
