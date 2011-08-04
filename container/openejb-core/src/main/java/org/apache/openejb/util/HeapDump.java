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
package org.apache.openejb.util;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class HeapDump {

    public static String to(String fileName) {
        dumpHeap(fileName);
        return fileName;
    }
    
    /**
     * Dumps the java heap to the specified file in hprof format.
     * This method will not overwrite the dump file, so make sure it doesn't already exist.
     *
     * @param fileName the dump file name which must not already exist.
     */
    public static void dumpHeap(String fileName) {
        Class clazz;
        try {
            clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: dumpHeap only works on a Sun Java 1.6+ VM containing " +
                    "the class com.sun.management.HotSpotDiagnosticMXBean");
            return;
        }

        // use JMX to find hot spot mbean
        Object hotspotMBean = null;
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server,
                    "com.sun.management:type=HotSpotDiagnostic",
                    clazz);
        } catch (Throwable e) {
            System.out.print("ERROR: dumpHeap was unable to obtain the HotSpotDiagnosticMXBean: ");
            e.printStackTrace();
        }

        // invoke the dumpHeap method
        try {
            Method method = hotspotMBean.getClass().getMethod("dumpHeap", String.class);
            method.invoke(hotspotMBean, fileName);
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause() != null ? e.getCause() : e;
            System.out.print("ERROR: dumpHeap threw an exception: ");
            t.printStackTrace();
        } catch (Throwable e) {
            System.out.print("ERROR: dumpHeap threw an exception: ");
            e.printStackTrace();
    }
}
}
