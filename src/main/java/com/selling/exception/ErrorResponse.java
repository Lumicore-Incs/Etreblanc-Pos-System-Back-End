package com.selling.exception;

import java.time.Instant;

/**
 * Simple error response payload returned to clients.
 */
public class ErrorResponse {
  private String message;
  private String error;
  private int status;
  private String traceId;
  private long timestamp;

  public ErrorResponse() {
    this.timestamp = Instant.now().toEpochMilli();
  }

  public ErrorResponse(String message, String error, int status, String traceId) {
    this.message = message;
    this.error = error;
    this.status = status;
    this.traceId = traceId;
    this.timestamp = Instant.now().toEpochMilli();
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
