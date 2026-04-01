package com.jeuxwebapi.services.validations;

import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.Validation;
import java.util.List;
import java.util.Map;

/** Проверка длины строковых полей DTO по ограничениям из контекста БД. */
public final class StringLengthValidation {

    private StringLengthValidation() {
    }

    /**
     * Если задано ограничение и значение длиннее допустимого — добавляет запись в {@code validations}.
     */
    public static void addIfTooLong(List<Validation> validations, String property, String value, Integer maxLen) {
        if (maxLen == null || value == null) {
            return;
        }
        if (value.length() > maxLen) {
            validations.add(new Validation(
                    property,
                    String.format("Длина превышает допустимые %d символов.", maxLen)));
        }
    }

    /**
     * То же, что {@link #addIfTooLong(List, String, String, Integer)}, но максимальная длина берётся из карты
     * {@link Services.DBContextInfoService#getStringFieldLengths(Class)} по имени поля сущности.
     *
     * @param entityFieldName имя поля в классе сущности (например {@code "Name"}).
     */
    public static void addIfTooLong(
            List<Validation> validations,
            String dtoProperty,
            String value,
            Map<String, Integer> entityStringLengths,
            String entityFieldName) {
        addIfTooLong(validations, dtoProperty, value, entityStringLengths.get(entityFieldName));
    }

    /**
     * Неуспешный результат сервиса с общим сообщением и списком ошибок по полям.
     */
    public static <T> ServiceDataResult<T> failure(List<Validation> validations) {
        ServiceDataResult<T> result = new ServiceDataResult<>();
        result.setResult(false);
        result.setMessage("Ошибка валидации");
        result.setValidations(validations);
        return result;
    }
}
