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

package ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.sseeventsink;

import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.SSEMessage;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

@Path("close")
public class CloseResource {

  private static volatile boolean exception = false;

  private static volatile boolean isClosed = false;

  @GET
  @Path("reset")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void reset(@Context SseEventSink sink, @Context Sse sse) {
    exception = false;
    isClosed = false;
    try (SseEventSink s = sink) {
      s.send(sse.newEvent("RESET"));
    }
  }

  @GET
  @Path("send")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void send(@Context SseEventSink sink, @Context Sse sse) {
    Thread t = new Thread(new Runnable() {
      public void run() {
        SseEventSink s = sink;
        s.send(sse.newEvent(SSEMessage.MESSAGE));
        s.close();
        isClosed = s.isClosed();
        if (!isClosed)
          return;
        s.close();
        isClosed = s.isClosed();
        if (!isClosed)
          return;
        s.close();
        isClosed = s.isClosed();
        if (!isClosed)
          return;
        try {
          s.send(sse.newEvent("SOMETHING"));
        } catch (IllegalStateException ise) {
          exception = true;
        }
      }
    });
    t.start();
  }

  @GET
  @Path("check")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void check(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      if (!isClosed) {
        s.send(sse.newEvent("Not closed"));
        return;
      }
      if (!exception) {
        s.send(sse.newEvent("No IllegalStateException is thrown"));
        return;
      }
      s.send(sse.newEvent("CHECK"));
    }
  }

  @GET
  @Path("closed")
  @Produces(MediaType.TEXT_PLAIN)
  public boolean isClosed() {
    return isClosed;
  }
}
