/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.configuration;

import java.util.LinkedList;
import java.util.List;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.api.rs.core.configurable.Assertable;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.CallableProvider;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.ConfigurableObject;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.Registrar;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.SingleCheckAssertable;
import ee.jakarta.tck.ws.rs.ee.rs.core.configurable.Resource;
import ee.jakarta.tck.ws.rs.ee.rs.core.configurable.TSAppConfig;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.MediaType;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * <p>
 * These tests follow the intended JAXRS rule to enable Features in the order of
 * them being registered. By this rule, it is possible to check the previous
 * Feature has been / has not been enabled.
 * </p>
 * 
 * <p>
 * The isEnabled works only in runtime which has its own version of
 * configuration, i.e. it does not work on Configurable.getConfiguration()
 * </p>
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = 7215781408688132392L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_core_configuration_web/resource/echo");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/core/configuration/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_core_configuration_web.war");
    archive.addClasses(Resource.class,
      TSAppConfig.class,
      Assertable.class,
      Registrar.class,
      CallableProvider.class,
      SingleCheckAssertable.class,
      ConfigurableObject.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */

  /*
   * @testName: isEnabledFeatureReturningFalseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:999; JAXRS:JAVADOC:1003;
   * 
   * @test_Strategy: Check if a feature instance of featureClass class has been
   * previously enabled in the runtime configuration context.
   * 
   * Returns true if the feature was successfully enabled, false otherwise
   */
  @Test
  public void isEnabledFeatureReturningFalseTest() throws Fault {
    final CheckingFeature feature1 = new FeatureReturningFalse1();
    final CheckingFeature feature2 = new FeatureReturningFalse2();
    final CheckingFeature feature3 = new FeatureReturningFalse3();
    final CheckingFeature feature4 = new FeatureReturningFalse4();

    feature1.addDisabledFeatures(feature2, feature3, feature4)
        .setName("feature1");
    feature2.addDisabledFeatures(feature1, feature3, feature4)
        .setName("feature2");
    feature3.addDisabledFeatures(feature1, feature2, feature4)
        .setName("feature3");
    feature4.addDisabledFeatures(feature1, feature2, feature3)
        .setName("feature4");

    Assertable assertable = new Assertable() {
      @Override
      public void check1OnClient(Client client) throws Fault {
        assertIsDisabled(client, feature1);
        assertIsDisabled(client, feature2);
        assertIsDisabled(client, feature3);
        assertIsDisabled(client, feature4);
      }

      @Override
      public void check2OnTarget(WebTarget target) throws Fault {
        assertIsDisabled(target, feature1);
        assertIsDisabled(target, feature2);
        assertIsDisabled(target, feature3);
        assertIsDisabled(target, feature4);
      }

      void assertIsDisabled(Configurable<?> config, CheckingFeature feature)
          throws Fault {
        boolean isEnabled = config.getConfiguration().isEnabled(feature);
        assertFalse(isEnabled, "Feature"+ feature.getName()+
            "is unexpectedly enabled"+ getLocation());
        logMsg("No feature enabled as expected", getLocation());
      }
    };
    Object[] instances = { feature1, feature2, feature3, feature4 };
    checkConfig(assertable, instances);
    logMsg(
        "The provider with unassignable contract has ben ignored as expected");
  }

  /*
   * @testName: isEnabledFeatureReturningTrueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:999; JAXRS:JAVADOC:1003;
   * 
   * @test_Strategy: Check if a feature instance of featureClass class has been
   * previously enabled in the runtime configuration context.
   * 
   * Returns true if the feature was successfully enabled, false otherwise
   */
  @Test
  public void isEnabledFeatureReturningTrueTest() throws Fault {
    final CheckingFeature feature1 = new FeatureReturningTrue1();
    final CheckingFeature feature2 = new FeatureReturningTrue2();
    final CheckingFeature feature3 = new FeatureReturningTrue3();
    final CheckingFeature feature4 = new FeatureReturningTrue4();

    feature1.addDisabledFeatures(feature2, feature3, feature4)
        .setName("feature1");
    feature2.addDisabledFeatures(feature3, feature4)
        .addEnabledFeatures(feature1).setName("feature2");
    feature3.addDisabledFeatures(feature4)
        .addEnabledFeatures(feature1, feature2).setName("feature3");
    feature4.addEnabledFeatures(feature1, feature2, feature3)
        .setName("feature4");

    Assertable assertable = new Assertable() {
      @Override
      public void check1OnClient(Client client) throws Fault {
        assertIsRegistered(client, feature1);
        assertIsNotRegistered(client, feature2);
        assertIsNotRegistered(client, feature3);
        assertIsNotRegistered(client, feature4);
      }

      @Override
      public void check2OnTarget(WebTarget target) throws Fault {
        assertIsRegistered(target, feature1);
        assertIsRegistered(target, feature2);
        assertIsNotRegistered(target, feature3);
        assertIsNotRegistered(target, feature4);
      }

      void assertIsRegistered(Configurable<?> config, CheckingFeature feature)
          throws Fault {
        Configuration configuration = config.getConfiguration();
        assertTrue(configuration.isRegistered(feature), "Feature"+
            feature.getName()+ "is NOT registered"+ getLocation());
        logMsg("Feature", feature.getName(), "registered as expected",
            getLocation());
      }

      void assertIsNotRegistered(Configurable<?> config,
          CheckingFeature feature) throws Fault {
        Configuration configuration = config.getConfiguration();
        assertFalse(configuration.isRegistered(feature), "Feature"+
            feature.getName()+ "is unexpectedly registered"+ getLocation());
        logMsg("Feature", feature.getName(), "NOT registered as expected",
            getLocation());
      }

    };
    Object[] instances = { feature1, feature2, feature3, feature4 };
    checkConfig(assertable, instances);
    logMsg(
        "The provider with unassignable contract has ben ignored as expected");
  }

  /*
   * @testName: isEnabledClassReturningFalseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1000; JAXRS:JAVADOC:1003;
   * 
   * @test_Strategy: Check if a feature instance of featureClass class has been
   * previously enabled in the runtime configuration context.
   * 
   * Returns true if the feature was successfully enabled, false otherwise
   */
  @SuppressWarnings("unchecked")
  @Test
  public void isEnabledClassReturningFalseTest() throws Fault {
    final CheckingFeature feature1 = new FeatureReturningFalse1();
    final CheckingFeature feature2 = new FeatureReturningFalse2();
    final CheckingFeature feature3 = new FeatureReturningFalse3();
    final CheckingFeature feature4 = new FeatureReturningFalse4();

    feature1
        .addDisabledClasses(FeatureReturningFalse2.class,
            FeatureReturningFalse3.class, FeatureReturningFalse4.class)
        .setName("feature1");
    feature2
        .addDisabledClasses(FeatureReturningFalse1.class,
            FeatureReturningFalse3.class, FeatureReturningFalse4.class)
        .setName("feature2");
    feature3
        .addDisabledClasses(FeatureReturningFalse1.class,
            FeatureReturningFalse2.class, FeatureReturningFalse4.class)
        .setName("feature3");
    feature4
        .addDisabledClasses(FeatureReturningFalse1.class,
            FeatureReturningFalse2.class, FeatureReturningFalse3.class)
        .setName("feature4");

    Assertable assertable = new Assertable() {
      @Override
      public void check1OnClient(Client client) throws Fault {
        assertIsDisabled(client, feature1);
        assertIsDisabled(client, feature2);
        assertIsDisabled(client, feature3);
        assertIsDisabled(client, feature4);
      }

      @Override
      public void check2OnTarget(WebTarget target) throws Fault {
        assertIsDisabled(target, feature1);
        assertIsDisabled(target, feature2);
        assertIsDisabled(target, feature3);
        assertIsDisabled(target, feature4);
      }

      void assertIsDisabled(Configurable<?> config, CheckingFeature feature)
          throws Fault {
        boolean isEnabled = config.getConfiguration().isEnabled(feature);
        assertFalse(isEnabled, "Feature"+ feature.getName()+
            "is unexpectedly enabled"+ getLocation());
        logMsg("No feature enabled as expected", getLocation());
      }
    };
    Object[] instances = { feature1, feature2, feature3, feature4 };
    checkConfig(assertable, instances);
    logMsg(
        "The provider with unassignable contract has ben ignored as expected");
  }

  /*
   * @testName: isEnabledFeatureClassReturningTrueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1000; JAXRS:JAVADOC:1003;
   * 
   * @test_Strategy: Check if a feature instance of featureClass class has been
   * previously enabled in the runtime configuration context.
   * 
   * Returns true if the feature was successfully enabled, false otherwise
   */
  @SuppressWarnings("unchecked")
  @Test
  public void isEnabledFeatureClassReturningTrueTest() throws Fault {
    final CheckingFeature feature1 = new FeatureReturningTrue1();
    final CheckingFeature feature2 = new FeatureReturningTrue2();
    final CheckingFeature feature3 = new FeatureReturningTrue3();
    final CheckingFeature feature4 = new FeatureReturningTrue4();

    feature1
        .addDisabledClasses(FeatureReturningTrue2.class,
            FeatureReturningTrue3.class, FeatureReturningTrue4.class)
        .setName("feature1");
    feature2
        .addDisabledClasses(FeatureReturningTrue3.class,
            FeatureReturningTrue4.class)
        .addEnabledClasses(FeatureReturningTrue1.class).setName("feature2");
    feature3.addDisabledClasses(FeatureReturningTrue4.class)
        .addEnabledClasses(FeatureReturningTrue1.class,
            FeatureReturningTrue2.class)
        .setName("feature3");
    feature4
        .addEnabledClasses(FeatureReturningTrue1.class,
            FeatureReturningTrue2.class, FeatureReturningTrue3.class)
        .setName("feature4");

    Assertable assertable = new Assertable() {
      @Override
      public void check1OnClient(Client client) throws Fault {
        assertIsRegistered(client, feature1);
        assertIsNotRegistered(client, feature2);
        assertIsNotRegistered(client, feature3);
        assertIsNotRegistered(client, feature4);
      }

      @Override
      public void check2OnTarget(WebTarget target) throws Fault {
        assertIsRegistered(target, feature1);
        assertIsRegistered(target, feature2);
        assertIsNotRegistered(target, feature3);
        assertIsNotRegistered(target, feature4);
      }

      void assertIsRegistered(Configurable<?> config, CheckingFeature feature)
          throws Fault {
        Configuration configuration = config.getConfiguration();
        assertTrue(configuration.isRegistered(feature), "Feature"+
            feature.getName()+ "is NOT registered"+ getLocation());
        logMsg("Feature", feature.getName(), "registered as expected",
            getLocation());
      }

      void assertIsNotRegistered(Configurable<?> config,
          CheckingFeature feature) throws Fault {
        Configuration configuration = config.getConfiguration();
        assertFalse(configuration.isRegistered(feature), "Feature"+
            feature.getName()+ "is unexpectedly registered"+ getLocation());
        logMsg("Feature", feature.getName(), "NOT registered as expected",
            getLocation());
      }

    };
    Object[] instances = { feature1, feature2, feature3, feature4 };
    checkConfig(assertable, instances);
    logMsg(
        "The provider with unassignable contract has ben ignored as expected");
  }

  // ///////////////////////////////////////////////////////////////////////

  /**
   * Check on every possible setting of configuration by a Feature or a
   * singleton
   * 
   */
  protected void checkConfig(Assertable assertable, Object[] registerables)
      throws Fault {
    checkConfig(new Registrar(), assertable, registerables);
  }

  protected void checkConfig(Registrar registrar, Assertable assertable,
      Object[] registerables) throws Fault {
    Entity<String> entity = Entity.entity("echo", MediaType.WILDCARD_TYPE);

    Client client = ClientBuilder.newClient();
    logMsg("Registering on Client");
    register(registrar, client, registerables[0]);

    WebTarget target = client.target(getAbsoluteUrl());
    logMsg("Registering on WebTarget");
    register(registrar, target, registerables[1]);

    Invocation.Builder builder = target.request();
    Invocation invocation = builder.buildPost(entity);

    String response = invocation.invoke(String.class);
    assertEquals(entity.getEntity(), response, "Unexpected response received",
        response);

    assertable.check1OnClient(client);
    assertable.incrementLocation();
    assertable.check2OnTarget(target);
  }

  protected void register(Registrar registrar, Configurable<?> config,
      Object registerable) {
    registrar.register(config, registerable);
  }

  // //////////////////////////////////////////////////////////////////////
  private abstract class NamedFeature implements Feature {
    String name;

    public String getName() {
      return name;
    }

    public NamedFeature setName(String name) {
      this.name = name;
      return this;
    }
  }

  class CheckingFeature extends NamedFeature {
    List<NamedFeature> enabledFeatures = new LinkedList<NamedFeature>();

    List<NamedFeature> disabledFeatures = new LinkedList<NamedFeature>();

    List<Class<? extends NamedFeature>> enabledFeatureClasses = new LinkedList<Class<? extends NamedFeature>>();

    List<Class<? extends NamedFeature>> disabledFeatureClasses = new LinkedList<Class<? extends NamedFeature>>();

    @Override
    public boolean configure(FeatureContext context) {
      for (NamedFeature feature : enabledFeatures) {
        if (!context.getConfiguration().isEnabled(feature))
          throw new RuntimeException(
              "Feature " + feature.getName() + " has NOT been enabled");
        logMsg("Feature", feature.getName(), "has been enabled as expected");
      }
      for (NamedFeature feature : disabledFeatures) {
        if (context.getConfiguration().isEnabled(feature))
          throw new RuntimeException("Feature " + feature.getName()
              + " has been unexpectedly enabled");
        logMsg("Feature", feature.getName(),
            "has NOT been enabled as expected");
      }
      for (Class<? extends NamedFeature> feature : enabledFeatureClasses) {
        if (!context.getConfiguration().isEnabled(feature))
          throw new RuntimeException(
              "Feature " + feature.getName() + " has NOT been enabled");
        logMsg("Feature", feature.getName(), "has been enabled as expected");
      }
      for (Class<? extends NamedFeature> feature : disabledFeatureClasses) {
        if (context.getConfiguration().isEnabled(feature))
          throw new RuntimeException("Feature " + feature.getName()
              + " has been unexpectedly enabled");
        logMsg("Feature", feature.getName(),
            "has NOT been enabled as expected");
      }
      return true;
    }

    public CheckingFeature addEnabledFeatures(NamedFeature... features) {
      if (features != null)
        for (NamedFeature feature : features)
          enabledFeatures.add(feature);
      return this;
    }

    public CheckingFeature addDisabledFeatures(NamedFeature... features) {
      if (features != null)
        for (NamedFeature feature : features)
          disabledFeatures.add(feature);
      return this;
    }

    public CheckingFeature addEnabledClasses(
        Class<? extends NamedFeature>... features) {
      if (features != null)
        for (Class<? extends NamedFeature> feature : features)
          enabledFeatureClasses.add(feature);
      return this;
    }

    public CheckingFeature addDisabledClasses(
        Class<? extends NamedFeature>... features) {
      if (features != null)
        for (Class<? extends NamedFeature> feature : features)
          disabledFeatureClasses.add(feature);
      return this;
    }
  }

  private class FeatureReturningFalse extends CheckingFeature {
    @Override
    public boolean configure(FeatureContext context) {
      super.configure(context);
      // false returning feature is not to be registered
      return false;
    }
  }

  private class FeatureReturningTrue extends CheckingFeature {
    @Override
    public boolean configure(FeatureContext context) {
      return super.configure(context);
    }
  }

  private class FeatureReturningFalse1 extends FeatureReturningFalse {
  };

  private class FeatureReturningFalse2 extends FeatureReturningFalse {
  };

  private class FeatureReturningFalse3 extends FeatureReturningFalse {
  };

  private class FeatureReturningFalse4 extends FeatureReturningFalse {
  };

  private class FeatureReturningTrue1 extends FeatureReturningTrue {
  };

  private class FeatureReturningTrue2 extends FeatureReturningTrue {
  };

  private class FeatureReturningTrue3 extends FeatureReturningTrue {
  };

  private class FeatureReturningTrue4 extends FeatureReturningTrue {
  };

}
