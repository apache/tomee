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

package ee.jakarta.tck.ws.rs.jaxrs21.spec.completionstage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

@Path("/async")
public class CompletionStageResource {

  public static final String MESSAGE = CompletionStageResource.class.getName();

  @GET
  public CompletionStage<String> async() {
    CompletableFuture<String> cs = new CompletableFuture<>();
    Executors.newSingleThreadExecutor().submit(() -> {
      try {
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        throw new WebApplicationException(e);
      }
      cs.complete(MESSAGE);
    });
    return cs;
  }
}
