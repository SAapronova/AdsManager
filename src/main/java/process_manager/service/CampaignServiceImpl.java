package process_manager.service;

import process_manager.dto.CampaignDto;
import process_manager.dto.CampaignInfoDto;
import process_manager.dto.TestCommunicationDto;
import process_manager.dto.mapper.CampaignInfoDtoMapper;
import process_manager.entity.Campaign;
import process_manager.entity.CampaignStatus;
import process_manager.exception.ValidationException;
import process_manager.exception.ValidationItem;
import process_manager.repository.CampaignRepository;
import process_manager.task.TestCommunicationSenderToUpc;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static process_manager.validators.ValidationMessages.*;

@Slf4j
@Service
public class CampaignServiceImpl implements CampaignService {
    public static final String CAMPAIGN_PROCESS_DEFINITION_KEY = "CampaignProcess";
    public static final String CAMPAIGN_TEST_COMMUNICATION_ATTRIBUTE = "-test";

    private final CampaignRepository campaignRepository;
    private final SegmentService segmentService;
    private final RuntimeService runtimeService;
    private final KafkaService kafkaService;
    private final TestCommunicationSenderToUpc senderToUpc;

    public CampaignServiceImpl(CampaignRepository campaignRepository,
                               SegmentService segmentService,
                               RuntimeService runtimeService,
                               KafkaService kafkaService,
                               @Lazy TestCommunicationSenderToUpc senderToUpc) {
        this.campaignRepository = campaignRepository;
        this.segmentService = segmentService;
        this.runtimeService = runtimeService;
        this.kafkaService = kafkaService;
        this.senderToUpc = senderToUpc;
    }

    @Transactional
    public Campaign getByCode(String campaignCode) {
        return campaignRepository.findCampaignByCampaignCode(campaignCode);
    }

    @Transactional
    public Campaign getById(UUID id) {
        return campaignRepository.findById(id).orElseThrow();
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
        variables.put("start_date", Timestamp.valueOf(campaign.getPeriodStart().minusHours(3)));
        variables.put("start_rule_date", Timestamp.valueOf(campaign.getPeriodStart()
                .minusDays(1).withHour(22).withMinute(0).withSecond(0).minusHours(3)));
        variables.put("wait_rule_date", Timestamp.valueOf(campaign.getPeriodStart()
                .withHour(3).withMinute(0).withSecond(0).minusHours(3)));
        variables.put("start_upc_date", Timestamp.valueOf(campaign.getPeriodStart()
                .withHour(10).withMinute(0).withSecond(0).minusHours(3)));
        variables.put("post_period_end", Timestamp.valueOf(campaign.getPostPeriodEnd()
                .plusDays(1).withHour(0).withMinute(0).withSecond(0).minusHours(3)));
        variables.put("check_clm_cycle", "PT6H");
        variables.put("refresh_status_time", "P1D");

        CompletableFuture.runAsync(() -> {
            ProcessInstance pi = runtimeService.startProcessInstanceByKey(CAMPAIGN_PROCESS_DEFINITION_KEY,
                    dto.getCampaignCode(), variables);
            log.info("Run process {} for campaign {} ", pi.getId(), dto.getCampaignCode());
        });
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

    @Override
    @Transactional
    public List<CampaignInfoDto> findAll() {
        return campaignRepository.findAll().stream().map(CampaignInfoDtoMapper::map).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Campaign createTestCommunication(TestCommunicationDto dto) {
        Integer maxLaunchCount = campaignRepository
                .findMaxLaunchCount(dto.getCampaignCode() + CAMPAIGN_TEST_COMMUNICATION_ATTRIBUTE);

        Campaign newCampaign = new Campaign()
                .setId(UUID.randomUUID())
                .setCampaignCode(dto.getCampaignCode() + CAMPAIGN_TEST_COMMUNICATION_ATTRIBUTE)
                .setCreateTime(LocalDateTime.now())
                .setPeriodStart(dto.getPeriodStart())
                .setPeriodEnd(dto.getPeriodEnd())
                .setLaunchCount(maxLaunchCount == null ? 1 : maxLaunchCount + 1);

        campaignRepository.save(newCampaign);
        segmentService.saveTestCommunicationSegment(newCampaign.getId(), dto.getSegments());
        log.info("Save new campaign: {}", newCampaign);

        CompletableFuture.runAsync(() ->
            senderToUpc.send(newCampaign.getId())
        );

        return newCampaign;
    }
}
