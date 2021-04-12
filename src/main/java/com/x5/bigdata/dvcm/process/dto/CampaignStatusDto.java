package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CampaignStatusDto {
    @JsonProperty("camp_id")
    private String campaignCode;

    @JsonProperty("status")
    private CampaignStatus status;

    @JsonProperty("date_updated")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime statusDate;
}
