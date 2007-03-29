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

public class IntraVmCopyMonitor {
    private static final ThreadLocal<IntraVmCopyMonitor> threadMonitor = new ThreadLocal<IntraVmCopyMonitor>();

    private boolean intraVmCopyOperation = false;

    private boolean statefulPassivationOperation = false;

    private boolean crossClassLoaderOperation = false;

    private IntraVmCopyMonitor() {
    }

    public static boolean exists() {
        return (threadMonitor.get() != null);
    }

    public static void release() {
        threadMonitor.set(null);
    }

    private static IntraVmCopyMonitor getMonitor() {
        IntraVmCopyMonitor monitor = threadMonitor.get();
        if (monitor == null) {
            monitor = new IntraVmCopyMonitor();
            threadMonitor.set(monitor);
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

    public static void preCrossClassLoaderOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.crossClassLoaderOperation = true;
    }

    public static void postCrossClassLoaderOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.crossClassLoaderOperation = false;
    }

    public static boolean isIntraVmCopyOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        return monitor.intraVmCopyOperation;
    }

    public static boolean isStatefulPassivationOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        return monitor.statefulPassivationOperation;
    }

    public static boolean isCrossClassLoaderOperation() {
        IntraVmCopyMonitor monitor = getMonitor();
        return monitor.crossClassLoaderOperation;
    }
}
