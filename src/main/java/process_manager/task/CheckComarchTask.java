package process_manager.task;

import process_manager.dto.ComarchStatusDto;
import process_manager.dto.OfferDataDto;
import process_manager.dto.SegmentDto;
import process_manager.entity.Campaign;
import process_manager.entity.CampaignStatus;
import process_manager.entity.Segment;
import process_manager.entity.SegmentType;
import process_manager.service.CampaignService;
import process_manager.service.SegmentService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Component
public class CheckComarchTask implements JavaDelegate {
    private static final String COMARCH_TASK = "/rule/check";

    private final CampaignService campaignService;
    private final SegmentService segmentService;
    private final RestTemplate restTemplate;
    private final String host;
    private final Integer port;

    public CheckComarchTask(CampaignService campaignService,
                            SegmentService segmentService,
                            RestTemplate restTemplate,
                            @Value("${dcvm.comarch.host:dcvm-comarch-service}")String host,
                            @Value("${dcvm.comarch.port:8080}")Integer port) {
        this.campaignService = campaignService;
        this.segmentService = segmentService;
        this.restTemplate = restTemplate;
        this.host = host;
        this.port = port;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        URI uri = new URI("http", null, host, port, COMARCH_TASK, null, null);
        String campaignCode = execution.getProcessBusinessKey();
        log.info("Init CheckComarchTask for campaign {} ", campaignCode);

        Campaign campaign = campaignService.getByCode(campaignCode);
        if (CampaignStatus.FINISH.equals(campaign.getStatus())) {
            return;
        }

        for (Segment segment : campaign.getSegments()) {
            if (!SegmentType.CONTROL_GROUP.equals(segment.getType())) {
                SegmentDto dto = SegmentDto.builder()
                        .campaignCode(campaignCode)
                        .periodStart(campaign.getPeriodStart())
                        .mechanics(segment.getOfferTemplate())
                        .mechanicsParams(OfferDataDto.builder()
                                .points(segment.getPoints())
                                .purchases(segment.getPurchases())
                                .minSum(segment.getMinSum())
                                .rewardPeriod(segment.getRewardPeriod())
                                .multiplier(segment.getMultiplier())
                                .zeroNameCategory(segment.getZeroNameCategory())
                                .firstNameCategory(segment.getFirstNameCategory())
                                .secondNameCategory(segment.getSecondNameCategory())
                                .discount(segment.getDiscount())
                                .cashback(segment.getCashback())
                                .maxBenefit(segment.getMaxBenefit())
                                .pluCount(segment.getPluCount())
                                .pluList(segment.getPluList())
                                .textSlipCheck(segment.getTextSlipCheck())
                                .build())
                        .build();

                log.info("CheckComarchTask request: {} ", dto);
                ComarchStatusDto statusDto = restTemplate.postForObject(uri, dto, ComarchStatusDto.class);
                log.info("CheckComarchTask response: {} ", statusDto);

                segmentService.setComarchStatus(segment.getId(), statusDto);
            }
        }
        log.info("End CheckComarchTask for campaign {} ", campaignCode);
    }
}
