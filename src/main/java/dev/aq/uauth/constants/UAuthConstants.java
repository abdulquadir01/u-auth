package dev.aq.uauth.constants;

public final class UAuthConstants {

  private UAuthConstants() {
    throw new IllegalStateException("Constant class can't be instantiated.");
  }

  public static final int TOKEN_VALIDITY_IN_MS = 1000 * 60 * 60;
  public static final int TOKEN_VALIDITY_IN_SEC = 60 * 60;
  public final String[] authorizedUrl = {"/api/v1/auth/**"};

  //Token types
  public enum TokenType {
    BEARER
  }

}
