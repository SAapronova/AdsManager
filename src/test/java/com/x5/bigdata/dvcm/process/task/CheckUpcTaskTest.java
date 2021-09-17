package com.x5.bigdata.dvcm.process.task;

import com.x5.bigdata.dvcm.process.config.AppConfig;
import com.x5.bigdata.dvcm.process.dto.GuestDto;
import com.x5.bigdata.dvcm.process.entity.*;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.GuestService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
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

    private static final Long GUEST_1_CODE = 3L;
    private static final Long GUEST_2_CODE = 4L;
    private static final Long GUEST_3_CODE = 15L;


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
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Captor
    private ArgumentCaptor<List<GuestDto>> captor;

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
                .andExpect(jsonPath("$.channel", is("SMS")))
                .andExpect(jsonPath("$.guest_list", hasSize(2)))
                .andExpect(jsonPath("$.guest_list[0]", is(GUEST_1_CODE.intValue())))
                .andExpect(jsonPath("$.guest_list[1]", is(GUEST_2_CODE.intValue())))
                .andRespond(withSuccess("[{\"guest_id\": 3, \"communication_status\":\"PENDING\"}, { \"guest_id\":4, \"communication_status\":\"ERROR\"}]", MediaType.APPLICATION_JSON));

        when(execution.getProcessBusinessKey()).thenReturn(CAMPAIGN_CODE);
        when(campaignService.getByCode(CAMPAIGN_CODE)).thenReturn(getCampaign());
        when(guestService.getFrozenCodesBySegmentId(TARGET_SEGMENT_ID)).thenReturn(List.of(1L, 2L));
        when(guestService.getFrozenCodesBySegmentId(CONTROL_SEGMENT_ID)).thenReturn(List.of(3L, 4L));

        Guest guest1 = generateGuest(GUEST_1_CODE, TARGET_SEGMENT_ID);
        Guest guest2 = generateGuest(GUEST_2_CODE, TARGET_SEGMENT_ID);
        Guest guest3 = generateGuest(GUEST_3_CODE, CONTROL_SEGMENT_ID);

        when(guestService.getRefreshableGuestsBySegmentId(TARGET_SEGMENT_ID)).thenReturn(List.of(guest1, guest2));
        when(guestService.getRefreshableGuestsBySegmentId(CONTROL_SEGMENT_ID)).thenReturn(List.of(guest3));

        checkUpcTask.execute(execution);

        verify(guestService, times(1)).setUpcStatus(eq(TARGET_SEGMENT_ID), captor.capture());

        List<GuestDto> statuses = captor.getValue();
        assertEquals(2, statuses.size());
        assertTrue(statuses.stream().anyMatch(guest -> guest.getCode().equals(GUEST_1_CODE)));
        assertTrue(statuses.stream().anyMatch(guest -> guest.getCode().equals(GUEST_2_CODE)));
        assertTrue(statuses.stream().anyMatch(guest -> guest.getCommunicationStatus()
                .equals(GuestCommunicationStatus.PENDING)));
        assertTrue(statuses.stream().anyMatch(guest -> guest.getCommunicationStatus()
                .equals(GuestCommunicationStatus.ERROR)));

        assertFalse(statuses.stream().anyMatch(guest -> guest.getCode().equals(GUEST_3_CODE)));

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

    private Guest generateGuest(Long code, UUID segmentId) {
        Guest guest = new Guest();
        guest.setCode(code);
        guest.setId(code);
        guest.setSegmentId(segmentId);

        return guest;
    }
}