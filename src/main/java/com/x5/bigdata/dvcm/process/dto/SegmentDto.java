package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.x5.bigdata.dvcm.process.entity.ChannelType;
import com.x5.bigdata.dvcm.process.entity.OfferTemplate;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentDto {
    @JsonProperty("camp_id")
    private String campaignCode;

    @JsonProperty("segment_type")
    private SegmentType segmentType;

    @JsonProperty("channel")
    private ChannelType channelType;

    @JsonProperty("date_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime periodStart;

    @JsonProperty("date_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime periodEnd;

    @JsonProperty("guest_list")
    List<Long> guests;

    @JsonProperty("mechanics")
    private OfferTemplate mechanics;

    @JsonProperty("mechanics_params")
    private MechanicsParamsDto mechanicsParams;

    @JsonProperty("content")
    private ContentParamsDto contentParams;

    @JsonProperty("phones")
    private List<Integer> phones;

    @JsonProperty("image_url")
    private String imageUrl;
}
