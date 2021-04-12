package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComarchRuleDto {
    @JsonProperty("rule_code")
    private String ruleCode;
}
