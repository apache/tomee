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
package org.apache.openjpa.enhance.ids;

public class HardwareId {
    
    public static boolean[] usedConstructor = new boolean[2];

    private String serial;
    
    private String model;
    
    public HardwareId() {
    }
    
    // Parms out of order
    public HardwareId(String model, String serial) {
        usedConstructor[0] = true;
        this.serial = serial;
        this.model = model;
    }
    
    public HardwareId(String model,int serial) {
        usedConstructor[1] = true;
        this.model = model;
        this.serial = Integer.toString(serial);
    }

    public String getSerial() {
        return serial;
    }

    public String getModel() {
        return model;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HardwareId) {
            HardwareId hid = (HardwareId)obj;
            return hid.getModel().equals(getModel()) &&
                hid.getSerial().equals(getSerial());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getSerial().hashCode() + getModel().hashCode();
    }
}
