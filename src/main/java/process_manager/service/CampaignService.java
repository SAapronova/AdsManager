package process_manager.service;

import process_manager.dto.CampaignDto;
import process_manager.dto.CampaignInfoDto;
import process_manager.dto.TestCommunicationDto;
import process_manager.entity.Campaign;
import process_manager.entity.CampaignStatus;

import java.util.List;
import java.util.UUID;

public interface CampaignService {

    Campaign getByCode(String campaignCode);

    Campaign getById(UUID id);

    Campaign create(CampaignDto dto);

    void setStatus(String campaignCode, CampaignStatus status);

    List<CampaignInfoDto> findAll();

    Campaign createTestCommunication(TestCommunicationDto dto);
}
