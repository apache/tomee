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
package org.apache.openejb.entity.bmp;

import java.util.Collection;
import java.util.Iterator;
import javax.ejb.TimerService;
import javax.ejb.Timer;

import org.apache.geronimo.interceptor.InvocationResult;

import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.timer.TimerState;
import org.apache.openejb.dispatch.AbstractMethodOperation;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.entity.EntityInstanceContext;

/**
 * Virtual operation handling removal of an instance.
 *
 * @version $Revision$ $Date$
 */
public class BmpRemoveMethod extends AbstractMethodOperation {
    public BmpRemoveMethod(Class beanClass, MethodSignature signature) {
        super(beanClass, signature);
    }

    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        InvocationResult result = invoke(invocation, EJBOperation.EJBREMOVE);
        if (result.isNormal()) {
            EntityInstanceContext ctx = (EntityInstanceContext) invocation.getEJBInstanceContext();
            //cancel timers
            TimerService timerService = ctx.getTimerService();
            if (timerService != null) {
                boolean oldTimerMethodAvailable = TimerState.getTimerState();
                TimerState.setTimerState(true);
                ctx.setTimerServiceAvailable(true);
                try {
                    Collection timers = timerService.getTimers();
                    for (Iterator iterator = timers.iterator(); iterator.hasNext();) {
                        Timer timer = (Timer) iterator.next();
                        timer.cancel();
                    }
                } finally {
                    ctx.setTimerServiceAvailable(false);
                    TimerState.setTimerState(oldTimerMethodAvailable);
                }
            }
                
            // clear id as we are no longer associated
            ctx.setId(null);
        }
        return result;
    }
}
