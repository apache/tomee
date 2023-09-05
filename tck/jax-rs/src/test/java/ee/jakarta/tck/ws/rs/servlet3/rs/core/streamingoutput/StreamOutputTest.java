/*
 * Copyright (c) 2011, 2018, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.servlet3.rs.core.streamingoutput;

import java.io.IOException;
import java.io.OutputStream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

@Path(value = "/StreamOutputTest")
public class StreamOutputTest {

  @GET
  @Path("/Test1")
  public StreamingOutput streamingOutput() {
    return new StreamingOutput() {
      public void write(OutputStream output) throws IOException {
        output.write("StreamingOutputTest1".getBytes());
      }
    };
  }

  @GET
  @Path("/Test2")
  public StreamingOutput test2() {
    return new StreamingOutput() {
      public void write(OutputStream output) throws IOException {
        throw new WebApplicationException(404);
      }
    };
  }

  @GET
  @Path("IOExceptionTest")
  public Response testIOException() {
    StreamingOutput so = new StreamingOutput() {
      public void write(OutputStream output) throws IOException {
        throw new IOException("TckIOExceptionTest");
      }
    };
    Response response = Response.ok(so).build();
    return response;
  }

}