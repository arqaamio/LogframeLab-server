package com.arqaam.logframelab.exception;

public class InvalidJwsTokenException extends RuntimeException {
  public InvalidJwsTokenException(String tokenType, String token, String message) {
    super(String.format("%s: [%s] token: [%s] ", message, tokenType, token));
  }
}
