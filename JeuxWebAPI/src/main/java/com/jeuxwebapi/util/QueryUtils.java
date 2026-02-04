package com.jeuxwebapi.util;

import org.hibernate.reactive.mutiny.Mutiny;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {
    private QueryUtils() {
    }

    public static boolean isDesc(String order) {
        return order != null && order.equalsIgnoreCase("desc");
    }

    public static void applyPaging(Mutiny.SelectionQuery<?> query, Integer skip, Integer size) {
        if (skip != null) {
            if (skip < 0) {
                throw badRequest("skip must be >= 0");
            }
            query.setFirstResult(skip);
        }
        if (size != null) {
            if (size < 0) {
                throw badRequest("size must be >= 0");
            }
            query.setMaxResults(size);
        }
    }

    public static List<Integer> parseTours(String tours) {
        if (tours == null || tours.isBlank()) {
            return List.of();
        }
        String[] parts = tours.split(",");
        List<Integer> result = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                result.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException ex) {
                throw badRequest("Invalid tours value: " + trimmed);
            }
        }
        return result;
    }

    public static LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw badRequest("Invalid date format. Expected yyyy-MM-dd");
        }
    }

    private static WebApplicationException badRequest(String message) {
        return new WebApplicationException(message, Response.Status.BAD_REQUEST);
    }
}
