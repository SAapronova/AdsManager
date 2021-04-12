package com.x5.bigdata.dvcm.process.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ChannelType {
    @JsonProperty("sms")
    SMS,

    @JsonProperty("viber")
    VIBER
}
