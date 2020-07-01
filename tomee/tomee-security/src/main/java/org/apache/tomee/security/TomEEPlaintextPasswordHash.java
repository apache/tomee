package org.apache.tomee.security;

import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.security.enterprise.identitystore.PasswordHash;

@Dependent
public class TomEEPlaintextPasswordHash implements PasswordHash {

  @Override
  public void initialize(final Map<String, String> parameters) {

  }

  @Override
  public String generate(final char[] password) {
    return new String(password);
  }

  @Override
  public boolean verify(final char[] password, final String hashedPassword) {
    // don't bother with constant time comparison; more portable
    // this way, and algorithm will be used only for testing.
    return (password != null && password.length > 0 && hashedPassword != null
        && hashedPassword.length() > 0
        && hashedPassword.equals(new String(password)));
  }
}