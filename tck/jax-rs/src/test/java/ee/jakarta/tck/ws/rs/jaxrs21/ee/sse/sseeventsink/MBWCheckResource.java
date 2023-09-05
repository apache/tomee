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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.xml.namespace.QName;

import ee.jakarta.tck.ws.rs.common.impl.SinglevaluedMap;
import ee.jakarta.tck.ws.rs.common.impl.StringDataSource;
import ee.jakarta.tck.ws.rs.common.impl.StringSource;
import ee.jakarta.tck.ws.rs.common.impl.StringStreamingOutput;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.SSEMessage;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.xml.bind.JAXBElement;

/**
 * Defined in Spec., Section 4.2.4
 */
@Path("mbw")
public class MBWCheckResource {
  static final String MESSAGE = SSEMessage.MESSAGE;

  @GET
  @Path("boolean")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendBoolean(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder().data(true)
          .mediaType(MediaType.TEXT_PLAIN_TYPE).build());
    }
  }

  @GET
  @Path("bytearray")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendByteArray(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder().data(MESSAGE.getBytes())
          .mediaType(MediaType.WILDCARD_TYPE).build());
    }
  }

  @GET
  @Path("char")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendChar(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder().data(MESSAGE.charAt(0))
          .mediaType(MediaType.TEXT_PLAIN_TYPE).build());
    }
  }

  @GET
  @Path("datasource")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendDatasource(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder()
          .data(new StringDataSource(MESSAGE, MediaType.TEXT_PLAIN_TYPE))
          .mediaType(MediaType.WILDCARD_TYPE).build());
    }
  }

  @GET
  @Path("double")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendDouble(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder().data(Double.MAX_VALUE)
          .mediaType(MediaType.TEXT_PLAIN_TYPE).build());
    }
  }

  @GET
  @Path("file")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendFile(@Context SseEventSink sink, @Context Sse sse) {
    File f;
    try (SseEventSink s = sink) {
      try {
        f = File.createTempFile("tck", "tempfile");
        Files.write(f.toPath(), MESSAGE.getBytes(), StandardOpenOption.CREATE,
            StandardOpenOption.APPEND);
        f.deleteOnExit();
        s.send(sse.newEventBuilder().data(f).mediaType(MediaType.WILDCARD_TYPE)
            .build());
      } catch (IOException e) {
        s.send(sse.newEvent(e.getMessage()));
        throw new RuntimeException(e); // log to server log
      }
    }
  }

  @GET
  @Path("inputstream")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendInputStream(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder()
          .data(new ByteArrayInputStream(MESSAGE.getBytes()))
          .mediaType(MediaType.WILDCARD_TYPE).build());
    }
  }

  @GET
  @Path("int")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendInt(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder().data(Integer.MIN_VALUE)
          .mediaType(MediaType.TEXT_PLAIN_TYPE).build());
    }
  }

  @GET
  @Path("jaxbelement")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendJAXBElement(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      JAXBElement<String> element = new JAXBElement<String>(new QName("name"),
          String.class, MESSAGE);
      s.send(sse.newEventBuilder().data(element)
          .mediaType(MediaType.APPLICATION_XML_TYPE).build());
    }
  }

  @GET
  @Path("multivaluedmap")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendMultivaluedMap(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      SinglevaluedMap<String, String> map = new SinglevaluedMap<>();
      map.add("name", MESSAGE);
      s.send(sse.newEventBuilder().data(map)
          .mediaType(MediaType.APPLICATION_FORM_URLENCODED_TYPE).build());
    }
  }

  @GET
  @Path("reader")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendReader(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder()
          .data(new InputStreamReader(
              new ByteArrayInputStream(MESSAGE.getBytes())))
          .mediaType(MediaType.WILDCARD_TYPE).build());
    }
  }

  @GET
  @Path("streamingoutput")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendStreamingOutput(@Context SseEventSink sink,
      @Context Sse sse) {
    try (SseEventSink s = sink) {
      StringStreamingOutput output = new StringStreamingOutput(MESSAGE);
      s.send(sse.newEventBuilder().data(output)
          .mediaType(MediaType.WILDCARD_TYPE).build());
    }
  }

  @GET
  @Path("string")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendString(@Context SseEventSink sink, @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder().data(MESSAGE)
          .mediaType(MediaType.WILDCARD_TYPE).build());
    }
  }

  @GET
  @Path("transformsource")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void sendTransformSource(@Context SseEventSink sink,
      @Context Sse sse) {
    try (SseEventSink s = sink) {
      s.send(sse.newEventBuilder().data(new StringSource(MESSAGE))
          .mediaType(MediaType.TEXT_XML_TYPE).build());
    }
  }
}
