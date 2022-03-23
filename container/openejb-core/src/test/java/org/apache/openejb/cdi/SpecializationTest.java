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
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Stateful;
import jakarta.enterprise.inject.Specializes;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class SpecializationTest extends TestCase {

    @Inject
    private BeanManager manager;

    @Test
    public void test() throws Exception {
        final Set<Bean<?>> beans = manager.getBeans(Color.class);
        assertEquals(1, beans.size());
    }

    @Module
    public Class[] getBeans() {
        return new Class[]{RedBean.class, CrimsonBean.class};
    }

    public static interface Color {
    }

    @Stateful
    public static class RedBean implements Color {
    }

    @Specializes
    @Stateful
    public static class CrimsonBean extends RedBean implements Color {
    }

}
