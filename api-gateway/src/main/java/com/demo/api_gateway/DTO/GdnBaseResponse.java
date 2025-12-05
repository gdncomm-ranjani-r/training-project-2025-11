package com.demo.api_gateway.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GdnBaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int status;
    private long timestamp;

    public GdnBaseResponse(boolean success, String message, T data, int status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> GdnBaseResponse<T> success(T data, String message, int status) {
        return new GdnBaseResponse<>(true, message, data, status);
    }

    public static <T> GdnBaseResponse<T> error(String message, int status) {
        return new GdnBaseResponse<>(false, message, null, status);
    }
}

