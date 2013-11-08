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
package org.apache.openjpa.persistence.jdbc;

import java.security.AccessController;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAProductDerivation;
import org.apache.openjpa.conf.Specification;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.kernel.JDBCStoreManager;
import org.apache.openjpa.jdbc.sql.MariaDBDictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.lib.conf.AbstractProductDerivation;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.PersistenceProductDerivation;

/**
 * Sets JDBC-specific JPA specification defaults.
 *
 * @author Abe White
 * @nojavadoc
 */
public class JDBCPersistenceProductDerivation 
    extends AbstractProductDerivation 
    implements OpenJPAProductDerivation {
    
    
    public void putBrokerFactoryAliases(Map<String,String> m) {
    }

    public int getType() {
        return TYPE_SPEC_STORE;
    }

    @Override
    public void validate()
        throws Exception {
        // make sure JPA is available
        AccessController.doPrivileged(J2DoPrivHelper.getClassLoaderAction(
            javax.persistence.EntityManagerFactory.class));
    }

    @Override
    public boolean beforeConfigurationLoad(Configuration c) {
        if (c instanceof OpenJPAConfiguration) {
            ((OpenJPAConfiguration) c).getStoreFacadeTypeRegistry().
                registerImplementation(FetchPlan.class, JDBCStoreManager.class, 
                JDBCFetchPlanImpl.class);
        }
        if (!(c instanceof JDBCConfigurationImpl))
            return false;

        JDBCConfigurationImpl conf = (JDBCConfigurationImpl) c;
        Specification jpa = PersistenceProductDerivation.SPEC_JPA;
        Specification ejb = PersistenceProductDerivation.ALIAS_EJB;

        conf.metaFactoryPlugin.setAlias(ejb.getName(),
            PersistenceMappingFactory.class.getName());
        conf.metaFactoryPlugin.setAlias(jpa.getName(),
            PersistenceMappingFactory.class.getName());

        conf.mappingFactoryPlugin.setAlias(ejb.getName(),
            PersistenceMappingFactory.class.getName());
        conf.mappingFactoryPlugin.setAlias(jpa.getName(),
            PersistenceMappingFactory.class.getName());

        conf.mappingDefaultsPlugin.setAlias(ejb.getName(),
            PersistenceMappingDefaults.class.getName());
        conf.mappingDefaultsPlugin.setAlias(jpa.getName(),
            PersistenceMappingDefaults.class.getName());
        
        conf.lockManagerPlugin.setAlias("mixed", "org.apache.openjpa.jdbc.kernel.MixedLockManager");
        
        return true;
    }

    @Override
    public boolean afterSpecificationSet(Configuration c) {
        if (!(c instanceof JDBCConfigurationImpl))
            return false;
        JDBCConfigurationImpl conf = (JDBCConfigurationImpl) c;
        Specification jpa = PersistenceProductDerivation.SPEC_JPA;
        if (!jpa.getName().equals(conf.getSpecificationInstance().getName()))
            return false;
        
        conf.mappingDefaultsPlugin.setDefault(jpa.getName());
        conf.mappingDefaultsPlugin.setString(jpa.getName());
        conf.lockManagerPlugin.setDefault("mixed");
        conf.lockManagerPlugin.setString("mixed");
        return true;
    } 
    
    /**
     * Hint keys correspond to some (not all) bean-style mutable property name in JDBCFetchConfiguration.
     * The fully qualified key is prefixed with <code>openjpa.jdbc</code>.
     */
    private static Set<String> _hints = new HashSet<String>();
    static {
        _hints.add("openjpa.FetchPlan.EagerFetchMode");
        _hints.add("openjpa.FetchPlan.FetchDirection");
        _hints.add("openjpa.FetchPlan.Isolation");
        _hints.add("openjpa.FetchPlan.JoinSyntax");
        _hints.add("openjpa.FetchPlan.LRSSize");
        _hints.add("openjpa.FetchPlan.ResultSetType");
        _hints.add("openjpa.FetchPlan.SubclassFetchMode");
        
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
