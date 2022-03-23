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
package org.apache.openejb.appexception;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.ApplicationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;

@RunWith(ApplicationComposer.class)
public class AppExceptionTest {
    @ApplicationException
    public static class RuntimeAppEx extends RuntimeException {
    }

    @ApplicationException
    public static class EjbJarRuntimeAppEx extends RuntimeException {
    }

    @Singleton
    public static class Ejb {
        public void runtime() {
            throw new RuntimeAppEx();
        }

        public void ejbjar() {
            throw new EjbJarRuntimeAppEx();
        }
    }

    @Module
    public EjbJar jar() {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(Ejb.class).localBean());
        return ejbJar;
    }

    @EJB
    private Ejb ejb;

    @Test(expected = RuntimeAppEx.class)
    public void runtime() {
        ejb.runtime();
    }

    @Test(expected = EjbJarRuntimeAppEx.class)
    public void ejbJar() {
        ejb.ejbjar();
    }
}
