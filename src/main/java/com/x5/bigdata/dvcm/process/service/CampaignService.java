package com.x5.bigdata.dvcm.process.service;

import com.x5.bigdata.dvcm.process.dto.CampaignDto;
import com.x5.bigdata.dvcm.process.dto.CampaignInfoDto;
import com.x5.bigdata.dvcm.process.entity.Campaign;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;

import java.util.List;

public interface CampaignService {

    Campaign getByCode(String campaignCode);

    Campaign create(CampaignDto dto);

    void setStatus(String campaignCode, CampaignStatus status);

    List<CampaignInfoDto> findAll();
}
