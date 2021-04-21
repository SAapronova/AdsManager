package com.x5.bigdata.dvcm.process.dto;

import com.x5.bigdata.dvcm.process.entity.CampaignStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CampaignInfoDto {
    private UUID id;
    private String campaignCode;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime postPeriodEnd;
    private LocalDateTime createTime;
    private CampaignStatus status;
    private List<SegmentInfoDto> segments;
}
