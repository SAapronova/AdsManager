package process_manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SegmentType {
    @JsonProperty("1")
    CONTROL_GROUP,

    @JsonProperty("2")
    TARGET_GROUP,

    @JsonProperty("6")
    TEST_COMMUNICATION
}
