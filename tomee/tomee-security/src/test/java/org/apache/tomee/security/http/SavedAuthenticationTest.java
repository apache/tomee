package org.apache.tomee.security.http;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashSet;

import org.junit.Test;

public class SavedAuthenticationTest {

  @Test
  public void testSerializable() {
    final SavedAuthentication savedAuthentication = new SavedAuthentication(null, new HashSet<>());
    assertTrue("SavedAuthentication must implement Serializable, since it will be set as a session attribute",
        savedAuthentication instanceof Serializable);
  }
}
