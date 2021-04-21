package com.x5.bigdata.dvcm.process.task;

import com.x5.bigdata.dvcm.process.dto.SegmentDto;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnfreezeTask implements JavaDelegate {
    private static final String URL = "/freeze/freeze/";

    private final CampaignService campaignService;
    private final RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String campaignCode = execution.getProcessBusinessKey();
        unfreeze(campaignCode);
    }

    public void unfreeze(String campaignCode) {
        log.info("Init UnfreezeTask for campaign {} ", campaignCode);

        SegmentDto dto = SegmentDto.builder()
                .campaignCode(campaignCode)
                .build();

        HttpEntity<SegmentDto> request = new HttpEntity<>(dto);
        restTemplate.exchange(URL, HttpMethod.DELETE, request, String.class);

        campaignService.setStatus(campaignCode, CampaignStatus.FINISH);
        log.info("End UnfreezeTask for campaign {} ", campaignCode);
    }
}
