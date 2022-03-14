package process_manager.exception;

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
