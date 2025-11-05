package com.qrware.dto; // Lub inny wspólny pakiet, np. com.qrware.payload

import java.time.LocalDateTime;

/**
 * Generyczna, ustandaryzowana odpowiedź API.
 * Używana przez wszystkie kontrolery do komunikacji z klientem.
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // Gettery
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // --- Metody statyczne dla wygody (opcjonalne, ale zalecane) ---

    /**
     * Tworzy pomyślną odpowiedź z danymi.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    /**
     * Tworzy pomyślną odpowiedź z własną wiadomością.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Tworzy odpowiedź błędu.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}