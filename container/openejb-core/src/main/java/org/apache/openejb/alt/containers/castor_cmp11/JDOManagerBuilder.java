/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.alt.containers.castor_cmp11;

import org.castor.jdo.conf.Database;
import org.castor.jdo.conf.DatabaseChoice;
import org.castor.jdo.conf.Driver;
import org.castor.jdo.conf.JdoConf;
import org.castor.jdo.conf.Jndi;
import org.castor.jdo.conf.Mapping;
import org.castor.jdo.conf.Param;
import org.castor.jdo.conf.TransactionDemarcation;
import org.castor.jdo.conf.TransactionManager;
import org.exolab.castor.jdo.JDOManager;
import org.exolab.castor.mapping.MappingException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @version $Revision$ $Date$
 */
public class JDOManagerBuilder {
    private static final String GLOBAL_TX_DATABASE = "Global_TX_Database";
    private static final String LOCAL_TX_DATABASE = "Local_TX_Database";

//    // todo replace with ConfigKeys.TRANSACTION_MANAGER_FACTORIES after upgrade to 1.0.1
//    public static final String TRANSACTION_MANAGER_FACTORIES = "org.castor.transactionmanager.Factories";
//
//    static {
//        Configuration config = Configuration.getInstance();
//        String property = config.getProperty(TRANSACTION_MANAGER_FACTORIES, null).trim();
//        if (property != null && property.length() > 0) property += ", ";
//        property += ThreadLocalTransactionManagerFactory.class.getName();
//        config.getProperties().put(TRANSACTION_MANAGER_FACTORIES, property);
//    }
//
    private final String engine;
    private final String transactionManagerJndiName;
    private final javax.transaction.TransactionManager transactionManager;
    private final List<Mapping> mappings = new ArrayList<Mapping>();

    public JDOManagerBuilder(String engine, String transactionManagerJndiName, javax.transaction.TransactionManager transactionManager) {
        this.engine = engine;
        this.transactionManagerJndiName = transactionManagerJndiName;
        this.transactionManager = transactionManager;
    }

    public void addMapping(URL location){
        Mapping mapping = new Mapping();
        mapping.setHref(location.toExternalForm());
        mappings.add(mapping);
    }

    /**
     *
     * <jdo-conf>
     *   <database name="Global_TX_Database" engine="instantdb">
     *     <jndi name="java:openejb/connector/Default JDBC Database" />
     *     <mapping href="default.cmp_mapping.xml" />
     *   </database>
     *   <transaction-demarcation mode="global">
     *     <transaction-manager name="jndi">
     *       <param name="jndiEnc" value="java:openejb/TransactionManager" />
     *     </transaction-manager>
     *   </transaction-demarcation>
     * </jdo-conf>
     *
     * @return
     * @throws MappingException
     * @param dataSourceJndiName
     */
    public JDOManager buildGlobalJDOManager(String dataSourceJndiName) throws MappingException {
//        javax.transaction.TransactionManager oldTransactionManager = ThreadLocalTransactionManagerFactory.transactionManager.get();
//        ThreadLocalTransactionManagerFactory.transactionManager.set(transactionManager);
//        try {

        JdoConf jdoConf = new JdoConf();
        Database database = new Database();
        jdoConf.setDatabase(new Database[]{database});

        database.setName(GLOBAL_TX_DATABASE);
        database.setEngine(engine);

        Jndi jndi = new Jndi();
        jndi.setName(dataSourceJndiName);

        DatabaseChoice databaseChoice = new DatabaseChoice();
        databaseChoice.setJndi(jndi);
        database.setDatabaseChoice(databaseChoice);

        database.setMapping(mappings.toArray(new Mapping[]{}));

        TransactionDemarcation transactionDemarcation = new TransactionDemarcation();
        transactionDemarcation.setMode("global");

        TransactionManager transactionManager = new TransactionManager();
//            transactionManager.setName("threadLocal");
        transactionManager.setName("jndi");
        Param param = new Param();
        param.setName("jndiEnc");
        param.setValue(transactionManagerJndiName);
        transactionManager.setParam(new Param[]{param});

        transactionDemarcation.setTransactionManager(transactionManager);
        jdoConf.setTransactionDemarcation(transactionDemarcation);

        JDOManager.loadConfiguration(jdoConf);

        // Construct a new JDOManager for the database
        return JDOManager.createInstance(database.getName());
//        } finally {
//            ThreadLocalTransactionManagerFactory.transactionManager.set(oldTransactionManager);
//        }
    }

    /**
     *
     * <jdo-conf>
     *   <database name="Local_TX_Database" engine="instantdb">
     *     <driver class-name="org.enhydra.instantdb.jdbc.idbDriver"
     *       url="jdbc:idb:conf/default.idb_database.conf">
     *       <param name="user" value="Admin" />
     *       <param name="password" value="pass" />
     *     </driver>
     *     <mapping href="default.cmp_mapping.xml" />
     *   </database>
     *   <transaction-demarcation mode="local">
     *     <transaction-manager name="jndi">
     *       <param name="jndiEnc" value="java:openejb/TransactionManager" />
     *     </transaction-manager>
     *   </transaction-demarcation>
     * </jdo-conf>
     *
     *
     * @return
     * @throws MappingException
     * @param driverClassName example "org.enhydra.instantdb.jdbc.idbDriver"
     * @param driverUrl example "jdbc:idb:conf/default.idb_database.conf"
     * @param username  example "Admin"
     * @param password  example "pass"
     */
    public JDOManager buildLocalJDOManager(String driverClassName, String driverUrl, String username, String password) throws MappingException {
//        javax.transaction.TransactionManager oldTransactionManager = ThreadLocalTransactionManagerFactory.transactionManager.get();
//        ThreadLocalTransactionManagerFactory.transactionManager.set(transactionManager);
//        try {

        JdoConf jdoConf = new JdoConf();
        Database database = new Database();
        jdoConf.setDatabase(new Database[]{database});

        database.setName(LOCAL_TX_DATABASE);
        database.setEngine(engine);

        Driver driver = new Driver();
        driver.setClassName(driverClassName);
        driver.setUrl(driverUrl);
        Param[] params = {new Param(), new Param()};
        params[0].setName("user");
        params[0].setValue(username);
        params[1].setName("password");
        params[1].setValue(password);
        driver.setParam(params);

        DatabaseChoice databaseChoice = new DatabaseChoice();
        databaseChoice.setDriver(driver);

        database.setDatabaseChoice(databaseChoice);

        database.setMapping(mappings.toArray(new Mapping[]{}));

        TransactionDemarcation transactionDemarcation = new TransactionDemarcation();
        transactionDemarcation.setMode("local");

        TransactionManager transactionManager = new TransactionManager();
//            transactionManager.setName("threadLocal");
        transactionManager.setName("jndi");
        Param param = new Param();
        param.setName("jndiEnc");
        param.setValue(transactionManagerJndiName);
        transactionManager.setParam(new Param[]{param});

        transactionDemarcation.setTransactionManager(transactionManager);

        jdoConf.setTransactionDemarcation(transactionDemarcation);

        JDOManager.loadConfiguration(jdoConf);

        // Construct a new JDOManager for the database
        return JDOManager.createInstance(database.getName());
//        } finally {
//            ThreadLocalTransactionManagerFactory.transactionManager.set(oldTransactionManager);
//        }
    }
}
