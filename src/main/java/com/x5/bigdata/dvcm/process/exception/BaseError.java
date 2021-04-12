package com.x5.bigdata.dvcm.process.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseError {

    @JsonProperty("type")
    private final ErrorType type;

    @JsonProperty("description")
    private final String description;

    public BaseError(@JsonProperty("type") ErrorType type) {
        this.type = type;
        description = null;
    }

    public BaseError(@JsonProperty("type") ErrorType type,
                     @JsonProperty("description") String description) {
        this.type = type;
        this.description = description;
    }

    public ErrorType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
