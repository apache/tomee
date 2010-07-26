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

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.AfterBegin;
import javax.ejb.AfterCompletion;
import javax.ejb.BeforeCompletion;
import javax.ejb.EJBException;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.SessionSynchronization;

import junit.framework.TestCase;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ValidationRunner.class)
public class CheckInvalidCallbacksTest extends TestCase {
    @Keys( { @Key(value = "ignoredAnnotation", count = 10, type = KeyType.WARNING), @Key("callback.missing.possibleTypo"), @Key("callback.badReturnType"),
            @Key("callback.badModifier"), @Key("callback.invalidArguments"), @Key("aroundInvoke.missing"), @Key("callback.missing"),
            @Key(value="callback.sessionSynchronization.invalidUse",count=2) })
    public EjbJar test() throws Exception {
        EjbJar ejbJar = new EjbJar();
        StatelessBean testBean = ejbJar.addEnterpriseBean(new StatelessBean("TestStateless", TestBean.class));
        testBean.addAroundInvoke("wrongMethod");
        testBean.addPostConstruct("wrongMethod");
        ejbJar.addEnterpriseBean(new SingletonBean("TestSingleton", TestBean.class));
        ejbJar.addEnterpriseBean(new StatefulBean("FooStateful", FooBean.class));
        ejbJar.addEnterpriseBean(new StatefulBean("BarStateful", BarBean.class));
        StatefulBean starBean = ejbJar.addEnterpriseBean(new StatefulBean("StarStateful", StarBean.class));
        starBean.setAfterBeginMethod(new NamedMethod("myAfterBegin"));
        starBean.setBeforeCompletionMethod(new NamedMethod("myBeforeCompletion"));
        starBean.setAfterCompletionMethod(new NamedMethod("myAfterCompletion"));
        return ejbJar;
    }

    public static class TestBean implements Callable {
        public Object call() throws Exception {
            return null;
        }

        @PostConstruct
        public void myConstruct() {}

        @PreDestroy
        public void myDestroy() {}

        @PostActivate
        public void myActivate() {}

        @PrePassivate
        public void myPassivate() {}

        @AfterBegin
        public void myAfterBegin() {}

        @BeforeCompletion
        public void beforeCompletion() {}

        @AfterCompletion
        public void afterCompletion(boolean committed) {}
    }

    public static class FooBean {
        @PostConstruct
        public Object myConstruct() {
            return null;
        }

        @PreDestroy
        public static final void myDestroy() {}

        @PostActivate
        public void myActivate(Object anInvalidArgument) {}

        @PrePassivate
        public void myPassivate() {}

        @AfterBegin
        public void myAfterBegin() {}

        @BeforeCompletion
        public void beforeCompletion() {}

        @AfterCompletion
        public void afterCompletion(boolean committed) {}

        @AfterCompletion
        public void afterCompletionTypo() {}
    }

    public static class BarBean implements SessionSynchronization {
        @AfterBegin
        public void myAfterBegin() {}

        @BeforeCompletion
        public void myBeforeCompletion() {}

        @AfterCompletion
        public void myAfterCompletion(boolean committed) {}

        @Override
        public void afterBegin() throws EJBException, RemoteException {}

        @Override
        public void afterCompletion(boolean arg0) throws EJBException, RemoteException {}

        @Override
        public void beforeCompletion() throws EJBException, RemoteException {}
    }

    public static class StarBean implements SessionSynchronization {
        public void myAfterBegin() {}

        public void myBeforeCompletion() {}

        public void myAfterCompletion(boolean committed) {}

        @Override
        public void afterBegin() throws EJBException, RemoteException {}

        @Override
        public void afterCompletion(boolean arg0) throws EJBException, RemoteException {}

        @Override
        public void beforeCompletion() throws EJBException, RemoteException {}
    }
}