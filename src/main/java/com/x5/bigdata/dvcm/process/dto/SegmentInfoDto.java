package com.x5.bigdata.dvcm.process.dto;

import com.x5.bigdata.dvcm.process.entity.OfferTemplate;
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
    private OfferTemplate offerTemplate;
    private Integer points;
    private Integer multiplier;
    private Integer discount;
    private Integer cacheBack;
    private Integer minSum;
    private Integer purchases;
    private Integer rewardPeriod;
    private Integer maxReward;
    private Boolean isRuleOn;
    private Boolean isSegmentOn;
    private String ruleCode;
    private Boolean isUpc;
}
