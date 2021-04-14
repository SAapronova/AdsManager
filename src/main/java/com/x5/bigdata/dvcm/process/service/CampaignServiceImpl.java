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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.x5.bigdata.dvcm.process.validators.ValidationMessages.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CampaignServiceImpl implements CampaignService {
    public static final String CAMPAIGN_PROCESS_DEFINITION_KEY = "CampaignProcess";
    private final CampaignRepository campaignRepository;
    private final SegmentService segmentService;
    private final RuntimeService runtimeService;
    private final TemplateDefinitionService templateDefinitionService;
    private final KafkaService kafkaService;

    @Transactional
    public Campaign getByCode(String campaignCode) {
        return campaignRepository.findCampaignByCampaignCode(campaignCode);
    }

    @Transactional
    public Campaign create(CampaignDto dto) {
        validate(dto);

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
        variables.put("start_date", Timestamp.valueOf(campaign.getPeriodStart()
                .atOffset(ZoneOffset.ofHours(3)).toLocalDateTime()));
        variables.put("start_rule_date", Timestamp.valueOf(campaign.getPeriodStart()
                .minusDays(1).withHour(22).withMinute(0).withSecond(0)
                .atOffset(ZoneOffset.ofHours(3)).toLocalDateTime()));
        variables.put("wait_rule_date", Timestamp.valueOf(campaign.getPeriodStart()
                .withHour(3).withMinute(0).withSecond(0)
                .atOffset(ZoneOffset.ofHours(3)).toLocalDateTime()));
        variables.put("start_upc_date", Timestamp.valueOf(campaign.getPeriodStart()
                .withHour(10).withMinute(0).withSecond(0)
                .atOffset(ZoneOffset.ofHours(3)).toLocalDateTime()));
        variables.put("post_period_end", Timestamp.valueOf(campaign.getPostPeriodEnd()
                .plusDays(1).withHour(0).withMinute(0).withSecond(0)
                .atOffset(ZoneOffset.ofHours(3)).toLocalDateTime()));
        variables.put("check_clm_cycle", "PT6H");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(CAMPAIGN_PROCESS_DEFINITION_KEY,
                campaign.getCampaignCode(), variables);
        log.info("Run process {} for campaign {} ", pi.getId(), campaign.getCampaignCode());

        return campaign;
    }

    protected void validate(CampaignDto dto) {
        if (campaignRepository.existsByCampaignCode(dto.getCampaignCode())) {
            throw new ValidationException(List.of(
                    new ValidationItem("camp_id", dto.getCampaignCode(), CAMPAIGN_ALREADY_EXISTS)));
        }
        if (dto.getPeriodStart().isAfter(dto.getPeriodEnd())) {
            throw new ValidationException(List.of(
                    new ValidationItem("date_start", dto.getPeriodStart().toString(), WRONG_PERIOD)));
        }

        if (dto.getPeriodEnd().isAfter(dto.getPostPeriodEnd())) {
            throw new ValidationException(List.of(
                    new ValidationItem("date_postperiod", dto.getPostPeriodEnd().toString(), WRONG_POST_PERIOD)));
        }
        templateDefinitionService.validateOfferData(dto.getSegments());
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
