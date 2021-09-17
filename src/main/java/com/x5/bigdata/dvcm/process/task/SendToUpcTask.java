package com.x5.bigdata.dvcm.process.task;

import com.x5.bigdata.dvcm.process.dto.ContentParamsDto;
import com.x5.bigdata.dvcm.process.dto.OfferDataDto;
import com.x5.bigdata.dvcm.process.dto.SegmentDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.service.GuestService;
import com.x5.bigdata.dvcm.process.service.SegmentService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class SendToUpcTask implements JavaDelegate, TestCommunicationSenderToUpc {
    private static final String SEND_PATH = "/communications/";

    private final CampaignService campaignService;
    private final SegmentService segmentService;
    private final GuestService guestService;
    private final RestTemplate restTemplate;
    private final URI uri;


    public SendToUpcTask (CampaignService campaignService,
                         GuestService guestService,
                         RestTemplate restTemplate,
                         SegmentService segmentService,
                         @Value("${dcvm.upc.host:dcvm-upc-service}") String host,
                         @Value("${dcvm.upc.port:8080}") Integer port) throws URISyntaxException {
        this.campaignService = campaignService;
        this.guestService = guestService;
        this.restTemplate = restTemplate;
        this.segmentService = segmentService;
        uri = new URI("http", null, host, port, SEND_PATH, null, null);
    }
    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String campaignCode = execution.getProcessBusinessKey();
        log.info("Init SendToUpcTask for campaign {} ", campaignCode);

        Campaign campaign = campaignService.getByCode(campaignCode);
        if (CampaignStatus.FINISH.equals(campaign.getStatus())) {
            return;
        }

        for (Segment segment : campaign.getSegments()) {
            if (!SegmentType.CONTROL_GROUP.equals(segment.getType())) {
                List<Long> codes = guestService.getFrozenCodesBySegmentId(segment.getId());

                if (!codes.isEmpty()) {
                    SegmentDto dto = SegmentDto.builder()
                            .campaignCode(campaignCode)
                            .periodStart(campaign.getPeriodStart())
                            .periodEnd(campaign.getPeriodEnd())
                            .segmentType(segment.getType())
                            .channelType(segment.getChannelType())
                            .mechanics(segment.getOfferTemplate())
                            .mechanicsParams(OfferDataDto.builder()
                                    .points(segment.getPoints())
                                    .purchases(segment.getPurchases())
                                    .minSum(segment.getMinSum())
                                    .rewardPeriod(segment.getRewardPeriod())
                                    .multiplier(segment.getMultiplier())
                                    .cashback(segment.getCashback())
                                    .discount(segment.getDiscount())
                                    .zeroNameCategory(segment.getZeroNameCategory())
                                    .firstNameCategory(segment.getFirstNameCategory())
                                    .secondNameCategory(segment.getSecondNameCategory())
                                    .maxBenefit(segment.getMaxBenefit())
                                    .pluCount(segment.getPluCount())
                                    .pluList(segment.getPluList())
                                    .textSlipCheck(segment.getTextSlipCheck())
                                    .build())
                            .contentParams(ContentParamsDto.builder()
                                    .contentText(segment.getContentText())
                                    .contentLinkText(segment.getContentLinkText())
                                    .contentLink(segment.getContentLink())
                                    .contentTemplate(segment.getContentText())
                                    .imageUrl(segment.getImageUrl())
                                    .build())
                            .phones(segment.getTestPhones())
                            .guests(codes)
                            .build();

                    log.info("SendToUpcTask request: {} ", dto);
                    restTemplate.postForObject(uri, dto, String.class);
                    segmentService.setIsUpc(segment.getId());
                }
            }
        }
        campaignService.setStatus(campaignCode, CampaignStatus.UPC);
        log.info("End SendToUpcTask for campaign {} ", campaignCode);
    }

    @Override
    public void send(UUID id) {
        Campaign campaign = campaignService.getById(id);

        for (Segment segment : campaign.getSegments()) {
            List<Long> codes = guestService.getCodesBySegmentId(segment.getId());
            if (!codes.isEmpty()) {
                SegmentDto dto = SegmentDto.builder()
                        .campaignCode(campaign.getCampaignCode() + campaign.getLaunchCount())
                        .periodStart(campaign.getPeriodStart())
                        .periodEnd(campaign.getPeriodEnd())
                        .segmentType(segment.getType())
                        .channelType(segment.getChannelType())
                        .contentParams(ContentParamsDto.builder()
                                .contentText(segment.getContentText())
                                .contentLinkText(segment.getContentLinkText())
                                .contentLink(segment.getContentLink())
                                .contentTemplate(segment.getContentText())
                                .imageUrl(segment.getImageUrl())
                                .build())
                        .phones(segment.getTestPhones())
                        .guests(codes)
                        .build();

                log.info("TestCommunicationSenderToUpc request: {} ", dto);
                restTemplate.postForObject(uri, dto, String.class);
            }
        }
        log.info("End TestCommunicationSenderToUpc for campaign {} ", campaign.getCampaignCode());
    }
}
