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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.bootstrap;

import org.apache.catalina.startup.Catalina;

import java.net.URL;
import java.net.URLClassLoader;

public class Start {

    public static void main(String[] args) {
        start();
    }
    public static void start() {

        final long start = System.currentTimeMillis();
        System.setProperty("catalina.home", "/tmp/apache-tomee-microprofile-8.0.0-M3");
        System.setProperty("catalina.base", "/tmp/apache-tomee-microprofile-8.0.0-M3");
        final URLClassLoader loader = new URLClassLoader(new URL[0], Start.class.getClassLoader());

        final Catalina catalina = new Catalina();
        catalina.setParentClassLoader(loader);
        catalina.setAwait(false);
        catalina.load();
        catalina.start();
        final long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed "+elapsed);
    }
}
