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
package org.apache.openejb.core.ivm;

import org.apache.openejb.util.FastThreadLocal;

public class IntraVmCopyMonitor {

    private static FastThreadLocal threadStorage = new FastThreadLocal();

    boolean intraVmCopyOperation = false;

    boolean statefulPassivationOperation = false;

    IntraVmCopyMonitor() {
    }

    public static boolean exists() {
        return (threadStorage.get() != null);
    }

    public static void release() {
        threadStorage.set(null);
    }

    static IntraVmCopyMonitor getMonitor() {
        IntraVmCopyMonitor monitor = (IntraVmCopyMonitor) threadStorage.get();
        if (monitor == null) {
            monitor = new IntraVmCopyMonitor();
            threadStorage.set(monitor);
        }
        return monitor;
    }

    public static void preCopyOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.intraVmCopyOperation = true;
    }

    public static void postCopyOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.intraVmCopyOperation = false;
    }

    public static void prePassivationOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.statefulPassivationOperation = true;
    }

    public static void postPassivationOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.statefulPassivationOperation = false;
    }

    public static boolean isIntraVmCopyOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        if (monitor.intraVmCopyOperation)
            return true;
        else
            return false;
    }

    public static boolean isStatefulPassivationOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        if (monitor.statefulPassivationOperation)
            return true;
        else
            return false;
    }
}
