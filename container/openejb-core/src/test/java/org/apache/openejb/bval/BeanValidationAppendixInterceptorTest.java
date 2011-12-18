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
package org.apache.openejb.bval;

import java.io.Serializable;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.apache.openejb.BeanContext;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class BeanValidationAppendixInterceptorTest {
    @Local public static interface Manager {
        String drive(Person person, @Min(18) int age);
        Person create(@NotNull String name);
    }

    @Remote public static interface ManagerRemote {
        String drive(Person person, @Min(16) int age);
        Person create(String name);
    }

    public static class Person implements Serializable {
        public String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Stateless public static class ManagerBean implements Manager {
        public String drive(Person person, int age) {
            return "vroom";
        }

        public Person create(String name) {
            Person person = new Person();
            person.setName(name);
            return person;
        }
    }

    @Stateless public static class ManagerBean2 implements Manager, ManagerRemote {
        public String drive(Person person, int age) {
            return "vroom";
        }

        public Person create(String name) {
            Person person = new Person();
            person.setName(name);
            return person;
        }
    }

    @Stateless @LocalBean public static class ManagerLocalBean {
        public void foo(@NotNull String bar) {
            // no-op
        }
    }

    @Test public void valid() {
        Person p = mgr.create("foo");
        mgr.drive(p, 18);
    }

    @Test public void notValid() {
        Person p = null;
        try {
            p = mgr.create(null);
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException cvs = (ConstraintViolationException) e.getCause();
            assertEquals(1, cvs.getConstraintViolations().size());
        }
        try {
            mgr.drive(p, 17);
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException cvs = (ConstraintViolationException) e.getCause();
            assertEquals(1, cvs.getConstraintViolations().size());
        }
    }

    @Test public void validRemote() {
        Person p = mgrRemote.create(null);
        mgrRemote.drive(p, 26);
        mgrRemote.drive(p, 17);
    }

    @Test public void notValidRemote() {
        Person p = mgrRemote.create("bar");
        try {
            mgrRemote.drive(p, 15);
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException cvs = (ConstraintViolationException) e.getCause();
            assertEquals(1, cvs.getConstraintViolations().size());
        }
    }

    @Test public void localBean() {
        mgrLB.foo("ok");
        try {
            mgrLB.foo(null);
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException cvs = (ConstraintViolationException) e.getCause();
            assertEquals(1, cvs.getConstraintViolations().size());
        }
    }

    @EJB private Manager mgr;
    @EJB private ManagerRemote mgrRemote;
    @EJB private ManagerLocalBean mgrLB;

    @Configuration public Properties config() {
        final Properties p = new Properties();
        p.put(BeanContext.USER_INTERCEPTOR_KEY, BeanValidationAppendixInterceptor.class.getName());
        return p;
    }

    @Module public EjbJar app() throws Exception {
        EjbJar ejbJar = new EjbJar("bval-interceptor");

        final StatelessBean bean1 = new StatelessBean(ManagerBean.class);
        bean1.addBusinessLocal(Manager.class);
        bean1.setLocalBean(new Empty());

        final StatelessBean bean3 = new StatelessBean(ManagerBean2.class);
        bean3.addBusinessRemote(ManagerRemote.class);
        bean3.addBusinessLocal(Manager.class);
        bean3.setLocalBean(new Empty());

        final StatelessBean bean2 = new StatelessBean(ManagerLocalBean.class);
        bean2.setLocalBean(new Empty());

        ejbJar.addEnterpriseBean(bean1);
        ejbJar.addEnterpriseBean(bean2);
        ejbJar.addEnterpriseBean(bean3);

        return ejbJar;
    }

}

