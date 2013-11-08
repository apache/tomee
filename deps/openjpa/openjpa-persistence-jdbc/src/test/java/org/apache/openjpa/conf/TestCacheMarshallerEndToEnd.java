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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.query.NamedQueryEntity;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.test.AbstractCachedEMFTestCase;
import org.apache.openjpa.lib.log.Log;

public class TestCacheMarshallerEndToEnd
    extends AbstractCachedEMFTestCase  {

    private static final Object[] STORE_PROPS = new Object[] {
        "openjpa.CacheMarshallers",
        "default(Id=" + MetaDataCacheMaintenance.class.getName()
            + ", OutputFile=target/"
                + MetaDataCacheMaintenance.class.getName() +".ser"
            + ", ConsumeSerializationErrors=false"
            + ", ValidationPolicy="
            + OpenJPAVersionAndConfigurationTypeValidationPolicy.class.getName()
            + ")",
        "openjpa.QueryCompilationCache",
        "java.util.concurrent.ConcurrentHashMap",
        AllFieldTypes.class,
        NamedQueryEntity.class,
        CLEAR_TABLES
    };

    private static final Object[] LOAD_PROPS = new Object[] {
        "openjpa.CacheMarshallers",
        "default(Id=" + MetaDataCacheMaintenance.class.getName()
            + ", InputURL=file:target/"
                + MetaDataCacheMaintenance.class.getName() + ".ser"
            + ", ConsumeSerializationErrors=false"
            + ", ValidationPolicy="
            + OpenJPAVersionAndConfigurationTypeValidationPolicy.class.getName()
            + ")",
        "openjpa.QueryCompilationCache",
        "java.util.concurrent.ConcurrentHashMap",
        AllFieldTypes.class,
        NamedQueryEntity.class
    };


    public void testCacheMarshallerEndToEnd()
        throws IOException {
        OpenJPAEntityManagerFactorySPI emf = createEMF(STORE_PROPS);
        CacheMarshallerImpl cm = (CacheMarshallerImpl)
            CacheMarshallersValue.getMarshallerById(
            emf.getConfiguration(), MetaDataCacheMaintenance.class.getName());
        cm.getOutputFile().delete();
        MetaDataCacheMaintenance maint = new MetaDataCacheMaintenance(
            JPAFacadeHelper.toBrokerFactory(emf), false);
        LogImpl log = new LogImpl();
        maint.setLog(log);
        maint.store();
        assertContains(log.lines, "    " + AllFieldTypes.class.getName());
        assertContains(log.lines, "    " + NamedQueryEntity.class.getName());
        assertContains(log.lines, "    NamedQueryEntity.namedQuery");
        clear(emf);
        closeEMF(emf);

        emf = createEMF(LOAD_PROPS);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new NamedQueryEntity("foo"));
        em.flush();
        Query q = em.createNamedQuery("NamedQueryEntity.namedQuery");
        assertEquals(1, q.getResultList().size());
        em.getTransaction().rollback();
        em.close();
        clear(emf);
        closeEMF(emf);
    }

    private void assertContains(List<String> lines, String prefix) {
        for (String line : lines)
            if (line.startsWith(prefix))
                return;
        fail("should contain a line starting with " + prefix
            + ": " + lines);
    }

    private class LogImpl implements Log {
        private List<String> lines = new ArrayList<String>();

        public boolean isTraceEnabled() {
            return true;
        }

        public boolean isInfoEnabled() {
            return true;
        }

        public boolean isWarnEnabled() {
            return true;
        }

        public boolean isErrorEnabled() {
            throw new UnsupportedOperationException();
        }

        public boolean isFatalEnabled() {
            throw new UnsupportedOperationException();
        }

        public void trace(Object o) {
            lines.add(o.toString());
        }

        public void trace(Object o, Throwable t) {
            throw new UnsupportedOperationException();
        }

        public void info(Object o) {
            lines.add(o.toString());
        }

        public void info(Object o, Throwable t) {
            throw new UnsupportedOperationException();
        }

        public void warn(Object o) {
            lines.add(o.toString());
        }

        public void warn(Object o, Throwable t) {
            lines.add(o.toString());
        }

        public void error(Object o) {
            throw new UnsupportedOperationException();
        }

        public void error(Object o, Throwable t) {
            throw new UnsupportedOperationException();
        }

        public void fatal(Object o) {
            throw new UnsupportedOperationException();
        }

        public void fatal(Object o, Throwable t) {
            throw new UnsupportedOperationException();
        }
    }
}
