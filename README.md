# JeuxSonde

## Проекты и назначение

- **JeuxDBContext** — модуль с JPA сущностями и контекстом БД (Hibernate).
- **JeuxDBContextTest** — тестовый модуль для проверки доступа к БД и сервисов загрузки.
- **JeuxWebAPI** — веб‑API на Quarkus для чтения данных из БД.

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

**Сборка**
```
mvn -pl JeuxWebAPI clean package
```

**Запуск в dev‑режиме**

Перед запуском убедитесь, что:
- PostgreSQL доступен по настройкам в `JeuxWebAPI\config\application.properties`
- порт `30881` свободен

Команда запуска (PowerShell):
```
mvn -pl JeuxWebAPI quarkus:dev
```

По умолчанию API доступно по адресу:
```
http://localhost:30881/api/q/v1
```

**Остановка**

Остановить процесс можно через Диспетчер задач или командой:
```
taskkill /PID <pid> /F
```

