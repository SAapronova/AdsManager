package com.x5.bigdata.dvcm.process.dto;

import com.x5.bigdata.dvcm.process.entity.SegmentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class SegmentInfoDto {
    private UUID id;
    private SegmentType type;
    private String channelType;
    private String contentText;
    private String contentLink;
    private String contentLinkText;
    private String imageUrl;
    private String offerTemplate;
    private Integer points;
    private Integer multiplier;
    private Integer discount;
    private Integer minSum;
    private Integer purchases;
    private Integer rewardPeriod;
    private String zeroNameCategory;
    private String firstNameCategory;
    private String secondNameCategory;
    private String textSlipCheck;
    private Integer pluCount;
    private String pluList;
    private Integer maxBenefit;
    private Integer cashback;
    private Boolean isRuleOn;
    private Boolean isSegmentOn;
    private String ruleCode;
    private Boolean isUpc;
}
