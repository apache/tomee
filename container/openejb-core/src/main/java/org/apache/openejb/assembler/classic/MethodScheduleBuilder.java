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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.BeanContext;
import org.apache.openejb.MethodContext;
import org.apache.openejb.core.timer.ScheduleData;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.TimerConfig;
import java.lang.reflect.Method;

public class MethodScheduleBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, MethodScheduleBuilder.class.getPackage().getName());

    public void build(final BeanContext beanContext, final EnterpriseBeanInfo beanInfo) {

        final Class<?> clazz = beanContext.getBeanClass();

        for (final MethodScheduleInfo info : beanInfo.methodScheduleInfos) {

            Method timeoutMethodOfSchedule = null;

            if (info.method.methodParams == null) {

                logger.info("Schedule timeout method with 'null' method parameters is invalid: " + info.method.methodName);

            } else {

                try {
                    timeoutMethodOfSchedule = MethodInfoUtil.toMethod(clazz, info.method);
                } catch (final IllegalStateException e) {
                    // method doesn't exist
                    logger.warning("Schedule method does not exist: " + info.method.methodName, e);
                    continue;
                }

            }

            MethodContext methodContext = null;

            if (timeoutMethodOfSchedule == null && beanContext.getEjbTimeout() != null) {
                methodContext = beanContext.getMethodContext(beanContext.getEjbTimeout());
            } else if (info.method.className == null
                || timeoutMethodOfSchedule.getDeclaringClass().getName().equals(info.method.className)) {

                methodContext = beanContext.getMethodContext(timeoutMethodOfSchedule);

            }

            this.addSchedulesToMethod(methodContext, info);
        }
    }

    private void addSchedulesToMethod(final MethodContext methodContext, final MethodScheduleInfo info) {

        if (methodContext == null) {
            return;
        }

        for (final ScheduleInfo scheduleInfo : info.schedules) {

            final ScheduleExpression expr = new ScheduleExpression();
            expr.second(scheduleInfo.second == null ? "0" : scheduleInfo.second);
            expr.minute(scheduleInfo.minute == null ? "0" : scheduleInfo.minute);
            expr.hour(scheduleInfo.hour == null ? "0" : scheduleInfo.hour);
            expr.dayOfWeek(scheduleInfo.dayOfWeek == null ? "*" : scheduleInfo.dayOfWeek);
            expr.dayOfMonth(scheduleInfo.dayOfMonth == null ? "*" : scheduleInfo.dayOfMonth);
            expr.month(scheduleInfo.month == null ? "*" : scheduleInfo.month);
            expr.year(scheduleInfo.year == null ? "*" : scheduleInfo.year);
            expr.timezone(scheduleInfo.timezone);
            expr.start(scheduleInfo.start);
            expr.end(scheduleInfo.end);

            final TimerConfig config = new TimerConfig();
            config.setInfo(scheduleInfo.info);
            config.setPersistent(scheduleInfo.persistent);

            methodContext.getSchedules().add(new ScheduleData(config, expr));
        }

    }


}
