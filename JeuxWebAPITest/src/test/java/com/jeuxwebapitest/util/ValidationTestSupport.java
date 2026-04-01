package com.jeuxwebapitest.util;

/** Вспомогательные константы и строки для тестов ответа валидации (длины полей как в JPA {@code @Column}). */
public final class ValidationTestSupport {

    private ValidationTestSupport() {
    }

    /** Строка длины {@code maxLen + 1} (превышает ограничение колонки на 1 символ). */
    public static String longerThan(int maxLen) {
        return "x".repeat(maxLen + 1);
    }

    /** Текст сообщения, который формирует {@code StringLengthValidation} на русском. */
    public static String expectedTooLongMessage(int maxLen) {
        return String.format("Длина превышает допустимые %d символов.", maxLen);
    }
}
