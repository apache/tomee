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
package org.apache.openejb.config;

import junit.framework.TestCase;

import java.util.logging.Logger;

public class MemorySettingsTest extends TestCase {

    public void test() {
        Runtime runtime = Runtime.getRuntime();

        System.out.println("maxMemory = " + runtime.maxMemory());

        System.out.println("freeMemory = " + runtime.freeMemory());

        System.out.println("totalMemory = " + runtime.totalMemory());

        Logger logger = Logger.getLogger(MemorySettingsTest.class.getName());

        logger.severe("maxMemory = " + runtime.maxMemory());

        logger.severe("freeMemory = " + runtime.freeMemory());

        logger.severe("totalMemory = " + runtime.totalMemory());
    }
}
