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
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class SessionScoped2Test extends TestCase {

    @Inject
    private BeanManager beanManager;

    @Test
    public void test() throws Exception {

        final Green green = createAndMutate(Green.class);

        {
            final Green green2 = getInstance(Green.class);

            assertEquals(green.getMessage(), green2.getMessage());
        }

        final Blue blue = createAndMutate(Blue.class);

        assertEquals(green.getMessage(), blue.getGreen().getMessage());

        {
            final Blue blue2 = getInstance(Blue.class);

            assertEquals(blue.getMessage(), blue2.getMessage());
        }

        final BrownLocal brownLocal = createAndMutate(BrownLocal.class);
        {
            final BrownLocal brownLocal2 = getInstance(BrownLocal.class);

            assertEquals(brownLocal.getMessage(), brownLocal2.getMessage());
        }

        final Green green2 = brownLocal.getGreen();
        green2.getMessage();

        final Orange orange = createAndMutate(Orange.class);
        assertNotNull(orange);
        assertNotNull(orange.getBlue());
        assertNotNull(orange.getBlue().getGreen());
        assertNotNull(orange.getGreen());

        final Green greenA = orange.getBlue().getGreen();
        final Green greenB = orange.getGreen();

        assertSame(greenA, greenB);
    }

    private <T extends Message> T createAndMutate(final Class<T> beanType) {

        final T instance = getInstance(beanType);

        final String prefix = beanType.getSimpleName();

        // Mutate the instance one more time
        instance.setMessage(prefix + ": hello again application");

        return instance;
    }

    private <T extends Message> T getInstance(final Class<T> beanType) {
        final Bean<T> bean = (Bean<T>) beanManager.getBeans(beanType).iterator().next();

        // This should create the instance and put it in the context
        return (T) beanManager.getReference(bean, beanType, beanManager.createCreationalContext(bean));
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
    @SessionScoped
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

    @SessionScoped
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

    @SessionScoped
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

    @SessionScoped
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

    public static interface Message extends java.io.Serializable {

        void setMessage(String id);

        String getMessage();
    }
}
