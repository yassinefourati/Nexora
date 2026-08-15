# Developer task runner
# Requires: Git Bash (Windows) or any POSIX shell (Mac / Linux)
# Usage: make <target>
#

.DEFAULT_GOAL := help
MVN := ./mvnw --no-transfer-progress

#  Build

.PHONY: build
build: ## Compile and package every module (skip tests)
	$(MVN) package -DskipTests

.PHONY: test
test: ## Run all tests across every module
	$(MVN) verify

.PHONY: test-unit
test-unit: ## Run unit tests only (no Spring context)
	$(MVN) test -Dtest="*ServiceTest,*Test" -DfailIfNoTests=false

.PHONY: test-integration
test-integration: ## Run integration tests only
	$(MVN) test -Dtest="*IntegrationTest" -DfailIfNoTests=false

.PHONY: clean
clean: ## Remove build artefacts from every module
	$(MVN) clean

#  Run

.PHONY: run
run: ## Run the presentation module with the dev profile (Docker Compose starts automatically)
	$(MVN) spring-boot:run -pl presentation -am -Dspring-boot.run.profiles=dev

.PHONY: run-local
run-local: ## Run the presentation module with the local profile (expects local infrastructure)
	$(MVN) spring-boot:run -pl presentation -am -Dspring-boot.run.profiles=local

#  Code quality

.PHONY: verify
verify: ## Full build + tests + architecture checks across every module
	$(MVN) verify

#  Help

.PHONY: help
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
