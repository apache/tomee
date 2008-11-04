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
package org.apache.openejb.config.rules;

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.StatelessBean;
import static org.apache.openejb.jee.TransactionType.CONTAINER;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class CheckUserTransactionRefsTest {

    private EjbModule module;
    private StatelessBean bean;

    private CheckUserTransactionRefs rule;

    @Before
    public void initialize() {
        bean = new StatelessBean("CheeseEjb", "org.acme.CheeseEjb");
        bean.setTransactionType(CONTAINER);

        module = new EjbModule(new EjbJar());
        module.getEjbJar().addEnterpriseBean(bean);

        rule = new CheckUserTransactionRefs();
        rule.module = module;
    }

    @Test
    public void testSLSBwithUserTransaction() {

        // "@Resource UserTransaction tx" declaration
        ResourceRef resourceRef = new ResourceRef();
        resourceRef.setResRefName("org.acme.CheeseEjb/tx");
        resourceRef.setResType("javax.transaction.UserTransaction");
        resourceRef.getInjectionTarget().add(new InjectionTarget("org.acme.CheeseEjb", "org.acme.CheeseEjb/tx"));
        bean.getResourceRef().add(resourceRef);

        rule.validate(module);

        Assert.assertThat(module.getValidation().getErrors().length, is(1));
        Assert.assertThat(module.getValidation().getWarnings().length, is(0));
        Assert.assertThat(module.getValidation().getFailures().length, is(0));
    }

    @After
    public void cleanUp() {
        module = null;
    }
}
