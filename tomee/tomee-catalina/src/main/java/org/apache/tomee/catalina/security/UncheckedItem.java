/*
 * Copyright 2018 OmniFaces.
 * Copyright 2003-2011 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina.security;

/**
 *
 * @author Guillermo González de Agüero
 */
class UncheckedItem {
    final static int NA = 0x00;
    final static int INTEGRAL = 0x01;
    final static int CONFIDENTIAL = 0x02;

    private int transportType = NA;
    private String name;

    public UncheckedItem(String name, int transportType) {
        setName(name);
        setTransportType(transportType);
    }

    public boolean equals(Object o) {
        if (o instanceof UncheckedItem) {
            UncheckedItem item = (UncheckedItem) o;
            return item.transportType == transportType && item.name.equals(this.name);
        }
        return false;
    }


    public int hashCode() {
        return name.hashCode() + transportType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTransportType() {
        return transportType;
    }

    public void setTransportType(int transportType) {
        this.transportType = transportType;
    }
}