/**
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
package org.apache.openejb.core.security;

import static junit.framework.Assert.assertEquals;

import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.core.security.jaas.UserPrincipal;
import org.apache.openejb.core.security.jaas.UsernamePasswordCallbackHandler;
import org.apache.openejb.util.URLs;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SQLLoginModuleTest {

	private static Connection conn;

	@BeforeClass
    public static void setUp() throws Exception {
        String path = System.getProperty("java.security.auth.login.config");
        if (path == null) {
            URL resource = SQLLoginModuleTest.class.getClassLoader()
					.getResource("login.config");
            if (resource != null) {
                path = URLs.toFilePath(resource);
                System.setProperty("java.security.auth.login.config", path);
            }
        }
        
        // Create the data source and initialize the database tables
        Driver hsqlDriver = (Driver) Class.forName("org.hsqldb.jdbcDriver").newInstance();
        Properties info = new Properties();
        info.setProperty("shutdown", "false");
        Connection conn = hsqlDriver.connect("jdbc:hsqldb:mem:sqltest", info);
//        Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:sqltest");
        conn.createStatement().execute("CREATE TABLE users (username VARCHAR(255), password VARCHAR(255))");
        conn.createStatement().execute("CREATE TABLE groups (grp VARCHAR(255), username VARCHAR(255))");

        // Add users
        PreparedStatement st = conn.prepareStatement("INSERT INTO users VALUES(?, ?)");
        st.setString(1, "jonathan");
        st.setString(2, "secret");
        st.execute();
        st.setString(1, "daniel");
        st.setString(2, "password");
        st.execute();
        st.close();

        // Add roles (groups)
        st = conn.prepareStatement("INSERT INTO groups VALUES(?, ?)");
        st.setString(1, "committer");
        st.setString(2, "jonathan");
        st.execute();
        st.setString(1, "contributor");
        st.setString(2, "daniel");
        st.execute();
        st.setString(1, "community");
        st.setString(2, "jonathan");
        st.execute();
        st.setString(2, "daniel");
        st.execute();
        st.close();

        conn.commit();
        conn.close();


        // debug
        conn = DriverManager.getConnection("jdbc:hsqldb:mem:sqltest", new Properties());
        PreparedStatement statement = conn.prepareStatement("SELECT username, password FROM users");
        ResultSet result = statement.executeQuery();
        int i = 0;
        while (result.next()) {
            i++;
            String userName = result.getString(1);
            String userPassword = result.getString(2);
            System.out.println(userName + "/" + userPassword);
        }
        System.out.println(i);
    }

	@AfterClass
	public static void tearDown() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// Can't do anything about it -- ignore
			}
			conn = null;
		}
	}

    @Test
    public void testLogin() throws LoginException {
        LoginContext context = new LoginContext("SQLLogin",
				new UsernamePasswordCallbackHandler("jonathan", "secret"));
        context.login();

        Subject subject = context.getSubject();

        assertEquals("Should have three principals", 3,
        		subject.getPrincipals().size());
        assertEquals("Should have one user principal", 1,
        		subject.getPrincipals(UserPrincipal.class).size());
        assertEquals("Should have two group principals", 2,
        		subject.getPrincipals(GroupPrincipal.class).size());

        context.logout();

        assertEquals("Should have zero principals", 0,
        		subject.getPrincipals().size());
    }

    @Test(expected = FailedLoginException.class)
    public void testBadUseridLogin() throws LoginException {
        LoginContext context = new LoginContext("SQLLogin",
				new UsernamePasswordCallbackHandler("nobody", "secret"));
        context.login();
    }

    @Test(expected = FailedLoginException.class)
    public void testBadPWLogin() throws LoginException {
        LoginContext context = new LoginContext("SQLLogin",
				new UsernamePasswordCallbackHandler("jonathan", "badpass"));
        context.login();
    }

}
