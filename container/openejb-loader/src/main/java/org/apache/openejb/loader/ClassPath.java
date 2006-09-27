package org.apache.openejb.loader;

import java.io.File;
import java.net.URL;

public interface ClassPath {

    ClassLoader getClassLoader();

    void addJarsToPath(File dir) throws Exception;

    void addJarToPath(URL dir) throws Exception;
}
