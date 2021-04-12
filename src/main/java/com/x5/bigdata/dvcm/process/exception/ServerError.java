package com.x5.bigdata.dvcm.process.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.x5.bigdata.dvcm.process.exception.ErrorType.SERVER_ERROR;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerError extends BaseError {

    public ServerError(@JsonProperty("description") String description) {
        super(SERVER_ERROR, description);
    }
}