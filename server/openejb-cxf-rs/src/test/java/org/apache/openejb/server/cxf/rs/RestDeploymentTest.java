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
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.AppContext;
import org.apache.openejb.Injection;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AnnotationDeployer;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.naming.IvmJndiFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.cxf.rs.beans.HookedRest;
import org.apache.openejb.server.cxf.rs.beans.MyExpertRestClass;
import org.apache.openejb.server.cxf.rs.beans.MyFirstRestClass;
import org.apache.openejb.server.cxf.rs.beans.MyNonListedRestClass;
import org.apache.openejb.server.cxf.rs.beans.MyRESTApplication;
import org.apache.openejb.server.cxf.rs.beans.MySecondRestClass;
import org.apache.openejb.server.cxf.rs.beans.RestWithInjections;
import org.apache.openejb.server.cxf.rs.beans.SimpleEJB;
import org.apache.openejb.server.httpd.HttpServer;
import org.apache.openejb.server.httpd.HttpServerFactory;
import org.apache.openejb.server.httpd.OpenEJBHttpServer;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;

/**
 * @author Romain Manni-Bucau
 */
public class RestDeploymentTest {
    private static CxfRSService service;
    private static HttpServer server;
    private static ServiceDaemon daemon;
    public static final String BASE_URL = "http://localhost:4204/my-web-app";

    @BeforeClass public static void start() throws Exception {
        WebApp webApp = new WebApp();
        webApp.setContextRoot("/my-web-app");
        webApp.setId("web");
        webApp.setVersion("2.5");
        WebModule webModule = new WebModule(webApp, webApp.getContextRoot(),
                Thread.currentThread().getContextClassLoader(), "myapp", webApp.getId());
        webModule.setFinder(new AnnotationFinder(new ClassesArchive(
                MyFirstRestClass.class, MySecondRestClass.class, MyNonListedRestClass.class,
                MyRESTApplication.class, MyExpertRestClass.class, HookedRest.class, RestWithInjections.class)).link());

        Assembler assembler = new Assembler();
        SystemInstance.get().setComponent(Assembler.class, assembler);

        AnnotationDeployer annotationDeployer = new AnnotationDeployer();
        ConfigurationFactory config = new ConfigurationFactory();
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        webModule = annotationDeployer.deploy(webModule);

        EjbJar ejbJar = new EjbJar("ejb");
        ejbJar.addEnterpriseBean(new StatelessBean(SimpleEJB.class));

        ConfigurationFactory factory = new ConfigurationFactory(false);

        AppModule appModule = new AppModule(Thread.currentThread().getContextClassLoader(), "foo");
        appModule.setModuleId("rest");
        appModule.getWebModules().add(webModule);
        appModule.getEjbModules().add(new EjbModule(ejbJar));
        annotationDeployer.deploy(appModule);

        AppInfo appInfo = factory.configureApplication(appModule);
        final AppContext application = assembler.createApplication(appInfo);

        Context ctx = (Context) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{Context.class}, new InvocationHandler() {
            @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (args.length == 1 && args[0].equals("SimpleEJBLocalBean")) {
                    return new SimpleEJB();
                }
                return method.invoke(new InitialContext(), args);
            }
        });

        CoreContainerSystem containerSystem = new CoreContainerSystem(new IvmJndiFactory());
        WebContext webContext = new WebContext(application);
        webContext.setId(webApp.getId());
        webContext.setClassLoader(webModule.getClassLoader());
        webContext.getInjections().add(new Injection("SimpleEJBLocalBean", "simple", RestWithInjections.class));
        webContext.setJndiEnc(ctx);
        containerSystem.addWebContext(webContext);
        SystemInstance.get().setComponent(ContainerSystem.class, containerSystem);

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.className = HttpServerFactory.class.getName();
        serviceInfo.properties = new Properties();
        serviceInfo.properties.setProperty("port", "-1");
        serviceInfo.properties.setProperty("bind", "foo");

        OpenEjbConfiguration configuration = new OpenEjbConfiguration();
        SystemInstance.get().setComponent(OpenEjbConfiguration.class, configuration);
        configuration.facilities = new FacilitiesInfo();
        configuration.facilities.services.add(serviceInfo);

        server = new OpenEJBHttpServer();
        daemon = new ServiceDaemon(server, 4204, "localhost");
        daemon.start();
        server.start();

        service = new CxfRSService();
        service.start();
    }

    @AfterClass public static void close() throws ServiceException {
        if (service != null) {
            service.stop();
        }
        if (daemon != null) {
            daemon.stop();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test public void first() {
        String hi = WebClient.create(BASE_URL).path("/first/hi").get(String.class);
        assertEquals("Hi from REST World!", hi);
    }

    @Test public void second() {
        String hi = WebClient.create(BASE_URL).path("/second/hi2/2nd").get(String.class);
        assertEquals("hi 2nd", hi);
    }

    @Test public void expert() throws Exception {
        Response response = WebClient.create(BASE_URL).path("/expert/still-hi").post("Pink Floyd");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        // convert entity to String
        // could be done with WebClient.create(BASE_URL).path("/expert/still-hi").post("Pink Floyd", String.class); too
        InputStream is = (InputStream) response.getEntity();
        StringWriter writer = new StringWriter();
        int c;
        while ((c = is.read()) != -1) {
            writer.write(c);
        }
        assertEquals("hi Pink Floyd", writer.toString());
    }

    @Test public void nonListed() { // default handler from openejb-http
        assertEquals("", WebClient.create(BASE_URL).path("/non-listed/yata/foo").get(String.class));
    }

    @Test public void hooked() {
        assertEquals(true, WebClient.create(BASE_URL).path("/hooked/post").get(Boolean.class).booleanValue());
    }

    @Test public void injectEjb() {
        assertEquals(true, WebClient.create(BASE_URL).path("/inject/ejb").get(Boolean.class).booleanValue());
    }
}
