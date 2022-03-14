package process_manager.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import process_manager.config.AppConfig;
import process_manager.entity.Campaign;
import process_manager.entity.Segment;
import process_manager.entity.SegmentType;
import process_manager.service.CampaignService;
import process_manager.service.GuestService;
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
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest({FreezeTask.class, AppConfig.class})
@TestPropertySource(properties = {
        "API=http://service"
})
class FreezeTaskTest {
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
    private FreezeTask freezeTask;
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
                .expect(anything())
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.camp_id", is(CAMPAIGN_CODE)))
                .andExpect(jsonPath("$.segment_type", is("1")))
                .andExpect(jsonPath("$.date_start", is("2021-01-02 12:30:00")))
                .andExpect(jsonPath("$.date_end", is("2021-03-04 11:20:00")))
                .andExpect(jsonPath("$.guest_list", hasSize(2)))
                .andExpect(jsonPath("$.guest_list[0]", is(3)))
                .andExpect(jsonPath("$.guest_list[1]", is(4)))
                .andRespond(withSuccess("{\"3\": true, \"4\": false}", MediaType.APPLICATION_JSON));

        mockServer
                .expect(anything())
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.camp_id", is(CAMPAIGN_CODE)))
                .andExpect(jsonPath("$.segment_type", is("2")))
                .andExpect(jsonPath("$.date_start", is("2021-01-02 12:30:00")))
                .andExpect(jsonPath("$.date_end", is("2021-03-04 11:20:00")))
                .andExpect(jsonPath("$.guest_list", hasSize(2)))
                .andExpect(jsonPath("$.guest_list[0]", is(1)))
                .andExpect(jsonPath("$.guest_list[1]", is(2)))
                .andRespond(withSuccess("{\"1\": true, \"2\": false}", MediaType.APPLICATION_JSON));

        when(execution.getProcessBusinessKey()).thenReturn(CAMPAIGN_CODE);
        when(campaignService.getByCode(CAMPAIGN_CODE)).thenReturn(getCampaign());
        when(guestService.getCodesBySegmentId(TARGET_SEGMENT_ID)).thenReturn(List.of(1L, 2L));
        when(guestService.getCodesBySegmentId(CONTROL_SEGMENT_ID)).thenReturn(List.of(3L, 4L));

        freezeTask.execute(execution);

        ArgumentCaptor<Map<String, Boolean>> targetCaptor = ArgumentCaptor.forClass(Map.class);
        verify(guestService, times(1)).setFrozen(eq(TARGET_SEGMENT_ID), targetCaptor.capture());

        Map<String, Boolean> targetStatuses = targetCaptor.getValue();
        assertEquals(2, targetStatuses.size());
        assertTrue(targetStatuses.get("1"));
        assertFalse(targetStatuses.get("2"));

        ArgumentCaptor<Map<String, Boolean>> controlCaptor = ArgumentCaptor.forClass(Map.class);
        verify(guestService, times(1)).setFrozen(eq(CONTROL_SEGMENT_ID), controlCaptor.capture());

        Map<String, Boolean> controlStatuses = controlCaptor.getValue();
        assertEquals(2, controlStatuses.size());
        assertTrue(controlStatuses.get("3"));
        assertFalse(controlStatuses.get("4"));
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
                                .setChannelType("VIBER"),
                        new Segment()
                                .setId(TARGET_SEGMENT_ID)
                                .setType(SegmentType.TARGET_GROUP)
                                .setChannelType("SMS")
                ));

        return campaign;
    }
}