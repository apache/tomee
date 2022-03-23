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
package org.apache.openejb.core.stateless;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodIntf;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class InterfaceTransactionTest extends TestCase {

    @EJB
    private OrangeRemote remote;

    @EJB
    private OrangeLocal local;

    @Module
    public EjbJar module() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(OrangeBean.class));

        final Method remoteMethod = new Method("OrangeBean", "isInTransaction").withInterface(MethodIntf.REMOTE);
        final Method localMethod = new Method("OrangeBean", "isInTransaction").withInterface(MethodIntf.LOCAL);

        final List<ContainerTransaction> transactions = ejbJar.getAssemblyDescriptor().getContainerTransaction();

        transactions.add(new ContainerTransaction(TransAttribute.REQUIRED, remoteMethod));
        transactions.add(new ContainerTransaction(TransAttribute.SUPPORTS, localMethod));

        return ejbJar;
    }

    @Test
    public void test() {

        assertTrue(remote.isInTransaction());
        assertFalse(local.isInTransaction());
    }

    @Local
    public interface OrangeLocal {
        public boolean isInTransaction();
    }

    @Remote
    public static interface OrangeRemote extends OrangeLocal {
    }

    @Stateless
    public static class OrangeBean implements OrangeLocal, OrangeRemote {

        @Override
        public boolean isInTransaction() {
            try {
                return Status.STATUS_ACTIVE == OpenEJB.getTransactionManager().getStatus();
            } catch (final SystemException e) {
                throw new EJBException(e);
            }
        }
    }
}
