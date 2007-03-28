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
package org.apache.openejb.util;

import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class ConfUtils {

    public static URL getConfResource(String name) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(name);

        try {

            File loginConfig = ConfUtils.install(resource, name);

            if (loginConfig != null){
                resource = loginConfig.toURL();
            }
        } catch (IOException e) {
        }

        return resource;
    }

    public static File install(String source, String name) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL resource = cl.getResource(source);
        return install(resource, name, false);
    }

    public static File install(URL resource, String name) throws IOException {
        return install(resource, name, false);
    }

    public static File install(URL resource, String name, boolean overwrite) throws IOException {
        if (resource == null) return null;

        SystemInstance system = SystemInstance.get();
        FileUtils base = system.getBase();
        File conf = base.getDirectory("conf");

        if (!conf.exists()) return null;

        File file = new File(conf, name);

        if (file.exists() && !overwrite) return file;

        InputStream in = resource.openStream();
        in = new BufferedInputStream(in);

        FileOutputStream fout = new FileOutputStream(file);
        BufferedOutputStream out = new BufferedOutputStream(fout);

        try {
            int b = in.read();
            while (b != -1) {
                out.write(b);
                b = in.read();
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
            try {
                out.close();
            } catch (IOException e) {
            }
        }

        return file;
    }
}
