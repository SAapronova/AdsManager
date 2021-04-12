package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.dto.ComarchStatusDto;
import com.x5.bigdata.dvcm.process.dto.CampaignSegmentDto;

import java.util.List;
import java.util.UUID;

public interface SegmentService {

    void save(UUID campaignId, List<CampaignSegmentDto> segments);

    void setComarchStatus(UUID segmentId, ComarchStatusDto statusDto);

    void setRuleCode(UUID segmentId, String ruleCode);

    void setIsUpc(UUID segmentId);
}
