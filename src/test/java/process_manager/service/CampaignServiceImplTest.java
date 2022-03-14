package process_manager.service;

import process_manager.dto.CampaignDto;
import process_manager.dto.TestCommunicationDto;
import process_manager.dto.TestCommunicationSegmentDto;
import process_manager.entity.Campaign;
import process_manager.entity.CampaignStatus;
import process_manager.entity.Segment;
import process_manager.entity.SegmentType;
import process_manager.exception.ValidationException;
import process_manager.exception.ValidationItem;
import process_manager.repository.CampaignRepository;
import process_manager.task.TestCommunicationSenderToUpc;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Assertions;
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
import process_manager.validators.ValidationMessages;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Thread.sleep;
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
        Assertions.assertEquals(campaign, campaignService.getByCode(CAMPAIGN_CODE));
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
    void create() throws InterruptedException {
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        when(runtimeService.startProcessInstanceByKey(eq(CampaignServiceImpl.CAMPAIGN_PROCESS_DEFINITION_KEY),
                eq(campaign.getCampaignCode()), captor.capture())).thenReturn(processInstance);

        campaign = campaignService.create(campaignDto);

        assertEquals(campaignDto.getCampaignCode(), campaign.getCampaignCode());
        assertEquals(campaignDto.getPeriodStart(), campaign.getPeriodStart());
        assertEquals(campaignDto.getPeriodEnd(), campaign.getPeriodEnd());
        assertEquals(campaignDto.getPostPeriodEnd(), campaign.getPostPeriodEnd());
        assertEquals(CampaignStatus.START, campaign.getStatus());

        sleep(2000);

        verify(segmentService, times(1)).save(campaign.getId(), campaignDto.getSegments());
        verify(runtimeService, times(1))
                .startProcessInstanceByKey(eq(CampaignServiceImpl.CAMPAIGN_PROCESS_DEFINITION_KEY), eq(campaign.getCampaignCode()), anyMap());

        Map<String, Object> variables = captor.getValue();
        assertEquals(campaignDto.getCampaignCode(), variables.get("camp_id"));
        assertEquals("2021-01-10T00:00", ((Timestamp) variables.get("start_date")).toLocalDateTime().plusHours(3).toString());
        assertEquals("2021-01-09T22:00", ((Timestamp) variables.get("start_rule_date")).toLocalDateTime().plusHours(3).toString());
        assertEquals("2021-01-10T03:00", ((Timestamp) variables.get("wait_rule_date")).toLocalDateTime().plusHours(3).toString());
        assertEquals("2021-01-10T10:00", ((Timestamp) variables.get("start_upc_date")).toLocalDateTime().plusHours(3).toString());
        assertEquals("2021-03-31T00:00", ((Timestamp) variables.get("post_period_end")).toLocalDateTime().plusHours(3).toString());
        assertEquals("PT6H", variables.get("check_clm_cycle").toString());
        assertEquals("P1D", variables.get("refresh_status_time").toString());

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
        Assertions.assertEquals(ValidationMessages.CAMPAIGN_ALREADY_EXISTS, item.getDescription());
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
        Assertions.assertEquals(ValidationMessages.WRONG_PERIOD, item.getDescription());
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
        Assertions.assertEquals(ValidationMessages.WRONG_POST_PERIOD, item.getDescription());
    }

    @Test
    void testCommunication() {
        TestCommunicationDto dto = getTestCommunicationDto();

        campaign.setCampaignCode(dto.getCampaignCode())
                .setSegments(List.of(
                        new Segment().setId(UUID.randomUUID()), new Segment().setId(UUID.randomUUID())));

        when(campaignRepository.findMaxLaunchCount(dto.getCampaignCode() + CampaignServiceImpl.CAMPAIGN_TEST_COMMUNICATION_ATTRIBUTE)).thenReturn(5);

        Campaign result = campaignService.createTestCommunication(dto);

        assertEquals(dto.getPeriodStart(), result.getPeriodStart());
        assertEquals(dto.getPeriodEnd(), result.getPeriodEnd());
        assertEquals(dto.getCampaignCode() + "-test", result.getCampaignCode());
        assertEquals(6, result.getLaunchCount());

        verify(campaignRepository, times(1)).save(result);
        verify(segmentService, times(1)).saveTestCommunicationSegment(result.getId(), dto.getSegments());
    }

    @Test
    void testCommunication_LikeFirstCampaign() {
        TestCommunicationDto dto = getTestCommunicationDto();

        campaign.setCampaignCode(dto.getCampaignCode())
                .setSegments(List.of(
                        new Segment().setId(UUID.randomUUID()), new Segment().setId(UUID.randomUUID())));

        when(campaignRepository.findMaxLaunchCount(dto.getCampaignCode() + CampaignServiceImpl.CAMPAIGN_TEST_COMMUNICATION_ATTRIBUTE)).thenReturn(null);

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