DOCKER_COMPOSE = docker compose
DEV_COMPOSE = docker compose -f docker-compose.dev.yml
TEST_COMPOSE = docker compose -f docker-compose.test.yml

up:
	$(DOCKER_COMPOSE) up --build

test:
	$(TEST_COMPOSE) up --build

dev:
	$(DEV_COMPOSE) up --build

down:
	$(DOCKER_COMPOSE) down -v

down-dev:
	$(DEV_COMPOSE) down -v

down-test:
	$(TEST_COMPOSE) down -v

logs:
	$(DOCKER_COMPOSE) logs -f

ps:
	$(DOCKER_COMPOSE) ps
