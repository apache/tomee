package org.apache.tomee.security.http;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

public class SavedRequestTest {
  @Test
  public void testSerializable() {
    final SavedRequest savedRequest = new SavedRequest();
    assertTrue("SavedRequest  must implement Serializable, since it will be set as a session attribute",
        savedRequest instanceof Serializable);
  }
}
