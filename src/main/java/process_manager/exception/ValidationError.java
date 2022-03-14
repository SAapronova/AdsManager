package process_manager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static process_manager.exception.ErrorType.VALIDATION_ERROR;

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