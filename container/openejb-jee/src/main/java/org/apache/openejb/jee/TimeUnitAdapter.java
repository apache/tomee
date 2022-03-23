/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.jee;

import java.util.concurrent.TimeUnit;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @version $Rev:$ $Date:$
 */
public class TimeUnitAdapter extends XmlAdapter<String, TimeUnit> {
    @Override
    public TimeUnit unmarshal(final String s) throws Exception {
        return TimeUnit.valueOf(s.toUpperCase());
    }

    @Override
    public String marshal(final TimeUnit t) throws Exception {
        if (t == TimeUnit.DAYS) return "Days";
        if (t == TimeUnit.HOURS) return "Hours";
        if (t == TimeUnit.MINUTES) return "Minutes";
        if (t == TimeUnit.SECONDS) return "Seconds";
        if (t == TimeUnit.MILLISECONDS) return "Milliseconds";
        if (t == TimeUnit.MICROSECONDS) return "Microseconds";
        if (t == TimeUnit.NANOSECONDS) return "Nanoseconds";
        throw new IllegalArgumentException("Unknown time unit: " + t);
    }
}
