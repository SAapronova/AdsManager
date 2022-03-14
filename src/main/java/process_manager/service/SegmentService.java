package process_manager.service;

import process_manager.dto.ComarchStatusDto;
import process_manager.dto.CampaignSegmentDto;
import process_manager.dto.TestCommunicationSegmentDto;

import java.util.List;
import java.util.UUID;

public interface SegmentService {

    void save(UUID campaignId, List<CampaignSegmentDto> segments);

    void setComarchStatus(UUID segmentId, ComarchStatusDto statusDto);

    void setRuleCode(UUID segmentId, String ruleCode);

    void setIsUpc(UUID segmentId);

    void saveTestCommunicationSegment(UUID campaignId, List<TestCommunicationSegmentDto> segments);
}
