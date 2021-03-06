package process_manager.controller;

import process_manager.dto.CampaignDto;
import process_manager.dto.CampaignInfoDto;
import process_manager.dto.TestCommunicationDto;
import process_manager.service.CampaignService;
import process_manager.task.UnfreezeTask;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/campaigns")
@Tag(name = "Campaign")
public class CampaignController {
    private final CampaignService campaignService;
    private final RestTemplate restTemplate;
    private final UnfreezeTask unfreezeTask;

    @PostMapping
    public boolean create(@RequestBody @Valid CampaignDto campaignDto) {
        log.info("New request {} ", campaignDto.getCampaignCode());
        campaignService.create(campaignDto);
        log.info("Save request {} ", campaignDto.getCampaignCode());
        return true;
    }

    @GetMapping
    public List<CampaignInfoDto> findAll() {
        return campaignService.findAll();
    }

    @PostMapping("/{campId}/unfreeze")
    public boolean unfreeze(@PathVariable(name = "campId") String campId) {
        unfreezeTask.unfreeze(campId);
        return true;
    }

    @PostMapping("/test-communications")
    public boolean testCommunication(@RequestBody @Valid TestCommunicationDto dto) {
        campaignService.createTestCommunication(dto);
        return true;
    }

}
