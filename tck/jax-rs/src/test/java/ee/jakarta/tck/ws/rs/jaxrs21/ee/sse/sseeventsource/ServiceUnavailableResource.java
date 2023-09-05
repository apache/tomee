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

package ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.sseeventsource;

import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.OutboundSSEEventImpl;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.SSEMessage;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

@Path("su")
public class ServiceUnavailableResource {
  private static volatile Boolean isServiceUnavailable = false;

  private static volatile int isConnectionLost = 0;

  private static volatile int retry = 1;

  private static int count = 0;

  static final String MESSAGE = SSEMessage.MESSAGE;

  @GET
  @Path("reset")
  public String reset() {
    retry = 0;
    isServiceUnavailable = false;
    isConnectionLost = 0;
    count = 0;
    return "RESET";
  }

  @GET
  @Path("count")
  public int count() {
    return count;
  }

  @GET
  @Path("available")
  @Produces(MediaType.TEXT_PLAIN)
  public String setUnavailable() {
    synchronized (isServiceUnavailable) {
      isServiceUnavailable = true;
      retry = 1;
    }
    return "OK";
  }

  @POST
  @Path("lost")
  @Produces(MediaType.TEXT_PLAIN)
  public int setConnectionLost(int count) {
    synchronized (isServiceUnavailable) {
      isConnectionLost = count;
      isServiceUnavailable = false;
      retry = 1;
    }
    return count;
  }

  @POST
  @Path("retry")
  @Produces(MediaType.TEXT_PLAIN)
  public int retry(int seconds) {
    synchronized (isServiceUnavailable) {
      retry = seconds;
    }
    return seconds;
  }

  @GET
  @Path("sse")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendMessage(@Context SseEventSink sink, @Context Sse sse) {
    synchronized (isServiceUnavailable) {
      if (isServiceUnavailable) {
        isServiceUnavailable = false;
        throw new WebApplicationException(Response.status(503)
            .header(HttpHeaders.RETRY_AFTER, String.valueOf(retry)).build());
      } else {
        try (SseEventSink s = sink) {
          s.send(sse.newEvent(MESSAGE));
        }
      }
    }
  }

  @GET
  @Path("sselost")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sseLost(@Context SseEventSink sink, @Context Sse sse) {
    synchronized (isServiceUnavailable) {
      count++;
      if (isConnectionLost != 0) {
        isConnectionLost--;
        sink.close();
        /*
         * To cancel a stream from the server, respond with a non
         * "text/event-stream" Content-Type or return an HTTP status other than
         * 200 OK and 503 Service Unavailable (e.g. 404 Not Found).
         */
      } else {
        try (SseEventSink s = sink) {
          s.send(sse.newEvent(MESSAGE));
        }
      }
    }
  }

  @GET
  @Path("reconnectdelay")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendRetry(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder().data(MESSAGE).reconnectDelay(3000L).build());
    }
  }

  @GET
  @Path("userreconnectdelay")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendUserRetry(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(
          (OutboundSseEvent) new OutboundSSEEventImpl(MESSAGE).setDelay(20000));
    }
  }
}
