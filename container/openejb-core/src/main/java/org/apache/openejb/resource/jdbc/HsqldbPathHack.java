/**
 *
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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.loader.SystemInstance;

import java.io.File;

public class HsqldbPathHack {
    private static final String HSQL_FILE_URL = "jdbc:hsqldb:file:";

    public static String toAbsolutePath(String url) {
        // is this a hsql file url?
        if (url == null || !url.startsWith(HSQL_FILE_URL)) {
            return url;
        }

        // get the relative path
        String path = url.substring(HSQL_FILE_URL.length());

        // make an absolute file
        File file = new File(path);
        if (!file.isAbsolute()) {
            File base = SystemInstance.get().getBase().getDirectory();
            file = new File(base, path);
        }

        // make an absolute url
        path = file.getAbsolutePath();
        return HSQL_FILE_URL + path;
    }
}
