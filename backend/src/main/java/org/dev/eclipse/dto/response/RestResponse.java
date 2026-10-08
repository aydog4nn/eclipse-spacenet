package org.dev.eclipse.dto.response;

public record RestResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> RestResponse<T> success(String message, T data) {
        return new RestResponse<>(true, message, data);
    }

    public static <T> RestResponse<T> success(T data) {
        return new RestResponse<>(true, "İşlem başarılı", data);
    }

    public static <T> RestResponse<T> fail(String message) {
        return new RestResponse<>(false, message, null);
    }
}
