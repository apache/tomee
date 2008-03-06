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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.loader;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.URLDecoder;
import java.io.File;

/*-------------------------------------------------------*/
/* System ClassLoader Support */
/*-------------------------------------------------------*/

public class SystemClassPath extends BasicURLClassPath {

    private URLClassLoader sysLoader;

    public void addJarsToPath(File dir) throws Exception {
        this.addJarsToPath(dir, getSystemLoader());
        this.rebuildJavaClassPathVariable();
    }

    public void addJarToPath(URL jar) throws Exception {

        this.addJarToPath(jar, getSystemLoader());
        this.rebuildJavaClassPathVariable();
    }

    public ClassLoader getClassLoader() {
        try {
            return getSystemLoader();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URLClassLoader getSystemLoader() throws Exception {
        if (sysLoader == null) {
            sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        }
        return sysLoader;
    }

    private void rebuildJavaClassPathVariable() throws Exception {
        sun.misc.URLClassPath cp = getURLClassPath(getSystemLoader());
        URL[] urls = cp.getURLs();

        if (urls.length < 1)
            return;

        StringBuffer path = new StringBuffer(urls.length * 32);

        File s = new File(URLDecoder.decode(urls[0].getFile()));
        path.append(s.getPath());

        for (int i = 1; i < urls.length; i++) {
            path.append(File.pathSeparator);

            s = new File(URLDecoder.decode(urls[i].getFile()));

            path.append(s.getPath());
        }
        try {
            System.setProperty("java.class.path", path.toString());
        } catch (Exception e) {
        }
    }
}
