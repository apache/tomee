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
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class DependentScopedTest extends TestCase {

    @Inject
    private BeanManager beanManager;

    @Test
    public void test() throws Exception {
        created.clear();
        destroyed.clear();

        final Bean<ColorWheelLocal> colorWheelBean = getBean(ColorWheelLocal.class);

        final CreationalContext<ColorWheelLocal> creationalContext = beanManager.createCreationalContext(colorWheelBean);

        final ColorWheelLocal colorWheel = colorWheelBean.create(creationalContext);

        assertEquals(6, created.size());

        assertEquals(6, colorWheel.getColors().size());

        colorWheelBean.destroy(colorWheel, creationalContext);

        assertEquals(6, destroyed.size());

    }

    private <T> Bean<T> getBean(Class<T> beanType) {
        return (Bean<T>) beanManager.getBeans(beanType).iterator().next();
    }


    @Module
    public SessionBean getEjbs() {
        return new StatefulBean(ColorWheel.class);
    }

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
//        beans.addManagedClass(ColorWheel.class);
        beans.addManagedClass(Red.class);
        beans.addManagedClass(Orange.class);
        beans.addManagedClass(Yellow.class);
        beans.addManagedClass(Green.class);
        beans.addManagedClass(Blue.class);
        beans.addManagedClass(Violet.class);
        return beans;
    }

    public static final List<Class> destroyed = new ArrayList<Class>();
    public static final List<Class> created = new ArrayList<Class>();

    public static interface ColorWheelLocal {
        public List<Color> getColors();
    }

    public static class ColorWheel implements ColorWheelLocal {

        private List<Color> colors = new ArrayList<Color>();

        @Inject
        public void set(Red color) {
            colors.add(color);
        }

        @Inject
        public void set(Orange color) {
            colors.add(color);
        }

        @Inject
        public void set(Yellow color) {
            colors.add(color);
        }

        @Inject
        public void set(Green color) {
            colors.add(color);
        }

        @Inject
        public void set(Blue color) {
            colors.add(color);
        }

        @Inject
        public void set(Violet color) {
            colors.add(color);
        }

        public List<Color> getColors() {
            return colors;
        }
    }

    public static class Color {

        public Class get() {
            return getClass();
        }

        @PostConstruct
        public void create() {
            created.add(getClass());
        }

        @PreDestroy
        public void destroy() {
            destroyed.add(getClass());
        }
    }

    public static class Red extends Color {
    }

    public static class Orange extends Color {

    }

    public static class Yellow extends Color {

    }

    public static class Green extends Color {

    }

    public static class Blue extends Color {

    }

    public static class Violet extends Color {

    }

}
