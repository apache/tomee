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
package org.apache.openjpa.jdbc.conf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.BrokerFactoryValue;
import org.apache.openjpa.conf.OpenJPAProductDerivation;
import org.apache.openjpa.jdbc.kernel.JDBCBrokerFactory;
import org.apache.openjpa.jdbc.sql.MariaDBDictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.lib.conf.AbstractProductDerivation;
import org.apache.openjpa.lib.conf.ConfigurationProvider;

/**
 * Sets JDBC as default store.
 */
public class JDBCProductDerivation extends AbstractProductDerivation
    implements OpenJPAProductDerivation {

    public static final String PREFIX = "openjpa.jdbc"; 
    
    public void putBrokerFactoryAliases(Map<String,String> m) {
        m.put("jdbc", JDBCBrokerFactory.class.getName());
    }

    public int getType() {
        return TYPE_STORE;
    }

    public boolean beforeConfigurationConstruct(ConfigurationProvider cp) {
        // default to JDBC when no broker factory set
        if (BrokerFactoryValue.get(cp) == null) {
            BrokerFactoryValue.set(cp, "jdbc");
            return true;
        }
        return false;
    }
    
    /**
     * Hint keys correspond to some (not all) bean-style mutable property name in JDBCFetchConfiguration.
     * The fully qualified key is prefixed with <code>openjpa.jdbc</code>.
     */
    private static Set<String> _hints = new HashSet<String>();
    static {
        _hints.add(PREFIX + ".EagerFetchMode");
        _hints.add(PREFIX + ".FetchDirection");
        _hints.add(PREFIX + ".TransactionIsolation");
        _hints.add(PREFIX + ".JoinSyntax");
        _hints.add(PREFIX + ".LRSSize");
        _hints.add(PREFIX + ".ResultSetType");
        _hints.add(PREFIX + ".SubclassFetchMode");
        
        _hints.add(MariaDBDictionary.SELECT_HINT);
        _hints.add(MySQLDictionary.SELECT_HINT);
        _hints.add(OracleDictionary.SELECT_HINT);
        
        _hints = Collections.unmodifiableSet(_hints);
    }

    @Override
    public Set<String> getSupportedQueryHints() {
        return _hints;
    }
}
