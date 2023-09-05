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

package ee.jakarta.tck.ws.rs.spec.client.webtarget;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanEntityProvider;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configuration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */

public class JAXRSClientIT extends JAXRSCommonClient {

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_client_webtarget_web/resource");
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
   * @testName: imutableWithRespectToUriMatrixPathTest
   * 
   * @assertion_ids: JAXRS:SPEC:66;
   * 
   * @test_Strategy: WebTarget instances are immutable with respect to their URI
   * (or URI template): methods for specifying additional path segments and
   * parameters return a new instance of WebTarget.
   */
  @Test
  public void imutableWithRespectToUriMatrixPathTest() throws Fault {
    IteratedList<WebTarget> targets = new IteratedList<WebTarget>(
        WebTarget.class);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("");
    targets.add(target);

    targets.doWithAll("matrixParam", "", new String[] { "" });
    assertFaultEqualWebTargets(targets);
    targets.doWithAll("matrixParam", "matrix", new String[] { "st" });
    assertFaultEqualWebTargets(targets);
    TestUtil.logMsg("checked matrixParam() method");

    targets.doWithAll("path", "");
    assertFaultEqualWebTargets(targets);
    targets.doWithAll("path", "/");
    assertFaultEqualWebTargets(targets);
    targets.doWithAll("path", "path");
    assertFaultEqualWebTargets(targets);
    targets.doWithAll("path", "path/path/path");
    assertFaultEqualWebTargets(targets);
    TestUtil.logMsg("checked path() method");
  }

  /*
   * @testName: imutableWithRespectToUriQueryResolveTemplateTest
   * 
   * @assertion_ids: JAXRS:SPEC:66;
   * 
   * @test_Strategy: WebTarget instances are immutable with respect to their URI
   * (or URI template): methods for specifying additional path segments and
   * parameters return a new instance of WebTarget.
   */
  @Test
  public void imutableWithRespectToUriQueryResolveTemplateTest() throws Fault {
    IteratedList<WebTarget> targets = new IteratedList<WebTarget>(
        WebTarget.class);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("");
    targets.add(target);

    targets.doWithAll("queryParam", "", new String[] { "" });
    assertFaultEqualWebTargets(targets);
    targets.doWithAll("queryParam", "path", new String[] { "xyz" });
    assertFaultEqualWebTargets(targets);
    targets.doWithAll("queryParam", "xyz", new String[] { "path" });
    assertFaultEqualWebTargets(targets);
    TestUtil.logMsg("checked queryParam() method");

    targets.doWithAll("resolveTemplateFromEncoded", "", "");
    assertFaultEqualWebTargets(targets);
    targets.doWithAll("resolveTemplateFromEncoded", "path", "xyz");
    assertFaultEqualWebTargets(targets);
    targets.doWithAll("resolveTemplateFromEncoded", "path/path/path", "");
    assertFaultEqualWebTargets(targets);
    TestUtil.logMsg("checked resolveTemplateFromEncoded() method");

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("", "path");
    targets.doWithAll("resolveTemplates", params);
    assertFaultEqualWebTargets(targets);
    params = new HashMap<String, Object>();
    params.put("path", "xyz");
    targets.doWithAll("resolveTemplates", params);
    assertFaultEqualWebTargets(targets);
    TestUtil.logMsg("checked resolveTemplates() method");

    targets.doWithAll("resolveTemplatesFromEncoded", params);
    assertFaultEqualWebTargets(targets);
    params = new HashMap<String, Object>();
    params.put("path", "xyz");
    targets.doWithAll("resolveTemplatesFromEncoded", params);
    assertFaultEqualWebTargets(targets);
    TestUtil.logMsg("checked resolveTemplatesFromEncoded() method");
  }

  /*
   * @testName: mutableWithRespectToConfigTest
   * 
   * @assertion_ids: JAXRS:SPEC:67;
   * 
   * @test_Strategy: However, WebTarget instances are mutable with respect to
   * their configuration. Thus, configuring a WebTarget does not create new
   * instances
   */
  @Test
  public void mutableWithRespectToConfigTest() throws Fault {
    // check no WebTarget is returned from configuration
    // cannot check subclass of Configuration if any because
    // that actually can return WebTarget, which is not a new instance.
    // That is not possible to check, do not know the values to be put
    // into a method as arguments
    // In configuration, it is possible to check that it is not a new
    // instance since the configuration is available by the time of the test
    // creation
    for (Method m : Configuration.class.getMethods()) {
      Class<?> ret = m.getReturnType();
      if (WebTarget.class.isAssignableFrom(ret))
        throw new Fault("Webterget instance created from configuration");
    }
  }

  /*
   * @testName: deepCopyConfigWebTargetLevelTest
   * 
   * @assertion_ids: JAXRS:SPEC:68; JAXRS:SPEC:72; JAXRS:JAVADOC:988;
   * 
   * @test_Strategy: Note that changes to hello's configuration do not affect
   * base, i.e. configuration inheritance requires performing a deep copy of the
   * configuration.
   * 
   * The following Client API types are configurable: Client, Invocation,
   * Invocation.Builder and WebTarget.
   * 
   * Get access to the underlying Configuration configuration.
   */
  @Test
  public void deepCopyConfigWebTargetLevelTest() throws Fault {
    Client client = ClientBuilder.newClient();
    Configuration config = client.getConfiguration();
    int registeredInstances = config.getInstances().size();
    // WebTarget level inheritance
    WebTarget target1 = client.target("");
    WebTarget target2 = client.target("");
    target1.register(new StringBeanEntityProvider());
    config = target2.getConfiguration();
    assertTrue(config.getInstances().size() == registeredInstances,
        "configuration() does not perform deep copy");
  }

  /*
   * @testName: deepCopyConfigClientLevelTest
   * 
   * @assertion_ids: JAXRS:SPEC:68; JAXRS:SPEC:72;
   * 
   * @test_Strategy: Note that changes to hello's configuration do not affect
   * base, i.e. configuration inheritance requires performing a deep copy of the
   * configuration.
   * 
   * The following Client API types are configurable: Client, Invocation,
   * Invocation.Builder and WebTarget.
   */
  @Test
  public void deepCopyConfigClientLevelTest() throws Fault {
    Client client = ClientBuilder.newClient();
    // Client level inheritance
    client.property("test", "test");
    Configuration conf2 = ClientBuilder.newClient().getConfiguration();
    Object o = conf2.getProperty("test");
    assertNull(o, "configuration() does not perform deep copy, o=", o);
  }

  /*
   * @testName: webTargetConfigNotImpactClientTest
   * 
   * @assertion_ids: JAXRS:SPEC:74;
   * 
   * @test_Strategy: However, any additional changes to the instance of
   * WebTarget will not impact the Client's configuration and vice versa.
   */
  @Test
  public void webTargetConfigNotImpactClientTest() throws Fault {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("resource");
    target.property("any", "some");
    Object property = client.getConfiguration().getProperty("any");
    assertTrue(property == null, "WebTarget config impacts Client config");

    client.property("some", "any");
    property = target.getConfiguration().getProperty("some");
    assertTrue(property == null, "Client config impacts WebTarget config");
  }

  /**
   * Assert when two web targets are equal
   */
  void assertFaultEqualWebTargets(List<WebTarget> t) throws Fault {
    TestUtil.logMsg("Testing " + t.size() + " WebTargets");
    for (int i = 0; i != t.size(); i++)
      for (int j = i + 1; j != t.size(); j++)
      assertTrue(t.get(i) != t.get(j), "WebTargets"+ t.get(i).toString()+
            "and"+ t.get(j).toString()+ "are equal!");
  }

}
