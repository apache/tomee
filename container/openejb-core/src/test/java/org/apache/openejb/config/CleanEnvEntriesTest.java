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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.util.SetAccessible;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.inject.spi.BeanManager;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class CleanEnvEntriesTest extends TestCase {

    @Test
    public void test() throws Exception {

        final Assembler assembler = new Assembler();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final SingletonBean singletonBean = new SingletonBean(Blue.class);
        // keep

        singletonBean.getEnvEntry().add(new EnvEntry()
                .name("message")
                .type(String.class)
                .value("hello")
                .injectionTarget(Blue.class, "message")
        );

        // remove
        singletonBean.getEnvEntry().add(new EnvEntry().name("novalue1").type(String.class));
        singletonBean.getEnvEntry().add(new EnvEntry().name("novalue2"));

        // fill in type
        singletonBean.getEnvEntry().add(new EnvEntry().name("value-but-no-type1").value("10")
                .injectionTarget(Blue.class, "number")
                .injectionTarget(Orange.class, "number") // attempt to confuse the type
        );

        singletonBean.getEnvEntry().add(new EnvEntry().name("value-but-no-type2").value("D")
                .injectionTarget(Blue.class, "letter"))
        ;

        singletonBean.getEnvEntry().add(new EnvEntry().name("value-but-no-type3").value("2")
                .injectionTarget(Blue.class, "vague")    // short
                .injectionTarget(Orange.class, "vague")  // character
        );

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(singletonBean);
        final EjbJarInfo ejbJarInfo = factory.configureApplication(ejbJar);

        final Map<String, EnvEntryInfo> entries = map(ejbJarInfo.enterpriseBeans.get(0).jndiEnc.envEntries);

        assertNotNull(entries.get("comp/env/control"));
        assertNotNull(entries.get("comp/env/value-but-no-type1"));
        assertNotNull(entries.get("comp/env/value-but-no-type2"));

        assertNull(entries.get("comp/env/novalue1"));
        assertNull(entries.get("comp/env/novalue2"));

        assertEquals(Integer.class.getName(), entries.get("comp/env/value-but-no-type1").type);
        assertEquals(Character.class.getName(), entries.get("comp/env/value-but-no-type2").type);
        assertEquals(String.class.getName(), entries.get("comp/env/value-but-no-type3").type);
    }

    private Map<String, EnvEntryInfo> map(List<EnvEntryInfo> envEntries) {
        Map<String, EnvEntryInfo> map = new HashMap<String, EnvEntryInfo>();
        for (EnvEntryInfo entry : envEntries) {
            map.put(entry.referenceName, entry);
        }
        return map;
    }


    public static class Blue {
        private Character letter;
        private Short vague;

        public void setNumber(int number) {
        }

        // False positive if case is not considered
        public void setNumBer(long number) {
        }
    }

    public static class Orange {
        private Object letter;
        private Character vague;

    }
}
