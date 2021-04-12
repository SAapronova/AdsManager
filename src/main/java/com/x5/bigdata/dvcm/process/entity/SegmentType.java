package com.x5.bigdata.dvcm.process.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SegmentType {
    @JsonProperty("1")
    CONTROL_GROUP,

    @JsonProperty("2")
    TARGET_GROUP
}
