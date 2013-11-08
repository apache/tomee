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

public class TestEJBNoPersistentFields extends AbstractTestCase {

    private Nholder holder;

    public TestEJBNoPersistentFields(String test) {
        super(test, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        deleteAll(Nholder.class);
    }

    public void testNoPersistentFields() {
        EntityManager em = currentEntityManager();
        startTx(em);

        holder = new Nholder();
        holder.setNpf(new NoPersistentFieldsPC());
        holder.setIdKey(1);

        em.persist(holder);
        endTx(em);

        Nholder holder2 = em.find(Nholder.class, 1);
        assertEquals(1, holder2.getIdKey());
        assertNotNull(holder2);
        assertNotNull(holder2.getNpf());

        endEm(em);
    }

    @SuppressWarnings("serial")
    @Entity
    @Table(name = "nholder2")
    public static class Nholder implements Serializable {

        @Id
        private int idkey;

        @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
        private NoPersistentFieldsPC npf;

        public Nholder() {
        }

        public Nholder(NoPersistentFieldsPC npf, int idkey) {
            this.npf = npf;
            this.idkey = idkey;
        }

        public void setNpf(NoPersistentFieldsPC npf) {
            this.npf = npf;
        }

        public NoPersistentFieldsPC getNpf() {
            return this.npf;
        }

        public int getIdKey() {
            return idkey;
        }

        public void setIdKey(int idkey) {
            this.idkey = idkey;
        }
    }

    @SuppressWarnings("serial")
    @Entity
    @Table(name = "npfp")
    public static class NoPersistentFieldsPC implements Serializable {

        public transient int blankInt;
        public transient String blankString;
    }
}
