package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OfferDataDto {

    @JsonProperty("points")
    private Integer points;

    @JsonProperty("min_sum")
    private Integer minSum;

    @JsonProperty("purchases_num")
    private Integer purchases;

    @JsonProperty("rewards_period")
    private Integer rewardPeriod;

    @JsonProperty("zero_name_category")
    private String zeroNameCategory;

    @JsonProperty("first_name_category")
    private String firstNameCategory;

    @JsonProperty("second_name_category")
    private String secondNameCategory;

    @JsonProperty("text_slip_check")
    private String textSlipCheck;

    @JsonProperty("multiplier")
    private Integer multiplier;

    @JsonProperty("discount")
    private Integer discount;

    @JsonProperty("plu_count")
    private Integer pluCount;

    @JsonProperty("plu_list")
    private String pluList;

    @JsonProperty("max_benefit")
    private Integer maxBenefit;

    @JsonProperty("cashback")
    private Integer cashback;

}
