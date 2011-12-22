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
package org.apache.openejb.config;

import junit.framework.TestCase;
import junit.framework.Assert;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.localclient.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.Persistence;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.persistence.PersistenceUnit;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class LocalClientNoInjectionTest extends TestCase {

    public void setUp() throws OpenEJBException, NamingException, IOException {
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        AppModule app = new AppModule(this.getClass().getClassLoader(), "test-app");

        Persistence persistence = new Persistence(new org.apache.openejb.jee.jpa.unit.PersistenceUnit("foo-unit"));
        app.getPersistenceModules().add(new PersistenceModule("root", persistence));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(SuperBean.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        ClientModule clientModule = new ClientModule(null, app.getClassLoader(), app.getJarLocation(), null, null);
        clientModule.getLocalClients().add(this.getClass().getName());

        app.getClientModules().add(clientModule);

        assembler.createApplication(config.configureApplication(app));
    }

    public void test() throws Exception {

        Properties properties = new Properties();
        properties.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        InitialContext context = new InitialContext(properties);
        context.bind("inject", this);

    }

    @Local
    @Remote
    @WebService
    public static interface Everything {
        public Object echo(Object o);
    }

    public static class SuperBean implements Everything {
        public Object echo(Object o) {
            return o;
        }
    }

    public static class Reference implements Serializable {
        private final String value;

        public Reference(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Reference value1 = (Reference) o;

            if (!value.equals(value1.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

}