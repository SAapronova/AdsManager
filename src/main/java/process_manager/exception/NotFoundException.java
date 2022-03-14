package process_manager.exception;

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
