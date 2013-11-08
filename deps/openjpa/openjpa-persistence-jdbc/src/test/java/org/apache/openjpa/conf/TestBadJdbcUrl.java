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
package org.apache.openjpa.conf;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import junit.framework.TestCase;

import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.util.UserException;

/**
 * Verifies appropriate exception is thrown when an incorrect protocol or
 * sub-protocol is specified in the JDBC URL. Per the JDBC specification, the
 * Driver should return a null connection upon getConnection() when a bad driver
 * is specified on the URL. OpenJPA must be able to handle this condition and
 * return an appropriate message.
 * 
 * OpenJPA JIRA: {@link http://issues.apache.org/jira/browse/OPENJPA-656}
 * 
 * @author Jeremy Bauer
 * @author Pinaki Poddar
 * 
 */
public class TestBadJdbcUrl extends TestCase {
    public static final String GOOD_URL =
        "jdbc:derby:target/database/openjpa-derby-database;create=true";
    public static final String GOOD_DRIVER =
        "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String GOOD_DATASOURCE =
        "org.apache.commons.dbcp.BasicDataSource";

	public static final String BAD_DRIVER = "bad.driver";
	public static final String BAD_URL_PROTOCOL = "bad.url.protocol";
	public static final String BAD_URL_SUBPROTOCOL = "bad.url.sub.protocol";
    public static final String BAD_CONN_PROPS =
        "connectionUrl=bad,connectionDriver=bad";

	/*
	 * Test specifying URL with bad protocol but a valid Driver.
	 */
	public void testBadUrlProtocolValueWithValidDriverClass() {
		Properties p = new Properties();
		p.put("openjpa.ConnectionDriverName", GOOD_DRIVER);
		p.put("openjpa.ConnectionURL", BAD_URL_PROTOCOL);
		verifyConnectException(p, PersistenceException.class,
                UserException.class, GOOD_DRIVER, BAD_URL_PROTOCOL);
	}

	/*
	 * Test specifying URL with bad protocol but a valid DataSource.
	 */
	public void testBadUrlProtocolValueWithValidDataSource() {
		Properties p = new Properties();
		p.put("openjpa.ConnectionDriverName", GOOD_DATASOURCE);
		p.put("openjpa.ConnectionURL", BAD_URL_PROTOCOL);
		p.put("openjpa.ConnectionProperties", BAD_CONN_PROPS);
		verifyConnectException(p, PersistenceException.class,
				null, (String[])null);
	}

	/*
	 * Test specifying URL with bad sub-protocol but a valid Driver.
	 */
	public void testBadUrlSubprotocolValueWithValidDriverClass() {
		Properties p = new Properties();
		p.put("openjpa.ConnectionDriverName", GOOD_DRIVER);
		p.put("openjpa.ConnectionURL", BAD_URL_SUBPROTOCOL);
		verifyConnectException(p, PersistenceException.class,
                UserException.class, GOOD_DRIVER, BAD_URL_SUBPROTOCOL);
	}

	/*
	 * Test specifying URL with bad sub-protocol but a valid Driver.
	 */
	public void testBadUrlSubprotocolValueWithValidDataSource() {
		Properties p = new Properties();
		p.put("openjpa.ConnectionDriverName", GOOD_DRIVER);
		p.put("openjpa.ConnectionURL", BAD_URL_SUBPROTOCOL);
		verifyConnectException(p, PersistenceException.class,
                UserException.class, GOOD_DRIVER, BAD_URL_SUBPROTOCOL);
	}

	/*
	 * Test specifying Valid URL with an invalid Driver.
	 */
	public void testValidUrlWithInvalidDriver() {
		Properties p = new Properties();
		p.put("openjpa.ConnectionDriverName", BAD_DRIVER);
		p.put("openjpa.ConnectionURL", GOOD_URL);
		verifyConnectException(p, PersistenceException.class,
				UserException.class, GOOD_URL, BAD_DRIVER);
	}

	/**
     * Attempts to connect with given properties and analyze exception for the
	 * existence of given target exception and error message strings.
	 * 
	 * @param props
	 *            the properties to initialize the persistence unit
	 * @param target
	 *            the type expected exception to be raised.
	 * @param nested
     *            the type expected nested exception. null implies not to look
	 *            for any.
	 * @param keys
	 *            the strings that must occur in the exception message.
	 */
	private void verifyConnectException(Properties props, Class targetType,
			Class nestedType, String... keys) {
		EntityManagerFactory emf = null;
		EntityManager em = null;
		try {
            emf = Persistence.createEntityManagerFactory("test", props);
			em = emf.createEntityManager();
			OpenJPAPersistence.cast(em).getConnection();
			fail("Should have caught a " + targetType.getName());
		} catch (Throwable t) {
			assertException(t, targetType, nestedType);
			assertMessage(t, keys);
		} finally {
			if (em != null)
				em.close();
			if (emf != null)
				emf.close();
		}
	}

	/**
     * Asserts that the given targetType is assignable from actual. Asserts that
	 * the nestedType is a nested within the given actual Throwable
	 * 
	 * @param actual
	 * @param targetType
	 * @param nestedType
	 */
	void assertException(final Throwable actual, Class targetType,
			Class nestedTargetType) {
		if (targetType == null)
			return;
		assertNotNull(actual);
		Class actualType = actual.getClass();
		if (!targetType.isAssignableFrom(actualType)) {
			fail(targetType.getName() + " is not assignable from "
					+ actualType.getName());
		}

		if (nestedTargetType != null) {
			Throwable nested = actual.getCause();
            Class nestedType = (nested == null) ? null : nested.getClass();
			while (nestedType != null) {
                if (nestedType.isAssignableFrom(nestedTargetType)) {
					return;
				} else {
					Throwable next = nested.getCause();
					if (next == null || next == nested)
						break;
					nestedType = next.getClass();
					nested     = next;
				}
			}
            fail("No nested type " + nestedTargetType + " in " + actual);
		}
	}

	/**
     * Assert that each of given keys are present in the message of the given
	 * Throwable.
	 */
	void assertMessage(Throwable actual, String... keys) {
		if (actual == null || keys == null)
			return;
		String message = actual.getMessage();
		for (String key : keys) {
            assertTrue(key + " is not in " + message, message.contains(key));
		}
	}
}
