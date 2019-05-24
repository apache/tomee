/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jug.routing;

import org.apache.openejb.resource.jdbc.router.AbstractRouter;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PollingRouter extends AbstractRouter {

    private Map<String, DataSource> dataSources = null;
    private ThreadLocal<DataSource> currentDataSource = new ThreadLocal<DataSource>() {
        @Override
        public DataSource initialValue() {
            return dataSources.get("jdbc/client1");
        }
    };

    @Override
    public DataSource getDataSource() {
        if (dataSources == null) {
            init();
        }
        return currentDataSource.get();
    }

    public void setDataSource(final String client) {
        if (dataSources == null) {
            init();
        }

        final String datasourceName = "jdbc/" + client;
        if (!dataSources.containsKey(datasourceName)) {
            throw new IllegalArgumentException("data source called " + datasourceName + " can't be found.");
        }
        final DataSource ds = dataSources.get(datasourceName);
        currentDataSource.set(ds);
    }

    private synchronized void init() {
        if (dataSources != null) {
            return;
        }

        dataSources = new HashMap<String, DataSource>();
        for (String ds : Arrays.asList("jdbc/client1", "jdbc/client2")) {
            try {
                final Object o = getOpenEJBResource(ds);
                if (o instanceof DataSource) {
                    dataSources.put(ds, DataSource.class.cast(o));
                }
            } catch (NamingException e) {
                // ignored
            }
        }
    }

    public void clear() {
        currentDataSource.remove();
    }
}
