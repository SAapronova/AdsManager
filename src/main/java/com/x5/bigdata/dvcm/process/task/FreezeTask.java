package com.x5.bigdata.dvcm.process.task;

import com.x5.bigdata.dvcm.process.dto.SegmentDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.GuestService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FreezeTask implements JavaDelegate {
    private static final String FREEZE_PATH = "/freeze/";

    private final CampaignService campaignService;
    private final GuestService guestService;
    private final RestTemplate restTemplate;
    private final String host;
    private final Integer port;

    public FreezeTask (CampaignService campaignService,
                         GuestService guestService,
                         RestTemplate restTemplate,
                         @Value("${dcvm.freeze.host:dcvm-freeze-service}") String host,
                         @Value("${ dcvm.freeze.port:8080}") Integer port) {
        this.campaignService = campaignService;
        this.guestService = guestService;
        this.restTemplate = restTemplate;
        this.host = host;
        this.port = port;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        URI uri = new URI("http", null, host, port, FREEZE_PATH, null, null);
        String campaignCode = execution.getProcessBusinessKey();
        log.info("Init FreezeTask for campaign {} ", campaignCode);

        Campaign campaign = campaignService.getByCode(campaignCode);

        for (Segment segment : campaign.getSegments()) {
            List<Long> codes = guestService.getCodesBySegmentId(segment.getId());

            if (!codes.isEmpty()) {
                SegmentDto dto = SegmentDto.builder()
                        .campaignCode(campaignCode)
                        .periodStart(campaign.getPeriodStart())
                        .periodEnd(campaign.getPeriodEnd())
                        .segmentType(segment.getType())
                        .guests(codes)
                        .build();

                log.info("FreezeTask request: {} ", dto);
                Map<String, Boolean> statuses = restTemplate.postForObject(uri, dto, HashMap.class);
                log.info("FreezeTask frozen: {} ",
                        statuses.entrySet().stream().filter(entry -> entry.getValue()).count());

                guestService.setFrozen(segment.getId(), statuses);
            }
        }
        campaignService.setStatus(campaignCode, CampaignStatus.FREEZE);
        log.info("End FreezeTask for campaign {} ", campaignCode);
    }
}
