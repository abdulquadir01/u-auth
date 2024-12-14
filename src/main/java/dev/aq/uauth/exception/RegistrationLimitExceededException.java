package dev.aq.uauth.exception;

public class RegistrationLimitExceededException extends RuntimeException {

  public RegistrationLimitExceededException(String message) {
    super(message);
  }

  public RegistrationLimitExceededException(String message, Throwable cause) {
    super(message, cause);
  }
}