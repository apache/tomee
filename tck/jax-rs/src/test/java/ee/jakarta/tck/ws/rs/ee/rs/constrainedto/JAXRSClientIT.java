/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.constrainedto;

import java.io.InputStream;
import java.io.IOException;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */

/**
 * test_strategy_common: If such a annotation is present, the JAX-RS runtime
 * will enforce the specified usage restriction. - Not optional
 * 
 * It is a configuration error to constraint a JAX-RS provider implementation to
 * a run-time context in which the provider cannot be applied. In such case a
 * JAX-RS runtime SHOULD inform a user about the issue and ignore the provider
 * implementation in further processing - Should not throw exception, just
 * ignore
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = 3343257931794865470L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_constrainedto_web/resource");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/constrainedto/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_constrainedto_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class,
      ClientSideReader.class, ClientSideWriter.class,
      ServerSideReader.class, ServerSideWriter.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }

  /* Run test */
  /*
   * @testName: serverSideReaderIsUsedOnServerTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:969;
   * 
   * @test_Strategy: jakarta.ws.rs.ConstrainedTo.value is used
   */
  @Test
  public void serverSideReaderIsUsedOnServerTest() throws Fault {
    setProperty(Property.CONTENT, "Anything");
    setProperty(Property.SEARCH_STRING, ServerSideReader.FAKE_MESSAGE);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(ServerSideReader.MEDIA_TYPE));
    invoke();
  }

  /*
   * @testName: clientSideReaderIsNotUsedOnServerTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:969;
   * 
   * @test_Strategy: jakarta.ws.rs.ConstrainedTo.value is used
   */
  @Test
  public void clientSideReaderIsNotUsedOnServerTest() throws Fault {
    setProperty(Property.CONTENT, Resource.MESSAGE);
    setProperty(Property.SEARCH_STRING, Resource.MESSAGE);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(ClientSideReader.MEDIA_TYPE));
    invoke();
  }

  /*
   * @testName: serverSideWriterIsUsedOnServerTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:969;
   * 
   * @test_Strategy: jakarta.ws.rs.ConstrainedTo.value is used
   */
  @Test
  public void serverSideWriterIsUsedOnServerTest() throws Fault {
    setProperty(Property.CONTENT, Resource.MESSAGE);
    setProperty(Property.SEARCH_STRING, ServerSideWriter.FAKE_MESSAGE);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(ServerSideWriter.MEDIA_TYPE));
    invoke();
  }

  /*
   * @testName: clientSideWriterIsNotUsedOnServerTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:969;
   * 
   * @test_Strategy: jakarta.ws.rs.ConstrainedTo.value is used
   */
  @Test
  public void clientSideWriterIsNotUsedOnServerTest() throws Fault {
    setPrintEntity(true);
    setProperty(Property.CONTENT, Resource.MESSAGE);
    setProperty(Property.SEARCH_STRING, Resource.MESSAGE);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(ClientSideWriter.MEDIA_TYPE));
    invoke();
  }

  /*
   * @testName: serverSideReaderIsNotUsedOnClientTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:969;
   * 
   * @test_Strategy: jakarta.ws.rs.ConstrainedTo.value is used
   */
  @Test
  public void serverSideReaderIsNotUsedOnClientTest() throws Fault {
    addProviders();
    setProperty(Property.CONTENT, ServerSideReader.MEDIA_TYPE.toString());
    setProperty(Property.SEARCH_STRING, Resource.MESSAGE);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "media"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    invoke();
  }

  /*
   * @testName: clientSideReaderIsUsedOnClientTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:969;
   * 
   * @test_Strategy: jakarta.ws.rs.ConstrainedTo.value is used
   */
  @Test
  public void clientSideReaderIsUsedOnClientTest() throws Fault {
    addProviders();
    setProperty(Property.CONTENT, ClientSideReader.MEDIA_TYPE.toString());
    setProperty(Property.SEARCH_STRING, ClientSideReader.FAKE_MESSAGE);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "media"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    invoke();
  }

  /*
   * @testName: serverSideWriterIsNotUsedOnClientTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:969;
   * 
   * @test_Strategy: jakarta.ws.rs.ConstrainedTo.value is used
   */
  @Test
  public void serverSideWriterIsNotUsedOnClientTest() throws Fault {
    addProviders();
    setProperty(Property.CONTENT, ServerSideWriter.MEDIA_TYPE.toString());
    setProperty(Property.SEARCH_STRING, Resource.MESSAGE);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "media"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    invoke();
  }

  /*
   * @testName: clientSideWriterIsUsedOnClientTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:969;
   * 
   * @test_Strategy: jakarta.ws.rs.ConstrainedTo.value is used this goes to
   * special resource method with response 204 to check the ClientSideWriter
   * worked on client rather then let it (wrongly) work on server and falsely
   * pass
   */
  @Test
  public void clientSideWriterIsUsedOnClientTest() throws Fault {
    addProviders();
    setProperty(Property.CONTENT, Resource.MESSAGE);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "clientwriter"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(ClientSideWriter.MEDIA_TYPE));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NO_CONTENT));
    invoke();
  }

  // /////////////////////////////////////////////////////////////////////
  protected void addProviders() {
    addProvider(ServerSideReader.class);
    addProvider(ClientSideReader.class);
    addProvider(ServerSideWriter.class);
    addProvider(ClientSideWriter.class);
  }
}
