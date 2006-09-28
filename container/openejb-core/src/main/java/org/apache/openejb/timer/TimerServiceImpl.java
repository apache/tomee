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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.apache.openejb.EJBInstanceContext;


/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TimerServiceImpl implements TimerService {

    private final EJBInstanceContext context;

    public TimerServiceImpl(EJBInstanceContext context) {
        this.context = context;
    }

    public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        checkState();
        return context.getBasicTimerService().createTimer(context.getId(), initialExpiration, intervalDuration, info);
    }

    public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        checkState();
        return context.getBasicTimerService().createTimer(context.getId(), expiration, info);
    }

    public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        checkState();
        return context.getBasicTimerService().createTimer(context.getId(), initialDuration, intervalDuration, info);
    }

    public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        checkState();
        return context.getBasicTimerService().createTimer(context.getId(), duration, info);
    }

    public Collection getTimers() throws IllegalStateException, EJBException {
        checkState();
        //TODO this check is here because entity bean remove calls this to get the list of timers to cancel.
        //Possibly there is a better place to check that the entity bean is a timed object.
        return context.getBasicTimerService() == null? Collections.EMPTY_SET: context.getBasicTimerService().getTimers(context.getId());
    }

    private void checkState() throws IllegalStateException {
        if (!TimerState.getTimerState()) {
            throw new IllegalStateException("Timer methods not available");
        }
    }
}
