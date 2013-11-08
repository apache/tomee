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
package org.apache.openjpa.persistence.kernel;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBLobs extends AbstractTestCase {

    private EntityManager _pm = null;
    private Inner _inner = null;

    public TestEJBLobs(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        super.setUp(Inner.class, Inner2.class);

        EntityManager em = currentEntityManager();
        startTx(em);

        endTx(em);
        endEm(em);

        Inner inner = new Inner();
        inner.setString("string");
        inner.setClob("clobField");
        inner.setEBlob("eblob");

        Inner2 inner2 = new Inner2();
        inner2.string = "inner2";
        inner.setBlob(inner2);

        _pm = currentEntityManager();
        startTx(_pm);
        _pm.persist(inner);
        try {
            endTx(_pm);
        }
        catch (Exception jdoe) {
            System.out.println(
                "An exception was thrown while persisting the entity : \n" +
                    getStackTrace(jdoe));
        }
        endEm(_pm);

        _pm = currentEntityManager();
        _inner = _pm.find(Inner.class, "string");
    }

    public void testOtherFields() {
        assertEquals("string", _inner.getString());
    }

    public void testClob() {
        assertEquals("clobField", _inner.getClob());
    }

    public void testBlob() {
        assertNotNull(_inner.getBlob());
        assertEquals("inner2", _inner.getBlob().string);
    }

    public void testSetNull() {
        startTx(_pm);
        _inner.setClob(null);
        _inner.setBlob(null);
        endTx(_pm);

        assertEquals(null, _inner.getBlob());
        assertEquals(null, _inner.getClob());
    }

    public void testDelete() {
        deleteAll(Inner.class);
    }

    public void testUpdate() {
        startTx(_pm);
        _inner.setClob("newvalue");
        Inner2 inner2 = new Inner2();
        inner2.string = "newinner2";
        _inner.setBlob(inner2);
        endTx(_pm);

        assertEquals("newvalue", _inner.getClob());
        assertEquals("newinner2", _inner.getBlob().string);
    }

    @Entity
    @Table(name = "inntable")
    public static class Inner {

        @Id
        private String string = null;
        private String clobField = null;
        private Object eblob = null;

        @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
        private Inner2 blobField = null;

        public String getString() {
            return string;
        }

        public void setString(String val) {
            string = val;
        }

        public String getClob() {
            return clobField;
        }

        public void setClob(String val) {
            clobField = val;
        }

        public String getEBlob() {
            return ((String) eblob);
        }

        public void setEBlob(String val) {
            eblob = val;
        }

        public Inner2 getBlob() {
            return blobField;
        }

        public void setBlob(Inner2 val) {
            blobField = val;
        }
    }

    @SuppressWarnings("serial")
    @Entity
    @Table(name="Inner2")
    public static class Inner2 implements Serializable {

        @Id
        public String string = null;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }
}
