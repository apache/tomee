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

import org.apache.openejb.quartz.impl.triggers.AbstractTrigger;
import org.apache.openejb.quartz.impl.triggers.SimpleTriggerImpl;

import jakarta.ejb.TimerConfig;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;

/**
 * @version $Rev$ $Date$
 */
public class SingleActionTimerData extends TimerData {
    private static final long serialVersionUID = 1L;

    private final Date expiration;

    public SingleActionTimerData(final long id, final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Method timeoutMethod, final TimerConfig timerConfig, final Date expiration) {
        super(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig);
        this.expiration = expiration;
    }

    @Override
    public TimerType getType() {
        return TimerType.SingleAction;
    }

    public Date getExpiration() {
        return expiration;
    }

    @Override
    public AbstractTrigger<?> initializeTrigger() {
        final SimpleTriggerImpl simpleTrigger = new SimpleTriggerImpl();
        simpleTrigger.setStartTime(expiration);
        return simpleTrigger;
    }

    @Override
    public String toString() {
        return TimerType.SingleAction.name() + " expiration = [" + DateFormat.getDateTimeInstance().format(expiration) + "]";
    }
}
