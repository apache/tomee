/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.itests.optional.app;

import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.core.LocalInitialContext;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.tomee.itests.optional.optlib.Car;
import org.apache.tomee.itests.optional.optlib.SomeBean;
import org.apache.tomee.itests.optional.optlib.Vehicle;
import org.junit.Test;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class OptionalDependencyResolutionTest {

    @Test
    public void testContainerBoot() throws NamingException {
        // we use classic discovery via the classpath
        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        p.put(LocalInitialContext.ON_CLOSE, LocalInitialContext.Close.DESTROY.name());
        try
        {
            this.getClass().getClassLoader().loadClass("org.apache.openejb.server.ServiceManager");
            p.put(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        }
        catch (final Exception e)
        {
            // ignored
        }

        Context context = new InitialContext(p);

        final BeanManager beanManager = CDI.current().getBeanManager();
        assertNotNull(beanManager);

        final Set<Bean<?>> someBeanBeans = beanManager.getBeans(SomeBean.class);
        assertTrue(!someBeanBeans.isEmpty());

        // as there should be a NoClassDefFound of the ThirdPartyApi.class we did not pick up
        // the Car but only the Bicycle
        final Set<Bean<?>> optionalBeanBeans = beanManager.getBeans(Vehicle.class);
        assertEquals(1, optionalBeanBeans.size());
        final Vehicle vehicle = CDI.current().select(Vehicle.class).get();
        assertFalse(vehicle.motorized());
    }
}
