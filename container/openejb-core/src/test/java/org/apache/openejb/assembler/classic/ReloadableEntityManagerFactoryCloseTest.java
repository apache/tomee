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
package org.apache.openejb.assembler.classic;

import jakarta.persistence.EntityManagerFactory;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * An application may close a container-managed {@code EntityManagerFactory} itself. Undeploy
 * must then not call {@code close()} on it a second time, which the providers reject with
 * "Attempting to execute an operation on a closed EntityManagerFactory" and which made
 * {@code Assembler.destroyApplication} report an undeploy error.
 *
 * @see <a href="https://issues.apache.org/jira/browse/TOMEE-4650">TOMEE-4650</a>
 */
public class ReloadableEntityManagerFactoryCloseTest {

    @Test
    public void closeIsNotPropagatedToAnAlreadyClosedDelegate() {
        final AtomicInteger closeCalls = new AtomicInteger();
        final EntityManagerFactory delegate = closeCountingEntityManagerFactory(closeCalls);

        final ReloadableEntityManagerFactory remf = newReloadableEntityManagerFactory(delegate);

        // the application closes the EMF itself
        remf.close();
        assertEquals(1, closeCalls.get());

        // undeploy closes it again, which must be a no-op
        remf.close();
        assertEquals("close() must not be propagated to an already closed EntityManagerFactory",
                1, closeCalls.get());
    }

    private ReloadableEntityManagerFactory newReloadableEntityManagerFactory(final EntityManagerFactory delegate) {
        final PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl();
        unitInfo.setPersistenceUnitName("close-test-unit");
        unitInfo.setProperties(new Properties());
        // keep the constructor from eagerly building the real EMF
        unitInfo.setLazilyInitialized(true);

        final EntityManagerFactoryCallable callable = new EntityManagerFactoryCallable(
                "org.apache.openjpa.persistence.PersistenceProviderImpl", unitInfo,
                getClass().getClassLoader(), null, false);

        final ReloadableEntityManagerFactory remf =
                new ReloadableEntityManagerFactory(getClass().getClassLoader(), callable, unitInfo);
        Reflections.set(remf, "delegate", delegate);
        return remf;
    }

    /**
     * A minimal {@code EntityManagerFactory} counting {@code close()} calls and reporting
     * itself as closed afterwards, as the providers do.
     */
    private EntityManagerFactory closeCountingEntityManagerFactory(final AtomicInteger closeCalls) {
        final boolean[] open = {true};

        final InvocationHandler handler = (final Object proxy, final Method method, final Object[] args) -> {
            switch (method.getName()) {
                case "close":
                    closeCalls.incrementAndGet();
                    open[0] = false;
                    return null;
                case "isOpen":
                    return open[0];
                default:
                    return null;
            }
        };

        return EntityManagerFactory.class.cast(Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{EntityManagerFactory.class},
                handler));
    }
}
