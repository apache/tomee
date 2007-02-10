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
package org.apache.openejb.assembler.classic;

import java.util.Map;
import java.util.TreeMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;

import junit.framework.TestCase;
import org.apache.openejb.BeanType;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.loader.SystemInstance;

public class PersistenceUnitRefTest extends TestCase {
    public void test() throws Exception {
        SystemInstance.get().setComponent(JtaEntityManagerRegistry.class, new JtaEntityManagerRegistry(null));

        JndiEncInfo jndiEncInfo = new JndiEncInfo();

        PersistenceContextReferenceInfo referenceInfo = new PersistenceContextReferenceInfo();
        referenceInfo.referenceName = "one";
        referenceInfo.persistenceUnitName = "one";
        jndiEncInfo.persistenceContextRefs.add(referenceInfo);

        referenceInfo = new PersistenceContextReferenceInfo();
        referenceInfo.referenceName = "two";
        referenceInfo.persistenceUnitName = "two";
        jndiEncInfo.persistenceContextRefs.add(referenceInfo);

        referenceInfo = new PersistenceContextReferenceInfo();
        referenceInfo.referenceName = "#one";
        referenceInfo.persistenceUnitName = "#one";
        jndiEncInfo.persistenceContextRefs.add(referenceInfo);

        referenceInfo = new PersistenceContextReferenceInfo();
        referenceInfo.referenceName = "module.jar#one";
        referenceInfo.persistenceUnitName = "module.jar#one";
        jndiEncInfo.persistenceContextRefs.add(referenceInfo);

        referenceInfo = new PersistenceContextReferenceInfo();
        referenceInfo.referenceName = "dotdot/my/module.jar#one";
        referenceInfo.persistenceUnitName = "../my/module.jar#one";
        jndiEncInfo.persistenceContextRefs.add(referenceInfo);

        referenceInfo = new PersistenceContextReferenceInfo();
        referenceInfo.referenceName = "dotdot/my/dot/module.jar#one";
        referenceInfo.persistenceUnitName = "../my/./module.jar#one";
        jndiEncInfo.persistenceContextRefs.add(referenceInfo);

        referenceInfo = new PersistenceContextReferenceInfo();
        referenceInfo.referenceName = "dotdot/some/dot/other.jar#two";
        referenceInfo.persistenceUnitName = "../some/./other.jar#two";
        jndiEncInfo.persistenceContextRefs.add(referenceInfo);

        Map<String, Map<String, EntityManagerFactory>> allFactories = new TreeMap<String, Map<String, EntityManagerFactory>>();
        Map<String, EntityManagerFactory> myModuleFactories = new TreeMap<String, EntityManagerFactory>();
        allFactories.put("my/module.jar", myModuleFactories);
        myModuleFactories.put("one", new MockEntityManagerFactory());

        Map<String, EntityManagerFactory> otherModuleFactories = new TreeMap<String, EntityManagerFactory>();
        allFactories.put("some/other.jar", otherModuleFactories);
        otherModuleFactories.put("two", new MockEntityManagerFactory());

        JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(jndiEncInfo, null, BeanType.STATELESS, allFactories, "my/module.jar");
        jndiEncBuilder.build();
    }

    private static class MockEntityManagerFactory implements EntityManagerFactory {
        public EntityManager createEntityManager() {
            return null;
        }

        public EntityManager createEntityManager(Map bindings) {
            return null;
        }

        public void close() {
        }

        public boolean isOpen() {
            return false;
        }
    }
}
