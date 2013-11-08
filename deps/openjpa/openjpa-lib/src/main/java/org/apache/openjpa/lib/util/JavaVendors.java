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

import java.security.AccessController;

/**
 * Utilities for dealing with different Java vendors.
 */
public enum JavaVendors {
    IBM("com.ibm.tools.attach.VirtualMachine"), SUN("com.sun.tools.attach.VirtualMachine"),
    // When in doubt, try the Sun implementation.
    OTHER("com.sun.tools.attach.VirtualMachine");

    static {
        String vendor =
            AccessController.doPrivileged(J2DoPrivHelper.getPropertyAction("java.vendor", "")).toUpperCase();
        if (vendor.contains("SUN MICROSYSTEMS")) {
            _vendor = SUN;
        } else if (vendor.contains("IBM")) {
            _vendor = IBM;
        } else {
            _vendor = OTHER;
        }
    }
    
    private static final JavaVendors _vendor;
    private String _virtualMachineClass = null;
    
    private JavaVendors(String vmClass) {
        _virtualMachineClass = vmClass;
    }

    /**
     * This static worker method returns the current Vendor.
     */
    public static JavaVendors getCurrentVendor() {
        return _vendor;
    }
    
    /**
     * This static worker method returns <b>true</b> if the current implementation is IBM.
     */
    public boolean isIBM() {
        return _vendor == IBM;
    }

    /**
     * This static worker method returns <b>true</b> if the current implementation is Sun.
     */
    public boolean isSun() {
        return _vendor == SUN;
    }
    
    public String getVirtualMachineClassName() {
        return _virtualMachineClass;
    }
}
