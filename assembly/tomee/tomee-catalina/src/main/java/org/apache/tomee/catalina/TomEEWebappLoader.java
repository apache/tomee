package org.apache.tomee.catalina;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.loader.WebappLoader;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.openejb.util.ArrayEnumeration;
import org.apache.tomcat.util.ExceptionUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author rmannibucau
 */
public class TomEEWebappLoader extends WebappLoader {
    private ClassLoader appClassLoader;
    private ClassLoader tomEEClassLoader;

    public TomEEWebappLoader(final ClassLoader classLoader) {
        appClassLoader = classLoader;
    }

    @Override public ClassLoader getClassLoader() {
        return tomEEClassLoader;
    }

    @Override protected void startInternal() throws LifecycleException {
        super.startInternal();
        final ClassLoader webappCl = super.getClassLoader();
        tomEEClassLoader = new TomEEClassLoader(appClassLoader, webappCl);
        try {
             DirContextURLStreamHandler.bind(tomEEClassLoader, getContainer().getResources());
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            throw new LifecycleException("start: ", t);
        }
    }

    public static class TomEEClassLoader extends ClassLoader {
        private ClassLoader app;
        private ClassLoader webapp;

        public TomEEClassLoader(final ClassLoader appCl, final ClassLoader webappCl) {
            super(webappCl); // in fact this classloader = webappclassloader since we add nothing to this
            app = appCl;
            webapp = webappCl;
        }

        /**
         * we totally override this method to be able to remove duplicated resources.
         *
         * @param name
         * @return
         * @throws IOException
         */
        @Override public Enumeration<URL> getResources(final String name) throws IOException {
            Set<URL> urls = new HashSet<URL>();

            // /!\ order is important here
            if (webapp instanceof WebappClassLoader && ((WebappClassLoader) webapp).isStarted() || webapp.getParent() == null) { // we set a parent so if it is null webapp was detroyed
                Enumeration<URL> webappClassLoaderResources = webapp.getResources(name);
                add(urls, webappClassLoaderResources);
            }

            Enumeration<URL> appClassLoaderResources = app.getResources(name);
            add(urls, appClassLoaderResources);

            return new ArrayEnumeration(urls);
        }

        private static void add(Collection<URL> urls, Enumeration<URL> enumUrls) {
            try {
                while (enumUrls.hasMoreElements()) {
                    urls.add(enumUrls.nextElement());
                }
            } catch (IllegalStateException ese) {
                // ignored: if jars are already closed...shutdown for instance
            }
        }
    }
}
