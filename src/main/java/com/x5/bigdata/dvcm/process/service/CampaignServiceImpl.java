package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.dto.CampaignDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import com.x5.bigdata.dvcm.process.exception.ValidationException;
import com.x5.bigdata.dvcm.process.exception.ValidationItem;
import com.x5.bigdata.dvcm.process.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.x5.bigdata.dvcm.process.validators.ValidationMessages.CAMPAIGN_ALREADY_EXISTS;

@Slf4j
@RequiredArgsConstructor
@Service
public class CampaignServiceImpl implements CampaignService {
    public static final String CAMPAIGN_PROCESS_DEFINITION_KEY = "CampaignProcess";
    private final CampaignRepository campaignRepository;
    private final SegmentService segmentService;
    private final RuntimeService runtimeService;
    private final KafkaService kafkaService;

    @Transactional
    public Campaign getByCode(String campaignCode) {
        return campaignRepository.findCampaignByCampaignCode(campaignCode);
    }

    @Transactional
    public Campaign create(CampaignDto dto) {
        if (campaignRepository.existsByCampaignCode(dto.getCampaignCode())) {
            throw new ValidationException(List.of(
                    new ValidationItem("camp_id", dto.getCampaignCode(), CAMPAIGN_ALREADY_EXISTS)));
        }

        Campaign campaign = new Campaign()
                .setId(UUID.randomUUID())
                .setCampaignCode(dto.getCampaignCode())
                .setCreateTime(LocalDateTime.now())
                .setPeriodStart(dto.getPeriodStart())
                .setPeriodEnd(dto.getPeriodEnd())
                .setPostPeriodEnd(dto.getPostPeriodEnd())
                .setStatus(CampaignStatus.START);

        campaign = campaignRepository.save(campaign);
        segmentService.save(campaign.getId(), dto.getSegments());
        log.info("Save new campaign: {}", campaign);

        Map<String, Object> variables = new HashMap<>();
        variables.put("camp_id", campaign.getCampaignCode());
        variables.put("next_time", java.sql.Timestamp.valueOf(campaign.getPeriodStart()));
        variables.put("post_period_end", java.sql.Timestamp.valueOf(campaign.getPostPeriodEnd()));
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(CAMPAIGN_PROCESS_DEFINITION_KEY,
                campaign.getCampaignCode(), variables);
        log.info("Run process {} for campaign {} ", pi.getId(), campaign.getCampaignCode());

        return campaign;
    }

    @Override
    @Transactional
    public void setStatus(String campaignCode, CampaignStatus status) {
        Campaign campaign = campaignRepository.findCampaignByCampaignCode(campaignCode);
        if (!campaign.getStatus().equals(status)) {
            campaign.setStatus(status);
            kafkaService.sendProcessStatus(campaignCode, status);
        }
    }
}
