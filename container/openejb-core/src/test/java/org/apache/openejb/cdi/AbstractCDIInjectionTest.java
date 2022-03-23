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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi;

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class AbstractCDIInjectionTest {

    @Inject
    private AppCDI cdi;

    @Module
    public EjbModule app() throws Exception {
        final StatelessBean bean = new StatelessBean(AppJpaDAO.class);
        bean.setLocalBean(new Empty());

        final StatelessBean test = new StatelessBean(AppCDI.class);
        bean.setLocalBean(new Empty());

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);
        ejbJar.addEnterpriseBean(test);

        final Beans beans = new Beans();
        beans.addManagedClass(PlcBaseDAO.class);
        beans.addManagedClass(PlcBaseJpaDAO.class);

        final EjbModule jar = new EjbModule(ejbJar);
        jar.setBeans(beans);

        return jar;
    }

    public static abstract class PlcBaseDAO {
    }

    public static abstract class PlcBaseJpaDAO extends PlcBaseDAO {
    }

    @Stateless
    public static class AppJpaDAO extends PlcBaseJpaDAO {
    }

    public static class AppCDI {
        @Inject
        private PlcBaseDAO baseDao;

        public boolean ok() {
            return baseDao != null;
        }
    }

    @Test
    public void valid() {
        assertTrue(cdi.ok());
    }
}
