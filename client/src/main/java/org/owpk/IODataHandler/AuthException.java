package org.owpk.IODataHandler;

import javax.security.sasl.AuthenticationException;

public class AuthException extends IllegalArgumentException {
  public AuthException(String detail) {
    super(detail);
  }
}
