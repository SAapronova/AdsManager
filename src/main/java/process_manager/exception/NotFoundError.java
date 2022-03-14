package process_manager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotFoundError extends BaseError {
    @JsonProperty("items")
    private final List<NotFoundItem> items;

    public NotFoundError(@JsonProperty("items") List<NotFoundItem> items) {
        super(ErrorType.NOT_FOUND_ERROR);
        this.items = items;
    }

    public List<NotFoundItem> getItems() {
        return items;
    }
}
