package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.dto.CampaignDto;
import com.x5.bigdata.dvcm.process.dto.TestCommunicationDto;
import com.x5.bigdata.dvcm.process.dto.TestCommunicationSegmentDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import com.x5.bigdata.dvcm.process.exception.ValidationException;
import com.x5.bigdata.dvcm.process.exception.ValidationItem;
import com.x5.bigdata.dvcm.process.repository.CampaignRepository;
import com.x5.bigdata.dvcm.process.task.TestCommunicationSenderToUpc;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.x5.bigdata.dvcm.process.service.CampaignServiceImpl.CAMPAIGN_PROCESS_DEFINITION_KEY;
import static com.x5.bigdata.dvcm.process.service.CampaignServiceImpl.CAMPAIGN_TEST_COMMUNICATION_ATTRIBUTE;
import static com.x5.bigdata.dvcm.process.validators.ValidationMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(CampaignServiceImpl.class)
class CampaignServiceImplTest {
    private static final String CAMPAIGN_CODE = "T-1-1-test";

    @Autowired
    private CampaignServiceImpl campaignService;

    @MockBean
    private SegmentService segmentService;
    @MockBean
    private CampaignRepository campaignRepository;
    @MockBean
    private RuntimeService runtimeService;
    @MockBean
    private TemplateDefinitionService templateDefinitionService;
    @MockBean
    private KafkaService kafkaService;
    @MockBean
    private TestCommunicationSenderToUpc senderToUpc;

    @Mock
    ProcessInstance processInstance;

    private Campaign campaign;
    private CampaignDto campaignDto;

    @BeforeEach
    public void setUp() {
        campaign = new Campaign()
                .setId(UUID.randomUUID())
                .setCampaignCode(CAMPAIGN_CODE);
        campaignDto = CampaignDto.builder()
                .campaignCode(CAMPAIGN_CODE)
                .periodStart(LocalDateTime.of(2021, 1, 10, 0, 0))
                .periodEnd(LocalDateTime.of(2021, 2, 20, 0, 0))
                .postPeriodEnd(LocalDateTime.of(2021, 3, 30, 0, 0))
                .build();
        when(campaignRepository.findCampaignByCampaignCode(CAMPAIGN_CODE)).thenReturn(campaign);
        when(campaignRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    void getByCode() {
        assertEquals(campaign, campaignService.getByCode(CAMPAIGN_CODE));
    }

    @Test
    void setStatus() {
        campaign.setStatus(CampaignStatus.CLM);

        campaignService.setStatus(CAMPAIGN_CODE, CampaignStatus.UPC);

        assertEquals(CampaignStatus.UPC, campaign.getStatus());
        verify(kafkaService, times(1)).sendProcessStatus(CAMPAIGN_CODE, CampaignStatus.UPC);
    }

    @Test
    void setStatus_NotChanged() {
        campaign.setStatus(CampaignStatus.CLM);

        campaignService.setStatus(CAMPAIGN_CODE, CampaignStatus.CLM);

        verify(kafkaService, times(0)).sendProcessStatus(CAMPAIGN_CODE, CampaignStatus.CLM);
    }

    @Test
    void create() {
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        when(runtimeService.startProcessInstanceByKey(eq(CAMPAIGN_PROCESS_DEFINITION_KEY),
                eq(campaign.getCampaignCode()), captor.capture())).thenReturn(processInstance);

        Campaign campaign = campaignService.create(campaignDto);

        assertEquals(campaignDto.getCampaignCode(), campaign.getCampaignCode());
        assertEquals(campaignDto.getPeriodStart(), campaign.getPeriodStart());
        assertEquals(campaignDto.getPeriodEnd(), campaign.getPeriodEnd());
        assertEquals(campaignDto.getPostPeriodEnd(), campaign.getPostPeriodEnd());
        assertEquals(CampaignStatus.START, campaign.getStatus());

        verify(segmentService, times(1)).save(campaign.getId(), campaignDto.getSegments());
        verify(runtimeService, times(1))
                .startProcessInstanceByKey(eq(CAMPAIGN_PROCESS_DEFINITION_KEY), eq(campaign.getCampaignCode()), anyMap());
        Map<String, Object> variables = captor.getValue();
        assertEquals(campaignDto.getCampaignCode(), variables.get("camp_id"));
        assertEquals("2021-01-10T00:00", ((Timestamp) variables.get("start_date")).toLocalDateTime().plusHours(3).toString());
        assertEquals("2021-01-09T22:00", ((Timestamp) variables.get("start_rule_date")).toLocalDateTime().plusHours(3).toString());
        assertEquals("2021-01-10T03:00", ((Timestamp) variables.get("wait_rule_date")).toLocalDateTime().plusHours(3).toString());
        assertEquals("2021-01-10T10:00", ((Timestamp) variables.get("start_upc_date")).toLocalDateTime().plusHours(3).toString());
        assertEquals("2021-03-31T00:00", ((Timestamp) variables.get("post_period_end")).toLocalDateTime().plusHours(3).toString());
        assertEquals("PT6H", variables.get("check_clm_cycle").toString());
    }

    @Test
    void validate() {
        when(campaignRepository.existsByCampaignCode(CAMPAIGN_CODE)).thenReturn(false);

        campaignService.validate(campaignDto);
    }

    @Test
    void validate_CampaignAlreadyExists() {
        when(campaignRepository.existsByCampaignCode(CAMPAIGN_CODE)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> campaignService.validate(campaignDto));

        assertEquals(1, exception.getItems().size());
        ValidationItem item = exception.getItems().get(0);
        assertEquals("camp_id", item.getName());
        assertEquals(CAMPAIGN_CODE, item.getValue());
        assertEquals(CAMPAIGN_ALREADY_EXISTS, item.getDescription());
    }

    @Test
    void validate_StartDateGreeterEnd() {
        when(campaignRepository.existsByCampaignCode(CAMPAIGN_CODE)).thenReturn(false);
        campaignDto.setPeriodStart(campaignDto.getPeriodEnd().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> campaignService.validate(campaignDto));

        assertEquals(1, exception.getItems().size());
        ValidationItem item = exception.getItems().get(0);
        assertEquals("date_start", item.getName());
        assertEquals(campaignDto.getPeriodStart().toString(), item.getValue());
        assertEquals(WRONG_PERIOD, item.getDescription());
    }

    @Test
    void validate_EndDateGreeterPostEnd() {
        when(campaignRepository.existsByCampaignCode(CAMPAIGN_CODE)).thenReturn(false);
        campaignDto.setPeriodEnd(campaignDto.getPostPeriodEnd().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> campaignService.validate(campaignDto));

        assertEquals(1, exception.getItems().size());
        ValidationItem item = exception.getItems().get(0);
        assertEquals("date_postperiod", item.getName());
        assertEquals(campaignDto.getPostPeriodEnd().toString(), item.getValue());
        assertEquals(WRONG_POST_PERIOD, item.getDescription());
    }

    @Test
    public void testCommunication() {
        TestCommunicationDto dto = getTestCommunicationDto();

        campaign.setCampaignCode(dto.getCampaignCode())
                .setSegments(List.of(
                        new Segment().setId(UUID.randomUUID()), new Segment().setId(UUID.randomUUID())));

        when(campaignRepository.findMaxLaunchCount(dto.getCampaignCode() + CAMPAIGN_TEST_COMMUNICATION_ATTRIBUTE)).thenReturn(5);

        Campaign result = campaignService.createTestCommunication(dto);

        assertEquals(dto.getPeriodStart(), result.getPeriodStart());
        assertEquals(dto.getPeriodEnd(), result.getPeriodEnd());
        assertEquals(dto.getCampaignCode() + "-test", result.getCampaignCode());
        assertEquals(6, result.getLaunchCount());

        verify(campaignRepository, times(1)).save(result);
        verify(segmentService, times(1)).saveTestCommunicationSegment(result.getId(), dto.getSegments());
    }

    @Test
    public void testCommunication_LikeFirstCampaign() {
        TestCommunicationDto dto = getTestCommunicationDto();

        campaign.setCampaignCode(dto.getCampaignCode())
                .setSegments(List.of(
                        new Segment().setId(UUID.randomUUID()), new Segment().setId(UUID.randomUUID())));

        when(campaignRepository.findMaxLaunchCount(dto.getCampaignCode() + CAMPAIGN_TEST_COMMUNICATION_ATTRIBUTE)).thenReturn(null);

        Campaign result = campaignService.createTestCommunication(dto);

        assertEquals(dto.getPeriodStart(), result.getPeriodStart());
        assertEquals(dto.getPeriodEnd(), result.getPeriodEnd());
        assertEquals(dto.getCampaignCode() + "-test", result.getCampaignCode());
        assertEquals(1, result.getLaunchCount());

        verify(campaignRepository, times(1)).save(result);
        verify(segmentService, times(1)).saveTestCommunicationSegment(result.getId(), dto.getSegments());
    }

    private TestCommunicationDto getTestCommunicationDto() {
        return TestCommunicationDto
                .builder()
                .campaignCode("123")
                .periodStart(LocalDateTime.MIN)
                .periodEnd(LocalDateTime.MAX)
                .segments(
                        List.of(
                                TestCommunicationSegmentDto
                                        .builder()
                                        .segmentType(SegmentType.TEST_COMMUNICATION)
                                        .build()))
                .build();
    }
}