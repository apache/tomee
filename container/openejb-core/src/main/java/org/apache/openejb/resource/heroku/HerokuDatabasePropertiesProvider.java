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
package org.apache.openejb.resource.heroku;

import org.apache.openejb.api.resource.PropertiesResourceProvider;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.PropertyPlaceHolderHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// kind of php to java
public class HerokuDatabasePropertiesProvider implements PropertiesResourceProvider {
    private final Map<String, String> jdbcMapping = new HashMap<String, String>() {{
        put("postgres", "postgresql");
        put("hsql", "hsqldb:hsql");
    }};

    private Properties properties;

    @Override
    public Properties provides() {
        try {
            final URI url = new URI(PropertyPlaceHolderHelper.simpleValue("${DATABASE_URL}")); // let it be overridable
            final String userInfo = url.getUserInfo();
            final String jdbcUrl =
                    "jdbc:" +
                    (jdbcMapping.containsKey(url.getScheme()) ? jdbcMapping.get(url.getScheme()) : url.getScheme()) +
                    "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") +
                    url.getPath();
            final PropertiesBuilder builder = new PropertiesBuilder().p("JdbcUrl", jdbcUrl);
            if (userInfo != null) {
                final int sep = userInfo.indexOf(':');
                if (sep > 0) {
                    builder.p("UserName", userInfo.substring(0, sep))
                            .p("Password", userInfo.substring(sep + 1, userInfo.length()));
                } else {
                    builder.p("UserName", userInfo);
                }
            }
            if (properties == null || "org.hsqldb.jdbcDriver".equals(properties.getProperty("JdbcDriver"))) {
                if ("postgres".equalsIgnoreCase(url.getScheme())) {
                    builder.p("JdbcDriver", "org.postgresql.Driver");
                } // else TODO
            }
            return builder.build();
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
