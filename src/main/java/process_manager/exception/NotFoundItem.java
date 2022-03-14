package process_manager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotFoundItem {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("value")
    private final String value;

    @JsonProperty("description")
    private final String description;
}
