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
package org.apache.openejb.config;

import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.core.singleton.SingletonContainer;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;

import static org.junit.Assert.assertEquals;

@SimpleLog
@Classes(AppContainerTest.Singleton1.class)
@RunWith(ApplicationComposer.class)
public class AppContainerTest {
    @org.apache.openejb.testing.Module
    public Resources resources() {
        final Resources resources = new Resources();
        final Container container = new Container();
        container.setId("theMine");
        container.setType("SINGLETON");
        container.setClassName("org.apache.openejb.config.AppContainerTest$MySingletonContainer");
        resources.getContainer().add(container);
        return resources;
    }

    @Singleton
    public static class Singleton1 {
        public String ok() {
            throw new UnsupportedOperationException("my container mocks it");
        }
    }

    @EJB
    private Singleton1 s1;

    @Test
    public void run() {
        assertEquals("yeah!", s1.ok());
    }

    public static class MySingletonContainer extends SingletonContainer {
        public MySingletonContainer() throws OpenEJBException {
            super("mine", null);
        }

        @Override
        public Object invoke(final Object deployID, final InterfaceType type, final Class callInterface,
                             final Method callMethod, final Object[] args, final Object primKey) throws OpenEJBException {
            return callMethod.getDeclaringClass() == Singleton1.class ? "yeah!" : super.invoke(deployID, type, callInterface, callMethod, args, primKey);
        }
    }
}
