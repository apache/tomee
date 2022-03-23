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
package org.apache.openejb.cdi;

import junit.framework.TestCase;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Local;
import jakarta.ejb.Stateful;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class ApplicationScopedTest extends TestCase {

    @Inject
    private BeanManager beanManager;

    @Test
    public void test() throws Exception {

        final Context appContext = beanManager.getContext(ApplicationScoped.class);


        final Green green = createAndMutate(appContext, Green.class);

        final Blue blue = createAndMutate(appContext, Blue.class);

        assertEquals(green.getMessage(), blue.getGreen().getMessage());

        final BrownLocal brownLocal = createAndMutate(appContext, BrownLocal.class);

        final Green green2 = brownLocal.getGreen();
        green2.getMessage();

        final Orange orange = createAndMutate(appContext, Orange.class);
        assertNotNull(orange);
        assertNotNull(orange.getBlue());
        assertNotNull(orange.getBlue().getGreen());
        assertNotNull(orange.getGreen());

        final Green greenA = orange.getBlue().getGreen();
        final Green greenB = orange.getGreen();

        assertSame(greenA, greenB);
    }

    private <T extends Message> T createAndMutate(final Context context, final Class<T> beanType) {

        final Bean<T> bean = (Bean<T>) beanManager.getBeans(beanType).iterator().next();

        // We haven't created anything yet, so the instance should not exist in the context
        assertNull(context.get(bean));


        final CreationalContext<T> cc1 = beanManager.createCreationalContext(bean);

        // This should create the instance and put it in the context
        final T instance = context.get(bean, cc1);


        // Assert the instance is now in the context and can be generically retrieved
        assertNotNull(context.get(bean));


        final String prefix = beanType.getSimpleName();

        // Mutate the instance...
        instance.setMessage(prefix + ": hello application");

        // Now check the reference in the context
        assertEquals(prefix + ": hello application", context.get(bean, cc1).getMessage());

        // Attempt to create a second instance (should not work)
        final CreationalContext<T> cc2 = beanManager.createCreationalContext(bean);

        // We should still have the same mutated instance as before
        assertEquals(prefix + ": hello application", context.get(bean, cc2).getMessage());

        // Mutate the instance one more time
        instance.setMessage(prefix + ": hello again application");

        // And double check that we still just have the single instance in the context
        assertEquals(prefix + ": hello again application", context.get(bean).getMessage());
        assertEquals(prefix + ": hello again application", context.get(bean, null).getMessage());
        assertEquals(prefix + ": hello again application", context.get(bean, cc1).getMessage());
        assertEquals(prefix + ": hello again application", context.get(bean, cc2).getMessage());

        return instance;
    }

    @Module
    public SessionBean getEjbs() {
        return new StatefulBean(Brown.class);
    }

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(Orange.class);
        beans.addManagedClass(Blue.class);
        beans.addManagedClass(Green.class);
        return beans;
    }

    @Local
    public static interface BrownLocal extends Message {

        public Green getGreen();
    }

    @Stateful
    @ApplicationScoped
    public static class Brown implements BrownLocal {
        private String id;

        @Inject
        private Green green;

        @Override
        public String getMessage() {
            return id;
        }

        @Override
        public void setMessage(final String id) {
            this.id = id;
        }

        @Override
        public Green getGreen() {
            return green;
        }
    }

    @ApplicationScoped
    public static class Orange implements Message {

        private String id;
        private Blue blue;

        public Orange() {
            System.out.println(this.getClass().getName());
        }

        @Override
        public String getMessage() {
            return id;
        }

        @Override
        public void setMessage(final String id) {
            this.id = id;
        }

        @Inject
        private Green green;

        @Inject
        public void setBlue(final Blue blue) {
            this.blue = blue;
        }

        public Blue getBlue() {
            return blue;
        }

        public Green getGreen() {
            return green;
        }
    }

    @ApplicationScoped
    public static class Blue implements Message {

        private String id;
        private Green green;

        public Blue() {
            System.out.println(this.getClass().getName());
        }

        @Override
        public String getMessage() {
            return id;
        }

        @Override
        public void setMessage(final String id) {
            this.id = id;
        }

        public Green getGreen() {
            return green;
        }

        @Inject
        public void setGreen(final Green green) {
            this.green = green;
        }
    }

    @ApplicationScoped
    public static class Green implements Message {

        private String id;

        public Green() {
            "".length();
            System.out.println(this.getClass().getName());
        }

        @Override
        public String getMessage() {
            return id;
        }

        @Override
        public void setMessage(final String id) {
            this.id = id;
        }
    }

    public static interface Message {

        void setMessage(String id);

        String getMessage();
    }
}
