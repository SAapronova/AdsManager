package process_manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import process_manager.entity.SegmentType;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Builder
public class TestCommunicationSegmentDto {

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
}
