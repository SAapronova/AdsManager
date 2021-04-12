package com.x5.bigdata.dvcm.process.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static com.x5.bigdata.dvcm.process.exception.ErrorType.NOT_FOUND_ERROR;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotFoundError extends BaseError {
    @JsonProperty("items")
    private final List<NotFoundItem> items;

    public NotFoundError(@JsonProperty("items") List<NotFoundItem> items) {
        super(NOT_FOUND_ERROR);
        this.items = items;
    }

    public List<NotFoundItem> getItems() {
        return items;
    }
}
