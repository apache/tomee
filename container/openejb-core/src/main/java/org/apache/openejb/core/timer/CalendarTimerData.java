/*
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

package org.apache.openejb.core.timer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.openejb.core.timer.EJBCronTrigger.ParseException;
import org.apache.openejb.quartz.impl.triggers.AbstractTrigger;

import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.TimerConfig;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class CalendarTimerData extends TimerData {
    private static final long serialVersionUID = 1L;

    private ScheduleExpression scheduleExpression;
    private boolean autoCreated;

    public CalendarTimerData(final long id, final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Method timeoutMethod, final TimerConfig timerConfig, final ScheduleExpression scheduleExpression, final boolean auto) {
        super(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig);
        this.scheduleExpression = scheduleExpression;
        this.autoCreated = auto;
    }

    @Override
    public TimerType getType() {
        return TimerType.Calendar;
    }

    public ScheduleExpression getSchedule() {
        return scheduleExpression;
    }

    public boolean isAutoCreated() {
        return autoCreated;
    }

    @Override
    public AbstractTrigger<?> initializeTrigger() {
        try {
            return new EJBCronTrigger(scheduleExpression);
        } catch (final ParseException e) {
            //TODO how to handle the ParseException
            throw new IllegalArgumentException("Fail to parse schedule expression " + scheduleExpression, e);
        }
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        super.doWriteObject(out);
        out.writeBoolean(autoCreated);
        out.writeObject(scheduleExpression);
    }

    private void readObject(final ObjectInputStream in) throws IOException {
        super.doReadObject(in);
        autoCreated = in.readBoolean();
        try {
            scheduleExpression = ScheduleExpression.class.cast(in.readObject());
        } catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String toString() {
        return TimerType.Calendar.name() + " scheduleExpression = [" + ToStringBuilder.reflectionToString(scheduleExpression) + "]";
    }
}
