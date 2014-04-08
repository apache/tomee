/*
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
    public static enum State {
        NONE, COPY(true), CLASSLOADER_COPY(true), PASSIVATION;

        private final boolean copy;

        State() {
           this.copy = false;
        }

        State(boolean copy) {
           this.copy = copy;
        }

        public boolean isCopy() {
            return copy;
        }
    }

    private static final ThreadLocal<IntraVmCopyMonitor> threadMonitor = new ThreadLocal<IntraVmCopyMonitor>();

    private State state = State.NONE;

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

    public static void pre(State state){
        getMonitor().state = state;
    }

    public static void post() {
        pre(State.NONE);
    }

    public static State state(){
        return getMonitor().state;
    }

    public static void prePassivationOperation() {
        pre(State.PASSIVATION);
    }

    public static void postPassivationOperation() {
        post();
    }

    public static void preCrossClassLoaderOperation() {
        pre(State.CLASSLOADER_COPY);
    }

    public static void postCrossClassLoaderOperation() {
        post();
    }

    public static void preCopyOperation() {
        pre(State.COPY);
    }

    public static void postCopyOperation() {
        post();
    }

    public static boolean isIntraVmCopyOperation() {
        return state() == State.COPY;
    }

    public static boolean isStatefulPassivationOperation() {
        return state() == State.PASSIVATION;
    }

    public static boolean isCrossClassLoaderOperation() {
        return state() == State.CLASSLOADER_COPY;
    }
}
