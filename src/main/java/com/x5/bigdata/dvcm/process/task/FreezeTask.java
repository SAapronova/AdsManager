package com.x5.bigdata.dvcm.process.task;

import com.x5.bigdata.dvcm.process.dto.SegmentDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.GuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FreezeTask implements JavaDelegate {
    private static final String URL = "/freeze/freeze/";

    private final CampaignService campaignService;
    private final GuestService guestService;
    private final RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
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
                try {
                    Map<String, Boolean> statuses = restTemplate.postForObject(URL, dto, HashMap.class);
                    log.info("FreezeTask frozen: {} ",
                            statuses.entrySet().stream().filter(Map.Entry::getValue).count());

                    guestService.setFrozen(segment.getId(), statuses);
                } catch (NullPointerException e) {
                    log.info("FreezeTask error : ", e.getCause());
                }
            }
        }
        campaignService.setStatus(campaignCode, CampaignStatus.FREEZE);
        log.info("End FreezeTask for campaign {} ", campaignCode);
    }
}
