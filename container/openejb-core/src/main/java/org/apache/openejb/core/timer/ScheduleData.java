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

import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;

/**
 * Simple object to hold the ScheduleExpression and TimerConfig pair
 *
 * Zero or many of these may be associated with a method and available
 * via the MethodSchedule list retrieved from the DeploymentInfo
 *
 * @version $Rev$ $Date$
 * 
 */
public class ScheduleData {

    private final ScheduleExpression expression;
    private final TimerConfig config;

    public ScheduleData(TimerConfig config, ScheduleExpression expression) {
        this.config = config;
        this.expression = expression;
    }

    public TimerConfig getConfig() {
        return config;
    }

    public ScheduleExpression getExpression() {
        return expression;
    }
}
