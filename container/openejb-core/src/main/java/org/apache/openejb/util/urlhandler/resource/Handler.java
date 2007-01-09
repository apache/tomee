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
package org.apache.openejb.util.urlhandler.resource;

import java.net.URL;
import java.net.URLConnection;

public class Handler extends java.net.URLStreamHandler {

    protected URLConnection openConnection(URL url) throws java.io.IOException {
        String cln = url.getHost();

        String resrce = url.getFile().substring(1);

        URL realURL;

        if (cln != null && cln.length() != 0) {
            Class clz;
            ClassLoader cl = getContextClassLoader();

            try {

                clz = Class.forName(cln, true, cl);
            } catch (ClassNotFoundException ex) {
                throw new java.net.MalformedURLException("Class " + cln + " cannot be found (" + ex + ")");
            }

            realURL = cl.getResource(resrce);

            if (realURL == null)
                throw new java.io.FileNotFoundException("Class resource " + resrce + " of class " + cln + " cannot be found");
        } else {
            ClassLoader cl = getContextClassLoader();
            realURL = cl.getResource(resrce);

            if (realURL == null)
                throw new java.io.FileNotFoundException("System resource " + resrce + " cannot be found");
        }

        return realURL.openConnection();
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                }
        );
    }

}
