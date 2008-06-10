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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.deployment.entity.cmp.cmr;


import javax.transaction.Transaction;
import javax.transaction.SystemException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.openejb.deployment.entity.cmp.AbstractCmpTest;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractCMRTest extends AbstractCmpTest {
    protected final AbstractName C_NAME_A = naming.createChildName(moduleName, "A", NameFactory.ENTITY_BEAN);
    protected final AbstractName C_NAME_B = naming.createChildName(moduleName, "B", NameFactory.ENTITY_BEAN);

    protected Object ahome;
    protected Object bhome;

    protected Transaction newTransaction() throws Exception {
        transactionManager.begin();
        return transactionManager.getTransaction();
    }

    protected abstract EJBClass getA();

    protected abstract EJBClass getB();

    protected void setUp() throws Exception {
        super.setUp();

        initCmpModule();

        addCmpEjb("A", getA().bean, null, null, getA().home, getA().local, Integer.class, C_NAME_A);
        addCmpEjb("B", getB().bean, null, null, getB().home, getB().local, Integer.class, C_NAME_B);

        startConfiguration();

        ahome = kernel.getAttribute(C_NAME_A, "ejbLocalHome");
        bhome = kernel.getAttribute(C_NAME_B, "ejbLocalHome");
    }

    protected void completeTransaction(Transaction ctx) throws SystemException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        if (transactionManager.getStatus() == Status.STATUS_ACTIVE) {
            ctx.commit();
        } else {
            ctx.rollback();
        }
    }

    protected class EJBClass {
        public EJBClass(Class bean, Class home, Class local) {
            this.bean = bean;
            this.home = home;
            this.local = local;
        }
        public Class bean;
        public Class home;
        public Class local;
    }
}

