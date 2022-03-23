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
package org.apache.openejb;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OpenEjbContainerNoRestartTest {
    @Test
    public void normalRestart() throws Exception {
        final EJBContainer container1 = EJBContainer.createEJBContainer(new Properties() {{
            put(EJBContainer.MODULES, new EjbJar());
        }});
        container1.close();
        final EJBContainer container2 = EJBContainer.createEJBContainer(new Properties() {{
            put(EJBContainer.MODULES, new EjbJar());
        }});
        container2.close();
        assertNotSame(container1, container2);
    }

    @Test
    public void noRestart() throws Exception {
        final EJBContainer container1 = EJBContainer.createEJBContainer(new Properties() {{
            put(EJBContainer.MODULES, new EjbJar());
            put(OpenEjbContainer.OPENEJB_EJBCONTAINER_CLOSE, OpenEjbContainer.OPENEJB_EJBCONTAINER_CLOSE_SINGLE);
        }});
        container1.close();
        final EJBContainer container2 = EJBContainer.createEJBContainer(new Properties() {{
            put(EJBContainer.MODULES, new EjbJar());
        }});
        container2.close();
        assertTrue(SystemInstance.isInitialized());
        assertSame(container1, container2);
        Reflections.invokeByReflection(container2, "doClose", new Class<?>[0], null);
        assertFalse(SystemInstance.isInitialized());
    }
}
