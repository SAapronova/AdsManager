package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.dto.CampaignSegmentDto;

import java.util.List;

public interface TemplateDefinitionService {
    void validateOfferData(List<CampaignSegmentDto> segments);
}
