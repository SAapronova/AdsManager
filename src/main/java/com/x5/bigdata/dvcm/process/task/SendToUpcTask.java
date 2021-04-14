package com.x5.bigdata.dvcm.process.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.bigdata.dvcm.process.dto.ContentParamsDto;
import com.x5.bigdata.dvcm.process.dto.MechanicsParamsDto;
import com.x5.bigdata.dvcm.process.dto.SegmentDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.GuestService;
import com.x5.bigdata.dvcm.process.service.SegmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendToUpcTask implements JavaDelegate {
    private static final String URL = "/cvm_upc/communications/";

    private final CampaignService campaignService;
    private final SegmentService segmentService;
    private final GuestService guestService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String campaignCode = execution.getProcessBusinessKey();
        log.info("Init SendToUpcTask for campaign {} ", campaignCode);

        Campaign campaign = campaignService.getByCode(campaignCode);

        for (Segment segment : campaign.getSegments()) {
            if (!SegmentType.CONTROL_GROUP.equals(segment.getType())) {
                List<Long> codes = guestService.getCodesBySegmentId(segment.getId());

                if (!codes.isEmpty()) {
                    SegmentDto dto = SegmentDto.builder()
                            .campaignCode(campaignCode)
                            .periodStart(campaign.getPeriodStart())
                            .periodEnd(campaign.getPeriodEnd())
                            .segmentType(segment.getType())
                            .channelType(segment.getChannelType())
                            .mechanics(segment.getOfferTemplate())
                            .mechanicsParams(MechanicsParamsDto.builder()
                                    .points(segment.getPoints())
                                    .purchasesNum(segment.getPurchases())
                                    .minSum(segment.getMinSum())
                                    .rewardsPeriod(segment.getRewardPeriod())
                                    .multiplier(segment.getMultiplier())
                                    .build())
                            .contentParams(ContentParamsDto.builder()
                                    .contentText(segment.getContentText())
                                    .contentLinkText(segment.getContentLinkText())
                                    .contentLink(segment.getContentLink())
                                    .contentTemplate(segment.getContentText())
                                    .imageUrl(segment.getImageUrl())
                                    .build())
                            .phones(segment.getTestPhones())
                            .guests(codes)
                            .build();

                    log.info("SendToUpcTask request: {} ", objectMapper.writeValueAsString(dto));
                    restTemplate.postForObject(URL, dto, String.class);
                    segmentService.setIsUpc(segment.getId());
                }
            }
        }
        campaignService.setStatus(campaignCode, CampaignStatus.UPC);
        log.info("End SendToUpcTask for campaign {} ", campaignCode);
    }
}
