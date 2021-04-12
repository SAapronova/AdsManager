package com.x5.bigdata.dvcm.process.exception;

import java.util.List;

public class NotFoundException extends RuntimeException {

    private final transient List<NotFoundItem> items;

    public NotFoundException(List<NotFoundItem> items) {
        this.items = items;
    }

    public List<NotFoundItem> getItems() {
        return items;
    }
}
