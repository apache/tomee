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
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class ScopedProducerTest extends TestCase {

    @Inject
    BeanManager beanManager;

    @Test
    public void test() {
        ColorProducerLocal colorProducerLocal = getInstance(ColorProducerLocal.class);
        ColorProducerLocal colorProducerLocalB = getInstance(ColorProducerLocal.class);

        colorProducerLocal.setColorClass(Blue.class);

        assertEquals(colorProducerLocal.getColorClass(), colorProducerLocalB.getColorClass());
    }

    private <T> T getInstance(Class<T> beanType) {
        final Bean<T> bean = (Bean<T>) beanManager.getBeans(beanType).iterator().next();

        // This should create the instance and put it in the context

        return (T) beanManager.getReference(bean, beanType, beanManager.createCreationalContext(bean));
    }

    @Module
    public SessionBean ejbs() {
        return new StatefulBean(ColorProducerBean.class);
    }

    public static interface ColorProducerLocal {
        public Class<? extends Color> getColorClass();
        public void setColorClass(Class<? extends Color> colorClass);
        public Color createColor();
        public void destroyColor(@Disposes Color color);

    }

    @ApplicationScoped
    @Stateful
    public static class ColorProducerBean implements ColorProducerLocal {

        private Class<? extends Color> colorClass;

        public Class<? extends Color> getColorClass() {
            return colorClass;
        }

        public void setColorClass(Class<? extends Color> colorClass) {
            this.colorClass = colorClass;
        }

        @Produces
        public Color createColor() {
            return new Red();
        }


        public void destroyColor(@Disposes Color color) {

        }
    }


    public interface Color {

    }

    public static class Red implements Color {

    }

    public static class Blue implements Color {

    }
}
