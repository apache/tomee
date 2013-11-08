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
package org.apache.openjpa.jdbc.sql;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;

/**
 * Default factory for SQL abstraction constructs.
 *
 * @author Abe White
 */
public class SQLFactoryImpl
    implements SQLFactory, Configurable {

    private JDBCConfiguration _conf = null;

    /**
     * System configuration.
     */
    public JDBCConfiguration getConfiguration() {
        return _conf;
    }

    public Select newSelect() {
        return new SelectImpl(_conf);
    }

    public Union newUnion(int selects) {
        return new LogicalUnion(_conf, selects);
    }

    public Union newUnion(Select[] selects) {
        return new LogicalUnion(_conf, selects);
    }

    public void setConfiguration(Configuration conf) {
        _conf = (JDBCConfiguration) conf;
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
    }
}
