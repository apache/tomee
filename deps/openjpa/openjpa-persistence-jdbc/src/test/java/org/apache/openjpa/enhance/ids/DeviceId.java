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

public class DeviceId {
    
    public static boolean[] usedConstructor = new boolean[3];

    private int id;
    
    private int type;
    
    public DeviceId() {
        usedConstructor[0] = true;
    }
    
    @SuppressWarnings("unused")
    private DeviceId(int i, int t) {
        usedConstructor[1] = true;
        id = i;
        type = t;
    }
    
    public DeviceId(int i) {
        usedConstructor[2] = true;
        id = i;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceId) {
            DeviceId did = (DeviceId)obj;
            return did.getId() == getId() &&
                did.getType() == getType();
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getId() + getType();
    }
}
