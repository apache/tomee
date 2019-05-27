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
package org.superbiz.event;

import org.apache.openejb.observer.Observes;

import java.util.logging.Logger;

/**
 * registered as service in openejb.xml, tomee.xml, resources.xml or openejb system properties:
 *
 * listener = new://Service?class-name=org.superbiz.event.MyListener
 * listener.logAllEvent = true
 */
public class MyListener {

    private static final Logger LOGGER = Logger.getLogger(MyListener.class.getName());

    private static boolean logAllEvent = false; // static for testing

    public void global(@Observes final Object event) {
        LOGGER.info(">>> an event occured -> " + event.toString());
    }

    // configurable
    public void setLogAllEvent(boolean logAllEvent) {
        this.logAllEvent = logAllEvent;
    }

    public static boolean isLogAllEvent() {
        return logAllEvent;
    }
}
