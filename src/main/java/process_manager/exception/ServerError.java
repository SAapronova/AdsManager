package process_manager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerError extends BaseError {

    public ServerError(@JsonProperty("description") String description) {
        super(ErrorType.SERVER_ERROR, description);
    }
}