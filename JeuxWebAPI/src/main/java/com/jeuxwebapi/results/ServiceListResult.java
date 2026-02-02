package com.jeuxwebapi.results;

import java.util.List;

public class ServiceListResult<T> {
    private boolean result;
    private String message;
    private List<Validation> validations;
    private String code;
    private int total;
    private List<T> items;

    public ServiceListResult() {
    }

    public ServiceListResult(
            boolean result,
            String message,
            List<Validation> validations,
            String code,
            int total,
            List<T> items
    ) {
        this.result = result;
        this.message = message;
        this.validations = validations;
        this.code = code;
        this.total = total;
        this.items = items;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Validation> getValidations() {
        return validations;
    }

    public void setValidations(List<Validation> validations) {
        this.validations = validations;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
