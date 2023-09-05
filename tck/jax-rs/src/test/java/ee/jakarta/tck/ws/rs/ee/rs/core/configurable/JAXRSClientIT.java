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

package ee.jakarta.tck.ws.rs.ee.rs.core.configurable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.api.rs.core.configurable.Assertable;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.CallableProvider;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.ConfigurableObject;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.Registrar;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.SingleCheckAssertable;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

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
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = -8051302528257391040L;

  private static final int configurableCnt = 2;

  private int registeredClassesCnt = -1;

  private int registeredInstancesCnt = -1;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_core_configurable_web/resource");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/core/configurable/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_core_configurable_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, Assertable.class, CallableProvider.class, Registrar.class, SingleCheckAssertable.class, ConfigurableObject.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */

  /*
   * @testName: registerClassWriterContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:756;
   * 
   * @test_Strategy: This registration method provides the same functionality as
   * register(Class) except the JAX-RS component class is only registered as a
   * provider of the listed extension provider or meta-provider contracts.
   */
  @Test
  public void registerClassWriterContractsTest() throws Fault {
    final String content = "registerClassWriterContractsTest";

    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = getCallableEntity(content);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register((Class<?>) registerable, MessageBodyWriter.class);
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      Assertable assertable = getAssertableWithRegisteredProviderClassesOnConfigurable(
          cnt, 1);

      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Invocation i = checkConfig(registrar, assertable, classes, entity);
      Response response = i.invoke();
      response.bufferEntity();
      String responseString = response.readEntity(String.class);
      assertEquals(content, responseString, "Expected", content,
          "differs from given", response);
      logMsg(
          "sucessufully wrote Callable by provider registered on Configurable",
          Assertable.getLocation(cnt));
      // check message body reader contract
      try {
        Callable<?> callable = response.readEntity(Callable.class);
        fault("MessageBodyReader contract has been unexpectedly registered",
            callable);
      } catch (Exception e) {
        logMsg("MessageBodyReader contract has not been registered as expected",
            e);
      }
    }
  }

  /*
   * @testName: registerClassReaderContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:756;
   * 
   * @test_Strategy: This registration method provides the same functionality as
   * register(Class) except the JAX-RS component class is only registered as a
   * provider of the listed extension provider or meta-provider contracts.
   */
  @Test
  public void registerClassReaderContractsTest() throws Fault {
    final String content = "registerClassReaderContractsTest";

    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity(content, MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register((Class<?>) registerable, MessageBodyReader.class);
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      Assertable assertable = getAssertableWithRegisteredProviderClassesOnConfigurable(
          cnt, 1);
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Invocation i = checkConfig(registrar, assertable, classes, entity);
      Response response = i.invoke();
      Callable<?> callable = response.readEntity(Callable.class);
      assertEquals(content, callable.toString(), "Expected", content,
          "differs from given", response);
      logMsg(
          "sucessufully read Callable by provider registered on Configurable",
          Assertable.getLocation(cnt));
    }
  }

  /*
   * @testName: registerClassEmptyContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:756;
   * 
   * @test_Strategy: Implementations MUST ignore attempts to register a
   * component class for an empty collection of contracts via this method and
   * SHOULD raise a warning about such event.
   */
  @Test
  public void registerClassEmptyContractsTest() throws Fault {
    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity("", MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register((Class<?>) registerable, new Class<?>[] {});
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    Assertable assertable = getAssertableWithNoRegisteredProvider();
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      checkConfig(registrar, assertable, classes, entity);
      logMsg("The provider of with contracts has ben ignored as expected");
    }
  }

  /*
   * @testName: registerClassNotAssignableContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:756;
   * 
   * @test_Strategy: Contracts that are not assignable from the registered
   * component class MUST be ignored
   */
  @Test
  public void registerClassNotAssignableContractsTest() throws Fault {
    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity("", MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register((Class<?>) registerable, ClientRequestFilter.class);
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    Assertable assertable = getAssertableWithNoRegisteredProvider();
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      checkConfig(registrar, assertable, classes, entity);
      logMsg(
          "The provider with unassignable contract has ben ignored as expected");
    }
  }

  /*
   * @testName: registerClassNullContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:756;
   * 
   * @test_Strategy: Implementations MUST ignore attempts to register a
   * component class for a null collection of contracts via this method and
   * SHOULD raise a warning about such event.
   */
  @Test
  public void registerClassNullContractsTest() throws Fault {
    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity("", MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register((Class<?>) registerable, (Class<?>[]) null);
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    Assertable assertable = getAssertableWithNoRegisteredProvider();
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      checkConfig(registrar, assertable, classes, entity);
      logMsg("The provider with null contract has ben ignored as expected");
    }
  }

  /*
   * @testName: registerClassBindingPriorityFirstIsSecondTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:755;
   * 
   * @test_Strategy: This registration method provides the same functionality as
   * register(Class) except that any binding priority specified on the
   * registered JAX-RS component class via
   * 
   * @Priority annotation is overridden with the supplied bindingPriority value.
   */
  @Test
  public void registerClassBindingPriorityFirstIsSecondTest() throws Fault {
    final String content = "registerClassBindingPriorityFirstIsSecondTest";
    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity(content, MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          config.register(FirstFilter.class, 400);
          config.register(SecondFilter.class, 399);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Assertable assertable = getAssertableWithRegisteredProviderClassesOnConfigurable(
          cnt, 2);
      Invocation i = checkConfig(registrar, assertable, classes, entity);
      String response = i.invoke(String.class);
      assertEquals(FirstFilter.class.getName(), response,
          "Unexpected filter ordering, the last was", response);
      logMsg(response, "has been executed as second, as expected");
    }
  }

  /*
   * @testName: registerClassBindingPriorityFirstIsFirstTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:755;
   * 
   * @test_Strategy: This registration method provides the same functionality as
   * register(Class) except that any binding priority specified on the
   * registered JAX-RS component class via
   * 
   * @Priority annotation is overridden with the supplied bindingPriority value.
   */
  @Test
  public void registerClassBindingPriorityFirstIsFirstTest() throws Fault {
    final String content = "registerClassBindingPriorityFirstIsFirstTest";
    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity(content, MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          config.register(FirstFilter.class, 300);
          config.register(SecondFilter.class, 399);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Assertable assertable = getAssertableWithRegisteredProviderClassesOnConfigurable(
          cnt, 2);
      Invocation i = checkConfig(registrar, assertable, classes, entity);
      String response = i.invoke(String.class);
      assertEquals(SecondFilter.class.getName(), response,
          "Unexpected filter ordering, the last was", response);
      logMsg(response, "has been executed as second, as expected");
    }
  }

  /*
   * @testName: registerObjectBindingPriorityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:759;
   * 
   * @test_Strategy: Any binding priority specified on the registered JAX-RS
   * component class via
   * 
   * @Priority annotation is overridden with the supplied bindingPriority value.
   */
  @Test
  public void registerObjectBindingPriorityTest() throws Fault {
    final String content = "registerObjectBindingPriorityTest";
    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity(content, MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          config.register(new FirstFilter(), 400);
          config.register(new SecondFilter(), 399);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Assertable assertable = getAssertableWithRegisteredProviderInstancesOnConfigurable(
          cnt, 2);
      Invocation i = checkConfig(registrar, assertable, classes, entity);
      String response = i.invoke(String.class);
      assertEquals(FirstFilter.class.getName(), response,
          "Unexpected filter ordering, the last was", response);
      logMsg(response, "has been executed as second, as expected");
    }
  }

  /*
   * @testName: registerObjectWriterContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:760;
   * 
   * @test_Strategy: This registration method provides the same functionality as
   * register(Object) except the JAX-RS component class is only registered as a
   * provider of the listed extension provider or meta-provider contracts.
   */
  @Test
  public void registerObjectWriterContractsTest() throws Fault {
    final String content = "registerObjectWriterContractsTest";

    Object[] instances = createProviderInstances();
    // entity to send to a server
    Entity<?> entity = getCallableEntity(content);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register(registerable, MessageBodyWriter.class);
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      Assertable assertable = getAssertableWithRegisteredProviderInstancesOnConfigurable(
          cnt, 1);

      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Invocation i = checkConfig(registrar, assertable, instances, entity);
      Response response = i.invoke();
      response.bufferEntity();
      String responseString = response.readEntity(String.class);
      assertEquals(content, responseString, "Expected", content,
          "differs from given", response);
      logMsg(
          "sucessufully wrote Callable by provider registered on Configurable",
          Assertable.getLocation(cnt));
      // check message body reader contract
      try {
        Callable<?> callable = response.readEntity(Callable.class);
        fault("MessageBodyReader contract has been unexpectedly registered",
            callable);
      } catch (Exception e) {
        logMsg("MessageBodyReader contract has not been registered as expected",
            e);
      }
    }
  }

  /*
   * @testName: registerObjectReaderContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:760;
   * 
   * @test_Strategy: This registration method provides the same functionality as
   * register(Object) except the JAX-RS component class is only registered as a
   * provider of the listed extension provider or meta-provider contracts.
   */
  @Test
  public void registerObjectReaderContractsTest() throws Fault {
    final String content = "registerClassReaderContractsTest";

    Object[] instances = createProviderInstances();
    // entity to send to a server
    Entity<?> entity = Entity.entity(content, MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register(registerable, MessageBodyReader.class);
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      Assertable assertable = getAssertableWithRegisteredProviderInstancesOnConfigurable(
          cnt, 1);
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Invocation i = checkConfig(registrar, assertable, instances, entity);
      Response response = i.invoke();
      Callable<?> callable = response.readEntity(Callable.class);
      assertEquals(content, callable.toString(), "Expected", content,
          "differs from given", response);
      logMsg(
          "sucessufully read Callable by provider registered on Configurable",
          Assertable.getLocation(cnt));
    }
  }

  /*
   * @testName: registerObjectEmptyContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:760;
   * 
   * @test_Strategy: Implementations MUST ignore attempts to register a
   * component class for an empty collection of contracts via this method and
   * SHOULD raise a warning about such event.
   */
  @Test
  public void registerObjectEmptyContractsTest() throws Fault {
    Object[] instances = createProviderInstances();
    // entity to send to a server
    Entity<?> entity = Entity.entity("", MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register(registerable, new Class<?>[] {});
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    Assertable assertable = getAssertableWithNoRegisteredProviderInstance();
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      checkConfig(registrar, assertable, instances, entity);
      logMsg("The provider with empty contracts has ben ignored as expected");
    }
  }

  /*
   * @testName: registerObjectNotAssignableContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:760;
   * 
   * @test_Strategy: Contracts that are not assignable from the registered
   * component class MUST be ignored
   */
  @Test
  public void registerObjectNotAssignableContractsTest() throws Fault {
    Object[] instances = createProviderInstances();
    // entity to send to a server
    Entity<?> entity = Entity.entity("", MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register(registerable, ClientRequestFilter.class);
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    Assertable assertable = getAssertableWithNoRegisteredProviderInstance();
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      checkConfig(registrar, assertable, instances, entity);
      logMsg(
          "The provider withO unassignable contract has ben ignored as expected");
    }
  }

  /*
   * @testName: registerObjectNullContractsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:760;
   * 
   * @test_Strategy: Implementations MUST ignore attempts to register a
   * component class for a null collection of contracts via this method and
   * SHOULD raise a warning about such event.
   */
  @Test
  public void registerObjectNullContractsTest() throws Fault {
    Object[] instances = createProviderInstances();
    // entity to send to a server
    Entity<?> entity = Entity.entity("", MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue)
          config.register(registerable, (Class<?>[]) null);
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    Assertable assertable = getAssertableWithNoRegisteredProvider();
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      checkConfig(registrar, assertable, instances, entity);
      logMsg("The provider with null contract has ben ignored as expected");
    }
  }

  /*
   * @testName: registerClassWriterContractsInMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:989;
   * 
   * @test_Strategy: This registration method provides same functionality as
   * register(Class, Class[]) except that any binding priority specified on the
   * registered JAX-RS component class using @Priority annotation is overridden
   * for each extension provider contract type separately with an integer
   * binding priority value specified as a value in the supplied map.
   */
  @Test
  public void registerClassWriterContractsInMapTest() throws Fault {
    final String content = "registerClassWriterContractsInMapTest";

    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = getCallableEntity(content);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
          contracts.put(MessageBodyWriter.class, 100);
          config.register((Class<?>) registerable, contracts);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      Assertable assertable = getAssertableWithRegisteredProviderClassesOnConfigurable(
          cnt, 1);

      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Invocation i = checkConfig(registrar, assertable, classes, entity);
      Response response = i.invoke();
      response.bufferEntity();
      String responseString = response.readEntity(String.class);
      assertEquals(content, responseString, "Expected", content,
          "differs from given", response);
      logMsg(
          "sucessufully wrote Callable by provider registered on Configurable",
          Assertable.getLocation(cnt));
      // check message body reader contract
      try {
        Callable<?> callable = response.readEntity(Callable.class);
        fault("MessageBodyReader contract has been unexpectedly registered",
            callable);
      } catch (Exception e) {
        logMsg("MessageBodyReader contract has not been registered as expected",
            e);
      }
    }
  }

  /*
   * @testName: registerClassReaderContractsInMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:989;
   * 
   * @test_Strategy: This registration method provides same functionality as
   * register(Class, Class[]) except that any binding priority specified on the
   * registered JAX-RS component class using @Priority annotation is overridden
   * for each extension provider contract type separately with an integer
   * binding priority value specified as a value in the supplied map.
   */
  @Test
  public void registerClassReaderContractsInMapTest() throws Fault {
    final String content = "registerClassReaderContractsInMapTest";

    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity(content, MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
          contracts.put(MessageBodyReader.class, 100);
          config.register((Class<?>) registerable, contracts);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      Assertable assertable = getAssertableWithRegisteredProviderClassesOnConfigurable(
          cnt, 1);
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Invocation i = checkConfig(registrar, assertable, classes, entity);
      Response response = i.invoke();
      Callable<?> callable = response.readEntity(Callable.class);
      assertEquals(content, callable.toString(), "Expected", content,
          "differs from given", response);
      logMsg(
          "sucessufully read Callable by provider registered on Configurable",
          Assertable.getLocation(cnt));
    }
  }

  /*
   * @testName: registerClassBindingPriorityInMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:989;
   * 
   * @test_Strategy: This registration method provides same functionality as
   * register(Class, Class[]) except that any binding priority specified on the
   * registered JAX-RS component class using @Priority annotation is overridden
   * for each extension provider contract type separately with an integer
   * binding priority value specified as a value in the supplied map.
   */
  @Test
  public void registerClassBindingPriorityInMapTest() throws Fault {
    final String content = "registerClassBindingPriorityInMapTest";
    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity(content, MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
          contracts.put(ClientRequestFilter.class, 400);
          config.register(FirstFilter.class, contracts);
          contracts.clear();
          contracts.put(ClientRequestFilter.class, 300);
          config.register(SecondFilter.class, contracts);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Assertable assertable = getAssertableWithRegisteredProviderClassesOnConfigurable(
          cnt, 2);
      Invocation i = checkConfig(registrar, assertable, classes, entity);
      String response = i.invoke(String.class);
      assertEquals(FirstFilter.class.getName(), response,
          "Unexpected filter ordering, the last was", response);
      logMsg(response, "has been executed as second, as expected");
    }
  }

  /*
   * @testName: registerClassNotAssignableContractsInMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:989;
   * 
   * @test_Strategy: Contracts that are not assignable from the registered
   * component class MUST be ignored
   */
  @Test
  public void registerClassNotAssignableContractsInMapTest() throws Fault {
    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity("", MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
          contracts.put(ClientRequestFilter.class, 400);
          config.register((Class<?>) registerable, contracts);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    Assertable assertable = getAssertableWithNoRegisteredProviderInstance();
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      checkConfig(registrar, assertable, classes, entity);
      logMsg(
          "The provider with unassignable contract has ben ignored as expected");
    }
  }

  /*
   * @testName: registerObjectWriterContractsInMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:990;
   * 
   * @test_Strategy: This registration method provides same functionality as
   * register(Object, Class[]) except that any binding priority specified on the
   * registered JAX-RS component class using @Priority annotation is overridden
   * for each extension provider contract type separately with an integer
   * binding priority value specified as a value in the supplied map.
   */
  @Test
  public void registerObjectWriterContractsInMapTest() throws Fault {
    final String content = "registerObjectWriterContractsInMapTest";

    Object[] instances = createProviderInstances();
    // entity to send to a server
    Entity<?> entity = getCallableEntity(content);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
          contracts.put(MessageBodyWriter.class, 100);
          config.register(registerable, contracts);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      Assertable assertable = getAssertableWithRegisteredProviderInstancesOnConfigurable(
          cnt, 1);

      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Invocation i = checkConfig(registrar, assertable, instances, entity);
      Response response = i.invoke();
      response.bufferEntity();
      String responseString = response.readEntity(String.class);
      assertEquals(content, responseString, "Expected", content,
          "differs from given", response);
      logMsg(
          "sucessufully wrote Callable by provider registered on Configurable",
          Assertable.getLocation(cnt));
      // check message body reader contract
      try {
        Callable<?> callable = response.readEntity(Callable.class);
        fault("MessageBodyReader contract has been unexpectedly registered",
            callable);
      } catch (Exception e) {
        logMsg("MessageBodyReader contract has not been registered as expected",
            e);
      }
    }
  }

  /*
   * @testName: registerObjectReaderContractsInMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:990;
   * 
   * @test_Strategy: This registration method provides same functionality as
   * register(Object, Class[]) except that any binding priority specified on the
   * registered JAX-RS component class using @Priority annotation is overridden
   * for each extension provider contract type separately with an integer
   * binding priority value specified as a value in the supplied map.
   */
  @Test
  public void registerObjectReaderContractsInMapTest() throws Fault {
    final String content = "registerObjectReaderContractsInMapTest";

    Object[] instances = createProviderInstances();
    // entity to send to a server
    Entity<?> entity = Entity.entity(content, MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
          contracts.put(MessageBodyReader.class, 100);
          config.register(registerable, contracts);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      Assertable assertable = getAssertableWithRegisteredProviderInstancesOnConfigurable(
          cnt, 1);
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Invocation i = checkConfig(registrar, assertable, instances, entity);
      Response response = i.invoke();
      Callable<?> callable = response.readEntity(Callable.class);
      assertEquals(content, callable.toString(), "Expected", content,
          "differs from given", response);
      logMsg(
          "sucessufully read Callable by provider registered on Configurable",
          Assertable.getLocation(cnt));
    }
  }

  /*
   * @testName: registerObjectBindingPriorityInMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:990;
   * 
   * @test_Strategy: This registration method provides same functionality as
   * register(Object, Class[]) except that any binding priority specified on the
   * registered JAX-RS component class using @Priority annotation is overridden
   * for each extension provider contract type separately with an integer
   * binding priority value specified as a value in the supplied map.
   */
  @Test
  public void registerObjectBindingPriorityInMapTest() throws Fault {
    final String content = "registerObjectBindingPriorityInMapTest";
    Class<?>[] classes = createProviderClasses();
    // entity to send to a server
    Entity<?> entity = Entity.entity(content, MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
          contracts.put(ClientRequestFilter.class, 400);
          config.register(new FirstFilter(), contracts);
          contracts.clear();
          contracts.put(ClientRequestFilter.class, 300);
          config.register(new SecondFilter(), contracts);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      Assertable assertable = getAssertableWithRegisteredProviderInstancesOnConfigurable(
          cnt, 2);
      Invocation i = checkConfig(registrar, assertable, classes, entity);
      String response = i.invoke(String.class);
      assertEquals(FirstFilter.class.getName(), response,
          "Unexpected filter ordering, the last was", response);
      logMsg(response, "has been executed as second, as expected");
    }
  }

  /*
   * @testName: registerObjectNotAssignableContractsInMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:990;
   * 
   * @test_Strategy: Contracts that are not assignable from the registered
   * component class MUST be ignored
   */
  @Test
  public void registerObjectNotAssignableContractsInMapTest() throws Fault {
    Object[] instances = createProviderInstances();
    // entity to send to a server
    Entity<?> entity = Entity.entity("", MediaType.WILDCARD);

    // register only once per client build
    IncrementableRegistrar registrar = new IncrementableRegistrar(0, 1) {
      @Override
      public void register(Configurable<?> config, Object registerable) {
        if (currentValue++ == finalValue) {
          Map<Class<?>, Integer> contracts = new HashMap<Class<?>, Integer>();
          contracts.put(ClientRequestFilter.class, 400);
          config.register(registerable, contracts);
        }
      }
    };

    setResourceMethod("echo");
    // build client configurableCnt times to register provider using a
    // different
    // configurable each time
    Assertable assertable = getAssertableWithNoRegisteredProviderInstance();
    for (int cnt = 0; cnt != configurableCnt; cnt++) {
      // Check the provider is registered
      logMsg("Check on Configurable", Assertable.getLocation(cnt));
      // set we want to register the provider on Configurable
      // Assertable::LOCATION[cnt]
      registrar.setCurrentValue(0).setFinalValue(cnt);
      checkConfig(registrar, assertable, instances, entity);
      logMsg(
          "The provider with unassignable contract has ben ignored as expected");
    }
  }

  // ///////////////////////////////////////////////////////////////////////
  private Assertable getAssertableWithRegisteredProviderClassesOnConfigurable(
      final int configurableIndex, final int numberOfRegisteredClasses) {
    Assertable assertable = new SingleCheckAssertable() {
      @Override
      protected void check(Configurable<?> configurable) throws Fault {
        assertSizeAndLog(configurable);
      }

      void assertSizeAndLog(Configurable<?> config) throws Fault {
        int size = config.getConfiguration().getClasses().size();
        int shouldBe = getLocationIndex() >= configurableIndex
            ? numberOfRegisteredClasses
            : 0;
        shouldBe += registeredClassesCnt;
        assertEqualsInt(size, shouldBe,
            "unexpected number of registered classes found:", size,
            getLocation());
        logMsg("Found", size, "provider(s) as expected");
      }
    };
    return assertable;
  }

  private Assertable getAssertableWithRegisteredProviderInstancesOnConfigurable(
      final int configurableIndex, final int numberOfRegisteredInstances) {
    Assertable assertable = new SingleCheckAssertable() {
      @Override
      protected void check(Configurable<?> configurable) throws Fault {
        assertSizeAndLog(configurable);
      }

      void assertSizeAndLog(Configurable<?> config) throws Fault {
        int size = config.getConfiguration().getInstances().size();
        int shouldBe = getLocationIndex() >= configurableIndex
            ? numberOfRegisteredInstances
            : 0;
        shouldBe += registeredInstancesCnt;
        assertEqualsInt(size, shouldBe,
            "unexpected number of registered classes found:", size,
            getLocation());
        logMsg("Found", size, "provider(s) as expected");
      }
    };
    return assertable;
  }

  private Assertable getAssertableWithNoRegisteredProvider() {
    return getAssertableWithRegisteredProviderClassesOnConfigurable(0, 0);
  }

  private Assertable getAssertableWithNoRegisteredProviderInstance() {
    return getAssertableWithRegisteredProviderInstancesOnConfigurable(0, 0);
  }

  /**
   * Provider has to not be anonymous class, because we need @Provider
   * annotation there
   */
  protected Object[] createProviderInstances() {
    Object[] instances = new CallableProvider[] { new CallableProvider() {
    }, new CallableProvider() {
    } };
    return instances;
  }

  protected static Class<?>[] createProviderClasses() {
    Class<?>[] classes = new Class<?>[] { CallableProvider.class,
        CallableProvider.class };
    return classes;
  }

  protected Invocation checkConfig(Registrar registrar, Assertable assertable,
      Object[] registerables, Entity<?> entity) throws Fault {
    Client client = ClientBuilder.newClient();
    Configuration config = client.getConfiguration();
    registeredClassesCnt = config.getClasses().size();
    registeredInstancesCnt = config.getInstances().size();
    logMsg("Already registered", registeredClassesCnt, "classes");
    logMsg("Already registered", registeredInstancesCnt, "instances");

    register(registrar, client, registerables[0]);
    assertable.check1OnClient(client);
    assertable.incrementLocation();

    WebTarget target = client.target(getAbsoluteUrl());
    register(registrar, target, registerables[1]);
    assertable.check2OnTarget(target);
    assertable.incrementLocation();

    Invocation.Builder builder = target.request();
    Invocation invocation = builder.buildPost(entity);
    return invocation;
  }

  protected void register(Registrar registrar, Configurable<?> config,
      Object registerable) {
    registrar.register(config, registerable);
  }

  protected class IncrementableRegistrar extends Registrar {
    int currentValue;

    int finalValue;

    int getFinalValue() {
      return finalValue;
    }

    IncrementableRegistrar setFinalValue(int finalValue) {
      this.finalValue = finalValue;
      return this;
    }

    IncrementableRegistrar(int initValue, int finalValue) {
      currentValue = initValue;
      this.finalValue = finalValue;
    }

    int getCurrentValue() {
      return currentValue;
    }

    IncrementableRegistrar setCurrentValue(int set) {
      currentValue = set;
      return this;
    }
  }

  private void setResourceMethod(String method) {
    setContextRoot(getContextRoot() + "/" + method);
  }

  private static Entity<?> getCallableEntity(final String content) {
    Entity<?> entity = Entity.entity(CallableProvider.createCallable(content),
        MediaType.WILDCARD_TYPE);
    return entity;
  }

}
