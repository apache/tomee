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
package org.apache.openejb.loader;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

/**
 * @version $Rev$ $Date$
 */
public class JarLocation {

    public static File get() {
        return jarLocation(JarLocation.class);
    }

    public static File jarLocation(Class clazz) {
        try {
            String classFileName = clazz.getName().replace(".", "/") + ".class";

            URL classURL = clazz.getClassLoader().getResource(classFileName);

            URI uri = null;
            String url = classURL.toExternalForm();
            if (url.contains(" ")) {
                url = url.replaceAll(" ", "%20");
            }
            uri = new URI(url);

            if (uri.getPath() == null){
                uri = new URI(uri.getRawSchemeSpecificPart());
            }

            String path = uri.getPath();
            if (path.contains("!")){
                path = path.substring(0, path.indexOf('!'));
            } else {
                path = path.substring(0, path.length() - classFileName.length());
            }

            return new File(URLDecoder.decode(path));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
