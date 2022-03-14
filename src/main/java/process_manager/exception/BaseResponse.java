package process_manager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    @JsonProperty("data")
    private final T data;

    @JsonProperty("error")
    private final Object error;

    private BaseResponse(@JsonProperty("data") T data,
                         @JsonProperty("error") Object error) {
        this.error = error;
        this.data = data;
    }

    public static  <T>BaseResponse<T> ok() {
        return new BaseResponse<>(null, null);
    }

    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(data, null);
    }

    public static <T>BaseResponse<T> fail(BaseError error) {
        return new BaseResponse<>(null, error);
    }

    public T getData() {
        return data;
    }

    public Object getError() {
        return error;
    }
}