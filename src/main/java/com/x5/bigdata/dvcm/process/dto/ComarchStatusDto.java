package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ComarchStatusDto {
    @JsonProperty("rule")
    private Boolean isRuleOn;

    @JsonProperty("segment")
    private Boolean isSegmentOn;
}
