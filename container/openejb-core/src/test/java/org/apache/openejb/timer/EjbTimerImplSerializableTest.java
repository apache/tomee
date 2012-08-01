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
package org.apache.openejb.timer;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class EjbTimerImplSerializableTest {
    @Test
    public void serializeDeserialize() throws Exception {
        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext("EJBWithTimer");
        final EjbTimerService timer = context.getEjbTimerService();
        assertNotNull(timer);
        assertThat(timer, instanceOf(EjbTimerServiceImpl.class));

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(timer);

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final EjbTimerService timerDeserialized = (EjbTimerService) ois.readObject();
        assertThat(timerDeserialized, instanceOf(EjbTimerServiceImpl.class));

        assertEqualsByReflection(timer, timerDeserialized, "deployment");
        assertEqualsByReflection(timer, timerDeserialized, "transacted");
        assertEqualsByReflection(timer, timerDeserialized, "retryAttempts");
    }

    private void assertEqualsByReflection(final EjbTimerService timer, final EjbTimerService timerDeserialized, final String name) throws Exception {
        final Field field = EjbTimerServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);

        final Object v1 = field.get(timer);
        final Object v2 = field.get(timerDeserialized);

        assertEquals(name, v1, v2);
    }

    @EJB
    private EJBWithTimer stateless;

    @Module
    public StatelessBean bean() {
        final StatelessBean bean = new StatelessBean(EJBWithTimer.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Stateless
    public static class EJBWithTimer {
        // no need to run it, we just want a timer
        @Schedule
        public void doSthg() {
            // no-op
        }
    }
}
