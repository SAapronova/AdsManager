package com.x5.bigdata.dvcm.process.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static com.x5.bigdata.dvcm.process.exception.ErrorType.VALIDATION_ERROR;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationError extends BaseError {
    @JsonProperty("items")
    private final List<ValidationItem> items;

    public ValidationError(@JsonProperty("items") List<ValidationItem> items) {
        super(VALIDATION_ERROR);
        this.items = items;
    }

    public List<ValidationItem> getItems() {
        return items;
    }
}