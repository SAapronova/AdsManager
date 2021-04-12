package com.x5.bigdata.dvcm.process.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.bigdata.dvcm.process.dto.SegmentDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
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
public class CheckUpcTask implements JavaDelegate {
    private static final String URL = "/cvm_upc/communications/check";

    private final CampaignService campaignService;
    private final GuestService guestService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String campaignCode = execution.getProcessBusinessKey();
        log.info("Init CheckUpcTask for campaign {} ", campaignCode);

        Campaign campaign = campaignService.getByCode(campaignCode);

        for (Segment segment : campaign.getSegments()) {
            if (!SegmentType.CONTROL_GROUP.equals(segment.getType())) {
                List<Long> codes = guestService.getCodesBySegmentId(segment.getId());

                if (!codes.isEmpty()) {
                    SegmentDto dto = SegmentDto.builder()
                            .campaignCode(campaignCode)
                            .channelType(segment.getChannelType())
                            .guests(codes)
                            .build();

                    log.info("CheckUpcTask request: {} ", objectMapper.writeValueAsString(dto));
                    Map<String, String> statuses = restTemplate.postForObject(URL, dto, HashMap.class);
                    log.info("CheckUpcTask response: {} ", statuses);

                    guestService.setUpcStatus(segment.getId(), statuses);
                }
            }
        }
        log.info("End CheckUpcTask for campaign {} ", campaignCode);
    }
}
