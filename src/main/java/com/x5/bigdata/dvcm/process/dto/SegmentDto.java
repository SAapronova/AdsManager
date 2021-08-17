package com.x5.bigdata.dvcm.process.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.x5.bigdata.dvcm.process.entity.SegmentType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentDto {
    @JsonProperty("camp_id")
    private String campaignCode;

    @JsonProperty("segment_type")
    private SegmentType segmentType;

    @JsonProperty("channel")
    private String channelType;

    @JsonProperty("date_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime periodStart;

    @JsonProperty("date_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime periodEnd;

    @JsonProperty("guest_list")
    List<Long> guests;

    @JsonProperty("mechanics")
    private String mechanics;

    @JsonProperty("mechanics_params")
    private OfferDataDto mechanicsParams;

    @JsonProperty("content")
    private ContentParamsDto contentParams;

    @JsonProperty("phones")
    private List<Integer> phones;

    @JsonProperty("image_url")
    private String imageUrl;

    @Override
    public String toString() {
        return format("{" +
                        "camp_id: %s," +
                        "segment_type: %s," +
                        "channel: %s," +
                        "date_start: %s," +
                        "date_end: %s," +
                        "guest_count: %s," +
                        "mechanics: %s," +
                        "mechanics_params: %s," +
                        "content: %s," +
                        "phones: %s," +
                        "image_url: %s" +
                        "}",
                campaignCode, segmentType, channelType, periodStart, periodEnd,
                (guests != null) ? guests.size() : 0,
                mechanics, mechanicsParams, contentParams, phones, imageUrl);
    }
}
