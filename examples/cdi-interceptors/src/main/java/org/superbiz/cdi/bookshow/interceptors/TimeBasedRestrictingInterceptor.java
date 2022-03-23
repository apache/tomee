/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.cdi.bookshow.interceptors;

import org.superbiz.cdi.AccessDeniedException;
import org.superbiz.cdi.bookshow.interceptorbinding.TimeRestricted;
import org.superbiz.cdi.bookshow.tracker.InterceptionOrderTracker;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@TimeRestricted
public class TimeBasedRestrictingInterceptor implements Serializable {

    private static final long serialVersionUID = 8139854519874743530L;

    @AroundInvoke
    public Object restrictAccessBasedOnTime(InvocationContext ctx) throws Exception {
        InterceptionOrderTracker.getMethodsInterceptedList().add(ctx.getMethod().getName());
        InterceptionOrderTracker.getInterceptedByList().add(this.getClass().getSimpleName());
        if (!isWorkingHours()) {
            throw new AccessDeniedException("You are not allowed to access the method at this time");
        }
        return ctx.proceed();
    }

    private boolean isWorkingHours() {
        /*
         * int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); if (hourOfDay >= 9 && hourOfDay <= 21) {
         * return true; } else { return false; }
         */
        return true; // Let's assume
    }
}
