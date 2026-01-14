package com.selling.dto;

import java.time.Instant;

/**
 * Common API response wrapper for consistent HTTP responses.
 */
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private int status;
  private long timestamp;

  public ApiResponse() {
    this.timestamp = Instant.now().toEpochMilli();
  }

  public ApiResponse(boolean success, String message, T data, int status) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.status = status;
    this.timestamp = Instant.now().toEpochMilli();
  }

  // Static factory methods for convenience
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Success", data, 200);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data, 200);
  }

  public static <T> ApiResponse<T> error(String message, int status) {
    return new ApiResponse<>(false, message, null, status);
  }

  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(true, "Created", data, 201);
  }

  // Getters and setters
  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
