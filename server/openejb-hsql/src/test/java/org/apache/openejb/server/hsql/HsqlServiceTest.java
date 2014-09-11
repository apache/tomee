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
package org.apache.openejb.server.hsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;

public class HsqlServiceTest {

	@Test
	public void testShouldStartDatabaseServerAndReadFromDatabase() throws Exception {

        Properties serviceProps = new Properties();
		
        final Properties initProps = new Properties();
        initProps.put(DeploymentsResolver.DEPLOYMENTS_CLASSPATH_PROPERTY, Boolean.toString(false));
        initProps.put("movieDatabase", "new://Resource?type=DataSource");
        initProps.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        initProps.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
        
        OpenEJB.init(initProps, new ServerFederation());
        
        int port = NetworkUtil.getNextAvailablePort();
        System.out.println("Using port " + port);
        
        HsqlService service = new HsqlService();
        serviceProps.put("bind", "127.0.0.1");
        serviceProps.put("port", String.valueOf(port));
        serviceProps.put("disabled", "false");
        
        service.init(serviceProps);
        service.start();

        Thread.sleep(5000);
        
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
	        Class.forName("org.hsqldb.jdbc.JDBCDriver");
	        connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:" + port + "/moviedb", "sa", "");
	        stmt = connection.prepareStatement("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
	        stmt.execute();
	        stmt.close();
	        connection.close();
        } finally {
        	if (stmt != null) {
        		try {
        			stmt.close();
        		} catch (Exception e) {
        		}
        	}

        	if (connection != null) {
        		try {
        			connection.close();
        		} catch (Exception e) {
        		}
        	}
        }
	}
}
