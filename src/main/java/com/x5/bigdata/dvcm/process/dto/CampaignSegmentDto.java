package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.x5.bigdata.dvcm.process.entity.ChannelType;
import com.x5.bigdata.dvcm.process.entity.OfferTemplate;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CampaignSegmentDto {
    @JsonProperty("segment_type")
    private SegmentType segmentType;

    @JsonProperty("channel")
    private ChannelType channel;

    @JsonProperty("guest_list")
    private List<Long> guests;

    @JsonProperty("content_text")
    private String contentText;

    @JsonProperty("content_link")
    private String contentLink;

    @JsonProperty("content_link_text")
    private String contentLinkText;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("offer_template")
    private OfferTemplate offerTemplate;
}
