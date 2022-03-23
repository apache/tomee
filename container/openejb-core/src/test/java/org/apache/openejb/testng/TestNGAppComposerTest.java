/**
 *
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
package org.apache.openejb.testng;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.testing.Module;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import jakarta.ejb.EJB;
import jakarta.transaction.SystemException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Listeners(ApplicationComposerListener.class)
public class TestNGAppComposerTest {
    @EJB
    private TestNGSingleton singleton;

    @Module
    public EnterpriseBean singleton() {
        return new SingletonBean(TestNGSingleton.class).localBean();
    }

    @Test
    public void notNull() {
        assertNotNull(singleton);
    }

    @Test
    public void ejb() {
        assertTrue(singleton.ejb());
    }

    public static class TestNGSingleton {
        public boolean ejb() {
            try {
                return OpenEJB.getTransactionManager().getTransaction() != null;
            } catch (final SystemException e) {
                return false;
            }
        }
    }
}
