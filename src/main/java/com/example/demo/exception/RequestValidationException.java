package com.example.demo.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class RequestValidationException extends RuntimeException {

  private Map<String, ValidationError> errorToMessageMap;

  public RequestValidationException(Map<String, ValidationError> errorToMessageMap) {
    super("Request validation failed");
    this.errorToMessageMap = errorToMessageMap;
  }

  @Getter
  public enum ValidationError {
    FIELD_NULL("000"),
    INVALID_FIELD_VALUE("001");

    private final String errorCode;

    ValidationError(String errorCode) {
      this.errorCode = errorCode;
    }
  }

}
