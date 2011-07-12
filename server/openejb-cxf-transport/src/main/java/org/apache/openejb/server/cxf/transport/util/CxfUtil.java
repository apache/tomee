/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.transport.util;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.extension.ExtensionManagerBus;

/**
 * @author Romain Manni-Bucau
 */
public final class CxfUtil {
    private CxfUtil() {
        // no-op
    }

    /*
     * Ensure the bus created is unqiue and non-shared.
     * The very first bus created is set as a default bus which then can
     * be (re)used in other places.
     */
    public static Bus getBus() {
        getDefaultBus();
        return new ExtensionManagerBus();
    }

    /*
     * Ensure the Spring bus is initialized with the CXF module classloader
     * instead of the application classloader.
     */
    public static Bus getDefaultBus() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.class.getClassLoader());
        try {
            return BusFactory.getDefaultBus();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}
