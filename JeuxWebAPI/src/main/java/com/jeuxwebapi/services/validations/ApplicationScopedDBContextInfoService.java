package com.jeuxwebapi.services.validations;

import Services.DBContextInfoService;
import jakarta.enterprise.context.ApplicationScoped;

/** CDI-бин Quarkus: делегирует базовой реализации из модуля JeuxDBContext. */
@ApplicationScoped
public class ApplicationScopedDBContextInfoService extends DBContextInfoService {
}
