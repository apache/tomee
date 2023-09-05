/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.jaxrs21.ee.sse;

import java.util.function.BiConsumer;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.util.Holder;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * @since 2.1
 */
public abstract class SSEJAXRSClient extends JaxrsCommonClient {

  private static final long serialVersionUID = 21L;

  protected long millis = 550L;

  protected int sleep = -1;

  public static final String MESSAGE = SSEMessage.MESSAGE;

  protected Holder<InboundSseEvent> querySSEEndpoint(String endpoint)
      throws Fault {
    return querySSEEndpoint(endpoint, (a, b) -> a.register(b::set));
  }

  protected Holder<InboundSseEvent> querySSEEndpoint(String endpoint,
      BiConsumer<SseEventSource, Holder<InboundSseEvent>> registrator)
      throws Fault {
    Holder<InboundSseEvent> holder = new Holder<>();
    WebTarget target = ClientBuilder.newClient()
        .target(getAbsoluteUrl(endpoint));

    try (SseEventSource source = SseEventSource.target(target).build()) {
      registrator.accept(source, holder);
      source.open();
      sleep = sleepUntilHolderGetsFilled(holder);
      assertTrue(source.isOpen(), "SseEventSource#isOpen returns false");
    // } catch (Fault f) {
    //   throw f;
    } catch (Exception e) {
      throw new Fault(e);
    }
    assertNotNull(holder.get(), "The message was not received");
    return holder;
  }

  protected void querySSEEndpointAndAssert(String endpoint) throws Fault {
    Holder<InboundSseEvent> holder = querySSEEndpoint(endpoint);
    assertEquals(MESSAGE, holder.get().readData(),
        "Unexpected message received", holder.get().readData());
  }

  protected <T> int sleepUntilHolderGetsFilled(Holder<T> holder) {
    try {
      for (int i = 0; i != 7; i++) {
        Thread.sleep(millis);
        if (holder.get() != null)
          return i + 1;
      }
    } catch (InterruptedException ie) {
      // ignore
    }
    return 7;
  }
}
