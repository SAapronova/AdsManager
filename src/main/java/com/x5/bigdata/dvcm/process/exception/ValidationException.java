package com.x5.bigdata.dvcm.process.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final transient List<ValidationItem> items;

    public ValidationException(List<ValidationItem> items) {
        this.items = items;
    }

    public List<ValidationItem> getItems() {
        return items;
    }
}
