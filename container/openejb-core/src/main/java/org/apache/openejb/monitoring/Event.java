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
package org.apache.openejb.monitoring;

//import org.apache.commons.math.stat.descriptive.SynchronizedDescriptiveStatistics;
//import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/**
 * @version $Rev$ $Date$
*/
@Managed(append = true)
public class Event {
    private final AtomicLong count = new AtomicLong();
    private final AtomicLong last = new AtomicLong();

    public void record() {
        last.getAndSet(System.currentTimeMillis());
        count.incrementAndGet();
    }

    @Managed
    public long get() {
        return count.get();
    }

    @Managed
    public String getLatest() {
        long last = this.last.get();

        if (last <= 0) return "-";
        
        DateFormat format = SimpleDateFormat.getDateTimeInstance();
        return format.format(new Date(last));
    }

    @Managed
    public long getLatestTime() {
        return this.last.get();
    }
}
