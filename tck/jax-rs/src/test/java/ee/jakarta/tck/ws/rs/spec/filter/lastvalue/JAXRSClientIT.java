/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.filter.lastvalue;

import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
/**
 * Test the interceptor is called when any entity provider is called
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  public static final String plaincontent = JAXRSClientIT.class.getName();

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_filter_lastvalue_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/filter/lastvalue/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_filter_lastvalue_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, ArrayListEntityProvider.class, FirstReaderInterceptor.class, FirstWriterInterceptor.class, LinkedListEntityProvider.class, SecondReaderInterceptor.class, SecondWriterInterceptor.class, JaxrsUtil.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }


  /* Run test */

  /*
   * @testName: readerContextOnContainerTest
   * 
   * @assertion_ids: JAXRS:SPEC:86;
   * 
   * @test_Strategy: JAX-RS implementations MUST use the last parameter values
   * set in the context object when calling the wrapped methods
   * MessageBodyReader.readFrom and MessageBodyWrite.writeTo.
   */
  @Test
  public void readerContextOnContainerTest() throws Fault {
    addInterceptors(FirstReaderInterceptor.class);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "postlist"));
    setRequestContentEntity(plaincontent);
    setProperty(Property.SEARCH_STRING,
        SecondReaderInterceptor.class.getName());
    setProperty(Property.SEARCH_STRING,
        SecondReaderInterceptor.class.getAnnotations()[0].annotationType()
            .getName());
    setProperty(Property.SEARCH_STRING, MediaType.TEXT_PLAIN);
    invoke();
    logMsg("Last values set in contexts were used as expected");
  }

  /*
   * @testName: readerContextOnClientTest
   * 
   * @assertion_ids: JAXRS:SPEC:86;
   * 
   * @test_Strategy: JAX-RS implementations MUST use the last parameter values
   * set in the context object when calling the wrapped methods
   * MessageBodyReader.readFrom and MessageBodyWrite.writeTo.
   */
  @Test
  public void readerContextOnClientTest() throws Fault {
    addProvider(FirstReaderInterceptor.class);
    addProvider(SecondReaderInterceptor.class);
    addProvider(ArrayListEntityProvider.class);
    addProvider(LinkedListEntityProvider.class);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "poststring"));
    setRequestContentEntity(plaincontent);
    invoke();
    Response response = getResponse();
    response.getHeaders().add(Resource.HEADERNAME,
        FirstReaderInterceptor.class.getName());
    @SuppressWarnings("unchecked")
    List<String> list = response.readEntity(List.class);
    assertTrue(ArrayList.class.isInstance(list),
        "Entity is not instanceof ArrayList");
    String entity = list.get(0);
    assertContains(entity, SecondReaderInterceptor.class.getName(),
        "Second value in reader interceptor is unexpectedly not used");
    assertContains(entity,
        SecondReaderInterceptor.class.getAnnotations()[0].annotationType()
            .getName(),
        "Second value in reader interceptor is unexpectedly not used");
    assertContains(entity, MediaType.TEXT_PLAIN,
        "Second value in reader interceptor is unexpectedly not used");
    logMsg("Last values set in contexts were used as expected");
  }

  /*
   * @testName: writerContextOnContainerTest
   * 
   * @assertion_ids: JAXRS:SPEC:86;
   * 
   * @test_Strategy: JAX-RS implementations MUST use the last parameter values
   * set in the context object when calling the wrapped methods
   * MessageBodyReader.readFrom and MessageBodyWrite.writeTo.
   */
  @Test
  public void writerContextOnContainerTest() throws Fault {
    addInterceptors(FirstWriterInterceptor.class);
    setProperty(Property.REQUEST, buildRequest(Request.GET, "getlist"));
    setProperty(Property.SEARCH_STRING,
        SecondWriterInterceptor.class.getName());
    setProperty(Property.SEARCH_STRING,
        SecondWriterInterceptor.class.getAnnotations()[0].annotationType()
            .getName());
    setProperty(Property.SEARCH_STRING, MediaType.TEXT_PLAIN);
    invoke();
    logMsg("Last values set in contexts were used as expected");
  }

  /*
   * @testName: writerContextOnClientTest
   * 
   * @assertion_ids: JAXRS:SPEC:86;
   * 
   * @test_Strategy: JAX-RS implementations MUST use the last parameter values
   * set in the context object when calling the wrapped methods
   * MessageBodyReader.readFrom and MessageBodyWrite.writeTo.
   */
  @Test
  public void writerContextOnClientTest() throws Fault {
    addProvider(FirstReaderInterceptor.class);
    addProvider(SecondReaderInterceptor.class);
    addProvider(ArrayListEntityProvider.class);
    addProvider(LinkedListEntityProvider.class);
    ArrayList<String> list = new ArrayList<String>();
    list.add(plaincontent);
    setRequestContentEntity(list);
    addInterceptors(FirstWriterInterceptor.class);
    setProperty(Property.REQUEST, buildRequest(Request.POST, "poststring"));
    setProperty(Property.SEARCH_STRING,
        SecondWriterInterceptor.class.getName());
    setProperty(Property.SEARCH_STRING,
        SecondWriterInterceptor.class.getAnnotations()[0].annotationType()
            .getName());
    setProperty(Property.SEARCH_STRING, MediaType.TEXT_PLAIN);
    invoke();
    logMsg("Last values set in contexts were used as expected");
  }

  // //////////////////////////////////////////////////////////////////////
  private void addInterceptors(Class<?> clazz) {
    setProperty(Property.REQUEST_HEADERS,
        Resource.HEADERNAME + ":" + clazz.getName());
  }
}
