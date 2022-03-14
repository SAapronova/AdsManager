package process_manager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.ToString;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ContentParamsDto {
    @JsonProperty("communication_link")
    private String contentLink;

    @JsonProperty("communication_template_title")
    private String contentLinkText;

    @JsonProperty("communication_template_text")
    private String contentText;

    @JsonProperty("communication_template")
    private String contentTemplate;

    @JsonProperty("communication_image")
    private String imageUrl;
}
