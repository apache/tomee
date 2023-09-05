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

package ee.jakarta.tck.ws.rs.jaxrs21.api.client.invocationbuilder;

import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.webclient.TestFailureException;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.client.ClientBuilder;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
 */
/**
 * @since 2.1
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 21L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs21_api_client_invocationbuilder_web/resource");
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

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs21_api_client_invocationbuilder_web.war");
    archive.addClasses(TCKRxInvoker.class, TCKRxInvokerProvider.class, 
      JAXRSCommonClient.class,
      TestFailureException.class);
    return archive;

  }


  /*
   * @testName: testRxClassGetsClassInstance
   * 
   * @assertion_ids: JAXRS:JAVADOC:1163; JAXRS:JAVADOC:1189;
   * 
   * @test_Strategy: Access a reactive invoker based on provider RxInvoker
   * subclass. Note that corresponding RxInvokerProvider must be registered to
   * client runtime.
   */
  @Test
  public void testRxClassGetsClassInstance() throws Fault {
    TCKRxInvoker invoker = ClientBuilder.newClient()
        .register(TCKRxInvokerProvider.class).target("somewhere").request()
        .rx(TCKRxInvoker.class);
    assertNotNull(invoker, "rx did not instantiated the invoker");
    assertEquals(invoker.getClass(), TCKRxInvoker.class,
        "Custom rxInvoker has not been created");

    invoker = ClientBuilder.newClient().target("somewhere")
        .register(TCKRxInvokerProvider.class).request().rx(TCKRxInvoker.class);
    assertNotNull(invoker, "rx did not instantiated the invoker");
    assertEquals(invoker.getClass(), TCKRxInvoker.class,
        "Custom rxInvoker has not been created");

    System.out.println("Custom rxInvoker has been created as expected");
  }

  /*
   * @testName: testRxClassThrowsWhenNotRegistered
   * 
   * @assertion_ids: JAXRS:JAVADOC:1163; JAXRS:JAVADOC:1189;
   * 
   * @test_Strategy: Access a reactive invoker based on provider RxInvoker
   * subclass. Note that corresponding RxInvokerProvider must be registered to
   * client runtime.
   */
  @Test
  public void testRxClassThrowsWhenNotRegistered() throws Fault {
    try {
      ClientBuilder.newClient().target("somewhere").request()
          .rx(TCKRxInvoker.class);
      System.out.println(
          "Illegal state exception has not been thrown when no provider is registered");
    } catch (IllegalStateException e) {
      System.out.println(
          "Illegal state exception has been thrown when no provider is registered as expected");
    }
  }

  /*
   * @testName: testRxClassExceutorServiceGetsClassInstance
   * 
   * @assertion_ids: JAXRS:JAVADOC:1163; JAXRS:JAVADOC:1189;
   * 
   * @test_Strategy: Access a reactive invoker based on provider RxInvoker
   * subclass. Note that corresponding RxInvokerProvider must be registered to
   * client runtime.
   */
  @Test
  public void testRxClassExceutorServiceGetsClassInstance() throws Fault {
    TCKRxInvoker invoker = ClientBuilder.newClient()
        .register(TCKRxInvokerProvider.class).target("somewhere").request()
        .rx(TCKRxInvoker.class);
    assertNotNull(invoker, "rx did not instantiated the invoker");
    assertEquals(invoker.getClass(), TCKRxInvoker.class,
        "Custom rxInvoker has not been created");

    invoker = ClientBuilder.newClient().target("somewhere")
        .register(TCKRxInvokerProvider.class).request().rx(TCKRxInvoker.class);
    assertNotNull(invoker, "rx did not instantiated the invoker");
    assertEquals(invoker.getClass(), TCKRxInvoker.class,
        "Custom rxInvoker has not been created");

    System.out.println("Custom rxInvoker has been created as expected");
  }

  /*
   * @testName: testRxClassExecutorServiceThrowsWhenNotRegistered
   * 
   * @assertion_ids: JAXRS:JAVADOC:1163; JAXRS:JAVADOC:1189;
   * 
   * @test_Strategy: Access a reactive invoker based on provider RxInvoker
   * subclass. Note that corresponding RxInvokerProvider must be registered to
   * client runtime.
   */
  @Test
  public void testRxClassExecutorServiceThrowsWhenNotRegistered() throws Fault {
    try {
      ClientBuilder.newClient().target("somewhere").request()
          .rx(TCKRxInvoker.class);
      System.out.println(
          "Illegal state exception has not been thrown when no provider is registered");
    } catch (IllegalStateException e) {
      System.out.println(
          "Illegal state exception has been thrown when no provider is registered as expected");
    }
  }
}
