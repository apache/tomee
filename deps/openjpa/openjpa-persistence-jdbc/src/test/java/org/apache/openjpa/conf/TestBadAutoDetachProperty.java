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

import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.apache.openjpa.lib.util.ParseException;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestBadAutoDetachProperty extends AbstractPersistenceTestCase {
    public void testEmptyValue() {
        HashMap props = new HashMap(System.getProperties());
        props.put("openjpa.AutoDetach", "");
        EntityManagerFactory emf = createNamedEMF("test", props);
        EntityManager em = emf.createEntityManager();
        clear(emf);
        closeEMF(emf);
    }

    public void testCommaOnlyValue() {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            HashMap props = new HashMap(System.getProperties());
            props.put("openjpa.AutoDetach", ",");
            emf = createNamedEMF("test", props);
            em = emf.createEntityManager();
        } catch (PersistenceException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                // Geronimo Persistence.class single provider semantics
                while (cause instanceof PersistenceException)
                    cause = ((PersistenceException) cause).getCause();
                if (!(cause instanceof ParseException)) {
                    fail("Should have caught PersistenceException whose cause was " + "a ParseException. "
                            + "Instead the cause was: " + cause);
                }
            } else {
                // Geronimo Persistence.class multiple providers semantics
                String msg = e.getMessage();
                if (msg.indexOf("org.apache.openjpa.lib.util.ParseException") == -1)
                    fail("Should have caught PersistenceException whose cause was " + "a ParseException. "
                            + "Instead the cause was: " + cause);
            }
        } catch (RuntimeException e) {
            fail("Should have caught a PersistenceException, instead caught: "
                    + e);
        } finally {
            clear(emf);
            closeEMF(emf);
        }
    }

    public void testEmptyItemValue() {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            HashMap props = new HashMap(System.getProperties());
            props.put("openjpa.AutoDetach", "close,,commit");
            emf = createNamedEMF("test", props);
            em = emf.createEntityManager();
        } catch (PersistenceException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                // Geronimo Persistence.class single provider semantics
                while (cause instanceof PersistenceException)
                    cause = ((PersistenceException) cause).getCause();
                if (!(cause instanceof ParseException)) {
                    fail("Should have caught PersistenceException whose cause was " + "a ParseException. "
                            + "Instead the cause was: " + cause);
                }
            } else {
                // Geronimo Persistence.class multiple providers semantics
                String msg = e.getMessage();
                if (msg.indexOf("org.apache.openjpa.lib.util.ParseException") == -1)
                    fail("Should have caught PersistenceException whose cause was " + "a ParseException. "
                            + "Instead the cause was: " + cause);
            }
        } catch (RuntimeException e) {
            fail("Should have caught a PersistenceException, instead caught: "
                    + e);
        } finally {
            clear(emf);
            closeEMF(emf);
        }
    }
}
