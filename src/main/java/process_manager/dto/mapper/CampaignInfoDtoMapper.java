package process_manager.dto.mapper;

import process_manager.dto.CampaignInfoDto;
import process_manager.dto.SegmentInfoDto;
import process_manager.entity.Campaign;

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
                                .cashback(segment.getCashback())
                                .minSum(segment.getMinSum())
                                .purchases(segment.getPurchases())
                                .rewardPeriod(segment.getRewardPeriod())
                                .maxBenefit(segment.getMaxBenefit())
                                .zeroNameCategory(segment.getZeroNameCategory())
                                .firstNameCategory(segment.getFirstNameCategory())
                                .secondNameCategory(segment.getSecondNameCategory())
                                .pluCount(segment.getPluCount())
                                .pluList(segment.getPluList())
                                .textSlipCheck(segment.getTextSlipCheck())
                                .isRuleOn(segment.getIsRuleOn())
                                .isSegmentOn(segment.getIsSegmentOn())
                                .ruleCode(segment.getRuleCode())
                                .isUpc(segment.getIsUpc())
                                .build()).collect(Collectors.toList()))
                .build();
    }
    private CampaignInfoDtoMapper() {
        throw new IllegalStateException();
    }
}
