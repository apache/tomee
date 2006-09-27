package org.apache.openejb.loader;

import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;

/*-------------------------------------------------------*/
/* Thread Context ClassLoader Support */
/*-------------------------------------------------------*/

public class ContextClassPath extends BasicURLClassPath {

    public ClassLoader getClassLoader() {
        return getContextClassLoader();
    }

    public void addJarsToPath(File dir) throws Exception {
        ClassLoader contextClassLoader = getContextClassLoader();
        if (contextClassLoader instanceof URLClassLoader) {
            URLClassLoader loader = (URLClassLoader) contextClassLoader;
            this.addJarsToPath(dir, loader);
        }
    }

    public void addJarToPath(URL jar) throws Exception {
        ClassLoader contextClassLoader = getContextClassLoader();
        if (contextClassLoader instanceof URLClassLoader) {
            URLClassLoader loader = (URLClassLoader) contextClassLoader;
            this.addJarToPath(jar, loader);
        }
    }
}
