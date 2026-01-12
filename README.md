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

2) Запустить `payments-service`:

```bash
./mvnw -f payments-service/pom.xml spring-boot:run
```

3) Запустить `market-app`:

```bash
./mvnw -f market-app/pom.xml spring-boot:run
```

### В Docker-контейнере

1) Запустить контейнер с Redis:

```bash
docker run --name my-market-redis -p 6379:6379 -d redis:7.4-alpine
```

2) Собрать jar-файлы:

```bash
./mvnw -pl payments-service,market-app -am clean package
```

3) Поднять контейнеры:

```bash
docker compose up --build
```

Убедиться, что все в порядке:

- http://localhost:8080/ должен открыть главную страницу магазина
- http://localhost:8081/api/payments/balance должен вернуть json с текущим балансом

### Про API

Спецификация находится в `openapi/payments-api.yaml`.

## Про данные

Стандартный предзаполненный набор товаров лежит в `src/main/resources/data.sql`.
Изображения товаров находятся в `src/main/resources/static/images`.