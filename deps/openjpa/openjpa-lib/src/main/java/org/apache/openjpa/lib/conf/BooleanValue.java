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
package org.apache.openjpa.lib.conf;

/**
 * A boolean {@link Value}.
 *
 * @author Marc Prud'hommeaux
 */
public class BooleanValue extends Value {

    private boolean value;
    
    public BooleanValue(String prop) {
        super(prop);
        setAliasListComprehensive(true);
    }

    public Class<Boolean> getValueType() {
        return boolean.class;
    }

    /**
     * The internal value.
     */
    public void set(boolean value) {
        assertChangeable();
        boolean oldValue = this.value;
        this.value = value;
        if (oldValue != value)
            valueChanged();
    }

    /**
     * The internal value.
     */
    public Boolean get() {
        return value;
    }

    protected String getInternalString() {
        return String.valueOf(value);
    }

    protected void setInternalString(String val) {
        set(Boolean.valueOf(val).booleanValue());
    }

    protected void setInternalObject(Object obj) {
        if (obj == null)
            set(false);
        else
            set(((Boolean) obj).booleanValue());
    }
}
