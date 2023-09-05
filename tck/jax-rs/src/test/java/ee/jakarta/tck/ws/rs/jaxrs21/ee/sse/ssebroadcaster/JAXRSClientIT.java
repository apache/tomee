/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.impl.JaxbKeyValueBean;
import ee.jakarta.tck.ws.rs.common.impl.ReplacingOutputStream;
import ee.jakarta.tck.ws.rs.common.impl.SecurityContextImpl;
import ee.jakarta.tck.ws.rs.common.impl.SinglevaluedMap;
import ee.jakarta.tck.ws.rs.common.impl.StringDataSource;
import ee.jakarta.tck.ws.rs.common.impl.StringSource;
import ee.jakarta.tck.ws.rs.common.impl.StringStreamingOutput;
import ee.jakarta.tck.ws.rs.common.impl.TRACE;
import ee.jakarta.tck.ws.rs.common.util.Holder;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;

import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.common.util.LinkedHolder;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.SSEJAXRSClient;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.sse.SSEMessage;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;


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

  private static final int CLIENTS = 5;

  public JAXRSClientIT() {
    setContextRoot("/jaxrs_jaxrs21_ee_sse_ssebroadcaster_web");
    setup();
  }

  @Override
  public void setup() {
    super.setup();
    target = ClientBuilder.newClient()
        .target(getAbsoluteUrl("broadcast/register"));
    clients = new BroadCasterClient[CLIENTS];
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
  public static WebArchive createDeployment() throws IOException {

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/jaxrs21/ee/sse/ssebroadcaster/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_jaxrs21_ee_sse_ssebroadcaster_web.war");
    archive.addClasses(TSAppConfig.class, BroadcastResource.class,
        Holder.class,
        SSEMessage.class,
        TRACE.class,
        StringSource.class,
        StringStreamingOutput.class,
        StringDataSource.class,
        SinglevaluedMap.class,
        SecurityContextImpl.class,
        ReplacingOutputStream.class,
        JaxbKeyValueBean.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }



  @Override
  public void cleanup() throws Fault {
    super.cleanup();
    try {
      for (int i = 0; i != clients.length; i++) {
        System.out.println("cleanup" + i);
        clients[i].close();
      }
    } catch (Exception e) {
      throw new Fault(e);
    }
  }

  private BroadCasterClient[] clients;

  /* Run test */
  ///////////////////////////////////////////////////////////////////////////////////////////

  /*
   * @testName: sseBroadcastTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1216; JAXRS:JAVADOC:1220; JAXRS:JAVADOC:1221;
   * JAXRS:JAVADOC:1222; JAXRS:JAVADOC:1224;
   * 
   * @test_Strategy:
   */
  @Test
  public void sseBroadcastTest() throws Fault {
    int MSG_MAX = 7;
    int wait = 25;

    setProperty(Property.REQUEST, buildRequest(Request.GET, "broadcast/clear"));
    setProperty(Property.SEARCH_STRING, "CLEAR");
    invoke();

    clients = new BroadCasterClient[CLIENTS];
    for (int i = 0; i != CLIENTS; i++) {
      clients[i] = new BroadCasterClient();
      Thread t = new Thread(clients[i]);
      t.start();
    }

    for (int i = 0; i != CLIENTS; i++) {
      while (clients[i].getEvents().size() == 0 && wait-- > 0)
        TestUtil.sleep(100);
    }

    for (int i = 0; i != MSG_MAX; i++) {
      setProperty(Property.CONTENT, SSEMessage.MESSAGE + i);
      setProperty(Property.REQUEST,
          buildRequest(Request.POST, "broadcast/broadcast"));
      setProperty(Property.SEARCH_STRING, TEST_PROPS.get(Property.CONTENT));
      invoke();
    }

    wait = 25;
    while (clients[0].holder.size() <= MSG_MAX && wait > 0) {
      TestUtil.sleep(200);
      wait--;
    }

    setProperty(Property.REQUEST, buildRequest(Request.GET, "broadcast/close"));
    setProperty(Property.SEARCH_STRING, "CLOSE");
    invoke();

    for (int i = 0; i != CLIENTS; i++) {
      List<String> events = clients[i].getEvents();
      for (String e : events) {
        logMsg("Client", i, "Received message", e);
      }
    }

    for (int i = 0; i != CLIENTS; i++) {
      List<String> events = clients[i].getEvents();
      assertEquals(events.size(), MSG_MAX + 1,
          "Received unexpected number of events", events.size());
      assertTrue(events.get(0).contains("WELCOME"),
          "Received unexpected message"+ events.get(0));
      for (int j = 0; j != MSG_MAX; j++)
        assertEquals(events.get(j + 1), SSEMessage.MESSAGE + j,
            "Received unexpected message", events.get(j + 1));
    }

    setProperty(Property.REQUEST, buildRequest(Request.GET, "broadcast/check"));
    invoke();
    String response = getResponseBody();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i != CLIENTS; i++) {
      sb.append("SseEventSink number ").append(i).append(" is closed:true");
    }
    sb.append("OnCloseSink has been called:true");
    assertEquals(response, sb.toString(), "Unexpected check message received",
        response);
  }

  private WebTarget target;

  class BroadCasterClient implements Runnable, AutoCloseable {
    MsgHolder holder = new MsgHolder();

    volatile boolean isClosed = false;

    @Override
    public void run() {
      try (SseEventSource source = SseEventSource.target(target).build()) {
        source.register(holder::add);
        source.open();
        while (!isClosed) {
          sleepUntilHolderGetsFilled(holder);
          System.out.append("WAITING:").println(toString());
        }
      }
    }

    @Override
    public void close() throws Exception {
      isClosed = true;
    }

    public List<String> getEvents() {
      return holder.asList();
    }
  }

  static class MsgHolder extends LinkedHolder<String> {
    public void add(InboundSseEvent value) {
      String data = value.readData();
      super.add(data);
      System.out.println("Received" + data);
    }
  }
}
