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

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.MethodContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerService;

public final class Timers {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.TIMER, TimerServiceWrapper.class);

    private Timers() {
        // no-op
    }

    public static Collection<Timer> all() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext beanContext = threadContext.getBeanContext();
        final ModuleContext module = beanContext.getModuleContext();

        final Collection<Timer> timers = new HashSet<>();
        for (final BeanContext c : module.getAppContext().getBeanContexts()) {
            if (c.getModuleContext() == module) { // filter by module
                if (c.getComponentType() != BeanType.STATEFUL) {
                    final TimerService timerService = getTimerService(null, c, true);
                    if (timerService == null) {
                        continue;
                    }
                    final Collection<Timer> beanTimers = timerService.getTimers();
                    timers.addAll(beanTimers);
                } else {
                    // for all instances
                    final TimerService timerService = getTimerService(null, c, true);
                    if (timerService == null) {
                        continue;
                    }
                    final Collection<Timer> beanTimers = timerService.getTimers();
                    timers.addAll(beanTimers);
                }
            }
        }
        return timers;
    }

    public static TimerService getTimerService(final Object pk, final BeanContext beanContext, final boolean nullIfNotRelevant) throws IllegalStateException {
        final EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService == null) {
            if (nullIfNotRelevant) {
                return null;
            }
            throw new IllegalStateException("This ejb does not support timers " + beanContext.getDeploymentID());
        } else if (beanContext.getEjbTimeout() == null) {

            HasSchedule hasSchedule = beanContext.get(HasSchedule.class);

            boolean hasSchedules = false;

            if (hasSchedule != null) {
                hasSchedules = hasSchedule.value;
            } else {
                for (final Iterator<Map.Entry<Method, MethodContext>> it = beanContext.iteratorMethodContext(); it.hasNext(); ) {
                    final Map.Entry<Method, MethodContext> entry = it.next();
                    final MethodContext methodContext = entry.getValue();
                    if (methodContext.getSchedules().size() > 0) {
                        hasSchedules = true;
                    }
                }
                synchronized (beanContext) { // surely not the best lock instance but works in this context
                    if (beanContext.get(HasSchedule.class) == null) {
                        beanContext.set(HasSchedule.class, new HasSchedule(hasSchedules));
                    }
                }
            }

            if (!hasSchedules) {
                if (nullIfNotRelevant) {
                    return null;
                }
                LOGGER.error("This ejb does not support timers " + beanContext.getDeploymentID() + " due to no timeout method nor schedules in methodContext is configured");
            }

        }
        return new TimerServiceImpl(timerService, pk, beanContext.getEjbTimeout());
    }

    private static final class HasSchedule {
        private final boolean value;

        private HasSchedule(final boolean value) {
            this.value = value;
        }
    }
}
