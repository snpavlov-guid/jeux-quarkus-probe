package Services;

import jakarta.persistence.Column;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/** Сведения о полях JPA-сущностей (длина строковых колонок по {@link Column}). */
public class DBContextInfoService {

    /**
     * Возвращает {@link Column#length()} для строкового поля или {@code null}, если аннотации {@link Column} нет.
     *
     * @param fieldName имя поля в классе сущности (как в исходном коде, например {@code "City"}).
     */
    public Integer getStringFieldLength(Class<?> entityClass, String fieldName) {
        Field field = findDeclaredField(entityClass, fieldName);
        if (field == null) {
            throw new IllegalArgumentException(
                    "Поле '" + fieldName + "' не найдено в классе " + entityClass.getName());
        }
        if (field.getType() != String.class) {
            throw new IllegalArgumentException(
                    "Поле '" + fieldName + "' в классе " + entityClass.getName()
                            + " не имеет тип String, фактический тип: " + field.getType().getName());
        }
        Column column = field.getAnnotation(Column.class);
        if (column == null) {
            return null;
        }
        return column.length();
    }

    /**
     * Для всех полей типа {@link String} с аннотацией {@link Column} возвращает карту
     * «имя поля в сущности» → {@link Column#length()}.
     */
    public Map<String, Integer> getStringFieldLengths(Class<?> entityClass) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (field.getType() != String.class) {
                    continue;
                }
                Column column = field.getAnnotation(Column.class);
                if (column == null) {
                    continue;
                }
                map.put(field.getName(), column.length());
            }
        }
        return map;
    }

    /** Ищет объявленное поле по цепочке суперклассов. */
    private static Field findDeclaredField(Class<?> start, String fieldName) {
        for (Class<?> c = start; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // искать в суперклассе
            }
        }
        return null;
    }
}
