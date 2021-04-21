package com.x5.bigdata.dvcm.process.controller;

import com.x5.bigdata.dvcm.process.dto.CampaignDto;
import com.x5.bigdata.dvcm.process.dto.CampaignInfoDto;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import com.x5.bigdata.dvcm.process.task.UnfreezeTask;
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
        campaignService.create(campaignDto);
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
}
