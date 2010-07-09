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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;

import javax.ejb.Stateful;
import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.LocalBean;
import javax.naming.InitialContext;
import javax.annotation.Resource;
import java.rmi.RemoteException;

/**
 * This test case serves as a nice tiny template for other test cases
 * and purposely doesn't do anything very complicated.
 *
 * @version $Rev$ $Date$
 */
public class StatefulTest extends TestCase {

    @Override
    protected void setUp() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(MyBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    public void test() throws Exception {
        InitialContext context = new InitialContext();
        MyBean myBean = (MyBean) context.lookup("MyBeanLocalBean");

        assertEquals("pan", myBean.echo("nap"));
    }


    @Stateful
    @LocalBean
    public static class MyBean implements SessionBean {

        public String echo(String string) {
            StringBuilder sb = new StringBuilder(string);
            return sb.reverse().toString();
        }

        public void ejbActivate() throws EJBException, RemoteException {
        }

        public void ejbPassivate() throws EJBException, RemoteException {
        }

        public void ejbRemove() throws EJBException, RemoteException {
        }

        @Resource
        public void setStatefulContext(SessionContext statefulContext) {
            System.out.println("StatefulTest$MyBean.setStatefulContext");
        }
        
        public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
            System.out.println("StatefulTest$MyBean.setSessionContext");
        }
    }
}
