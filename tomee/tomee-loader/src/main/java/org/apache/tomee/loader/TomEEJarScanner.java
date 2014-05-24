/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tomee.loader;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.startup.TldConfig;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.config.TldScanner;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.descriptor.XmlErrorHandler;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanner;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

// we don't respect the StandardJarScanner contract for default callbacks (FragmentJarScannerCallback and TldJarScannerCallback)
// but that's our default, this is overridable if needed
@SuppressWarnings("unchecked")
public class TomEEJarScanner extends StandardJarScanner {

    private static final Log log = LogFactory.getLog(StandardJarScanner.class);

    /**
     * The string resources for this package.
     */
    private static final StringManager sm = StringManager.getManager(Constants.Package);

    private static final Method tldConfigScanStream;
    private static final Field tldConfig;
    private static final Method tldLocationScanStream;
    private static final Field tldLocationCache;

    // tld of the server
    private static final Set<URL> SERVER_URLS;

    // TldConfig of the server (common)
    private static final Set<String> TAG_LIB_URIS;
    private static final ArrayList<String> LISTENERS;

    // TldLocationCache of the server (common)
    private static final Hashtable<String, Object> MAPPINGS;

    private static final Thread SERVER_SCANNING_THREAD;

    static {
        try {
            final ClassLoader loader = TomEEJarScanner.class.getClassLoader();

            tldConfigScanStream = TldConfig.class.getDeclaredMethod("tldScanStream", InputStream.class);
            tldConfigScanStream.setAccessible(true);
            tldConfig = loader.loadClass("org.apache.catalina.startup.TldConfig$TldJarScannerCallback")
                .getDeclaredFields()[0]; // there is a unique field and this way it is portable
            //.getDeclaredField("this$0");
            tldConfig.setAccessible(true);

            final Class<?> tldLocationsCache = loader.loadClass("org.apache.jasper.compiler.TldLocationsCache");
            tldLocationScanStream = tldLocationsCache.getDeclaredMethod("tldScanStream", String.class, String.class, InputStream.class);
            tldLocationScanStream.setAccessible(true);
            tldLocationCache = loader.loadClass("org.apache.jasper.compiler.TldLocationsCache$TldJarScannerCallback")
                .getDeclaredFields()[0];
            tldLocationCache.setAccessible(true);

            // init server cache
            SERVER_URLS = TldScanner.scan(TomEEJarScanner.class.getClassLoader());

            final Context fakeWebApp = (Context) Proxy.newProxyInstance(loader, new Class<?>[]{Context.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if ("getTldNamespaceAware".equals(method.getName())) {
                            return Globals.STRICT_SERVLET_COMPLIANCE;
                        } else if ("getTldValidation".equals(method.getName())) {
                            return Globals.STRICT_SERVLET_COMPLIANCE;
                        } else if ("getXmlValidation".equals(method.getName())) {
                            return Globals.STRICT_SERVLET_COMPLIANCE;
                        } else if ("getXmlBlockExternal".equals(method.getName())) {
                            return Globals.IS_SECURITY_ENABLED;
                        }
                        return null;
                    }
                }
            );
            final TldConfig config = new TldConfig();
            config.lifecycleEvent(new LifecycleEvent(fakeWebApp, Lifecycle.AFTER_INIT_EVENT, null));

            final Object fakeSc = Proxy.newProxyInstance(loader, new Class<?>[]{ServletContext.class}, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    return null;
                }
            });
            final Object locationsCacheInstance = tldLocationsCache.getConstructor(ServletContext.class).newInstance(fakeSc);

            if (!SERVER_URLS.isEmpty()) {
                SERVER_SCANNING_THREAD = new Thread() {
                    @Override
                    public void run() {
                        for (URL current : SERVER_URLS) {
                            tldConfig(config, current);
                            tldLocationCache(locationsCacheInstance, current);
                        }
                    }
                };
                SERVER_SCANNING_THREAD.setName("TomEE-server-tld-reading");
                SERVER_SCANNING_THREAD.setDaemon(true);
                SERVER_SCANNING_THREAD.start();
            } else {
                SERVER_SCANNING_THREAD = null;
            }

            TAG_LIB_URIS = (Set<String>) Reflections.get(config, "taglibUris");
            LISTENERS = (ArrayList<String>) Reflections.get(config, "listeners");
            MAPPINGS = (Hashtable<String, Object>) Reflections.get(locationsCacheInstance, "mappings");
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    @Override
    public void scan(final ServletContext context, final ClassLoader classLoader, final JarScannerCallback callback, final Set<String> jarsToIgnore) {
        if ("FragmentJarScannerCallback".equals(callback.getClass().getSimpleName())) {
            final EmbeddedJarScanner embeddedJarScanner = new EmbeddedJarScanner();
            embeddedJarScanner.scan(context, classLoader, callback, jarsToIgnore);
        } else if ("TldJarScannerCallback".equals(callback.getClass().getSimpleName())) {

            final String cbName = callback.getClass().getName();
            if (cbName.equals(tldConfig.getDeclaringClass().getName())) {
                ensureServerTldsScanned();
                try {
                    final TldConfig config;
                    try {
                        config = (TldConfig) tldConfig.get(callback);
                    } catch (IllegalAccessException e) {
                        throw new OpenEJBException("scan with default algo");
                    }

                    final Set<URL> urls = TldScanner.scan(classLoader != null ? classLoader : context.getClassLoader());
                    for (URL url : urls) {
                        if (!SERVER_URLS.contains(url)) {
                            tldConfig(config, url);
                        }
                    }

                    // add already scanned ones
                    for (String uri : TAG_LIB_URIS) {
                        config.addTaglibUri(uri);
                    }
                    for (String listener : LISTENERS) {
                        if (!"org.apache.myfaces.webapp.StartupServletContextListener".equals(listener)) { // done elsewhere
                            config.addApplicationListener(listener);
                        }
                    }
                } catch (final OpenEJBException oe) {
                    log.error(oe.getMessage(), oe);
                }
            } else if (cbName.equals(tldLocationCache.getDeclaringClass().getName())) {
                ensureServerTldsScanned();
                try {
                    final Object tldLocationsCache;
                    try {
                        tldLocationsCache = tldLocationCache.get(callback);
                    } catch (IllegalAccessException e) {
                        throw new OpenEJBException("scan with default algo");
                    }

                    final Set<URL> urls = TldScanner.scan(context.getClassLoader());
                    for (URL url : urls) {
                        if (!SERVER_URLS.contains(url)) {
                            tldLocationCache(tldLocationsCache, url);
                        }
                    }

                    // add server ones
                    final Hashtable<String, Object> mappings = (Hashtable<String, Object>) Reflections.get(tldLocationsCache, "mappings");
                    mappings.putAll((Map<String, Object>) MAPPINGS.clone());
                } catch (final OpenEJBException oe) {
                    log.error(oe.getMessage(), oe);
                }
            } else {
                log.debug("This callback " + callback + " is not known and perf optim will not be available");
            }
        } else {
            log.info("Not expected scanner: " + callback);
            super.scan(context, classLoader, callback, jarsToIgnore);
        }
    }

    private void ensureServerTldsScanned() {
        if (SERVER_SCANNING_THREAD == null) {
            return;
        }

        try {
            SERVER_SCANNING_THREAD.join();
        } catch (InterruptedException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    private static void tldLocationCache(final Object tldLocationsCache, final URL url) {
        String resource = url.toString();
        String entry = null;

        if (resource.contains("!/")) {
            final String path = url.getPath();
            final int endIndex = path.indexOf("!/");
            resource = path.substring(0, endIndex);
            entry = path.substring(endIndex + 2, path.length());
        }

        InputStream is = null;
        try {
            is = url.openStream();
            tldLocationScanStream.invoke(tldLocationsCache, resource, entry, is);
        } catch (Exception e) {
            log.warn(sm.getString("jarScan.webinflibFail", url.toExternalForm()), e);
        } finally {
            IO.close(is);
        }
    }

    private static void tldConfig(final TldConfig config, final URL current) {
        InputStream is = null;
        try {
            is = current.openStream();
            final XmlErrorHandler handler = (XmlErrorHandler) tldConfigScanStream.invoke(config, is);
            handler.logFindings(log, current.toExternalForm());
        } catch (Exception e) {
            log.warn(sm.getString("jarScan.webinflibFail", current), e);
        } finally {
            IO.close(is);
        }
    }
}
