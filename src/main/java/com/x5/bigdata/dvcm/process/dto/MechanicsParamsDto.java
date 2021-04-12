package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MechanicsParamsDto {
    @JsonProperty("OFFER_POINT_COEFFICIENT")
    private Integer multiplier;

    @JsonProperty("OFFER_BONUS_AMT")
    private Integer points;

    @JsonProperty("OFFER_LIST_PRODUCT1")
    private String category1;

    @JsonProperty("OFFER_CH_SUM")
    private Integer minSum;

    @JsonProperty("OFFER_CH_BALL_AMOUNT")
    private Integer purchasesNum;

    @JsonProperty("OFFER_EXPT_DELAY")
    private Integer rewardsPeriod;
}
