package com.x5.bigdata.dvcm.process.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.bigdata.dvcm.process.config.AppConfig;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.GuestService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest({CheckUpcTask.class, AppConfig.class})
@TestPropertySource(properties = {
        "API=http://service"
})
class CheckUpcTaskTest {
    private static final String CAMPAIGN_CODE = "A-1-1-test";
    private static final UUID TARGET_SEGMENT_ID = UUID.randomUUID();
    private static final UUID CONTROL_SEGMENT_ID = UUID.randomUUID();

    @MockBean
    private CampaignService campaignService;
    @MockBean
    private GuestService guestService;
    @MockBean
    private SpringBootProcessApplication camunda;

    @Mock
    private DelegateExecution execution;

    @Autowired
    private CheckUpcTask checkUpcTask;
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
                .expect(requestTo("http://service/cvm_upc/communications/check"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.camp_id", is(CAMPAIGN_CODE)))
                .andExpect(jsonPath("$.channel", is("SMS")))
                .andExpect(jsonPath("$.guest_list", hasSize(2)))
                .andExpect(jsonPath("$.guest_list[0]", is(1)))
                .andExpect(jsonPath("$.guest_list[1]", is(2)))
                .andRespond(withSuccess("{\"1\": \"ok\"}", MediaType.APPLICATION_JSON));

        when(execution.getProcessBusinessKey()).thenReturn(CAMPAIGN_CODE);
        when(campaignService.getByCode(CAMPAIGN_CODE)).thenReturn(getCampaign());
        when(guestService.getCodesBySegmentId(TARGET_SEGMENT_ID)).thenReturn(List.of(1L, 2L));
        when(guestService.getCodesBySegmentId(CONTROL_SEGMENT_ID)).thenReturn(List.of(3L, 4L));

        checkUpcTask.execute(execution);

        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(guestService, times(1)).setUpcStatus(eq(TARGET_SEGMENT_ID), captor.capture());

        Map<String, String> statuses = captor.getValue();
        assertEquals(1, statuses.size());
        assertEquals("ok", statuses.get("1"));
    }

    private Campaign getCampaign() {
        Campaign campaign = new Campaign()
                .setCampaignCode(CAMPAIGN_CODE)
                .setSegments(List.of(
                        new Segment()
                                .setId(CONTROL_SEGMENT_ID)
                                .setType(SegmentType.CONTROL_GROUP)
                                .setChannelType("VIBER"),
                        new Segment()
                                .setId(TARGET_SEGMENT_ID)
                                .setType(SegmentType.TARGET_GROUP)
                                .setChannelType("SMS")
                ));

        return campaign;
    }

}