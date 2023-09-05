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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

import ee.jakarta.tck.ws.rs.common.impl.ReplacingOutputStream;
import ee.jakarta.tck.ws.rs.common.impl.SecurityContextImpl;
import ee.jakarta.tck.ws.rs.common.impl.SinglevaluedMap;
import ee.jakarta.tck.ws.rs.common.impl.StringDataSource;
import ee.jakarta.tck.ws.rs.common.impl.StringSource;
import ee.jakarta.tck.ws.rs.common.impl.StringStreamingOutput;
import ee.jakarta.tck.ws.rs.common.impl.TRACE;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.OutboundSSEEventImpl;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.SSEEventImpl;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.activation.DataSource;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;
import jakarta.xml.bind.JAXBElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import ee.jakarta.tck.ws.rs.common.impl.JaxbKeyValueBean;
import ee.jakarta.tck.ws.rs.common.util.Holder;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.common.util.LinkedHolder;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.SSEJAXRSClient;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.SSEMessage;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
/**
 * @since 2.1
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends SSEJAXRSClient {

  private static final long serialVersionUID = 21L;

  private int mediaTestLevel = 0;

  public JAXRSClientIT() {
    setup();
    mediaTestLevel = 0;
    setContextRoot("/jaxrs_jaxrs21_ee_sse_sseeventsource_web");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/jaxrs21/ee/sse/sseeventsource/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_jaxrs21_ee_sse_sseeventsource_web.war");
    archive.addClasses(TSAppConfig.class, MediaTypeResource.class,
      RepeatedCasterResource.class, ServiceUnavailableResource.class,
      JaxrsUtil.class,
      SSEEventImpl.class,
      SSEMessage.class,
      OutboundSSEEventImpl.class,
      TRACE.class,
      StringSource.class,
      StringStreamingOutput.class,
      StringDataSource.class,
      SinglevaluedMap.class,
      SecurityContextImpl.class,
      ReplacingOutputStream.class,
      JaxbKeyValueBean.class
      );
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */
  /*
   * @testName: defaultWaiting1s
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * 
   * @test_Strategy: In addition to handling the standard connection loss
   * failures, JAX-RS SseEventSource automatically deals with any HTTP 503
   * Service Unavailable responses from an SSE endpoint, that contain a
   * "Retry-After" HTTP header with a valid value. The HTTP 503 + "Retry-After"
   * technique is often used by HTTP endpoints as a means of connection and
   * traffic throttling. In case a HTTP 503 + "Retry-After" response is received
   * in return to a connection request, JAX-RS SSE event source will
   * automatically schedule a new reconnect attempt and use the received
   * "Retry-After" HTTP header value as a one-time override of the reconnect
   * delay.
   */
  @Test
  public void defaultWaiting1s() throws Fault {
    // define
    Holder<InboundSseEvent> holder = new Holder<>();
    WebTarget target = ClientBuilder.newClient()
        .target(getAbsoluteUrl("su/sse"));

    // check its working
    try (SseEventSource source = SseEventSource.target(target).build()) {
      source.register(holder::set);
      source.open();
      sleepUntilHolderGetsFilled(holder);
    } catch (Exception e) {
      throw new Fault(e);
    }
    assertNotNull(holder.get(), "Holder was not filled");
    logTrace("Received message", holder.get().readData());
    holder.set(null);

    // set to return 503
    setProperty(Property.REQUEST, buildRequest(Request.GET, "su/available"));
    setProperty(Property.SEARCH_STRING, "OK");
    invoke();

    try (SseEventSource source = SseEventSource.target(target).build()) {
      source.register(holder::set);
      source.open();
    } catch (Exception e) {
      throw new Fault(e);
    }
    assertNull(holder.get(),
        "The event should be sent after default 1s in retry, not sooner");
    // SseEventSource.close() has been called

    // set to return 503
    setProperty(Property.REQUEST, buildRequest(Request.GET, "su/available"));
    setProperty(Property.SEARCH_STRING, "OK");
    invoke();

    holder = querySSEEndpoint("su/sse");
    logTrace("Received message", holder.get().readData());
    logTrace("Slept for", sleep * millis, "ms");
    assertTrue(sleep < 4,
        "The message has been sent unexpectedly late, after 2000ms");
  }

  /*
   * @testName: wait2Seconds
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * 
   * @test_Strategy: In addition to handling the standard connection loss
   * failures, JAX-RS SseEventSource automatically deals with any HTTP 503
   * Service Unavailable responses from an SSE endpoint, that contain a
   * "Retry-After" HTTP header with a valid value. The HTTP 503 + "Retry-After"
   * technique is often used by HTTP endpoints as a means of connection and
   * traffic throttling. In case a HTTP 503 + "Retry-After" response is received
   * in return to a connection request, JAX-RS SSE event source will
   * automatically schedule a new reconnect attempt and use the received
   * "Retry-After" HTTP header value as a one-time override of the reconnect
   * delay.
   */
  @Test
  public void wait2Seconds() throws Fault {
    // set to return 503
    setProperty(Property.REQUEST, buildRequest(Request.GET, "su/available"));
    setProperty(Property.SEARCH_STRING, "OK");
    invoke();
    // set to wait 2 seconds
    setRequestContentEntity("2");
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.REQUEST, buildRequest(Request.POST, "su/retry"));
    setProperty(Property.SEARCH_STRING, "2");
    invoke();

    Holder<InboundSseEvent> holder = querySSEEndpoint("su/sse");
    logTrace("Received message", holder.get().readData());
    logTrace("Slept for", sleep * millis, "ms");
    assertTrue(sleep > 3,
        "The message has been sent unexpectedly soon, sooner then 2000ms");
    assertTrue(sleep < 6,
        "The message has been sent unexpectedly late, after 3000ms");
    assertEquals(SSEMessage.MESSAGE, holder.get().readData(),
        "Unexpected message received", holder.get().readData());
  }

  /*
   * @testName: connectionLostForDefault500msTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * 
   * @test_Strategy: By default, when a connection the the SSE endpoint is lost,
   * the event source will wait 500 ms before attempting to reconnect to the SSE
   * endpoint.
   */
  @Test
  @Disabled
  public void connectionLostForDefault500msTest() throws Fault {
    resetUnavailableServer();

    // wait for 3 reconnections
    setRequestContentEntity("3");
    setProperty(Property.REQUEST, buildRequest(Request.POST, "su/lost"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.SEARCH_STRING, "3");
    invoke();

    Holder<InboundSseEvent> holder = querySSEEndpoint("su/sselost");
    logTrace("Received message", holder.get().readData());
    logTrace("Slept for", sleep * millis, "ms");
    assertTrue(sleep > 2,
        "The message has been sent unexpectedly soon, sooner then 1500ms");
    assertTrue(sleep < 5,
        "The message has been sent unexpectedly late, after 3000ms");
    assertEquals(SSEMessage.MESSAGE, holder.get().readData(),
        "Unexpected message received", holder.get().readData());

    int cnt = getServerCount();
    logTrace("Received count:", cnt);
    assertTrue(cnt > 3 && cnt < 7,
        "The client tried to reconnected unexpectedly"+ cnt+ "times!");
  }

  /*
   * @testName: reconnectByEventMethodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1215; JAXRS:JAVADOC:1228; JAXRS:JAVADOC:1197;
   * JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199; JAXRS:JAVADOC:1200;
   * JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202; JAXRS:JAVADOC:1241;
   * JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * 
   * @test_Strategy: The SSE endpoint can however control the client-side retry
   * delay by including a special retry field value in the any send event.
   * JAX-RS SseEventSource tracks any received SSE event retry field values set
   * by the endpoint and adjusts the reconnect delay accordingly, using the last
   * received retry field value as the reconnect delay.
   */
  @Test
  public void reconnectByEventMethodTest() throws Fault {
    Holder<InboundSseEvent> holder = querySSEEndpoint("su/reconnectdelay");
    assertTrue(holder.get().isReconnectDelaySet(),
        "ReconnectDelay was not set");
    long delay = holder.get().getReconnectDelay();
    assertEquals(delay, 3000L, "Received unexepcted ReconnectDelay", delay);
    assertEquals(SSEMessage.MESSAGE, holder.get().readData(),
        "Unexpected message received", holder.get().readData());
  }

  /*
   * @testName: userReconnectByEventMethodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * 
   * @test_Strategy: The SSE endpoint can however control the client-side retry
   * delay by including a special retry field value in the any send event.
   * JAX-RS SseEventSource tracks any received SSE event retry field values set
   * by the endpoint and adjusts the reconnect delay accordingly, using the last
   * received retry field value as the reconnect delay.
   */
  @Test
  public void userReconnectByEventMethodTest() throws Fault {
    Holder<InboundSseEvent> holder = querySSEEndpoint("su/userreconnectdelay");
    assertTrue(holder.get().isReconnectDelaySet(),
        "ReconnectDelay was not set");
    long delay = holder.get().getReconnectDelay();
    assertEquals(delay, 20000L, "Received unexepcted ReconnectDelay", delay);
    assertEquals(SSEMessage.MESSAGE, holder.get().readData(),
        "Unexpected message received", holder.get().readData());
  }

  /*
   * @testName: stringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   */
  @Test
  public void stringTest() throws Fault {
    mediaTest(String.class, SSEMessage.MESSAGE, MediaType.TEXT_PLAIN,
        MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE);
  }

  /*
   * @testName: byteArrayTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy:
   */
  @Test
  public void byteArrayTest() throws Fault {
    BiPredicate<Object, Object> p = (a, b) -> a.equals(new String((byte[]) b));
    mediaTest(byte[].class, SSEMessage.MESSAGE, p, MediaType.TEXT_PLAIN,
        MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE);
  }

  /*
   * @testName: inputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy:
   */
  @Test
  public void inputStreamTest() throws Fault {
    BiPredicate<Object, Object> p = (a, b) -> {
      try {
        return JaxrsUtil.readFromStream((InputStream) b).equals(a);
      } catch (IOException e) {
        return false;
      }
    };
    mediaTest(InputStream.class, SSEMessage.MESSAGE, p, MediaType.TEXT_PLAIN,
        MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE);
  }

  /*
   * @testName: readerTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy:
   */
  @Test
  public void readerTest() throws Fault {
    BiPredicate<Object, Object> p = (a, b) -> {
      try {
        return JaxrsUtil.readFromReader((Reader) b).equals(a);
      } catch (IOException e) {
        return false;
      }
    };
    mediaTest(Reader.class, SSEMessage.MESSAGE, p, MediaType.TEXT_PLAIN,
        MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE);
  }

  /*
   * @testName: fileTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy:
   */
  @Test
  public void fileTest() throws Fault {
    BiPredicate<Object, Object> p = (a, b) -> {
      try {
        return JaxrsUtil.readFromFile((File) b).equals(a);
      } catch (IOException e) {
        return false;
      }
    };
    mediaTest(File.class, SSEMessage.MESSAGE, p, MediaType.TEXT_PLAIN,
        MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE);
  }

  /*
   * @testName: dataSourceTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy:
   */
  @Test
  public void dataSourceTest() throws Fault {
    BiPredicate<Object, Object> p = (a, b) -> {
      try {
        return JaxrsUtil.readFromStream(((DataSource) b).getInputStream())
            .equals(a);
      } catch (IOException e) {
        return false;
      }
    };
    mediaTest(DataSource.class, SSEMessage.MESSAGE, p, MediaType.TEXT_PLAIN,
        MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE);
  }

  /*
   * @testName: transformSourceTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy:
   */
  @Test
  public void transformSourceTest() throws Fault {
    mediaTestLevel = 2;
    BiPredicate<Object, Object> p = (a, b) -> {
      try {
        Source s = (Source) b;
        if (StreamSource.class.isInstance(s)) {
          return JaxrsUtil.readFromStream(((StreamSource) b).getInputStream())
              .equals(a);
        } else {
          return true;
        }
      } catch (IOException e) {
        return false;
      }
    };
    mediaTest(Source.class, SSEMessage.MESSAGE, p, MediaType.TEXT_XML,
        MediaType.TEXT_XML_TYPE, MediaType.APPLICATION_ATOM_XML_TYPE);
  }

  /*
   * @testName: jaxbElementTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy:
   */
  @Test
  @Tag("xml_binding")
  public void jaxbElementTest() throws Fault {
    mediaTestLevel = 3;
    @SuppressWarnings("unchecked")
    BiPredicate<Object, Object> p = (a, b) -> ((JAXBElement<String>) b)
        .getValue().equals(a);
    mediaTest(JAXBElement.class, SSEMessage.MESSAGE, p, MediaType.TEXT_XML,
        MediaType.TEXT_XML_TYPE, MediaType.APPLICATION_XML_TYPE,
        new GenericType<JAXBElement<String>>() {
        }, "media/jaxb");
  }

  /*
   * @testName: xmlTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy:
   */
  @Test
  @Tag("xml_binding")
  public void xmlTest() throws Fault {
    mediaTestLevel = 2;
    BiPredicate<Object, Object> p = (a, b) -> ((JaxbKeyValueBean) b).getValue().equals(a);
    mediaTest(JaxbKeyValueBean.class, SSEMessage.MESSAGE, p, MediaType.TEXT_XML, MediaType.TEXT_XML_TYPE,
        MediaType.APPLICATION_XML_TYPE, createGenericType(JaxbKeyValueBean.class), "media/xml");
  }

  /*
   * @testName: multivaluedMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198; JAXRS:JAVADOC:1199;
   * JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201; JAXRS:JAVADOC:1202;
   * JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240; JAXRS:JAVADOC:1236;
   * JAXRS:JAVADOC:1237; JAXRS:JAVADOC:1238; JAXRS:JAVADOC:1239;
   * JAXRS:JAVADOC:1240;
   * 
   * @test_Strategy:
   */
  @Test
  public void multivaluedMapTest() throws Fault {
    mediaTestLevel = 3;
    @SuppressWarnings("unchecked")
    BiPredicate<Object, Object> p = (a,
        b) -> ((MultivaluedMap<String, String>) b).getFirst("key").equals(a);
    mediaTest(MultivaluedMap.class, SSEMessage.MESSAGE, p,
        MediaType.APPLICATION_FORM_URLENCODED,
        MediaType.APPLICATION_FORM_URLENCODED_TYPE,
        MediaType.APPLICATION_FORM_URLENCODED_TYPE,
        new GenericType<MultivaluedMap<String, String>>() {
        }, "media/map");
  }

  /*
   * @testName: connectionLostFor1500msTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1242; JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198;
   * JAXRS:JAVADOC:1199; JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201;
   * JAXRS:JAVADOC:1202; JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240;
   * JAXRS:JAVADOC:1236;
   * 
   * @test_Strategy: reconnectingEvery should send request to the server just
   * twice, once without response, once after reconnect timeout with a response
   */
  @Test
  public void connectionLostFor1500msTest() throws Fault {
    resetUnavailableServer();

    // set to wait 3 seconds
    setRequestContentEntity("1");
    setProperty(Property.REQUEST, buildRequest(Request.POST, "su/lost"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.SEARCH_STRING, "1");
    invoke();

    Holder<InboundSseEvent> holder = new Holder<>();
    WebTarget target = ClientBuilder.newClient()
        .target(getAbsoluteUrl("su/sselost"));
    try (SseEventSource source = SseEventSource.target(target)
        .reconnectingEvery(2000L, TimeUnit.MILLISECONDS).build()) {
      source.register(holder::set);
      source.open();
      sleep = sleepUntilHolderGetsFilled(holder);
    }
    int cnt = getServerCount();
    logTrace("Slept for", sleep * millis, "ms");
    assertNotNull(holder.get(), "Message not received, reconnect was done",
        cnt - 1, "times.");
    logTrace("Received message", holder.get().readData());
    assertTrue(sleep > 3,
        "The message has been sent unexpectedly soon, sooner then 2000ms");
    assertTrue(sleep < 5,
        "The message has been sent unexpectedly late, after 3000ms");
    assertEquals(SSEMessage.MESSAGE, holder.get().readData(),
        "Unexpected message received", holder.get().readData());

    assertEquals(cnt, 2, "Server was reconnected", cnt, "times, unexpectedly");
  }

  /*
   * @testName: closeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1233; JAXRS:JAVADOC:1197; JAXRS:JAVADOC:1198;
   * JAXRS:JAVADOC:1199; JAXRS:JAVADOC:1200; JAXRS:JAVADOC:1201;
   * JAXRS:JAVADOC:1202; JAXRS:JAVADOC:1241; JAXRS:JAVADOC:1240;
   * JAXRS:JAVADOC:1236;
   * 
   * @test_Strategy:
   */
  @Test
  public void closeTest() throws Fault {
    boolean isOpen = true;
    LinkedHolder<InboundSseEvent> holder = new LinkedHolder<>();
    WebTarget target = ClientBuilder.newClient()
        .target(getAbsoluteUrl("repeat/cast"));

    // set clear
    setRequestContentEntity(Boolean.TRUE);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "repeat/set"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    invoke();
    assertTrue(getResponse().readEntity(Boolean.class),
        "Cast has not been set");

    // ask for a message
    SseEventSource source = SseEventSource.target(target).build();
    source.register(holder::add);
    source.open();
    sleepUntilHolderGetsFilled(holder);
    assertNotNull(holder.get(), "Message was not received");
    for (InboundSseEvent e : holder)
      logMsg("Received message no", e.readData());

    // check the session is opened
    setProperty(Property.REQUEST, buildRequest(Request.GET, "repeat/isopen"));
    invoke();
    isOpen = getResponseBody(Boolean.class);
    assertTrue(isOpen, "SseEventSource is closed");

    // Wait 3 times
    for (int i = 0; i != 3; i++) {
      holder.clear();
      sleepUntilHolderGetsFilled(holder);
      for (InboundSseEvent e : holder)
        logMsg("Received message no", e.readData());
    }
    // close
    source.close();
    assertFalse(source.isOpen(), "SseEventSource was not closed");

    // check it was closed
    int cnt = 0;
    int size = holder.size();
    try {
      while (isOpen && cnt < 20) { // check the server sends events
        holder.clear();
        cnt++;
        setProperty(Property.REQUEST,
            buildRequest(Request.GET, "repeat/isopen"));
        invoke();
        isOpen = getResponseBody(Boolean.class);

        assertFalse(size == 0 && isOpen,
            "Message was not received and SseEventSink is open");
        sleepUntilHolderGetsFilled(holder);
        for (InboundSseEvent e : holder)
          logMsg("Received message no", e.readData());
        size = holder.size();
      }
    } finally {
      System.out.println("Sending false");
      setRequestContentEntity(Boolean.FALSE);
      setProperty(Property.REQUEST, buildRequest(Request.POST, "repeat/set"));
      setProperty(Property.REQUEST_HEADERS,
          buildContentType(MediaType.TEXT_PLAIN_TYPE));
      invoke();
      System.out.println("Sent false");
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////

  private <T> void mediaTest(Class<T> queryClass, Object compare,
      String queryMedia, MediaType classMedia, MediaType genericTypeMedia)
      throws Fault {
    mediaTest(queryClass, compare, (a, b) -> a.equals(b), queryMedia,
        classMedia, genericTypeMedia);
  }

  private <T> void mediaTest(Class<T> queryClass, Object compare,
      BiPredicate<Object, Object> comparator, String queryMedia,
      MediaType classMedia, MediaType genericTypeMedia) throws Fault {
    mediaTest(queryClass, compare, comparator, queryMedia, classMedia,
        genericTypeMedia, createGenericType(queryClass), "media/data");
  }

  private <T> void mediaTest(Class<T> queryClass, Object compare,
      BiPredicate<Object, Object> comparator, String queryMedia,
      MediaType classMedia, MediaType genericTypeMedia, GenericType<?> type,
      String path) throws Fault {
    setRequestContentEntity(queryMedia);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "media/set"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.SEARCH_STRING, queryMedia);
    invoke();

    Holder<InboundSseEvent> holder = querySSEEndpoint(path);
    assertHolder(queryClass, compare, comparator, classMedia, genericTypeMedia,
        type, holder);
    logTrace("readData(", queryClass,
        ") returned message as expected when register(Consumer<InboundSseEvent>)");

    Holder<Throwable> exception = new Holder<>();
    holder = querySSEEndpoint(path,
        (a, b) -> a.register(b::set, exception::set));
    assertHolder(queryClass, compare, comparator, classMedia, genericTypeMedia,
        type, holder);
    logTrace("readData(", queryClass,
        ") returned message as expected when register(Consumer<InboundSseEvent>, Consumer<Throwable>)");

    final Holder<Boolean> finished = new Holder<>(false);
    Runnable r = () -> finished.set(true);
    holder = querySSEEndpoint(path,
        (a, b) -> a.register(b::set, exception::set, r));
    assertHolder(queryClass, compare, comparator, classMedia, genericTypeMedia,
        type, holder);
    logTrace("readData(", queryClass,
        ") returned message as expected register(Consumer<InboundSseEvent>, Consumer<Throwable>, Runnable)");
  }

  private <T> void assertHolder(Class<T> queryClass, Object compare,
      BiPredicate<Object, Object> comparator, MediaType classMedia,
      MediaType genericTypeMedia, GenericType<?> type,
      Holder<InboundSseEvent> holder) throws Fault {
    switch (mediaTestLevel) {
    case 0:
      assertTrue(comparator.test(compare, holder.get().readData(queryClass)),
          "Unexpected message received"+ holder.get().readData());
    case 1:
      assertTrue(comparator.test(compare, holder.get().readData(type)),
          "Unexpected message received"+ holder.get().readData());
    case 2:
      assertTrue(
          comparator.test(compare,
              holder.get().readData(queryClass, classMedia)),
          "Unexpected message received"+ holder.get().readData());
    case 3:
      assertTrue(
          comparator.test(compare,
              holder.get().readData(type, genericTypeMedia)),
          "Unexpected message received"+ holder.get().readData());
    }
  }

  private <T> GenericType<T> createGenericType(Class<T> clazz) {
    GenericType<T> type = new GenericType<>(clazz);
    return type;
  }

  private int getServerCount() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "su/count"));
    invoke();
    return Integer.parseInt(getResponseBody());
  }

  private void resetUnavailableServer() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "su/reset"));
    setProperty(Property.SEARCH_STRING, "RESET");
    invoke();
  }
}
