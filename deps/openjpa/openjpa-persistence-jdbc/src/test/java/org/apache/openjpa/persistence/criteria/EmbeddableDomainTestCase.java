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
package org.apache.openjpa.persistence.criteria;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.embed.Company1;
import org.apache.openjpa.persistence.embed.Company2;
import org.apache.openjpa.persistence.embed.Department1;
import org.apache.openjpa.persistence.embed.Department2;
import org.apache.openjpa.persistence.embed.Department3;
import org.apache.openjpa.persistence.embed.Division;
import org.apache.openjpa.persistence.embed.Embed;
import org.apache.openjpa.persistence.embed.Embed_Coll_Embed;
import org.apache.openjpa.persistence.embed.Embed_Coll_Integer;
import org.apache.openjpa.persistence.embed.Embed_Embed;
import org.apache.openjpa.persistence.embed.Embed_Embed_ToMany;
import org.apache.openjpa.persistence.embed.Embed_MappedToOne;
import org.apache.openjpa.persistence.embed.Embed_ToMany;
import org.apache.openjpa.persistence.embed.Embed_ToOne;
import org.apache.openjpa.persistence.embed.Employee1;
import org.apache.openjpa.persistence.embed.Employee2;
import org.apache.openjpa.persistence.embed.Employee3;
import org.apache.openjpa.persistence.embed.EmployeeName3;
import org.apache.openjpa.persistence.embed.EmployeePK2;
import org.apache.openjpa.persistence.embed.EntityA_Coll_Embed_Embed;
import org.apache.openjpa.persistence.embed.EntityA_Coll_Embed_ToOne;
import org.apache.openjpa.persistence.embed.EntityA_Coll_String;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Coll_Embed;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Coll_Integer;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Embed;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Embed_ToMany;
import org.apache.openjpa.persistence.embed.EntityA_Embed_MappedToOne;
import org.apache.openjpa.persistence.embed.EntityA_Embed_ToMany;
import org.apache.openjpa.persistence.embed.EntityA_Embed_ToOne;
import org.apache.openjpa.persistence.embed.EntityB1;
import org.apache.openjpa.persistence.embed.Item1;
import org.apache.openjpa.persistence.embed.Item2;
import org.apache.openjpa.persistence.embed.Item3;
import org.apache.openjpa.persistence.embed.VicePresident;

/**
 */
public abstract class EmbeddableDomainTestCase extends AbstractCriteriaTestCase {
    protected static OpenJPAEntityManagerFactorySPI emf = null;
    protected static SQLAuditor auditor = null;

    protected CriteriaBuilder cb = null;
    protected EntityManager em = null;

    protected static Class<?>[] CLASSES =
        { Company1.class, Company2.class, Department1.class, Department2.class, Department3.class, Division.class,
            Embed.class, Embed_Coll_Embed.class, Embed_Coll_Integer.class, Embed_Embed.class, Embed_Embed_ToMany.class,
            Embed_MappedToOne.class, Embed_ToMany.class, Embed_ToOne.class, Employee1.class, Employee2.class,
            Employee3.class, EmployeeName3.class, EmployeePK2.class, EntityA_Coll_Embed_Embed.class,
            EntityA_Coll_Embed_ToOne.class, EntityA_Coll_String.class, EntityA_Embed_Coll_Embed.class,
            EntityA_Embed_Coll_Integer.class, EntityA_Embed_Embed.class, EntityA_Embed_Embed_ToMany.class,
            EntityA_Embed_MappedToOne.class, EntityA_Embed_ToMany.class, EntityA_Embed_ToOne.class, EntityB1.class,
            Item1.class, Item2.class, Item3.class, VicePresident.class };

    protected Class<?>[] getDomainClasses() {
        return CLASSES;
    }

    @Override
    public void setUp() throws Exception {
        if (getEntityManagerFactory() == null) {
            auditor = new SQLAuditor();
            setEntityManagerFactory(createNamedEMF(getDomainClasses()));
            assertNotNull(getEntityManagerFactory());
            setDictionary();
        }
        em = getEntityManagerFactory().createEntityManager();
        cb = getEntityManagerFactory().getCriteriaBuilder();
    }

    @Override
    public void tearDown() throws Exception {
        if (auditor != null) {
            auditor.clear();
            auditor = null;
        }
        if (em != null && em.isOpen()) {
            em.close();
            em = null;
        }
        cb = null;
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
    
    protected OpenJPAEntityManagerFactorySPI getEntityManagerFactory() {
        return emf;
    }

    protected void setEntityManagerFactory(OpenJPAEntityManagerFactorySPI emf) {
        EmbeddableDomainTestCase.emf = emf;
    }

    protected SQLAuditor getAuditor() {
        return auditor;
    }

    protected void setAuditor(SQLAuditor auditor) {
        EmbeddableDomainTestCase.auditor = auditor;
    }

    protected CriteriaBuilder getCriteriaBuilder() {
        return cb;
    }

    protected void setCriteriaBuilder(CriteriaBuilder cb) {
        this.cb = cb;
    }

    protected EntityManager getEntityManager() {
        return em;
    }

    protected void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
