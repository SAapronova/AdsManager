package com.x5.bigdata.dvcm.process.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.bigdata.dvcm.process.config.AppConfig;
import com.x5.bigdata.dvcm.process.entity.*;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.GuestService;
import com.x5.bigdata.dvcm.process.service.SegmentService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest({SendToUpcTask.class, AppConfig.class})
@TestPropertySource(properties = {
        "API=http://service"
})
class SendToUpcTaskTest {
    private static final String CAMPAIGN_CODE = "A-1-1-test";
    private static final UUID TARGET_SEGMENT_ID = UUID.randomUUID();
    private static final UUID CONTROL_SEGMENT_ID = UUID.randomUUID();
    private static final String OFFER_TEMPLATE = "TST_SAS_14";

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
    private SendToUpcTask sendToUpcTask;
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
                .expect(requestTo("http://service/cvm_upc/communications/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.camp_id", is(CAMPAIGN_CODE)))
                .andExpect(jsonPath("$.segment_type", is("2")))
                .andExpect(jsonPath("$.channel", is("SMS")))
                .andExpect(jsonPath("$.date_start", is("2021-01-02 12:30:00")))
                .andExpect(jsonPath("$.date_end", is("2021-03-04 11:20:00")))
                .andExpect(jsonPath("$.mechanics", is("TST_SAS_14")))
                .andExpect(jsonPath("$.mechanics_params.multiplier", is(1)))
                .andExpect(jsonPath("$.mechanics_params.points", is(2)))
                .andExpect(jsonPath("$.mechanics_params.min_sum", is(3)))
                .andExpect(jsonPath("$.mechanics_params.purchases_num", is(4)))
                .andExpect(jsonPath("$.mechanics_params.rewards_period", is(5)))
                .andExpect(jsonPath("$.mechanics_params.plu_list", is("[123123, 321321, 555]")))
                .andExpect(jsonPath("$.mechanics_params.first_name_category", is("first")))
                .andExpect(jsonPath("$.content.communication_link", is("content link")))
                .andExpect(jsonPath("$.content.communication_template_title", is("content link text")))
                .andExpect(jsonPath("$.content.communication_template_text", is("content text")))
                .andExpect(jsonPath("$.content.communication_template", is("content text")))
                .andExpect(jsonPath("$.content.communication_image", is("image url")))
                .andExpect(jsonPath("$.guest_list", hasSize(2)))
                .andExpect(jsonPath("$.guest_list[0]", is(1)))
                .andExpect(jsonPath("$.guest_list[1]", is(2)))
                .andRespond(withSuccess("ok", MediaType.APPLICATION_JSON));

        when(execution.getProcessBusinessKey()).thenReturn(CAMPAIGN_CODE);
        when(campaignService.getByCode(CAMPAIGN_CODE)).thenReturn(getCampaign());
        when(guestService.getCodesBySegmentId(TARGET_SEGMENT_ID)).thenReturn(List.of(1L, 2L));
        when(guestService.getCodesBySegmentId(CONTROL_SEGMENT_ID)).thenReturn(List.of(3L, 4L));
        when(guestService.getFrozenCodesBySegmentId(TARGET_SEGMENT_ID)).thenReturn(List.of(1L, 2L));
        when(guestService.getFrozenCodesBySegmentId(CONTROL_SEGMENT_ID)).thenReturn(List.of(3L, 4L));

        sendToUpcTask.execute(execution);

        verify(segmentService, times(1)).setIsUpc(TARGET_SEGMENT_ID);
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
                                .setOfferTemplate(OFFER_TEMPLATE)
                                .setMultiplier(1)
                                .setPoints(2)
                                .setMinSum(3)
                                .setPurchases(4)
                                .setRewardPeriod(5)
                                .setPluList("[123123, 321321, 555]")
                                .setFirstNameCategory("first")
                                .setChannelType("VIBER"),
                        new Segment()
                                .setId(TARGET_SEGMENT_ID)
                                .setOfferTemplate(OFFER_TEMPLATE)
                                .setMultiplier(1)
                                .setPoints(2)
                                .setMinSum(3)
                                .setPurchases(4)
                                .setRewardPeriod(5)
                                .setPluList("[123123, 321321, 555]")
                                .setFirstNameCategory("first")
                                .setContentText("content text")
                                .setContentLink("content link")
                                .setContentLinkText("content link text")
                                .setImageUrl("image url")
                                .setType(SegmentType.TARGET_GROUP)
                                .setChannelType("SMS")
                ));

        return campaign;
    }
}