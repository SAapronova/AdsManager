package process_manager.entity;

public enum GuestCommunicationStatus {
    PENDING,
    ERROR,
    DELIVERED,
    DEFERRED,
    SYSTEM_ERROR;

    public static GuestCommunicationStatus getEnum(String value) {
        for(GuestCommunicationStatus enumValue : values())
            if(enumValue.toString().equalsIgnoreCase(value)) return enumValue;
        throw new IllegalArgumentException();
    }
}
