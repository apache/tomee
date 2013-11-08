/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.util;

import java.sql.Timestamp;

/**
 * Helper base class attempts to return java.sql.Timestamp object with
 * nanosecond precision. 
 * 
 * @author Albert Lee
 */
public class TimestampHelper {

    // number of millisecond, mircoseconds and nanoseconds in one second.
    protected static final long MilliMuliplier = 1000L;
    protected static final long MicroMuliplier = MilliMuliplier * 1000L;
    protected static final long NanoMuliplier = MicroMuliplier * 1000L;

    // number of seconds passed 1970/1/1 00:00:00 GMT.
    private static long sec0;
    // fraction of seconds passed 1970/1/1 00:00:00 GMT, offset by
    // the base System.nanoTime (nano0), in nanosecond unit.
    private static long nano0;

    static {
        // initialize base time in second and fraction of second (ns).
        long curTime = System.currentTimeMillis();
        sec0 = curTime / MilliMuliplier;
        nano0 = (curTime % MilliMuliplier) * MicroMuliplier - System.nanoTime();
    }

    /*
     * Return a java.sql.Timestamp object of current time.
     */
    public static Timestamp getNanoPrecisionTimestamp() {
        long nano_delta = nano0 + System.nanoTime();
        long sec1 = sec0 + (nano_delta / NanoMuliplier);
        long nano1 = nano_delta % NanoMuliplier;

        Timestamp rtnTs = new Timestamp(sec1 * MilliMuliplier);
        rtnTs.setNanos((int) nano1);
        return rtnTs;
    }
}
