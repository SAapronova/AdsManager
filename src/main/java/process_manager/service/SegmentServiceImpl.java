package process_manager.service;

import process_manager.dto.CampaignSegmentDto;
import process_manager.dto.ComarchStatusDto;
import process_manager.dto.TestCommunicationSegmentDto;
import process_manager.entity.Segment;
import process_manager.repository.SegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SegmentServiceImpl implements SegmentService {
    private final SegmentRepository segmentRepository;
    private final GuestService guestService;

    @Override
    public void save(UUID campaignId, List<CampaignSegmentDto> segments) {
        segments.forEach(segmentDto -> {
            Segment segment = new Segment()
                    .setId(UUID.randomUUID())
                    .setCampaignId(campaignId)
                    .setType(segmentDto.getSegmentType())
                    .setChannelType(segmentDto.getChannel())
                    .setContentText(segmentDto.getContentText())
                    .setContentLink(segmentDto.getContentLink())
                    .setContentLinkText(segmentDto.getContentLinkText())
                    .setImageUrl(segmentDto.getImageUrl())
                    .setOfferTemplate(segmentDto.getOfferTemplate())
                    .setPurchases(segmentDto.getOfferData().getPurchases())
                    .setCashback(segmentDto.getOfferData().getCashback())
                    .setDiscount(segmentDto.getOfferData().getDiscount())
                    .setMultiplier(segmentDto.getOfferData().getMultiplier())
                    .setMinSum(segmentDto.getOfferData().getMinSum())
                    .setRewardPeriod(segmentDto.getOfferData().getRewardPeriod())
                    .setPoints(segmentDto.getOfferData().getPoints())
                    .setFirstNameCategory(segmentDto.getOfferData().getFirstNameCategory())
                    .setSecondNameCategory(segmentDto.getOfferData().getSecondNameCategory())
                    .setZeroNameCategory(segmentDto.getOfferData().getZeroNameCategory())
                    .setMaxBenefit(segmentDto.getOfferData().getMaxBenefit())
                    .setPluCount(segmentDto.getOfferData().getPluCount())
                    .setTextSlipCheck(segmentDto.getOfferData().getTextSlipCheck());
            Optional.ofNullable(segmentDto.getOfferData().getPluList()).ifPresent(pluList -> segment.setPluList(pluList.substring(1, pluList.length()-1)));

            segmentRepository.save(segment);
            guestService.save(segment.getId(), segmentDto.getGuests());
        });
    }

    @Override
    @Transactional
    public void setComarchStatus(UUID segmentId, ComarchStatusDto statusDto) {
        Segment segment = segmentRepository.getOne(segmentId);
        segment.setIsSegmentOn(statusDto.getIsSegmentOn());
        segment.setIsRuleOn(statusDto.getIsRuleOn());
        segmentRepository.save(segment);
    }

    @Override
    @Transactional
    public void setRuleCode(UUID segmentId, String ruleCode) {
        Segment segment = segmentRepository.getOne(segmentId);
        segment.setRuleCode(ruleCode);
        segmentRepository.save(segment);
    }

    @Override
    public void setIsUpc(UUID segmentId) {
        Segment segment = segmentRepository.getOne(segmentId);
        segment.setIsUpc(true);
        segmentRepository.save(segment);
    }

    @Override
    public void saveTestCommunicationSegment(UUID campaignId, List<TestCommunicationSegmentDto> segments) {
        segments.forEach(segmentDto -> {
            Segment segment = new Segment()
                    .setId(UUID.randomUUID())
                    .setCampaignId(campaignId)
                    .setType(segmentDto.getSegmentType())
                    .setChannelType(segmentDto.getChannel())
                    .setContentText(segmentDto.getContentText())
                    .setContentLink(segmentDto.getContentLink())
                    .setContentLinkText(segmentDto.getContentLinkText())
                    .setImageUrl(segmentDto.getImageUrl());

            segmentRepository.save(segment);
            guestService.save(segment.getId(), segmentDto.getGuests());
        });
    }
}
