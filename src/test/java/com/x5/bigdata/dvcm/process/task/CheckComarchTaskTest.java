package com.x5.bigdata.dvcm.process.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.bigdata.dvcm.process.config.AppConfig;
import com.x5.bigdata.dvcm.process.dto.ComarchStatusDto;
import com.x5.bigdata.dvcm.process.entity.*;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.GuestService;
import com.x5.bigdata.dvcm.process.service.SegmentService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest({CheckComarchTask.class, AppConfig.class})
@TestPropertySource(properties = {
        "API=http://service"
})
class CheckComarchTaskTest {
    private static final String CAMPAIGN_CODE = "A-1-1-test";
    private static final UUID TARGET_SEGMENT_ID = UUID.randomUUID();
    private static final UUID CONTROL_SEGMENT_ID = UUID.randomUUID();

    @MockBean
    private CampaignService campaignService;
    @MockBean
    private SegmentService segmentService;
    @MockBean
    private GuestService guestService;
    @MockBean
    private SpringBootProcessApplication camunda;

    @Mock
    private DelegateExecution execution;

    @Autowired
    private CheckComarchTask checkComarchTask;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void execute() throws Exception {
        mockServer
                .expect(requestTo("http://service/cvm_comarch/rule/check"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.camp_id", is(CAMPAIGN_CODE)))
                .andExpect(jsonPath("$.date_start", is("2021-01-02 12:30:00")))
                .andExpect(jsonPath("$.mechanics", is("TST_SAS_14")))
                .andExpect(jsonPath("$.mechanics_params.OFFER_POINT_COEFFICIENT", is(1)))
                .andExpect(jsonPath("$.mechanics_params.OFFER_BONUS_AMT", is(2)))
                .andExpect(jsonPath("$.mechanics_params.OFFER_CH_SUM", is(3)))
                .andExpect(jsonPath("$.mechanics_params.OFFER_CH_BALL_AMOUNT", is(4)))
                .andExpect(jsonPath("$.mechanics_params.OFFER_EXPT_DELAY", is(5)))
                .andRespond(withSuccess("{\"rule\": true, \"segment\": true}", MediaType.APPLICATION_JSON));

        when(execution.getProcessBusinessKey()).thenReturn(CAMPAIGN_CODE);
        when(campaignService.getByCode(CAMPAIGN_CODE)).thenReturn(getCampaign());
        when(guestService.getCodesBySegmentId(TARGET_SEGMENT_ID)).thenReturn(List.of(1L, 2L));
        when(guestService.getCodesBySegmentId(CONTROL_SEGMENT_ID)).thenReturn(List.of(3L, 4L));

        checkComarchTask.execute(execution);

        ArgumentCaptor<ComarchStatusDto> captor = ArgumentCaptor.forClass(ComarchStatusDto.class);
        verify(segmentService, times(1)).setComarchStatus(eq(TARGET_SEGMENT_ID), captor.capture());

        assertTrue(captor.getValue().getIsRuleOn());
        assertTrue(captor.getValue().getIsSegmentOn());
    }

    private Campaign getCampaign() {
        Campaign campaign = new Campaign()
                .setCampaignCode(CAMPAIGN_CODE)
                .setPeriodStart(LocalDateTime.of(2021, 1, 2, 12, 30))
                .setPeriodEnd(LocalDateTime.of(2021, 3, 4, 11, 20))
                .setSegments(List.of(
                        new Segment()
                                .setId(CONTROL_SEGMENT_ID)
                                .setType(SegmentType.CONTROL_GROUP)
                                .setOfferTemplate(OfferTemplate.TST_SAS_14)
                                .setMultiplier(1)
                                .setPoints(2)
                                .setMinSum(3)
                                .setPurchases(4)
                                .setRewardPeriod(5)
                                .setChannelType(ChannelType.VIBER),
                        new Segment()
                                .setId(TARGET_SEGMENT_ID)
                                .setOfferTemplate(OfferTemplate.TST_SAS_14)
                                .setMultiplier(1)
                                .setPoints(2)
                                .setMinSum(3)
                                .setPurchases(4)
                                .setRewardPeriod(5)
                                .setContentText("content text")
                                .setContentLink("content link")
                                .setContentLinkText("content link text")
                                .setImageUrl("image url")
                                .setType(SegmentType.TARGET_GROUP)
                                .setChannelType(ChannelType.SMS)
                ));

        return campaign;
    }
}