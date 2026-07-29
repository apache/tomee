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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.loader.SystemInstance;

import junit.framework.TestCase;

public class AppNamingReadOnlyTest extends TestCase {

    private AppContext appContext;

    @Override
    protected void tearDown() throws Exception {
        SystemInstance.reset();
        super.tearDown();
    }

    public void testReadOnlyAppNamingContext() throws SystemException, URISyntaxException {
        final List<BeanContext> beanContexts = getMockBeanContextsList();
        appContext.setReadOnlyAppNamingContext(true);

        final Assembler assembler = new Assembler();
        assertTrue(assembler.setAppNamingContextReadOnly(beanContexts));

        assertWriteRefused(beanContexts.get(0).getJndiContext());
    }

    // the shared application context must stay writable while further modules can still bind into it
    public void testAppContextStaysWritableUntilTheLastModule() throws Exception {
        final List<BeanContext> beanContexts = getMockBeanContextsList();
        appContext.setReadOnlyAppNamingContext(true);
        // one web module still to come, as for an ear whose war is started by TomcatWebAppBuilder
        appContext.setPendingLateModules(1);

        final Assembler assembler = new Assembler();
        assertTrue(assembler.setAppNamingContextReadOnly(beanContexts));

        // the bean context is closed straight away, it can no longer receive bindings
        assertWriteRefused(beanContexts.get(0).getJndiContext());

        // but the application context still accepts the container's own bindings, as JndiBuilder
        // does for the ejb modules of an ear's web modules, which deploy later
        assertNotNull(appContext.getAppJndiContext().createSubcontext("app/late"));

        // and once the last module is in, it closes too
        assertTrue(assembler.setAppNamingContextReadOnly(beanContexts));
        assertWriteRefused(appContext.getAppJndiContext());
    }

    // an ear with two web modules only closes its application context on the third pass: the one
    // createApplication does, then one per web module started by the web app builder
    public void testAppContextWaitsForEveryLateModule() throws Exception {
        getMockBeanContextsList();
        appContext.setPendingLateModules(2);

        assertFalse("createApplication pass must not close the app context", appContext.lastModuleDeployed());
        assertFalse("first web module must not close the app context", appContext.lastModuleDeployed());
        assertTrue("last web module closes the app context", appContext.lastModuleDeployed());
        // and it stays closed for any further call
        assertTrue(appContext.lastModuleDeployed());
    }

    // a standalone module is fully deployed in one pass, so it closes immediately
    public void testStandaloneModuleClosesOnTheFirstPass() throws Exception {
        getMockBeanContextsList();
        appContext.setPendingLateModules(0);

        assertTrue(appContext.lastModuleDeployed());
    }

    // opting in gives the read only component naming context the specification requires
    // (EE.5.3.4, Enterprise Beans 10.4.4)
    public void testAppNamingContextReadOnlyWhenEnabled() throws SystemException, URISyntaxException {
        final List<BeanContext> beanContexts = getMockBeanContextsList();
        // the flag is what createApplication derives from the properties
        appContext.setReadOnlyAppNamingContext(true);

        final Assembler assembler = new Assembler();
        assertTrue(assembler.setAppNamingContextReadOnly(beanContexts));

        assertWriteRefused(beanContexts.get(0).getJndiContext());
    }

    // the naming context stays writable unless the application opts in
    public void testAppNamingContextWritableByDefault() throws SystemException, URISyntaxException, NamingException {
        final List<BeanContext> beanContexts = getMockBeanContextsList();
        appContext.setReadOnlyAppNamingContext(false);

        final Assembler assembler = new Assembler();
        assertFalse(assembler.setAppNamingContextReadOnly(beanContexts));

        assertNotNull(beanContexts.get(0).getJndiContext().createSubcontext("sub"));
    }

    /**
     * A read only context either throws OperationNotSupportedException or silently ignores the write,
     * depending on openejb.jndiExceptionOnFailedWrite. This only checks that the context is marked.
     */
    private void assertWriteRefused(final Context context) {
        try {
            assertNull(context.createSubcontext("sub"));
        } catch (final OperationNotSupportedException e) {
            // ok
        } catch (final NamingException e) {
            throw new AssertionError(e);
        }
    }

    private List<BeanContext> getMockBeanContextsList() throws SystemException, URISyntaxException {
        final IvmContext beanJndiContext = new IvmContext();
        final IvmContext appJndiContext = new IvmContext();

        appContext = new AppContext("appId", SystemInstance.get(), this.getClass().getClassLoader(),
                appJndiContext, appJndiContext, false);
        final ModuleContext mockModuleContext = new ModuleContext("moduleId", new URI(""), "uniqueId", appContext,
                beanJndiContext, this.getClass().getClassLoader());
        final BeanContext mockBeanContext = new BeanContext("test", beanJndiContext, mockModuleContext,
                this.getClass(), this.getClass(), new HashMap<>());

        final List<BeanContext> beanContextsList = new ArrayList<>();
        beanContextsList.add(mockBeanContext);

        return beanContextsList;
    }
}
