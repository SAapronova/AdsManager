package com.x5.bigdata.dvcm.process.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.bigdata.dvcm.process.dto.ComarchRuleDto;
import com.x5.bigdata.dvcm.process.dto.OfferDataDto;
import com.x5.bigdata.dvcm.process.dto.SegmentDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.GuestService;
import com.x5.bigdata.dvcm.process.service.SegmentService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class SendToComarchTask implements JavaDelegate {
    private static final String COMARCH_TASK = "/rule/";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CampaignService campaignService;
    private final SegmentService segmentService;
    private final GuestService guestService;
    private final String host;
    private final Integer port;

    public SendToComarchTask(RestTemplate restTemplate,
                             ObjectMapper objectMapper,
                             CampaignService campaignService,
                             SegmentService segmentService,
                             GuestService guestService,
                             @Value("${dcvm.comarch.host:dcvm-comarch-service}")String host,
                             @Value("${dcvm.comarch.port:8080}")Integer port) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.campaignService = campaignService;
        this.segmentService = segmentService;
        this.guestService = guestService;
        this.host = host;
        this.port = port;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        URI uri = new URI("http", null, host, port, COMARCH_TASK, null, null);
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
                            .mechanicsParams(OfferDataDto.builder()
                                    .points(segment.getPoints())
                                    .purchases(segment.getPurchases())
                                    .minSum(segment.getMinSum())
                                    .rewardPeriod(segment.getRewardPeriod())
                                    .multiplier(segment.getMultiplier())
                                    .discount(segment.getDiscount())
                                    .cashback(segment.getCashback())
                                    .firstNameCategory(segment.getFirstNameCategory())
                                    .secondNameCategory(segment.getSecondNameCategory())
                                    .zeroNameCategory(segment.getZeroNameCategory())
                                    .maxBenefit(segment.getMaxBenefit())
                                    .pluCount(segment.getPluCount())
                                    .pluList(segment.getPluList())
                                    .textSlipCheck(segment.getTextSlipCheck())
                                    .build())
                            .guests(codes)
                            .build();

                    log.info("SendToComarchTask request: {} ", dto);
                    ComarchRuleDto ruleDto = restTemplate.postForObject(uri, dto, ComarchRuleDto.class);
                    log.info("SendToComarchTask responce: {} ", objectMapper.writeValueAsString(ruleDto));

                    segmentService.setRuleCode(segment.getId(), Optional.ofNullable(ruleDto).orElseThrow().getRuleCode());
                }
            }
        }
        campaignService.setStatus(campaignCode, CampaignStatus.CLM);
        log.info("End SendToComarchTask for campaign {} ", campaignCode);
    }
}
