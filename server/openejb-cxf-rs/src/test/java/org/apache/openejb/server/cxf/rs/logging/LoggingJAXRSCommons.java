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
package org.apache.openejb.server.cxf.rs.logging;

import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.log.LoggerCreator;
import org.apache.openejb.server.rest.RESTService;
import org.apache.openejb.util.JuliLogStream;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.LogStreamAsync;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.NetworkUtil;
import org.apache.openejb.util.reflection.Reflections;

import jakarta.ejb.Singleton;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LoggingJAXRSCommons {

    protected static int port = -1;
    protected Collection<String> msgs;

    protected synchronized boolean assertJAXRSConfiguration() {
        final Iterator<String> iterator = msgs.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().contains("Registered JAX-RS Configuration:")) {
                return true;
            }
        }
        return false;
    }

    protected java.util.logging.Logger getLooger() throws Exception {
        final Logger logger = Logger.getInstance(LogCategory.OPENEJB_RS, RESTService.class);
        final LogStreamAsync stream = LogStreamAsync.class.cast(Reflections.get(logger, "logStream"));
        final JuliLogStream ls = JuliLogStream.class.cast(Reflections.get(stream, "ls"));
        final LoggerCreator julCreator = LoggerCreator.class.cast(Reflections.get(ls, "logger"));
        return julCreator.call();
    }

    protected EjbModule getEjbModule(String pojoDeploymentClassName, String ejbModuleId) throws Exception {
        final EjbModule module = new EjbModule(new EjbJar(), new OpenejbJar());

        if (ejbModuleId != null) {
            module.setModuleId(ejbModuleId);
        }

        final EnterpriseBean bean = new SingletonBean(LogginTestBean.class).localBean();
        module.getEjbJar().addEnterpriseBean(bean);

        final Resources resources = new Resources();

        final Service feature = new Service("xml", null);
        feature.setClassName(JAXBElementProvider.class.getName());
        feature.getProperties().put("eventHandler", "$handler");

        resources.getService().add(feature);
        module.initResources(resources);


        if (pojoDeploymentClassName != null) {
            final PojoDeployment e = new PojoDeployment();
            e.setClassName(pojoDeploymentClassName);
            e.getProperties().setProperty("cxf.jaxrs.providers", "xml");
            module.getOpenejbJar().getPojoDeployment().add(e);
        }

        return module;
    }

    protected EjbModule getEjbModule(String ejbModuleId) throws Exception {
        return getEjbModule(null, ejbModuleId);
    }

    protected EjbModule getEjbModule() throws Exception {
        return getEjbModule(null, null);
    }

    protected WebApp getWebApp() throws Exception {
        return new WebApp();
    }

    protected static void configurePort() {
        port = NetworkUtil.getNextAvailablePort();
    }

    protected void configureLoggin() throws Exception {
        msgs = new LinkedList<>();
        final java.util.logging.Logger looger = getLooger();
        looger.addHandler(new Handler() {
            @Override
            public void publish(final LogRecord record) {
                synchronized (LoggingJAXRSCommons.this) {
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

            public Collection getMsg() {
                synchronized (LoggingJAXRSCommons.this) {
                    return msgs;
                }
            }
        });
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
