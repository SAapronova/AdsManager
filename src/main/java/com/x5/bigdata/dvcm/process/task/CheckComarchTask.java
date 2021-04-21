package com.x5.bigdata.dvcm.process.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.bigdata.dvcm.process.dto.ComarchStatusDto;
import com.x5.bigdata.dvcm.process.dto.MechanicsParamsDto;
import com.x5.bigdata.dvcm.process.dto.SegmentDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.SegmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckComarchTask implements JavaDelegate {
    private static final String URL = "/cvm_comarch/rule/check";

    private final CampaignService campaignService;
    private final SegmentService segmentService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String campaignCode = execution.getProcessBusinessKey();
        log.info("Init CheckComarchTask for campaign {} ", campaignCode);

        Campaign campaign = campaignService.getByCode(campaignCode);
        if (CampaignStatus.FINISH.equals(campaign.getStatus())) {
            return;
        }

        for (Segment segment : campaign.getSegments()) {
            if (!SegmentType.CONTROL_GROUP.equals(segment.getType())) {
                SegmentDto dto = SegmentDto.builder()
                        .campaignCode(campaignCode)
                        .periodStart(campaign.getPeriodStart())
                        .mechanics(segment.getOfferTemplate())
                        .mechanicsParams(MechanicsParamsDto.builder()
                                .points(segment.getPoints())
                                .purchasesNum(segment.getPurchases())
                                .minSum(segment.getMinSum())
                                .rewardsPeriod(segment.getRewardPeriod())
                                .multiplier(segment.getMultiplier())
                                .build())
                        .build();

                log.info("CheckComarchTask request: {} ", objectMapper.writeValueAsString(dto));
                ComarchStatusDto statusDto = restTemplate.postForObject(URL, dto, ComarchStatusDto.class);
                log.info("CheckComarchTask response: {} ", statusDto);

                segmentService.setComarchStatus(segment.getId(), statusDto);
            }
        }
        log.info("End CheckComarchTask for campaign {} ", campaignCode);
    }
}
