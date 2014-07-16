/*
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Handler extends URLStreamHandler {

    protected URLConnection openConnection(final URL url) throws IOException {
        final String cln = url.getHost();
        final String resrce = url.getFile().substring(1);
        final URL realURL;
        if (cln != null && cln.length() != 0) {
            final ClassLoader cl = getContextClassLoader();
            try {
                Class.forName(cln, true, cl);
            } catch (final ClassNotFoundException ex) {
                throw (IOException) new MalformedURLException("Class " + cln + " cannot be found (" + ex + ")").initCause(ex);
            }
            realURL = cl.getResource(resrce);
            if (realURL == null) {
                throw new FileNotFoundException("Class resource " + resrce + " of class " + cln + " cannot be found");
            }
        } else {
            final ClassLoader cl = getContextClassLoader();
            realURL = cl.getResource(resrce);
            if (realURL == null) {
                throw new FileNotFoundException("System resource " + resrce + " cannot be found");
            }
        }
        return realURL.openConnection();
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            }
        );
    }

}
