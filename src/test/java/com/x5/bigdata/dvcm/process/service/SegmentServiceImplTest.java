package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.dto.CampaignSegmentDto;
import com.x5.bigdata.dvcm.process.dto.ComarchStatusDto;
import com.x5.bigdata.dvcm.process.dto.OfferDataDto;
import com.x5.bigdata.dvcm.process.entity.OfferTemplate;
import com.x5.bigdata.dvcm.process.entity.Segment;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import com.x5.bigdata.dvcm.process.repository.SegmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(SegmentServiceImpl.class)
class SegmentServiceImplTest {
    private static final UUID CAMPAIGN_ID = UUID.randomUUID();
    private static final UUID SEGMENT_ID = UUID.randomUUID();

    @Autowired
    private SegmentService segmentService;

    @MockBean
    private SegmentRepository segmentRepository;

    @MockBean
    private GuestService guestService;

    private Segment segment;

    @BeforeEach
    public void setUp() {
        segment = new Segment().setId(SEGMENT_ID).setCampaignId(CAMPAIGN_ID);

        when(segmentRepository.getOne(SEGMENT_ID)).thenReturn(segment);
    }

    @Test
    void setIsUpc() {
        segmentService.setIsUpc(SEGMENT_ID);

        verify(segmentRepository, times(1)).save(segment);
        assertTrue(segment.getIsUpc());
    }

    @Test
    void setRuleCode() {
        String ruleCode = "123";
        segmentService.setRuleCode(SEGMENT_ID, ruleCode);

        verify(segmentRepository, times(1)).save(segment);
        assertEquals(ruleCode, segment.getRuleCode());
    }

    @Test
    void setComarchStatus() {
        segment.setIsSegmentOn(false);
        segment.setIsRuleOn(null);

        ComarchStatusDto statusDto = new ComarchStatusDto();
        statusDto.setIsSegmentOn(true);
        statusDto.setIsRuleOn(true);

        segmentService.setComarchStatus(SEGMENT_ID, statusDto);

        verify(segmentRepository, times(1)).save(segment);
        assertTrue(segment.getIsSegmentOn());
        assertTrue(segment.getIsRuleOn());
    }

    @Test
    void save() {

        segmentService.save(CAMPAIGN_ID, getSegmentDtoList());

        ArgumentCaptor<Segment> segmentCaptor = ArgumentCaptor.forClass(Segment.class);
        verify(segmentRepository, times(2)).save(segmentCaptor.capture());

        Segment targetSegment = segmentCaptor.getAllValues().get(0);
        assertEquals(CAMPAIGN_ID, targetSegment.getCampaignId());
        assertEquals(SegmentType.TARGET_GROUP, targetSegment.getType());
        assertEquals("SMS", targetSegment.getChannelType());
        assertEquals(OfferTemplate.TST_SAS_14, targetSegment.getOfferTemplate());
        assertEquals(1, targetSegment.getMinSum());
        assertEquals(2, targetSegment.getPoints());
        assertEquals(3, targetSegment.getPurchases());
        assertEquals(4, targetSegment.getRewardPeriod());

        Segment controlSegment = segmentCaptor.getAllValues().get(1);
        assertEquals(CAMPAIGN_ID, controlSegment.getCampaignId());
        assertEquals(SegmentType.CONTROL_GROUP, controlSegment.getType());
        assertEquals("VIBER", controlSegment.getChannelType());
        assertEquals(OfferTemplate.TST_SAS_14, controlSegment.getOfferTemplate());
        assertEquals(5, controlSegment.getMinSum());
        assertEquals(6, controlSegment.getPoints());
        assertEquals(7, controlSegment.getPurchases());
        assertEquals(8, controlSegment.getRewardPeriod());

        ArgumentCaptor<List<Long>> guestCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(guestService, times(2)).save(idCaptor.capture(), guestCaptor.capture());

        assertEquals(targetSegment.getId(), idCaptor.getAllValues().get(0));
        assertEquals(controlSegment.getId(), idCaptor.getAllValues().get(1));

        assertEquals(List.of(1L, 2L), guestCaptor.getAllValues().get(0));
        assertEquals(List.of(3L, 4L), guestCaptor.getAllValues().get(1));

    }

    private List<CampaignSegmentDto> getSegmentDtoList() {
        return List.of(
                CampaignSegmentDto.builder()
                        .channel("SMS")
                        .segmentType(SegmentType.TARGET_GROUP)
                        .offerTemplate(OfferTemplate.TST_SAS_14)
                        .offerData(OfferDataDto.builder()
                                .minSum(1)
                                .points(2)
                                .purchases(3)
                                .rewardPeriod(4)
                                .build())
                        .guests(List.of(1L, 2L))
                        .build(),
                CampaignSegmentDto.builder()
                        .channel("VIBER")
                        .segmentType(SegmentType.CONTROL_GROUP)
                        .offerTemplate(OfferTemplate.TST_SAS_14)
                        .offerData(OfferDataDto.builder()
                                .minSum(5)
                                .points(6)
                                .purchases(7)
                                .rewardPeriod(8)
                                .build())
                        .guests(List.of(3L, 4L))
                        .build());
    }
}