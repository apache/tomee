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

import static ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.SSEJAXRSClient.MESSAGE;

import java.util.concurrent.CompletableFuture;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

@Path("stage")
public class StageCheckerResource {
  public static final String DONE = "CompletionStage has been done";

  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void send(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      CompletableFuture<?> stage = s.send(sse.newEvent(MESSAGE))
          .toCompletableFuture();
      while (!stage.isDone()) {
        try {
          Thread.sleep(200L);
        } catch (InterruptedException e) {
          e.printStackTrace();
          sse.newEvent(e.getMessage());
          return;
        }
      }
      s.send(sse.newEvent(DONE));
    }
  }
}
