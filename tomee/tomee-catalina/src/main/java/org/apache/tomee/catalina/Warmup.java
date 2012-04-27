/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.openejb.config.TldScanner;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.util.DaemonThreadFactory;

import javax.xml.bind.JAXBException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The classes listed and loaded eagerly have static initializers which take a tiny bit of time.
 * These initializers cannot be actually run in parallel because classes are loaded serially,
 * but executing them back to back does help speed things up a bit.
 *
 * @version $Rev$ $Date$
 */
public class Warmup {

    /**
     * Referencing this method is enough to cause the static initializer
     * to be called.  Referencing this method in several classes is therefore safe.
     *
     * This method itself does nothing.
     */
    public static void warmup() {
    }

    static {
        initialize();
    }

    private static void initialize() {
        final String[] classes = {
                "java.util.concurrent.TimeUnit",
                "java.util.concurrent.atomic.AtomicLong",
                "javassist.util.proxy.ProxyFactory",
                "javax.el.ExpressionFactory",
                "javax.faces.component.UIViewRoot",
                "javax.imageio.ImageIO",
                "javax.naming.spi.NamingManager",
                "javax.servlet.ServletOutputStream",
                "org.apache.bval.jsr303.ApacheValidatorFactory",
                "org.apache.bval.jsr303.ConstraintAnnotationAttributes",
                "org.apache.bval.jsr303.ConstraintDefaults",
                "org.apache.bval.jsr303.groups.GroupsComputer",
                "org.apache.bval.jsr303.xml.ValidationMappingParser",
                "org.apache.bval.util.PrivilegedActions",
                "org.apache.catalina.authenticator.AuthenticatorBase",
                "org.apache.catalina.connector.Connector",
                "org.apache.catalina.connector.CoyoteAdapter",
                "org.apache.catalina.connector.Request",
                "org.apache.catalina.connector.Response",
                "org.apache.catalina.core.ApplicationFilterChain",
                "org.apache.catalina.core.StandardContext",
                "org.apache.catalina.core.StandardServer",
                "org.apache.catalina.deploy.NamingResources",
                "org.apache.catalina.loader.WebappLoader",
                "org.apache.catalina.mbeans.GlobalResourcesLifecycleListener",
                "org.apache.catalina.mbeans.MBeanUtils",
                "org.apache.catalina.realm.RealmBase",
                "org.apache.catalina.security.SecurityUtil",
                "org.apache.catalina.servlets.DefaultServlet",
                "org.apache.catalina.session.ManagerBase",
                "org.apache.catalina.startup.Bootstrap",
                "org.apache.catalina.startup.Catalina",
                "org.apache.catalina.startup.CatalinaProperties",
                "org.apache.catalina.startup.ContextConfig",
                "org.apache.catalina.users.MemoryUserDatabase",
                "org.apache.catalina.util.ExtensionValidator",
                "org.apache.catalina.util.LifecycleBase",
                "org.apache.catalina.util.ServerInfo",
                "org.apache.catalina.valves.AccessLogValve",
                "org.apache.catalina.valves.ValveBase",
                "org.apache.coyote.AbstractProtocol",
                "org.apache.coyote.ajp.AbstractAjpProtocol",
                "org.apache.coyote.http11.AbstractHttp11Protocol",
                "org.apache.geronimo.transaction.manager.TransactionManagerImpl",
                "org.apache.jasper.compiler.JspRuntimeContext",
                "org.apache.juli.logging.DirectJDKLog",
                "org.apache.myfaces.context.servlet.ServletExternalContextImplBase",
                "org.apache.myfaces.shared.config.MyfacesConfig",
                "org.apache.myfaces.spi.WebConfigProviderFactory",
                "org.apache.naming.ContextBindings",
                "org.apache.naming.resources.BaseDirContext",
                "org.apache.openejb.ClassLoaderUtil",
                "org.apache.openejb.InterfaceType",
                "org.apache.openejb.assembler.classic.Assembler",
                "org.apache.openejb.assembler.classic.AssemblerTool",
                "org.apache.openejb.cdi.CdiBuilder",
                "org.apache.openejb.cdi.ThreadSingletonServiceImpl",
                "org.apache.openejb.config.AnnotationDeployer",
                "org.apache.openejb.config.AnnotationDeployer$4",
                "org.apache.openejb.config.AppValidator",
                "org.apache.openejb.config.AutoConfig",
                "org.apache.openejb.config.ConfigurationFactory",
                "org.apache.openejb.config.MBeanDeployer",
                "org.apache.openejb.config.NewLoaderLogic",
                "org.apache.openejb.config.PersistenceContextAnnFactory",
                "org.apache.openejb.config.sys.JaxbJavaee",
                "org.apache.openejb.core.ServerFederation",
                "org.apache.openejb.core.ivm.EjbHomeProxyHandler$1",
                "org.apache.openejb.core.ivm.EjbHomeProxyHandler$MethodType",
                "org.apache.openejb.core.managed.ManagedContainer$MethodType",
                "org.apache.openejb.loader.FileUtils",
                "org.apache.openejb.loader.IO",
                "org.apache.openejb.loader.SystemInstance",
                "org.apache.openejb.monitoring.StatsInterceptor",
                "org.apache.openejb.persistence.JtaEntityManagerRegistry",
                "org.apache.openejb.server.ServiceLogger",
                "org.apache.openejb.server.ejbd.EjbDaemon",
                "org.apache.openejb.server.ejbd.EjbRequestHandler",
                "org.apache.openejb.util.Duration",
                "org.apache.openejb.util.Join",
                "org.apache.openejb.util.JuliLogStreamFactory",
                "org.apache.openejb.util.LogCategory",
                "org.apache.openejb.util.Logger",
                "org.apache.openejb.util.Messages",
                "org.apache.openejb.util.SafeToolkit",
                "org.apache.openejb.util.StringTemplate",
                "org.apache.openejb.util.proxy.ProxyManager",
                "org.apache.openjpa.enhance.PCRegistry",
                "org.apache.openjpa.lib.util.Localizer",
                "org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap",
                "org.apache.tomcat.util.buf.B2CConverter",
                "org.apache.tomcat.util.buf.ByteChunk",
                "org.apache.tomcat.util.digester.Digester",
                "org.apache.tomcat.util.file.Matcher",
                "org.apache.tomcat.util.http.Cookies",
                "org.apache.tomcat.util.http.HttpMessages",
                "org.apache.tomcat.util.http.mapper.Mapper",
                "org.apache.tomcat.util.net.AbstractEndpoint",
                "org.apache.tomcat.util.scan.StandardJarScanner",
                "org.apache.tomcat.util.threads.ThreadPoolExecutor",
                "org.apache.tomee.catalina.BackportUtil",
                "org.apache.tomee.catalina.BackportUtil$1",
                "org.apache.tomee.catalina.TomcatLoader",
                "org.apache.webbeans.config.WebBeansFinder",
                "org.apache.webbeans.container.InjectionResolver",
                "org.apache.webbeans.util.WebBeansUtil",
                "org.apache.webbeans.web.context.WebContextsService",
                "org.apache.xbean.naming.reference.SimpleReference",
                "org.apache.xbean.propertyeditor.PropertyEditors",
                "org.apache.xbean.propertyeditor.ReferenceIdentityMap",
                "org.apache.xbean.recipe.ReflectionUtil",
                "org.slf4j.LoggerFactory",
                "org.slf4j.impl.StaticLoggerBinder",
        };

        final ExecutorService executor = Executors.newFixedThreadPool(4, new DaemonThreadFactory("warmup"));

        executor.execute(new JaxbJavaeeLoad(WebApp.class));
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TldScanner.scan(TomcatLoader.class.getClassLoader());
                } catch (Throwable throwable) {
                }
            }
        });

        for (final String className : classes) {
            final ClassLoader loader = Warmup.class.getClassLoader();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Class.forName(className, true, loader);
                    } catch (Throwable throwable) {
                    }
                }
            });
        }
    }

    private static class JaxbJavaeeLoad implements Runnable {

        private final Class<?> type;

        private JaxbJavaeeLoad(Class<?> type) {
            this.type = type;
        }

        @Override
        public void run() {
            try {
                JaxbJavaee.getContext(type);
            } catch (JAXBException e) {
            }
        }
    }

}
