# My market app

Веб-приложение "Витрина интернет-магазина" на Spring Boot (Web MVC + Thymeleaf), H2, Spring Data JPA (Hibernate), Java 21.

## Требования к окружению
- Java 21
- Docker
- Maven Wrapper (уже в проекте) или Maven

## Как запустить тесты
Из корня проекта выполнить:
```bash
./mvnw test
```

## Как поднять
### Локально
Из корня проекта выполнить:
```bash
./mvnw spring-boot:run
```
### В Docker-контейнере
1) Собрать образ:
```bash
docker build -t my-market-app .
```
2) Запустить:
```bash
docker run --rm -p 8080:8080 my-market-app
```

Убедиться, что все в порядке: http://localhost:8080/ должен открыть главную страницу магазина.

## Про данные
Стандартный предзаполненный набор товаров лежит в `src/main/resources/data.sql`.
Изображения товаров находятся в `src/main/resources/static/images`.