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
package org.apache.openejb.core.ivm.naming;

import junit.framework.TestCase;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.loader.SystemInstance;

import javax.naming.Context;
import javax.naming.OperationNotSupportedException;

/**
 * The Enterprise Beans spec (10.4.4) and EE.5.3.4 require the component naming context to be read-only:
 * writes against java:comp and friends must not take effect.
 */
public class JavaCompReadOnlyTest extends TestCase {

    private AppContext deploy() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar("testmodule");
        ejbJar.addEnterpriseBean(new SingletonBean(Bean.class));

        final AppModule module = new AppModule(new EjbModule(ejbJar));
        return assembler.createApplication(config.configureApplication(module));
    }

    public void testCompContextRefusesWrites() throws Exception {
        final AppContext app = deploy();
        try {
            final BeanContext bean = app.getBeanContexts().get(0);
            final Context comp = bean.getJndiContext();

            assertWriteRefused(comp, "bind", () -> comp.bind("newName", "newValue"));
            assertWriteRefused(comp, "rebind", () -> comp.rebind("newName", "newValue"));
            assertWriteRefused(comp, "rename", () -> comp.rename("comp", "renameTo"));
            assertWriteRefused(comp, "unbind", () -> comp.unbind("comp"));
            assertWriteRefused(comp, "destroySubcontext", () -> comp.destroySubcontext("comp"));

            // createSubcontext either throws or returns null, depending on jndiExceptionOnFailedWrite
            try {
                assertNull(comp.createSubcontext("newName"));
            } catch (final OperationNotSupportedException expected) {
                // ok
            }

            // nothing the writes attempted may be observable afterwards
            assertNotBound(comp, "newName");
            assertNotBound(comp, "renameTo");

            // and the pre-existing binding must have survived unbind/rename/destroySubcontext
            assertTrue(comp.lookup("comp") instanceof Context);
        } finally {
            SystemInstance.reset();
        }
    }

    public void testAppContextRefusesWrites() throws Exception {
        final AppContext app = deploy();
        try {
            final Context appCtx = app.getAppJndiContext();

            assertWriteRefused(appCtx, "bind", () -> appCtx.bind("newName", "newValue"));
            assertNotBound(appCtx, "newName");
            assertTrue(appCtx.lookup("app") instanceof Context);
        } finally {
            SystemInstance.reset();
        }
    }

    private interface Write {
        void run() throws Exception;
    }

    private void assertWriteRefused(final Context ctx, final String operation, final Write write) {
        try {
            write.run();
            fail(operation + " should have been refused on a read-only naming context");
        } catch (final OperationNotSupportedException expected) {
            // ok
        } catch (final Exception e) {
            throw new AssertionError("unexpected exception from " + operation, e);
        }
    }

    private void assertNotBound(final Context ctx, final String name) throws Exception {
        try {
            assertNull(name + " must not be bound", ctx.lookup(name));
        } catch (final javax.naming.NameNotFoundException expected) {
            // ok
        }
    }

    public static class Bean {
    }
}
