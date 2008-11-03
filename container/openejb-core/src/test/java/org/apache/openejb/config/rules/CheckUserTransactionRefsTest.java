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

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.StatelessBean;
import static org.apache.openejb.jee.TransactionType.CONTAINER;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class CheckUserTransactionRefsTest {

    AppModule appModule;

    @Before
    public void setUp() {
        // Stateless session bean with container-managed transaction demarcation
        StatelessBean cmtdSlsb = new StatelessBean("CheckUserTransactionRefsTest", "nopackage.CheckUserTransactionRefsTestBean");
        cmtdSlsb.setTransactionType(CONTAINER);

        // "@Resource UserTransaction tx" declaration
        ResourceRef resourceRef = new ResourceRef();
        resourceRef.setName("nopackage.CheckUserTransactionRefsTestBean/tx");
        resourceRef.setResType("javax.transaction.UserTransaction");

        cmtdSlsb.getResourceRef().add(resourceRef);

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(cmtdSlsb);
        EjbModule ejbModule = new EjbModule(ejbJar);

        appModule = new AppModule(getClass().getClassLoader(), "some/path");
        appModule.getEjbModules().add(ejbModule);

    }

    @Test
    public void testSLSBwithUserTransaction() {

        CheckUserTransactionRefs checkUserTransactionRefs = new CheckUserTransactionRefs();
        // FIXME: It's not possible to run validate(ejbModule) with warn without setting up module in the validate first
        checkUserTransactionRefs.validate(appModule);

        assert appModule.getValidation().getErrors().length == 0;
        assert appModule.getValidation().getWarnings().length == 1;
        assert appModule.getValidation().getFailures().length == 0;
    }

    @After
    public void cleanUp() {
        appModule = null;
    }
}
