package process_manager.service;

import process_manager.dto.CampaignSegmentDto;
import process_manager.dto.ComarchStatusDto;
import process_manager.dto.OfferDataDto;
import process_manager.entity.Segment;
import process_manager.entity.SegmentType;
import process_manager.repository.SegmentRepository;
import org.junit.jupiter.api.Assertions;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(SegmentServiceImpl.class)
class SegmentServiceImplTest {
    private static final UUID CAMPAIGN_ID = UUID.randomUUID();
    private static final UUID SEGMENT_ID = UUID.randomUUID();
    private static final String OFFER_TEMPLATE = "TST_SAS_14";

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
        Assertions.assertEquals(SegmentType.TARGET_GROUP, targetSegment.getType());
        assertEquals("SMS", targetSegment.getChannelType());
        assertEquals(OFFER_TEMPLATE, targetSegment.getOfferTemplate());
        assertEquals(3, targetSegment.getMinSum());
        assertEquals(1, targetSegment.getPoints());
        assertEquals(2, targetSegment.getPurchases());
        assertEquals(4, targetSegment.getRewardPeriod());
        assertNull(targetSegment.getCashback());
        assertEquals(6, targetSegment.getDiscount());
        assertEquals("segment.getZeroNameCategory()", targetSegment.getZeroNameCategory());
        assertEquals("segment.getFirstNameCategory()", targetSegment.getFirstNameCategory());
        assertEquals("segment.getSecondNameCategory()", targetSegment.getSecondNameCategory());
        assertEquals(7, targetSegment.getMaxBenefit());
        assertNull(targetSegment.getPluCount());
        assertEquals("123, 321, 123", targetSegment.getPluList());
        assertEquals("slipCheck", targetSegment.getTextSlipCheck());

        Segment controlSegment = segmentCaptor.getAllValues().get(1);
        assertEquals(CAMPAIGN_ID, controlSegment.getCampaignId());
        assertEquals(SegmentType.CONTROL_GROUP, controlSegment.getType());
        assertEquals("VIBER", controlSegment.getChannelType());
        assertEquals(OFFER_TEMPLATE, controlSegment.getOfferTemplate());
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
                        .offerTemplate(OFFER_TEMPLATE)
                        .offerData(OfferDataDto.builder()
                                .points(1)
                                .purchases(2)
                                .minSum(3)
                                .rewardPeriod(4)
                                .multiplier(5)
                                .cashback(null)
                                .discount(6)
                                .zeroNameCategory("segment.getZeroNameCategory()")
                                .firstNameCategory("segment.getFirstNameCategory()")
                                .secondNameCategory("segment.getSecondNameCategory()")
                                .maxBenefit(7)
                                .pluCount(null)
                                .pluList("[123, 321, 123]")
                                .textSlipCheck("slipCheck")
                                .build())
                        .guests(List.of(1L, 2L))
                        .build(),
                CampaignSegmentDto.builder()
                        .channel("VIBER")
                        .segmentType(SegmentType.CONTROL_GROUP)
                        .offerTemplate(OFFER_TEMPLATE)
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