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
import org.apache.openejb.MethodContext;
import org.apache.openejb.core.ObjectInputStreamFiltered;
import org.apache.openejb.core.timer.CalendarTimerData;
import org.apache.openejb.core.timer.EjbTimeoutJob;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.core.timer.ScheduleData;
import org.apache.openejb.core.timer.TimerData;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.openejb.quartz.JobDataMap;
import org.apache.openejb.quartz.impl.triggers.AbstractTrigger;

import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class EjbTimerImplSerializableTest {
    @Test
    public void serializeDeserialize() throws Exception {
        final EjbTimerService timer = timerService();
        assertNotNull(timer);
        assertThat(timer, instanceOf(EjbTimerServiceImpl.class));

        final byte[] serial = serialize(timer);
        final EjbTimerService timerDeserialized = (EjbTimerService) deserialize(serial);

        assertThat(timerDeserialized, instanceOf(EjbTimerServiceImpl.class));
        assertThat(((EjbTimerServiceImpl) timerDeserialized).getScheduler(), notNullValue());

        assertEqualsByReflection(timer, timerDeserialized, "deployment");
        assertEqualsByReflection(timer, timerDeserialized, "transacted");
        assertEqualsByReflection(timer, timerDeserialized, "retryAttempts");
    }

    @Test
    public void serializationOfCalendarData() throws Exception {
        final CalendarTimerData data = timerData();

        final byte[] serial = serialize(data);
        final CalendarTimerData dataDeserialized = (CalendarTimerData) deserialize(serial);

        assertThat(dataDeserialized, instanceOf(CalendarTimerData.class));

        assertEqualsByReflection(data, dataDeserialized, "id");
        assertEqualsByReflection(data, dataDeserialized, "deploymentId");
        assertEqualsByReflection(data, dataDeserialized, "timeoutMethod");
        assertEqualsByReflection(data, dataDeserialized, "info");
    }

    @Test
    public void jobDataMapSerial() throws Exception {
        final CalendarTimerData data = timerData();
        final EjbTimerServiceImpl timerService = (EjbTimerServiceImpl) timerService();

        data.setScheduler(timerService.getScheduler());
        // small hack for the test
        final Field preventSynch = TimerData.class.getDeclaredField("synchronizationRegistered");
        preventSynch.setAccessible(true);
        preventSynch.set(data, true);
        data.newTimer();

        final AbstractTrigger<?> trigger = (AbstractTrigger<?>) data.getTrigger();
        trigger.setJobName("my-job");
        trigger.setJobGroup("my-group");

        final JobDataMap triggerDataMap = trigger.getJobDataMap();
        triggerDataMap.put(EjbTimeoutJob.EJB_TIMERS_SERVICE, timerService);
        triggerDataMap.put(EjbTimeoutJob.TIMER_DATA, data);

        final byte[] serial = serialize(triggerDataMap);
        final JobDataMap map = (JobDataMap) deserialize(serial);
        assertTrue(map.containsKey(EjbTimeoutJob.EJB_TIMERS_SERVICE));
        assertTrue(map.containsKey(EjbTimeoutJob.TIMER_DATA));
    }

    private static Object deserialize(final byte[] serial) throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(serial);
        final ObjectInputStream ois = new ObjectInputStreamFiltered(bais);
        return ois.readObject();
    }

    private static byte[] serialize(final Object data) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(data);
        return baos.toByteArray();
    }

    private static void assertEqualsByReflection(final Object o1, final Object o2, final String name) throws Exception {
        Class<?> clazz = o1.getClass();
        Field field = null;
        while (!Object.class.equals(clazz) && clazz.getSuperclass() != null && field == null) {
            try {
                field = clazz.getDeclaredField(name);
                field.setAccessible(true);
            } catch (final NoSuchFieldException ignored) {
                // no-op
            }
            clazz = clazz.getSuperclass();
        }

        if (field == null) {
            fail();
        }

        final Object v1 = field.get(o1);
        final Object v2 = field.get(o2);

        assertEquals(name, v1, v2);
    }

    private static EjbTimerService timerService() {
        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext("EJBWithTimer");
        return context.getEjbTimerService();
    }

    public static CalendarTimerData timerData() throws Exception {
        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext("EJBWithTimer");
        final EjbTimerService timer = context.getEjbTimerService();
        final MethodContext ctx = context.getMethodContext(EJBWithTimer.class.getMethod("doSthg"));
        final ScheduleData sd = ctx.getSchedules().iterator().next();
        return new CalendarTimerData(1, (EjbTimerServiceImpl) timer, context.getDeploymentID().toString(), null, ctx.getBeanMethod(), sd.getConfig(), sd.getExpression(), false);
    }

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
