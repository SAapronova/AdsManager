package com.x5.bigdata.dvcm.process.controller;

import com.x5.bigdata.dvcm.process.dto.CampaignDto;
import com.x5.bigdata.dvcm.process.service.CampaignService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/campaigns")
@Tag(name = "Campaign")
public class CampaignController {
    private final CampaignService campaignService;
    private final RestTemplate restTemplate;

    @PostMapping
    public boolean create(@RequestBody @Valid CampaignDto campaignDto) {
        campaignService.create(campaignDto);
        return true;
    }
}
