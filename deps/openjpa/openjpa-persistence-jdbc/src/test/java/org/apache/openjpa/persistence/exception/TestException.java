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
package org.apache.openjpa.persistence.exception;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLErrorCodeReader;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests proper JPA exceptions are raised by the implementation. 
 * Actual runtime type of the raised exception is a subclass of JPA-defined 
 * exception.
 * The raised exception may nest the expected exception. 
 * 
 * @author Pinaki Poddar
 */
public class TestException extends SingleEMFTestCase {
	private static long ID_COUNTER = System.currentTimeMillis();
    
	public void setUp() {
        super.setUp(PObject.class, CLEAR_TABLES);
    }
    
	/**
	 * Tests that when Optimistic transaction consistency is violated, the
     * exception thrown is an instance of javax.persistence.OptimisticException.
	 */
	// TODO: Re-enable this test once OPENJPA-991 issue is corrected.
	public void disabledTestThrowsOptimisticException() {
    
        boolean supportsQueryTimeout = ((JDBCConfiguration) emf
            .getConfiguration()).getDBDictionaryInstance().supportsQueryTimeout;
    
		EntityManager em1 = emf.createEntityManager();
		EntityManager em2 = emf.createEntityManager();
		assertNotEquals(em1, em2);
		
		em1.getTransaction().begin();
		PObject pc = new PObject();
		long id = ++ID_COUNTER;
		pc.setId(id);
		em1.persist(pc);
		em1.getTransaction().commit();
		em1.clear();
		
		em1.getTransaction().begin();
		em2.getTransaction().begin();
		
		PObject pc1 = em1.find(PObject.class, id);
		PObject pc2 = em2.find(PObject.class, id);
		
		assertTrue(pc1 != pc2);
		
		pc1.setName("Modified in TXN1");
        if (supportsQueryTimeout) {
            em1.flush();
            try {
                pc2.setName("Modified in TXN2");
                em2.flush();
                fail("Expected " + OptimisticLockException.class);
            } catch (Throwable t) {
                assertException(t, OptimisticLockException.class);
            }
        } else {
            pc2.setName("Modified in TXN2");
        }
		
		em1.getTransaction().commit();
		try {
			em2.getTransaction().commit();
			fail("Expected " + OptimisticLockException.class);
		} catch (Throwable t) {
			assertException(t, OptimisticLockException.class);
		}
	}
	
	public void testThrowsEntityExistsException() {
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		PObject pc = new PObject();
		long id = ++ID_COUNTER;
		pc.setId(id);
		em.persist(pc);
		em.getTransaction().commit();
		em.clear();
		
		em.getTransaction().begin();
		PObject pc2 = new PObject();
		pc2.setId(id);
		em.persist(pc2);
		try {
			em.getTransaction().commit();
			fail("Expected " + EntityExistsException.class);
		} catch (Throwable t) {
			assertException(t, EntityExistsException.class);
		}
		em.close();
	}
	
	public void testThrowsEntityNotFoundException() {
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		PObject pc = new PObject();
		long id = ++ID_COUNTER;
		pc.setId(id);
		em.persist(pc);
		em.getTransaction().commit();
		
		EntityManager em2 = emf.createEntityManager();
		em2.getTransaction().begin();
		PObject pc2 = em2.find(PObject.class, id);
		assertNotNull(pc2);
		em2.remove(pc2);
		em2.getTransaction().commit();
		
		try {
			em.refresh(pc);
			fail("Expected " + EntityNotFoundException.class);
		} catch (Throwable t) {
			assertException(t, EntityNotFoundException.class);
		}
		em.close();
	}
	
	public void testErrorCodeConfigurationHasAllKnownDictionaries() {
		SQLErrorCodeReader reader = new SQLErrorCodeReader();
		InputStream in = DBDictionary.class.getResourceAsStream
			("sql-error-state-codes.xml");
		assertNotNull(in);
		List<String> names = reader.getDictionaries(in);
		assertTrue(names.size()>=18);
		for (String name:names) {
			try {
                Class.forName(name, false, Thread.currentThread()
                        .getContextClassLoader());
			} catch (Throwable t) {
                fail("DB dictionary " + name + " can not be loaded");
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * Invalid query throws IllegalArgumentException on construction 
	 * as per JPA spec.
	 */
	public void testIllegalArgumennExceptionOnInvalidQuery() {
	    EntityManager em = emf.createEntityManager();
	    try {
	      em.createQuery("This is not a valid JPQL query");
	      fail("Did not throw IllegalArgumentException for invalid query.");
	    } catch (Throwable t) {
		   assertException(t, IllegalArgumentException.class);
	    }
	    em.close();
	}
	
	/**
	 * Invalid named query fails as per spec on factory based construction. 
	 */
     public void testIllegalArgumennExceptionOnInvalidNamedQuery() {
         EntityManager em = emf.createEntityManager();
         try {
             Query query = em.createNamedQuery("This is invalid Named query");
             fail("Did not throw IllegalArgumentException for invalid query.");
         } catch (Throwable t) {
             assertException(t, IllegalArgumentException.class);
         }
         em.close();
      }
	
	/**
     * Asserts that the given expected type of the exception is equal to or a
	 * subclass of the given throwable or any of its nested exception.
     * Otherwise fails assertion and prints the given throwable and its nested
	 * exception on the console. 
	 */
	public void assertException(Throwable t, Class expectedType) {
		if (!isExpectedException(t, expectedType)) {
		    getLog().error("TestException.assertException() - unexpected exception type", t);
			//t.printStackTrace();
			print(t, 0);
            fail(t + " or its cause is not instanceof " + expectedType);
		} else {
		    if (getLog().isTraceEnabled()) {
	            getLog().trace("TestException.assertException() - caught expected exception type=" +
	                expectedType, t);
		    }
		}
	}
	
	/**
	 * Affirms if the given expected type of the exception is equal to or a
	 * subclass of the given throwable or any of its nested exception.
	 */
	boolean isExpectedException(Throwable t, Class expectedType) {
		if (t == null) 
			return false;
		if (expectedType.isAssignableFrom(t.getClass()))
				return true;
		return isExpectedException(t.getCause(), expectedType);
	}
	
	void print(Throwable t, int tab) {
		if (t == null) return;
		StringBuilder str = new StringBuilder(80);
		for (int i=0; i<tab*4;i++)
		    str.append(" ");
		String sqlState = (t instanceof SQLException) ? 
			"(SQLState=" + ((SQLException)t).getSQLState() + ":" 
				+ t.getMessage() + ")" : "";
		str.append(t.getClass().getName() + sqlState);
		getLog().error(str);
		if (t.getCause() == t) 
			return;
		print(t.getCause(), tab+1);
	}
}
