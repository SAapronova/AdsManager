package process_manager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CampaignDto {
    @Schema(description = "Код кампании")
    @JsonProperty("camp_id")
    @NotNull
    private String campaignCode;

    @Schema(description = "Дата начала кампании",
            type = "string",
            pattern = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}",
            example = "2021-01-30 12:00:00")
    @JsonProperty("date_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime periodStart;

    @Schema(description = "Дата окончания кампании",
            type = "string",
            pattern = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}",
            example = "2021-01-30 12:00:00")
    @JsonProperty("date_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime periodEnd;

    @Schema(description = "Дата окончания пост-периода",
            type = "string",
            pattern = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}",
            example = "2021-01-30 12:00:00")
    @JsonProperty("date_postperiod")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime postPeriodEnd;

    @JsonProperty("segments")
    @NotNull
    @Valid
    private List<CampaignSegmentDto> segments;
}
