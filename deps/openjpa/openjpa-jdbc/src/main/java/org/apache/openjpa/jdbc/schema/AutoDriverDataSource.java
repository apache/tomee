/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.jdbc.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Automatic Commons DBCP pooling or Simple non-pooling driver data source.
 * If the commons-dbcp packages are on the class path, then they will be used,
 * else it will fall back to non-DBCP mode.
 */
public class AutoDriverDataSource
    extends DBCPDriverDataSource {
    
    @Override
    public Connection getConnection(Properties props) throws SQLException {
        // if we're using managed transactions, or user specified a DBCP driver
        // or DBCP is not on the classpath, then use SimpleDriver
        if (conf == null || conf.isTransactionModeManaged() || conf.isConnectionFactoryModeManaged() ||
                !isDBCPLoaded(getClassLoader())) {
            return getSimpleConnection(props);
        } else {
            // use DBCPDriverDataSource
            return getDBCPConnection(props);
        }
    }
}
