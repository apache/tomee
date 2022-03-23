/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource;

import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class AliasesTest {
    @EJB
    private BeanWithAliasesInjections bean;

    @Test
    public void validAliases() {
        assertTrue(bean.isDs0Ds2());
        assertTrue(bean.isDs0Short());
        assertTrue(bean.isDs0NotDs1());
        assertTrue(bean.checkEm0());
        assertTrue(bean.checkEm1());
    }

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(BeanWithAliasesInjections.class).localBean();
    }

    @Module
    public Persistence persistence0() throws Exception {
        final PersistenceUnit unit = new PersistenceUnit("AliasesTest-unit-0");
        unit.setJtaDataSource("aliased-2");
        return new Persistence(unit);
    }

    @Module
    public Persistence persistence1() throws Exception {
        final PersistenceUnit unit = new PersistenceUnit("AliasesTest-unit-1");
        unit.setJtaDataSource("aliased-1");
        return new Persistence(unit);
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();

        p.put("aliased-0", "new://Resource?type=DataSource&aliases=aliased-2,short");
        p.put("aliased-0.JdbcUrl", "jdbc:hsqldb:mem:aliased0");

        p.put("aliased-1", "new://Resource?type=DataSource");
        p.put("aliased-1.JdbcUrl", "jdbc:hsqldb:mem:aliased1");

        return p;
    }

    public static class BeanWithAliasesInjections {
        @Resource(name = "aliased-0")
        private DataSource ds0;

        @Resource(name = "aliased-1")
        private DataSource ds1;

        @Resource(name = "aliased-2")
        private DataSource ds2;

        @Resource(name = "short")
        private DataSource shortNameDs;

        @jakarta.persistence.PersistenceUnit(unitName = "AliasesTest-unit-0")
        private EntityManagerFactory emf0;

        @jakarta.persistence.PersistenceUnit(unitName = "AliasesTest-unit-1")
        private EntityManagerFactory emf1;

        public boolean isDs0Ds2() {
            return ds0 == ds2;
        }

        public boolean isDs0Short() {
            return ds0 == shortNameDs;
        }

        public boolean isDs0NotDs1() {
            return ds0 != ds1;
        }

        public boolean checkEm0() {
            return ds0 == ReloadableEntityManagerFactory.class.cast(emf0).info().getJtaDataSource();
        }

        public boolean checkEm1() {
            return ds1 == ReloadableEntityManagerFactory.class.cast(emf1).info().getJtaDataSource();
        }
    }
}
