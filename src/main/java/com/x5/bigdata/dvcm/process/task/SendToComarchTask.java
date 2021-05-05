package com.x5.bigdata.dvcm.process.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.bigdata.dvcm.process.dto.ComarchRuleDto;
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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendToComarchTask implements JavaDelegate {
    private static final String URL = "/cvm_comarch/rule/";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CampaignService campaignService;
    private final SegmentService segmentService;
    private final GuestService guestService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String campaignCode = execution.getProcessBusinessKey();
        log.info("Init SendToComarchTask for campaign {} ", campaignCode);

        Campaign campaign = campaignService.getByCode(campaignCode);
        if (CampaignStatus.FINISH.equals(campaign.getStatus())) {
            return;
        }

        for (Segment segment : campaign.getSegments()) {
            if (!SegmentType.CONTROL_GROUP.equals(segment.getType())) {
                List<Long> codes = guestService.getFrozenCodesBySegmentId(segment.getId());

                if (!codes.isEmpty()) {
                    SegmentDto dto = SegmentDto.builder()
                            .campaignCode(campaignCode)
                            .periodStart(campaign.getPeriodStart())
                            .periodEnd(campaign.getPeriodEnd())
                            .segmentType(segment.getType())
                            .imageUrl(segment.getImageUrl())
                            .mechanics(segment.getOfferTemplate())
                            .mechanicsParams(MechanicsParamsDto.builder()
                                    .points(segment.getPoints())
                                    .purchasesNum(segment.getPurchases())
                                    .minSum(segment.getMinSum())
                                    .rewardsPeriod(segment.getRewardPeriod())
                                    .multiplier(segment.getMultiplier())
                                    .build())
                            .guests(codes)
                            .build();

                    log.info("SendToComarchTask request: {} ", dto);
                    ComarchRuleDto ruleDto = restTemplate.postForObject(URL, dto, ComarchRuleDto.class);
                    log.info("SendToComarchTask responce: {} ", objectMapper.writeValueAsString(ruleDto));

                    segmentService.setRuleCode(segment.getId(), Optional.ofNullable(ruleDto).orElseThrow().getRuleCode());
                }
            }
        }
        campaignService.setStatus(campaignCode, CampaignStatus.CLM);
        log.info("End SendToComarchTask for campaign {} ", campaignCode);
    }
}
