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
package org.apache.openejb.cdi.transactional;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionScoped;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class TransactionContextTest {
    @Module
    @Classes(cdi = true, value = { TxBean.class, Wrapper.class })
    public EjbJar jar() {
        return new EjbJar();
    }

    @Inject
    private BeanManager bm;

    @Inject
    private Wrapper wrapper;

    @Test(expected = ContextNotActiveException.class)
    public void notActive() {
        bm.getReference(bm.resolve(bm.getBeans(TxBean.class)), TxBean.class, bm.createCreationalContext(null)).toString();
    }

    @Test
    public void normalCase() {
        TxBean.destroyed = false;
        assertNotNull(wrapper.id());
        assertTrue(TxBean.destroyed);
    }

    @Singleton
    public static class Wrapper {
        @Inject
        private TxBean bean;

        public String id() {
            return bean.id();
        }
    }

    @TransactionScoped
    public static class TxBean implements Serializable {
        private static boolean destroyed;

        public String id() {
            try {
                return OpenEJB.getTransactionManager().getTransaction().toString();
            } catch (final SystemException e) {
                throw new IllegalStateException(e);
            }
        }

        @PreDestroy
        public void killed() {
            destroyed = true;
        }
    }
}
