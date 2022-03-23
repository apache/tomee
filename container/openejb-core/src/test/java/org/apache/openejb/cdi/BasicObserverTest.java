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

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class BasicObserverTest {

    @Inject
    private BeanManager beanManager;

    @Test
    public void test() throws Exception {
        assertNotNull(beanManager);

        final Catastrophy catastrophy = new Catastrophy();
        beanManager.fireEvent(catastrophy);

        assertEquals(1, catastrophy.getClasses().size());
        assertEquals(SuperHero.class, catastrophy.getClasses().get(0));
    }

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(SuperHero.class);
        return beans;
    }

    public static class SuperHero {

        public void jumpToAction(@Observes final Catastrophy catastrophy) {
            catastrophy.getClasses().add(this.getClass());
        }
    }

    public static class Catastrophy {
        private final List<Class> classes = new ArrayList<>();

        public List<Class> getClasses() {
            return classes;
        }
    }
}
