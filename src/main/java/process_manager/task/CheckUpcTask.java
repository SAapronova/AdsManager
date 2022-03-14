package process_manager.task;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import process_manager.dto.GuestDto;
import process_manager.dto.SegmentDto;
import com.x5.bigdata.dvcm.process.entity.*;
import process_manager.entity.*;
import process_manager.service.CampaignService;
import process_manager.service.GuestService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CheckUpcTask implements JavaDelegate {
    private static final String CHECK_PATH = "/communications/check";

    private final CampaignService campaignService;
    private final GuestService guestService;
    private final RestTemplate restTemplate;
    private final String host;
    private final Integer port;


    public CheckUpcTask (CampaignService campaignService,
                         GuestService guestService,
                         RestTemplate restTemplate,
                         @Value("${dcvm.upc.host:dcvm-upc-service}") String host,
                         @Value("${dcvm.upc.port:8080}") Integer port) {
        this.campaignService = campaignService;
        this.guestService = guestService;
        this.restTemplate = restTemplate;
        this.host = host;
        this.port = port;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String campaignCode = execution.getProcessBusinessKey();
        checkUpcStatuses(campaignCode);
    }

    public void checkUpcStatuses(String campaignCode) throws URISyntaxException {
        URI uri = new URI("http", null, host, port, CHECK_PATH, null, null);

        log.info("Init CheckUpcTask for campaign {} ", campaignCode);

        Campaign currentCampaign = campaignService.getByCode(campaignCode);

        if (CampaignStatus.FINISH.equals(currentCampaign.getStatus())) {
            return;
        }

        for (Segment segment : currentCampaign.getSegments()) {
            if (!SegmentType.CONTROL_GROUP.equals(segment.getType())) {

                List<Guest> codes = guestService.getRefreshableGuestsBySegmentId(segment.getId());

                int guestCount = codes.size();
                int i0 = 0;
                while (i0 < guestCount) {
                    int i1 = Math.min(guestCount, i0 + 5000);
                    SegmentDto dto = SegmentDto.builder()
                            .campaignCode(campaignCode)
                            .channelType(segment.getChannelType())
                            .guests(codes.subList(i0, i1).stream().map(Guest::getCode).collect(Collectors.toList()))
                            .build();

                    log.info("CheckUpcTask request: {} ", dto);
                    ResponseEntity<List<GuestDto>> rEntity = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(dto),
                            new ParameterizedTypeReference<>() {});

                    List<GuestDto> guestStatuses = rEntity.getBody();
                    log.info("CheckUpcTask response: {} ", guestStatuses.size());
                    guestService.setUpcStatus(segment.getId(), guestStatuses);
                    i0 = i1;
                }
            }
        }

        log.info("End CheckUpcTask for campaign {}", campaignCode);
    }
}
