# JeuxSonde

## Проекты и назначение

- **JeuxDBContext** — модуль с JPA сущностями и контекстом БД (Hibernate).
- **JeuxDBContextTest** — тестовый модуль для проверки доступа к БД и сервисов загрузки.
- **JeuxWebAPI** — веб‑API на Quarkus для чтения данных из БД.
- **JeuxWebAPITest** — модуль тестов веб‑API (QuarkusTest + RestAssured).

## Команды сборки и запуска

Ниже приведены команды для PowerShell, выполнять из корня репозитория:

```
cd C:\Projects\X-Probes\JsrvProbe\JeuxSonde
```

### JeuxDBContext

**Сборка модуля**
```
mvn -pl JeuxDBContext clean install
```

### JeuxDBContextTest

**Сборка/запуск тестов**
```
mvn -pl JeuxDBContextTest clean test
```

### JeuxWebAPI

## Асинхронная реализация (Mutiny + Hibernate Reactive)

В API используется реактивный стек:
- REST-эндпоинты возвращают `Uni` (Mutiny).
- Доступ к БД реализован через Hibernate Reactive и `Mutiny.SessionFactory`.
- Асинхронные цепочки в сервисах выполняются последовательно в пределах одной сессии.

### Настройки для асинхронного решения

Основные параметры:
- `quarkus.datasource.reactive.url` — реактивный URL подключения к PostgreSQL.
- `quarkus.hibernate-orm.database.*` — общие настройки схемы/генерации (используются и в reactive-режиме).
- `quarkus.hibernate-orm.packages=Entities` — пакет с JPA сущностями.
- `quarkus.index-dependency.jeuxdbcontext.*` — индексирование сущностей из модуля `JeuxDBContext`.
- `quarkus.oidc.client-id` — обязателен для старта WebAPI при включенном OIDC.

Файл настроек WebAPI: `JeuxWebAPI\config\application.properties`  
Файл настроек тестов: `JeuxWebAPITest\config\application.properties`

**Сборка**
```
mvn -pl JeuxWebAPI clean package
```

## Аутентификация

Веб‑API защищено OIDC (Keycloak).

- Все эндпоинты модулей `League/Match/Team/Tournament` требуют `@Authenticated`.
- Операции создания/обновления/удаления дополнительно требуют роли:
  - `probe-owner` или `probe-contrib`.

### Настройка Keycloak

Настройки OIDC находятся в `JeuxWebAPI\config\application.properties`:
- `quarkus.oidc.auth-server-url=http://localhost:8082/realms/probe-app`
- `quarkus.oidc.application-type=service`

### Тесты WebAPI

Тесты используют внешний Keycloak и получают токен через password grant.
Параметры запроса токена задаются в `JeuxWebAPITest\config\application.properties`:

- `keycloak.auth.token-url=http://localhost:8082/realms/probe-app/protocol/openid-connect/token`
- `keycloak.auth.client-id=probe-app-client`
- `keycloak.auth.grant-type=password`
- `keycloak.auth.username=testuser01`
- `keycloak.auth.password=Test-user123`

В тестовой конфигурации Dev Services отключены:
- `quarkus.devservices.enabled=false`

**Запуск в dev‑режиме**

Перед запуском убедитесь, что:
- PostgreSQL доступен по настройкам в `JeuxWebAPI\config\application.properties`
- порт `30881` свободен
- задан параметр `RFLeagueId` в `JeuxWebAPI\config\application.properties`

Команда запуска (PowerShell):
```
mvn -pl JeuxWebAPI quarkus:dev
```

По умолчанию API доступно по адресу:
```
http://localhost:30881/api/q/v1
```

**Остановка**

Найти процесс, который занял порт можно командой:
```
netstat -a -n -o | Select-String -Pattern ':30881'
```

Остановить процесс можно через Диспетчер задач или командой:
```
taskkill /PID <pid> /F
```

### JeuxWebAPITest

**Сборка/запуск тестов**

Перед запуском убедитесь, что:
- PostgreSQL доступен по настройкам в `JeuxWebAPITest\config\application.properties`
- задан параметр `RFLeagueId` в `JeuxWebAPITest\config\application.properties`

Команда запуска (PowerShell):
```
mvn -pl JeuxWebAPITest test
```

