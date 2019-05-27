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
package org.superbiz.dynamicdatasourcerouting;

import org.apache.openejb.resource.jdbc.router.AbstractRouter;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeterminedRouter extends AbstractRouter {

    private String dataSourceNames;
    private String defaultDataSourceName;
    private Map<String, DataSource> dataSources = null;
    private ThreadLocal<DataSource> currentDataSource = new ThreadLocal<DataSource>();

    /**
     * @param datasourceList datasource resource name, separator is a space
     */
    public void setDataSourceNames(String datasourceList) {
        dataSourceNames = datasourceList;
    }

    /**
     * lookup datasource in openejb resources
     */
    private void init() {
        dataSources = new ConcurrentHashMap<String, DataSource>();
        for (String ds : dataSourceNames.split(" ")) {
            try {
                Object o = getOpenEJBResource(ds);
                if (o instanceof DataSource) {
                    dataSources.put(ds, DataSource.class.cast(o));
                }
            } catch (NamingException e) {
                // ignored
            }
        }
    }

    /**
     * @return the user selected data source if it is set
     *         or the default one
     * @throws IllegalArgumentException if the data source is not found
     */
    @Override
    public DataSource getDataSource() {
        // lazy init of routed datasources
        if (dataSources == null) {
            init();
        }

        // if no datasource is selected use the default one
        if (currentDataSource.get() == null) {
            if (dataSources.containsKey(defaultDataSourceName)) {
                return dataSources.get(defaultDataSourceName);

            } else {
                throw new IllegalArgumentException("you have to specify at least one datasource");
            }
        }

        // the developper set the datasource to use
        return currentDataSource.get();
    }

    /**
     * @param datasourceName data source name
     */
    public void setDataSource(String datasourceName) {
        if (dataSources == null) {
            init();
        }
        if (!dataSources.containsKey(datasourceName)) {
            throw new IllegalArgumentException("data source called " + datasourceName + " can't be found.");
        }
        DataSource ds = dataSources.get(datasourceName);
        currentDataSource.set(ds);
    }

    /**
     * reset the data source
     */
    public void clear() {
        currentDataSource.remove();
    }

    public void setDefaultDataSourceName(String name) {
        this.defaultDataSourceName = name;
    }
}
