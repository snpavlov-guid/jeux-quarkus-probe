package com.jeuxwebapi.results;

import java.util.List;

public class ServiceDataResult<T> {
    private boolean result;
    private String message;
    private List<Validation> validations;
    private String code;
    private T data;

    public ServiceDataResult() {
    }

    public ServiceDataResult(
            boolean result,
            String message,
            List<Validation> validations,
            String code,
            T data
    ) {
        this.result = result;
        this.message = message;
        this.validations = validations;
        this.code = code;
        this.data = data;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
