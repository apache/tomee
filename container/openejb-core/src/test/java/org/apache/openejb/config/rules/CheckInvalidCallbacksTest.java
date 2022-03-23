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

import junit.framework.TestCase;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.AfterBegin;
import jakarta.ejb.AfterCompletion;
import jakarta.ejb.BeforeCompletion;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.SessionSynchronization;
import jakarta.interceptor.InvocationContext;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ValidationRunner.class)
public class CheckInvalidCallbacksTest extends TestCase {
    @Keys({@Key(value = "ignoredMethodAnnotation", count = 10, type = KeyType.WARNING), @Key("callback.invalidArguments"), @Key("callback.badReturnType"),
        @Key("callback.badModifier"), @Key("callback.invalidArguments"), @Key("aroundInvoke.missing"), @Key("callback.missing"),
        @Key(value = "callback.sessionSynchronization.invalidUse", count = 2)})
    public EjbJar test() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean testBean = ejbJar.addEnterpriseBean(new StatelessBean("TestStateless", TestBean.class));
        testBean.addAroundInvoke("wrongMethod");
        testBean.addPostConstruct("wrongMethod");
        ejbJar.addEnterpriseBean(new SingletonBean("TestSingleton", TestBean.class));
        ejbJar.addEnterpriseBean(new StatefulBean("FooStateful", FooBean.class));
        ejbJar.addEnterpriseBean(new StatefulBean("BarStateful", BarBean.class));
        final StatefulBean starBean = ejbJar.addEnterpriseBean(new StatefulBean("StarStateful", StarBean.class));
        starBean.setAfterBeginMethod(new NamedMethod("myAfterBegin"));
        starBean.setBeforeCompletionMethod(new NamedMethod("myBeforeCompletion"));
        starBean.setAfterCompletionMethod(new NamedMethod("myAfterCompletion"));
        return ejbJar;
    }

    @Keys(@Key("aroundInvoke.missing.possibleTypo"))
    public EjbJar test1() {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean testBean = ejbJar.addEnterpriseBean(new StatelessBean(MoonBean.class));
        testBean.addAroundInvoke("foo");
        return ejbJar;
    }

    @Keys(@Key(value = "callback.sessionbean.invalidusage", count = 6))
    public EjbJar test2() {
        System.setProperty("openejb.validation.output.level", "VERBOSE");
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean sun = ejbJar.addEnterpriseBean(new StatelessBean("SunStateless", Sun.class));
        sun.setLocalHome(SunLocalHome.class.getName());
        sun.setLocal(SunLocal.class.getName());
        final StatefulBean meteor = ejbJar.addEnterpriseBean(new StatefulBean("MeteorStateful", Meteor.class));
        meteor.setLocal(SunLocal.class.getName());
        meteor.setLocalHome(SunLocalHome.class.getName());
        return ejbJar;
    }

    @Keys(@Key(value = "callback.invocationcontext.notallowed", count = 4))
    public EjbJar test3() {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(CallbackViolatorBean.class));
        return ejbJar;
    }

    public static class TestBean implements Callable {
        public Object call() throws Exception {
            return null;
        }

        @PostConstruct
        public void myConstruct() {
        }

        @PreDestroy
        public void myDestroy() {
        }

        @PostActivate
        public void myActivate() {
        }

        @PrePassivate
        public void myPassivate() {
        }

        @AfterBegin
        public void myAfterBegin() {
        }

        @BeforeCompletion
        public void beforeCompletion() {
        }

        @AfterCompletion
        public void afterCompletion(final boolean committed) {
        }
    }

    public static class FooBean {
        @PostConstruct
        public Object myConstruct() {
            return null;
        }

        @PreDestroy
        public static final void myDestroy() {
        }

        @PostActivate
        public void myActivate(final Object anInvalidArgument) {
        }

        @PrePassivate
        public void myPassivate() {
        }

        @AfterBegin
        public void myAfterBegin() {
        }

        @BeforeCompletion
        public void beforeCompletion() {
        }

        @AfterCompletion
        public void afterCompletion(final boolean committed) {
        }

        @AfterCompletion
        public void afterCompletionTypo() {
        }
    }

    public static class BarBean implements SessionSynchronization {
        @AfterBegin
        public void myAfterBegin() {
        }

        @BeforeCompletion
        public void myBeforeCompletion() {
        }

        @AfterCompletion
        public void myAfterCompletion(final boolean committed) {
        }

        @Override
        public void afterBegin() throws EJBException, RemoteException {
        }

        @Override
        public void afterCompletion(final boolean arg0) throws EJBException, RemoteException {
        }

        @Override
        public void beforeCompletion() throws EJBException, RemoteException {
        }
    }

    public static class StarBean implements SessionSynchronization {
        public void myAfterBegin() {
        }

        public void myBeforeCompletion() {
        }

        public void myAfterCompletion(final boolean committed) {
        }

        @Override
        public void afterBegin() throws EJBException, RemoteException {
        }

        @Override
        public void afterCompletion(final boolean arg0) throws EJBException, RemoteException {
        }

        @Override
        public void beforeCompletion() throws EJBException, RemoteException {
        }
    }

    public static class MoonBean {
        public Object foo() {
            return null;
        }

        public void foo(final String str) {
        }
    }

    public static interface SunLocalHome extends EJBLocalHome {
        public SunLocal create() throws CreateException;
    }

    public static interface SunLocal extends EJBLocalObject {
    }

    public class Sun implements SessionBean {
        @PostConstruct
        public void myPostConstruct() {
        }

        @PreDestroy
        public void myPreDestroy() {
        }

        @PostConstruct
        public void ejbCreate() throws CreateException {
        }

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbRemove() throws EJBException, RemoteException {
        }

        @Override
        public void setSessionContext(final SessionContext arg0) throws EJBException, RemoteException {
        }
    }

    public class Meteor implements SessionBean {
        @PostConstruct
        public void myPostConstruct() {
        }

        @PreDestroy
        public void myPreDestroy() {
        }

        public void ejbCreate() throws CreateException {
        }

        @Override
        @PostActivate
        public void ejbActivate() throws EJBException, RemoteException {
        }

        @Override
        @PrePassivate
        public void ejbPassivate() throws EJBException, RemoteException {
        }

        @Override
        @PreDestroy
        public void ejbRemove() throws EJBException, RemoteException {
        }

        @Override
        public void setSessionContext(final SessionContext arg0) throws EJBException, RemoteException {
        }

        @PostActivate
        public void myPostActivate() {
        }

        @PrePassivate
        public void myPrePassivate() {
        }

        @AfterBegin
        public void myAfterBegin() {
        }

        @BeforeCompletion
        public void beforeCompletion() {
        }

        @AfterCompletion
        public void afterCompletion(final boolean committed) {
        }
    }

    public class CallbackViolatorBean {
        @PostConstruct
        public void postConstruct(final InvocationContext ic) {
        }

        @PreDestroy
        public void preDestroy(final InvocationContext ic) {
        }

        @PrePassivate
        public void prePassivate(final InvocationContext ic) {
        }

        @PostActivate
        public void postActivate(final InvocationContext ic) {
        }
    }
}