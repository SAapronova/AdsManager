package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.x5.bigdata.dvcm.process.entity.OfferTemplate;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@Builder
public class CampaignSegmentDto {
    @JsonProperty("segment_type")
    @NotNull
    private SegmentType segmentType;

    @JsonProperty("channel")
    @NotBlank
    private String channel;

    @JsonProperty("guest_list")
    @NotNull
    @Size(min = 1)
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
    @NotNull
    private OfferTemplate offerTemplate;

    @JsonProperty("offer_data")
    @NotNull
    @Valid
    private OfferDataDto offerData;
}
