package process_manager.repository;

import process_manager.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    boolean existsByCampaignCode(String campaignCode);

    Campaign findCampaignByCampaignCode(String campaignCode);

    @Query("select max(launchCount) from Campaign where campaignCode = :campaignCode")
    Integer findMaxLaunchCount(String campaignCode);

}
