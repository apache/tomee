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

package org.apache.openejb.resource.jdbc;

import javax.sql.CommonDataSource;
import java.lang.reflect.Method;

public final class DataSourceHelper {
    private DataSourceHelper() {
        // no-op
    }

    public static void setUrl(final CommonDataSource dataSource, final String url, final ClassLoader classLoader, final String clazz, final String method) throws Exception {
        final Class<?> loadedClass = classLoader.loadClass(clazz);
        final Method setUrl = loadedClass.getMethod(method, String.class);
        setUrl.setAccessible(true);
        setUrl.invoke(dataSource, url);
    }

    public static void setUrl(final CommonDataSource dataSource, final String url) throws Exception {
        // TODO This is a big whole and we will need to rework this
        if (url.contains("jdbc:derby:")) {
            DataSourceHelper.setUrl(dataSource, url.replace("jdbc:derby:", ""), dataSource.getClass().getClassLoader(), "org.apache.derby.jdbc.EmbeddedDataSource", "setDatabaseName");
        } else {
            DataSourceHelper.setUrl(dataSource, url, dataSource.getClass().getClassLoader(), "org.hsqldb.jdbc.JDBCDataSource", "setDatabase");
        }
    }
}
