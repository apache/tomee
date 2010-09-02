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

import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ValidationRunner.class)
public class CheckUserTransactionRefsTest  {

    @Keys(@Key("userTransactionRef.forbiddenForCmtdBeans"))
    public EjbJar testSLSBwithUserTransaction() throws Exception {
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(TestBean.class));
        return ejbJar;
    }

    @Stateless
    @TransactionManagement(TransactionManagementType.CONTAINER)
    public static class TestBean implements Callable {

        @Resource
        private UserTransaction userTransaction;

        public Object call() throws Exception {
            return null;
        }
    }

}
