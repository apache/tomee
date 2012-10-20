/**
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
package org.apache.tomee.myfaces;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.myfaces.context.servlet.StartupServletExternalContextImpl;
import org.apache.myfaces.ee6.MyFacesContainerInitializer;
import org.apache.myfaces.spi.FacesConfigResourceProvider;
import org.apache.myfaces.spi.FacesConfigResourceProviderFactory;
import org.apache.myfaces.webapp.AbstractFacesInitializer;
import org.apache.myfaces.webapp.StartupServletContextListener;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.log.RemoveLogMessage;

import javax.faces.context.ExternalContext;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TomEEMyFacesContainerInitializer implements ServletContainerInitializer {
    public static final String OPENEJB_JSF_SKIP = "openejb.jsf.skip";

    private final MyFacesContainerInitializer delegate;

    public TomEEMyFacesContainerInitializer() {
        delegate = new MyFacesContainerInitializer();
    }

    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {
        // try to skip first
        if ("true".equalsIgnoreCase(ctx.getInitParameter("org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE"))
                || "true".equals(SystemInstance.get().getProperty(OPENEJB_JSF_SKIP, "false"))) {
            return;
        }

        // some message filtering, not a perf killer since this class don't log a lot
        final Logger abstractInitializerLogger = Logger.getLogger(AbstractFacesInitializer.class.getName());
        abstractInitializerLogger.setFilter(new RemoveLogMessage(
                new RemoveLogMessage(abstractInitializerLogger.getFilter(),
                        Level.WARNING, "No mappings of FacesServlet found. Abort initializing MyFaces."),
                Level.WARNING, "No mappings of FacesServlet found. Abort destroy MyFaces."));

        if ((classes != null && !classes.isEmpty()) || isFacesConfigPresent(ctx)) {
            // we found a faces-config.xml or some classes so let's delegate to myfaces

            // since we don't want to call isFacesConfigPresent again (it scan all jars!!!!)
            // forcing classes to not be empty
            Set<Class<?>> passedClasses = classes;
            if (passedClasses == null) {
                passedClasses = new HashSet<Class<?>>();
            }
            if (passedClasses.isEmpty()) {
                passedClasses.add(TomEEMyFacesContainerInitializer.class);
            }

            if (ctx instanceof ApplicationContextFacade) {
                try {
                    final ApplicationContext appCtx = (ApplicationContext) get(ApplicationContextFacade.class, ctx);
                    final Context tomcatCtx = (Context) get(ApplicationContext.class, appCtx);
                    if (!Arrays.asList(tomcatCtx.findApplicationListeners()).contains(StartupServletContextListener.class.getName())) {
                        addListener(ctx);
                    }
                } catch (Exception e) {
                    // add it, not important we'll simply get a warning saying it is already here
                    addListener(ctx);
                }
            }

            // finally delegating begin sure we'll not call isFacesConfigPresent
            delegate.onStartup(classes, ctx);
        }
    }

    private void addListener(final ServletContext ctx) {
        final Logger logger = Logger.getLogger(AbstractFacesInitializer.class.getName());
        logger.log(Level.INFO, "Installing <listener>" + StartupServletContextListener.class.getName() + "</listener>");
        ctx.addListener(StartupServletContextListener.class);
    }

    // that's the reason why we fork: we don't want to consider our internal faces-config.xml
    // see delegate for details
    private boolean isFacesConfigPresent(ServletContext servletContext) {
        try {
            if (servletContext.getResource("/WEB-INF/faces-config.xml") != null) {
                return true;
            }

            final String configFilesAttrValue = servletContext.getInitParameter(FacesServlet.CONFIG_FILES_ATTR);
            if (configFilesAttrValue != null) {
                String[] configFiles = configFilesAttrValue.split(",");
                for (String file : configFiles) {
                    if (servletContext.getResource(file.trim()) != null) {
                        return true;
                    }
                }
            }

            final ExternalContext externalContext = new StartupServletExternalContextImpl(servletContext, true);
            final FacesConfigResourceProviderFactory factory = FacesConfigResourceProviderFactory.
                    getFacesConfigResourceProviderFactory(externalContext);
            final FacesConfigResourceProvider provider = factory.createFacesConfigResourceProvider(externalContext);
            final Collection<URL> metaInfFacesConfigUrls =  provider.getMetaInfConfigurationResources(externalContext);
            if (metaInfFacesConfigUrls == null) {
                return false;
            }

            // remove our internal faces-config.xml
            final Iterator<URL> it = metaInfFacesConfigUrls.iterator();
            while (it.hasNext()) {
                if (it.next().toExternalForm().replace(File.separator, "/").contains("/openwebbeans-jsf-")) {
                    it.remove();
                }
            }

            return !metaInfFacesConfigUrls.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private static Object get(final Class<?> clazz, final Object facade) throws Exception {
        final Field field = clazz.getDeclaredField("context");
        boolean acc = field.isAccessible();
        field.setAccessible(true);
        try {
            return field.get(facade);
        } finally {
            field.setAccessible(acc);
        }
    }
}
