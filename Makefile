
.PHONY: help backend frontend build test

help:
	@echo "Comandos disponíveis:"
	@echo "make backend  - Iniciar API Spring Boot"
	@echo "make frontend - Compilar Android"
	@echo "make build    - Compilar tudo"
	@echo "make test     - Executar testes"

backend:
	cd backend && ./mvnw spring-boot:run

frontend:
	cd frontend && ./gradlew :app:assembleDebug

build:
	cd backend && ./mvnw package -DskipTests
	cd frontend && ./gradlew :app:assembleDebug

test:
	cd backend && ./mvnw test
	cd frontend && ./gradlew :app:testDebugUnitTest
