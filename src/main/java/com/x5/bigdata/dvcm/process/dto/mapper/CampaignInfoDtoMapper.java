package com.x5.bigdata.dvcm.process.dto.mapper;

import com.x5.bigdata.dvcm.process.dto.CampaignInfoDto;
import com.x5.bigdata.dvcm.process.dto.SegmentInfoDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;

import java.util.stream.Collectors;

public class CampaignInfoDtoMapper {
    public static CampaignInfoDto map(Campaign campaign) {
        return CampaignInfoDto.builder()
                .id(campaign.getId())
                .campaignCode(campaign.getCampaignCode())
                .periodStart(campaign.getPeriodStart())
                .periodEnd(campaign.getPeriodEnd())
                .postPeriodEnd(campaign.getPostPeriodEnd())
                .status(campaign.getStatus())
                .createTime(campaign.getCreateTime())
                .segments(campaign.getSegments().stream().map(segment ->
                        SegmentInfoDto.builder()
                                .id(segment.getId())
                                .type(segment.getType())
                                .channelType(segment.getChannelType())
                                .contentLink(segment.getContentLink())
                                .contentText(segment.getContentText())
                                .contentLinkText(segment.getContentLinkText())
                                .imageUrl(segment.getImageUrl())
                                .offerTemplate(segment.getOfferTemplate())
                                .points(segment.getPoints())
                                .multiplier(segment.getMultiplier())
                                .discount(segment.getDiscount())
                                .cacheBack(segment.getCacheBack())
                                .minSum(segment.getMinSum())
                                .purchases(segment.getPurchases())
                                .rewardPeriod(segment.getRewardPeriod())
                                .maxReward(segment.getMaxReward())
                                .isRuleOn(segment.getIsRuleOn())
                                .isSegmentOn(segment.getIsSegmentOn())
                                .ruleCode(segment.getRuleCode())
                                .isUpc(segment.getIsUpc())
                                .build()).collect(Collectors.toList()))
                .build();
    }
}
