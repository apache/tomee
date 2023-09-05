/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.jaxrs31.spec.extensions;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.Response;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.client.JdkLoggingFilter;
import ee.jakarta.tck.ws.rs.jaxrs21.spec.completionstage.CompletionStageResource;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = 31L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_jaxrs31_spec_jdkservices_web");
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
    StringAsset dynamicFeatureServiceFile = new StringAsset(TckDynamicFeature.class.getName());
    StringAsset featureServiceFile = new StringAsset(TckFeature.class.getName());
    String prefix = "classes/META-INF/services/";

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_jaxrs31_spec_jdkservices_web.war");
    archive.addClasses(TSAppConfig.class, DynamicFeatureResource.class,
            TckFeature.class, TckFeature.TckFeatureFilter.class,
            TckDynamicFeature.class, TckDynamicFeature.TckDynamicFeatureFilter.class);
    archive.addAsWebInfResource(dynamicFeatureServiceFile, prefix + DynamicFeature.class.getName());
    archive.addAsWebInfResource(featureServiceFile, prefix + Feature.class.getName());
    return archive;

  }

  /* Run test */

  /*
   * @testName: featureIsRegisteredTest
   * 
   * @assertion_ids: JAXRS:SPEC:137;
   * 
   * @test_Strategy:
   */
  @Test
  public void featureIsRegisteredTest() throws Fault {
    try (Response response = ClientBuilder.newClient().target(getAbsoluteUrl("staticFeature")).request().get()) {
      assertEquals(200, response.getStatus());
      assertEquals(TckFeature.class.getName(), response.readEntity(String.class));
    } catch (Exception e) {
      fault(e);
    }
  }

  /*
   * @testName: dynamicFeatureIsRegisteredTest
   *
   * @assertion_ids: JAXRS:SPEC:137;
   *
   * @test_Strategy:
   */
  @Test
  public void dynamicFeatureIsRegisteredTest() throws Fault {
    try (Response response = ClientBuilder.newClient().target(getAbsoluteUrl("dynamicFeature")).request().get()) {
      assertEquals(200, response.getStatus());
      assertEquals(TckDynamicFeature.class.getName(), response.readEntity(String.class));
    } catch (Exception e) {
      fault(e);
    }
  }
}
