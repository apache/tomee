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
package org.apache.openejb.resource.openshift;

import org.apache.openejb.api.resource.PropertiesResourceProvider;
import org.apache.openejb.testng.PropertiesBuilder;

import java.util.Properties;

public class OpenshiftMySQLPropertiesProvider implements PropertiesResourceProvider {
    @Override
    public Properties provides() {
        return new PropertiesBuilder()
                .p("JdbcDriver", "com.mysql.jdbc.Driver")
                .p("JdbcUrl", String.format(
                        "jdbc:mysql://%s:%s/%s?tcpKeepAlive=true",
                        System.getenv("OPENSHIFT_MYSQL_DB_HOST"),
                        System.getenv("OPENSHIFT_MYSQL_DB_PORT"),
                        System.getenv("OPENSHIFT_APP_NAME")))
                .p("UserName", System.getenv("OPENSHIFT_MYSQL_DB_USERNAME"))
                .p("Password", System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD"))
                .p("ValidationQuery", "SELECT 1")
                .build();
    }
}
