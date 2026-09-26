/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.logging;

import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.openejb.arquillian.common.ArquillianUtil;
import org.apache.openejb.log.LoggerCreator;
import org.apache.openejb.server.rest.RESTService;
import org.apache.openejb.util.JuliLogStream;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.LogStreamAsync;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.reflection.Reflections;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;

import jakarta.ejb.Singleton;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static org.junit.Assume.assumeTrue;

public class LoggingJAXRSCommons {

    // the tests reflect into the OpenEJB logging internals of the JVM they run in,
    // inherited by the subclasses and evaluated before Arquillian deploys the archive
    @ClassRule
    public static final ExternalResource EMBEDDED_ONLY = new ExternalResource() {
        @Override
        protected void before() {
            assumeTrue("needs the container in the test JVM",
                    ArquillianUtil.isCurrentAdapter("tomee-embedded"));
        }
    };

    // static: filled by LogCapture during the deployment, read by the in-container test
    protected static Collection<String> msgs;
    private static Handler handler;

    protected boolean assertJAXRSConfiguration() {
        synchronized (LoggingJAXRSCommons.class) {
            final Iterator<String> iterator = msgs.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().contains("Registered JAX-RS Configuration:")) {
                    return true;
                }
            }
            return false;
        }
    }

    protected static java.util.logging.Logger getLooger() throws Exception {
        final Logger logger = Logger.getInstance(LogCategory.OPENEJB_RS, RESTService.class);
        Object stream = Reflections.get(logger, "logStream");
        if (LogStreamAsync.class.isInstance(stream)) { // not async in TomEE
            stream = Reflections.get(stream, "ls");
        }
        final JuliLogStream ls = JuliLogStream.class.cast(stream);
        final LoggerCreator julCreator = LoggerCreator.class.cast(Reflections.get(ls, "logger"));
        return julCreator.call();
    }

    protected static WebArchive archive(final Class<?> test) {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(LoggingJAXRSCommons.class, test)
                // addClasses() brings the member classes too, each test adds back the ones its module uses
                .deleteClasses(LogginTestBean.class, LogginTestApplication.class);
    }

    protected static WebArchive getEjbModule(final WebArchive war, final String pojoDeploymentClassName) {
        war.addClass(LogginTestBean.class)
                .addAsWebInfResource(new StringAsset(
                        "<resources>\n" +
                        "  <Service id=\"xml\" class-name=\"" + JAXBElementProvider.class.getName() + "\">\n" +
                        "    eventHandler = $handler\n" +
                        "  </Service>\n" +
                        "</resources>\n"), "resources.xml");

        if (pojoDeploymentClassName != null) {
            war.addAsWebInfResource(new StringAsset(
                    "<openejb-jar>\n" +
                    "  <pojo-deployment class-name=\"" + pojoDeploymentClassName + "\">\n" +
                    "    <properties>\n" +
                    "      cxf.jaxrs.providers = xml\n" +
                    "    </properties>\n" +
                    "  </pojo-deployment>\n" +
                    "</openejb-jar>\n"), "openejb-jar.xml");
        }

        return war;
    }

    protected static WebArchive getEjbModule(final WebArchive war) {
        return getEjbModule(war, null);
    }

    protected static void configureLoggin() throws Exception {
        synchronized (LoggingJAXRSCommons.class) {
            msgs = new LinkedList<>();
        }
        handler = new Handler() {
            @Override
            public void publish(final LogRecord record) {
                synchronized (LoggingJAXRSCommons.class) {
                    msgs.add(record.getMessage());
                }
            }

            @Override
            public void flush() {
                // no-op
            }

            @Override
            public void close() throws SecurityException {
                // no-op
            }
        };
        getLooger().addHandler(handler);
    }

    // captures the logs of the JAX-RS deployment of the webapp, which happens after the listeners started
    @WebListener
    public static class LogCapture implements ServletContextListener {
        @Override
        public void contextInitialized(final ServletContextEvent sce) {
            try {
                configureLoggin();
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void contextDestroyed(final ServletContextEvent sce) {
            try {
                getLooger().removeHandler(handler);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Singleton
    @Path("loggin-test-bean")
    public static class LogginTestBean {

        @PUT
        public void test() {

        }
    }

    @ApplicationPath("/api")
    public static class LogginTestApplication extends Application {
    }
}
