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

package org.apache.openejb.assembler.classic.migrate.database;

import javax.sql.DataSource;

import org.hsqldb.Server;
import org.junit.Before;
import org.junit.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This class is to be used instead of the ImportSql class, to import scripts
 * into the database.
 * 
 * @version $Rev$ $Date$
 */
public class ImportByLiquibaseTest {
	
	final String DB_PATH = "mem:testdb;sql.enforce_strict_size=true;sql.restrict_exec=true";
	
	@Before
	public void createDatabase() {
		Server server = new Server();
		server.setDatabaseName(0, "testdb");
		server.setDatabasePath(0, DB_PATH);		
		server.setPort(9001); // default port
		server.start();
	}

	@Test
	public void test() throws Exception {
		final ClassLoader classLoader = getClass().getClassLoader();
		final String RESOURCE = "src/test/resources"; 
		
		final ImportByLiquibase importByLiquibase = new ImportByLiquibase(classLoader, RESOURCE, getDataSource());
		importByLiquibase.doImport();
		importByLiquibase.doValidate(); 
		
	}

	private DataSource getDataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
		final String url = "jdbc:hsqldb:" + DB_PATH + ";ifexists=true;shutdown=true;";
		hikariConfig.setJdbcUrl(url);		
		hikariConfig.setUsername("sa");
		hikariConfig.setPassword("");

		hikariConfig.setMaximumPoolSize(10); 
		hikariConfig.setConnectionTestQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
		hikariConfig.setPoolName("hikariCP");

		HikariDataSource dataSource = new HikariDataSource(hikariConfig);

		return dataSource;
	}

}