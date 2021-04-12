package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.dto.CampaignDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;

public interface CampaignService {

    Campaign getByCode(String campaignCode);

    Campaign create(CampaignDto dto);
}
