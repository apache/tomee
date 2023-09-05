/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.client.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanEntityProvider;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 7355465562476492891L;

  public JAXRSClientIT() {
    setClientAndWebTarget();
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  protected transient WebTarget target;

  protected transient Client client;

  /*
   * @testName: closeOnClientConfigTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientConfigTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, client, "getConfiguration");
  }

  /*
   * @testName: closeOnClientInvocationWithLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientInvocationWithLinkTest() throws Fault {
    client.close();
    Link link = Link.fromUri("cts").build();
    assertException(IllegalStateException.class, client, "invocation", link);
  }

  /*
   * @testName: closeOnClientTargetWithLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientTargetWithLinkTest() throws Fault {
    client.close();
    Link link = Link.fromUri("cts").build();
    assertException(IllegalStateException.class, client, "target", link);
  }

  /*
   * @testName: closeOnClientTargetWithStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientTargetWithStringTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, client, "target", "cts");
  }

  /*
   * @testName: closeOnClientTargetWithUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientTargetWithUriTest() throws Fault {
    client.close();
    URI uri = URI.create("cts");
    assertException(IllegalStateException.class, client, "target", uri);
  }

  /*
   * @testName: closeOnClientRegisterClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientRegisterClassTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, client, "register",
        StringBeanEntityProvider.class);
  }

  /*
   * @testName: closeOnClientRegisterObjectTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientRegisterObjectTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, client, "register",
        new StringBeanEntityProvider());
  }

  /*
   * @testName: closeOnClientRegisterClassWithContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientRegisterClassWithContractsTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, client, "register",
        StringBeanEntityProvider.class,
        new Class[] { MessageBodyWriter.class });
  }

  /*
   * @testName: closeOnClientRegisterClassWithPriorityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientRegisterClassWithPriorityTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, client, "register",
        StringBeanEntityProvider.class, 100);
  }

  /*
   * @testName: closeOnClientRegisterClassMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientRegisterClassMapTest() throws Fault {
    client.close();
    Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
    contracts.put(MessageBodyReader.class, 100);
    assertException(IllegalStateException.class, client, "register",
        StringBeanEntityProvider.class, contracts);
  }

  /*
   * @testName: closeOnClientRegisterObjectWithContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientRegisterObjectWithContractsTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, client, "register",
        new StringBeanEntityProvider(),
        new Class[] { MessageBodyReader.class });
  }

  /*
   * @testName: closeOnClientRegisterObjectWithPriorityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientRegisterObjectWithPriorityTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, client, "register",
        new StringBeanEntityProvider(), 100);
  }

  /*
   * @testName: closeOnClientRegisterObjectWithMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientRegisterObjectWithMapTest() throws Fault {
    client.close();
    Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
    contracts.put(MessageBodyReader.class, 100);
    assertException(IllegalStateException.class, client, "register",
        new StringBeanEntityProvider(), contracts);
  }

  /*
   * @testName: closeOnClientPropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientPropertyTest() throws Fault {
    client.close();
    Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
    contracts.put(MessageBodyReader.class, 100);
    assertException(IllegalStateException.class, client, "property", "A", "B");
  }

  /*
   * @testName: closeOnClientTargetWithUriBuilderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalTStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnClientTargetWithUriBuilderTest() throws Fault {
    client.close();
    Link link = Link.fromUri("cts").build();
    UriBuilder builder = UriBuilder.fromUri(link.getUri());
    assertException(IllegalStateException.class, client, "target", builder);
  }

  /*
   * @testName: closeOnWebTargetConfigTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetConfigTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "getConfiguration");
  }

  /*
   * @testName: closeOnWebTargetGetUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetGetUriTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "getUri");
  }

  /*
   * @testName: closeOnWebTargetGetUriBuilderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetGetUriBuilderTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "getUriBuilder");
  }

  /*
   * @testName: closeOnWebTargetMatrixParamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetMatrixParamTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "matrixParam", "cts",
        new Object[] { "tck" });
  }

  /*
   * @testName: closeOnWebTargetPathTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetPathTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "path", "cts");
  }

  /*
   * @testName: closeOnWebTargetQueryParamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetQueryParamTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "queryParam", "cts",
        new Object[] { "tck" });
  }

  /*
   * @testName: closeOnWebTargetRegisterClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRegisterClassTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "register",
        StringBeanEntityProvider.class);
  }

  /*
   * @testName: closeOnWebTargetRegisterObjectTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRegisterObjectTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "register",
        new StringBeanEntityProvider());
  }

  /*
   * @testName: closeOnWebTargetRegisterClassWithContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRegisterClassWithContractsTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "register",
        StringBeanEntityProvider.class,
        new Class[] { MessageBodyWriter.class });
  }

  /*
   * @testName: closeOnWebTargetRegisterClassWithPriorityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRegisterClassWithPriorityTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "register",
        StringBeanEntityProvider.class, 100);
  }

  /*
   * @testName: closeOnWebTargetRegisterClassMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRegisterClassMapTest() throws Fault {
    client.close();
    Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
    contracts.put(MessageBodyReader.class, 100);
    assertException(IllegalStateException.class, target, "register",
        StringBeanEntityProvider.class, contracts);
  }

  /*
   * @testName: closeOnWebTargetRegisterObjectWithContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRegisterObjectWithContractsTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "register",
        new StringBeanEntityProvider(),
        new Class[] { MessageBodyReader.class });
  }

  /*
   * @testName: closeOnWebTargetRegisterObjectWithPriorityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRegisterObjectWithPriorityTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "register",
        new StringBeanEntityProvider(), 100);
  }

  /*
   * @testName: closeOnWebTargetRegisterObjectWithMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRegisterObjectWithMapTest() throws Fault {
    client.close();
    Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
    contracts.put(MessageBodyReader.class, 100);
    assertException(IllegalStateException.class, target, "register",
        new StringBeanEntityProvider(), contracts);
  }

  /*
   * @testName: closeOnWebTargetRequestTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRequestTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "request");
  }

  /*
   * @testName: closeOnWebTargetRequestWithMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRequestWithMediaTypeTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "request",
        new Object[] { new MediaType[] { MediaType.APPLICATION_XML_TYPE } });
  }

  /*
   * @testName: closeOnWebTargetRequestWithMediaTypeNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetRequestWithMediaTypeNameTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "request",
        new Object[] { new String[] { MediaType.APPLICATION_XML } });
  }

  /*
   * @testName: closeOnWebTargetResolveTemplateStringObjectTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetResolveTemplateStringObjectTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "resolveTemplate",
        "name", "value");
  }

  /*
   * @testName: closeOnWebTargetResolveTemplateStringObjectBooleanTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetResolveTemplateStringObjectBooleanTest()
      throws Fault {
    client.close();
    assertException(IllegalStateException.class, target, "resolveTemplate",
        "name", "value", true);
  }

  /*
   * @testName: closeOnWebTargetResolveTemplateFromEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetResolveTemplateFromEncodedTest() throws Fault {
    client.close();
    assertException(IllegalStateException.class, target,
        "resolveTemplateFromEncoded", "name", "value");
  }

  /*
   * @testName: closeOnWebTargetResolveTemplatesMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetResolveTemplatesMapTest() throws Fault {
    client.close();
    Map<String, Object> map = new HashMap<String, Object>();
    assertException(IllegalStateException.class, target, "resolveTemplates",
        map);
  }

  /*
   * @testName: closeOnWebTargetResolveTemplatesMapBooleanTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetResolveTemplatesMapBooleanTest() throws Fault {
    client.close();
    Map<String, Object> map = new HashMap<String, Object>();
    assertException(IllegalStateException.class, target, "resolveTemplates",
        map, true);
  }

  /*
   * @testName: closeOnWebTargetPropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetPropertyTest() throws Fault {
    client.close();
    Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
    contracts.put(MessageBodyReader.class, 100);
    assertException(IllegalStateException.class, target, "property", "A", "B");
  }

  /*
   * @testName: closeOnWebTargetResolveTemplatesFromEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:409;
   * 
   * @test_Strategy: Close client instance and all it's associated resources.
   * Subsequent calls have no effect and are ignored. Once the client is closed,
   * invoking any other method on the client instance would result in an
   * IllegalStateException being thrown. Calling this method effectively
   * invalidates all WebTarget resource targets produced by the client instance.
   * Invoking any method on such targets once the client is closed would result
   * in an IllegalStateException being thrown.
   */
  @Test
  public void closeOnWebTargetResolveTemplatesFromEncodedTest() throws Fault {
    client.close();
    Map<String, Object> map = new HashMap<String, Object>();
    assertException(IllegalStateException.class, target,
        "resolveTemplatesFromEncoded", map);
  }

  /*
   * @testName: invocationFromLinkExceptionNoLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:411;
   * 
   * @test_Strategy: jakarta.ws.rs.client.Client.invocation( Link ) throws
   * NullPointerException in case argument is null.
   */
  @Test
  public void invocationFromLinkExceptionNoLinkTest() throws Fault {
    String exceptionMessage = "NullPointerException successfully thrown when no link";
    String noExceptionMessage = "NullPointerException not thrown when no link";
    checkInvocationException(null, NullPointerException.class, exceptionMessage,
        noExceptionMessage);
  }

  /*
   * @testName: targetStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:413;
   * 
   * @test_Strategy: Build a new web resource target.
   */
  @Test
  public void targetStringTest() throws Fault {
    // setClientAndWebTarget is called in constructor
    assertTrue(target != null, "WebTarget is null");
  }

  /*
   * @testName: targetWithStringIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:413;
   * 
   * @test_Strategy: jakarta.ws.rs.client.Client.target( String ) throws
   * IllegalArgumentException in case the supplied string is not a valid URI
   * template.
   */
  @Test
  public void targetWithStringIllegalArgumentExceptionTest() throws Fault {
    String sWebTarget = ":cts:8080//tck:90090//jaxrs ";
    try {
      new URI(sWebTarget);
      throw new Fault("URI(" + sWebTarget + ") is valid");
    } catch (URISyntaxException e1) {
      logMsg("Uri is not a valid URI as expected:", e1);
    }
    try {
      target = client.target(sWebTarget);
      throw new Fault(
          "IllegalArgumentException was not thrown for target " + sWebTarget);
    } catch (IllegalArgumentException e) {
      TestUtil
          .logMsg("IllegalArgumentException was successfully thrown for target "
              + sWebTarget + " as expected");
    }
  }

  /*
   * @testName: targetWithStringNullPointerExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:413;
   * 
   * @test_Strategy: jakarta.ws.rs.client.Client.target( String ) throws throws
   * NullPointerException in case the supplied argument is null.
   */
  @Test
  public void targetWithStringNullPointerExceptionTest() throws Fault {
    String sWebTarget = null;
    try {
      target = client.target(sWebTarget);
      throw new Fault("NullPointerException was not thrown for null target");
    } catch (NullPointerException e) {
      TestUtil.logMsg(
          "NullPointerException was successfully thrown for null target as expected");
    }
  }

  /*
   * @testName: targetUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:416;
   * 
   * @test_Strategy: Build a new web resource target.
   */
  @Test
  public void targetUriTest() throws Fault {
    URI uri = URI.create(getUrl("call"));
    target = client.target(uri);
    assertTrue(target != null, "WebTarget is null");
  }

  /*
   * @testName: targetWithUriNullPointerExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:416;
   * 
   * @test_Strategy: jakarta.ws.rs.client.Client.target( URI ) throws throws
   * NullPointerException in case the supplied argument is null.
   */
  @Test
  public void targetWithUriNullPointerExceptionTest() throws Fault {
    URI uri = null;
    try {
      target = client.target(uri);
      throw new Fault("NullPointerException was not thrown for null target");
    } catch (NullPointerException e) {
      TestUtil.logMsg(
          "NullPointerException was successfully thrown for null target as expected");
    }
  }

  /*
   * @testName: targetUriBuilderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:418;
   * 
   * @test_Strategy: Build a new web resource target.
   */
  @Test
  public void targetUriBuilderTest() throws Fault {
    UriBuilder builder = UriBuilder.fromUri(getUrl("call"));
    target = client.target(builder);
    assertTrue(target != null, "WebTarget is null");
  }

  /*
   * @testName: targetWithUriBuilderNullPointerExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:418;
   * 
   * @test_Strategy: jakarta.ws.rs.client.Client.target( URI ) throws throws
   * NullPointerException in case the supplied argument is null.
   */
  @Test
  public void targetWithUriBuilderNullPointerExceptionTest() throws Fault {
    UriBuilder builder = null;
    try {
      target = client.target(builder);
      throw new Fault("NullPointerException was not thrown for null target");
    } catch (NullPointerException e) {
      TestUtil.logMsg(
          "NullPointerException was successfully thrown for null target as expected");
    }
  }

  /*
   * @testName: targetLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:420;
   * 
   * @test_Strategy: Build a new web resource target.
   */
  @Test
  public void targetLinkTest() throws Fault {
    URI uri = UriBuilder.fromPath(getUrl("call")).build();
    Link link = Link.fromUri(uri).build();
    target = client.target(link);
    assertTrue(target != null, "WebTarget is null");
  }

  /*
   * @testName: targetWithLinkNullPointerExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:420;
   * 
   * @test_Strategy: jakarta.ws.rs.client.Client.target( URI ) throws throws
   * NullPointerException in case the supplied argument is null.
   */
  @Test
  public void targetWithLinkNullPointerExceptionTest() throws Fault {
    Link link = null;
    try {
      target = client.target(link);
      throw new Fault("NullPointerException was not thrown for null target");
    } catch (NullPointerException e) {
      TestUtil.logMsg(
          "NullPointerException was successfully thrown for null target as expected");
    }
  }

  // //////////////////////////////////////////////////////////////////////
  /** Check exception when calling Client#invocation() and log */
  protected static <T extends Exception> void checkInvocationException(
      Link link, Class<T> exception, String messageOnException,
      String messageNoException) throws Fault {
    Client client = ClientBuilder.newClient();
    try {
      client.invocation(link);
      throw new Fault(messageNoException);
    } catch (Exception e) {
      if (exception.isInstance(e))
        TestUtil.logMsg(messageOnException);
      else
        throw new Fault(e);
    }
  }

  protected void setClientAndWebTarget() {
    client = ClientBuilder.newClient();
    target = client.target("cts");
  }

  protected static void assertException(Class<? extends Exception> exception,
      Object object, String method, Object... args) throws Fault {

    Method m = getMethodByName(object.getClass(), method, args);
    assertTrue(m != null, "No method " + method + " for object " +
        object.getClass().getName() + " found");
    System.out.println(m);
    try {
      m.invoke(object, args);
      assertTrue(false, "Method " + method + " did not throw " +
          exception.getSimpleName());
    } catch (Exception e) {
      if (e.getCause() == null
          || !exception.isAssignableFrom(e.getCause().getClass()))
        throw new Fault(e);
      logMsg(exception.getName(), "has been successfully thrown", e.getCause());
    }
  }

  protected static Method getMethodByName(Class<?> clazz, String name,
      Object... args) {
    Method[] methods = clazz.getMethods();
    for (Method m : methods)
      if (m.getName().equals(name) && fitsMethodArguments(m, args))
        return m;
    return null;
  }

  protected static boolean fitsMethodArguments(Method method, Object... args) {
    if (method.getParameterTypes().length != args.length)
      return false;
    Class<?>[] argClass = method.getParameterTypes();
    for (int i = 0; i != argClass.length; i++) {
      if (args[i].getClass() == Class.class
          && argClass[i].getClass() != Class.class)
        return false;
      if (!argClass[i].isPrimitive()
          && !argClass[i].isAssignableFrom(args[i].getClass()))
        return false;
      if (argClass[i].isPrimitive()
          && (!(args[i] instanceof Number || args[i] instanceof Boolean)))
        return false;
    }
    return true;
  }

  protected String getUrl(String method) {
    StringBuilder url = new StringBuilder();
    url.append("http://").append(_hostname).append(":").append(_port);
    url.append("/").append(method);
    return url.toString();
  }
}
