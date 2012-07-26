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
package org.apache.openejb.resource.jdbc.pool;

import org.apache.openejb.resource.XAResourceWrapper;

import javax.sql.DataSource;
import java.util.Properties;

public interface DataSourceCreator {
    DataSource managed(String name, DataSource ds);
    DataSource poolManaged(String name, DataSource ds);
    DataSource pool(String name, DataSource ds);
    DataSource poolManagedWithRecovery(String name, XAResourceWrapper xaResourceWrapper, String driver, Properties properties);
    DataSource poolManaged(String name, String driver, Properties properties);
    DataSource pool(String name, String driver, Properties properties);

    boolean hasCreated(Object object);
    void destroy(Object object) throws Throwable;
}
