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
