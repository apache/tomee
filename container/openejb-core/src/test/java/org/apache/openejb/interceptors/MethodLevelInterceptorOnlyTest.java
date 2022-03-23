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
package org.apache.openejb.interceptors;

import junit.framework.TestCase;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class MethodLevelInterceptorOnlyTest extends TestCase {

    @EJB
    private MethodLevelInterceptorOnlyParent bean;

    @Module
    public EjbJar module() {
        final EjbJar ejbJar = new EjbJar();

        final StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(MethodLevelInterceptorOnlySLSBean.class));

        final AssemblyDescriptor assembly = ejbJar.getAssemblyDescriptor();

        assembly.addInterceptorBinding(new InterceptorBinding("*", new Interceptor(DefaultInterceptorOne.class)));
        assembly.addInterceptorBinding(new InterceptorBinding("*", new Interceptor(DefaultInterceptorTwo.class)));
        assembly.addInterceptorBinding(new InterceptorBinding(bean)).setExcludeDefaultInterceptors(true);
        return ejbJar;
    }

    @Test
    public void testInterceptedGenerifiedBusinessIntfMethod() throws Exception {

        assert bean != null;

        final List<String> expected = new ArrayList<>();
        expected.add("MethodLevelInterceptorOne");
        expected.add("makePersistent");

        final List<String> actual = bean.makePersistent(null);
        assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
    }
}