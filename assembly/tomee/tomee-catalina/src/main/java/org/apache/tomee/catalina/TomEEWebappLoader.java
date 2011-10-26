package org.apache.tomee.catalina;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappLoader;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.tomcat.util.ExceptionUtils;

/**
 * simply override getClassLoader().
 *
 * Note: internally it still uses a webappclassloader.
 *
 * @author rmannibucau
 */
public class TomEEWebappLoader extends WebappLoader {
    private ClassLoader appClassLoader;

    public TomEEWebappLoader(ClassLoader classLoader) {
        appClassLoader = classLoader;
    }

    @Override public ClassLoader getClassLoader() {
        return appClassLoader;
    }

    @Override protected void startInternal() throws LifecycleException {
        super.startInternal();
        try {
            // override the webappclassloader by the app classloader
            DirContextURLStreamHandler.bind(appClassLoader, getContainer().getResources());
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            throw new LifecycleException("start: ", t);
        }
    }
}
