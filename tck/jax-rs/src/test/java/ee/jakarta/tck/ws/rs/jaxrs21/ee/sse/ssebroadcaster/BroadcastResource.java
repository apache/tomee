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

package ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.ssebroadcaster;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

import ee.jakarta.tck.ws.rs.common.util.Holder;

@Path("broadcast")
public class BroadcastResource {
  private static final List<SseEventSink> sinkList = Collections
      .synchronizedList(new LinkedList<>());

  private volatile static Holder<SseBroadcaster> broadcaster = new Holder<>();

  private final static Holder<SseEventSink> onCloseSink = new Holder<>();

  private static int cnt;

  @GET
  @Path("clear")
  public String clear() {
    sinkList.clear();
    onCloseSink.set(null);
    broadcaster.set(null);
    cnt = 0;
    return "CLEAR";
  }

  @GET
  @Path("register")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void register(@Context SseEventSink sink, @Context Sse sse) {
    synchronized (broadcaster) {
      if (broadcaster.get() == null)
        broadcaster.set(sse.newBroadcaster());
      onCloseSink.set(null);
      broadcaster.get().register(sink);
      broadcaster.get().onClose(onCloseSink::set);
      sinkList.add(sink);
    }
    sink.send(sse.newEvent("WELCOME" + cnt++));
  }

  @POST
  @Path("broadcast")
  public String broadcast(@Context Sse sse, String message) {
    synchronized (broadcaster) {
      broadcaster.get().broadcast(sse.newEvent(message));
    }
    return message;
  }

  @GET
  @Path("close")
  public String close() {
    synchronized (broadcaster) {
      broadcaster.get().close();
    }
    return "CLOSE";
  }

  @GET
  @Path("check")
  public String check() {
    StringBuffer sb = new StringBuffer();
    synchronized (broadcaster) {
      Iterator<SseEventSink> si = sinkList.iterator();
      for (int i = 0; i != sinkList.size(); i++) {
        sb.append("SseEventSink number ").append(i).append(" is closed:")
            .append(si.next().isClosed());
      }
      sb.append("OnCloseSink has been called:")
          .append(onCloseSink.get() != null);
    }
    return sb.toString();
  }
}
