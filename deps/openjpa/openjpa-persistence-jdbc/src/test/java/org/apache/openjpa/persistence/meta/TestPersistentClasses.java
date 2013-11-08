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
package org.apache.openjpa.persistence.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.persistence.meta.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.meta.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.meta.common.apps.RuntimeTest3;
import org.apache.openjpa.persistence.common.utils.*;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

public class TestPersistentClasses
    extends AbstractTestCase {

    public TestPersistentClasses(String test) {
        super(test, "metacactusapp");
    }

    private void assertClass(Class cls, OpenJPAConfiguration conf,
        boolean shouldExist) {
        assertClass(cls.getName(), conf, shouldExist);
    }

    private void assertClass(String clsName, OpenJPAConfiguration conf,
        boolean shouldExist) {

        Collection names = conf.getMetaDataRepositoryInstance().
            getPersistentTypeNames(false, null);

        if (shouldExist)
            assertTrue("expected element " + clsName + " was not found in "
                + names, names.contains(clsName));
        else
            assertTrue("unexpected element " + clsName + " was found in "
                + names, !names.contains(clsName));
    }

    public void testNoneConfigured() {

        Map map = new HashMap();
        map.put("openjpa.MetaDataFactory", "jpa");
        OpenJPAEntityManagerFactory pmf = getEmf(map);

        assertNull(((OpenJPAEntityManagerFactorySPI) pmf).getConfiguration()
            .getMetaDataRepositoryInstance().

            getPersistentTypeNames(false, null));

        pmf.close();
    }

    public void testJPAClasspathScanner() {
        Map map = new HashMap();
        map.put("openjpa.MetaDataFactory",
                "jpa(ClasspathScan=src;jdk1.5-test)");
        OpenJPAEntityManagerFactory pmf = getEmf(map);

        assertClass(RuntimeTest1.class,
            ((OpenJPAEntityManagerFactorySPI) pmf).getConfiguration(), false);

        assertClass(RuntimeTest2.class,
            ((OpenJPAEntityManagerFactorySPI) pmf).getConfiguration(), false);

        assertClass("openjpa.meta.GenericFields",
            ((OpenJPAEntityManagerFactorySPI) pmf).getConfiguration(), true);

        pmf.close();
    }

    public void testTypes() {

        Map map = new HashMap();

        map.put("openjpa.MetaDataFactory",
            "jpa(Types=org.apache.openjpa.persistence.kernel.RuntimeTest1;" +
                "org.apache.openjpa.persistence.kernel.RuntimeTest2)");

        OpenJPAEntityManagerFactory pmf = getEmf(map);
        assertClass(RuntimeTest1.class,
            ((OpenJPAEntityManagerFactorySPI) pmf).getConfiguration(), true);

        assertClass(RuntimeTest2.class,
            ((OpenJPAEntityManagerFactorySPI) pmf).getConfiguration(), true);

        assertClass(RuntimeTest3.class,
            ((OpenJPAEntityManagerFactorySPI) pmf).getConfiguration(), false);

        pmf.close();
    }
}

