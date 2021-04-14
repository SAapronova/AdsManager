package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;

@Getter
@Setter
@Builder
public class OfferDataDto {
    @JsonProperty("points")
    @Min(1)
    private Integer points;

    @JsonProperty("min_sum")
    @Min(1)
    private Integer minSum;

    @JsonProperty("purchases_num")
    @Min(1)
    private Integer purchases;

    @JsonProperty("rewards_period")
    @Min(1)
    private Integer rewardPeriod;
}
