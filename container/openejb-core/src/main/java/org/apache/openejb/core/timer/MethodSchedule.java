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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.timer;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Holds the basic binding information for a method and the ScheduleDatas associated with it.
 * ScheduleData is a simple object that wraps both javax.ejb.ScheduleExpression and javax.ejb.TimerConfig
 *
 */
public class MethodSchedule {

    private final Method method;

    private final List<ScheduleData> schedules;

    public MethodSchedule(Method method, List<ScheduleData> schedules) {
        this.method = method;
        this.schedules = schedules;
    }

    public Method getMethod() {
        return method;
    }

    public List<ScheduleData> getSchedules() {
        return schedules;
    }
}
