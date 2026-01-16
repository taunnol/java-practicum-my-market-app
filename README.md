# My market app

Веб-приложение "Витрина интернет-магазина" на реактивном стеке: Spring Boot + Spring WebFlux (embedded Netty), H2 через
драйвер r2dbc-h2, Redis для кеша товаров, Java 21.

Состоит из двух подпроектов:

- `market-app` - основное приложение интернет-магазина
- `payments-service` - сервис платежей

## Требования к окружению

- Java 21
- Docker
- Maven Wrapper (уже в проекте) или Maven
- Redis (для локального запуска)

## Как запустить тесты

Из корня проекта выполнить:

```bash
./mvnw -pl payments-service,market-app -am clean test
```

Запустятся тесты для обоих модулей.

## Как поднять

### Локально

1) Запустить локально установленный Redis:

```bash
redis-server
```

2) Поднять Keycloak c импортом realm:

```bash
docker run --name my-market-keycloak -p 8090:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v "$(pwd)/keycloak/realm-my-market.json:/opt/keycloak/data/import/realm-my-market.json" \
  quay.io/keycloak/keycloak:24.0.5 start-dev --import-realm
```

3) Запустить `payments-service`:

```bash
OAUTH2_ISSUER_URI=http://localhost:8090/realms/my-market \
PAYMENTS_ALLOWED_CLIENT_ID=market-app \
./mvnw -f payments-service/pom.xml spring-boot:run
```

4) Запустить `market-app`:

```bash
OAUTH2_ISSUER_URI=http://localhost:8090/realms/my-market \
OAUTH2_CLIENT_ID=market-app \
OAUTH2_CLIENT_SECRET=market-app-secret \
./mvnw -f market-app/pom.xml spring-boot:run
```

### В Docker-контейнере

1) Собрать jar-файлы:

```bash
./mvnw -pl payments-service,market-app -am clean package
```

2) Поднять контейнеры:

```bash
docker compose up --build
```

Убедиться, что все в порядке:

- http://localhost:8080/ должен открыть главную страницу магазина. 
Доступные тестовые пользователи:
  - `admin`:`admin`
  - `user`:`password`
  - `user2`:`password2`
- http://localhost:8081/api/payments/balance без токена должен вернуть 401, а с ним - json с текущим балансом
- http://localhost:8090/ должен открыть панель Keycloak

## Про API

Спецификация находится в `openapi/payments-api.yaml`.

## Про данные

- Стандартный предзаполненный набор пользователей и товаров лежит в `market-app/src/main/resources/data.sql`.
- Изображения товаров находятся в `market-app/src/main/resources/static/images`.