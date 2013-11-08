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

import org.apache.commons.lang.StringUtils;

/**
 * A string {@link Value}.
 *
 * @author Marc Prud'hommeaux
 */
public class StringValue extends Value {

    private String value;

    public StringValue(String prop) {
        super(prop);
    }

    public Class<String> getValueType() {
        return String.class;
    }

    /**
     * The internal value.
     */
    public String get() {
        return value;
    }

    /**
     * The internal value.
     */
    public void set(String value) {
        assertChangeable();
        String oldValue = this.value;
        this.value = value;
        if (!StringUtils.equals(value, oldValue))
            valueChanged();
    }

    protected String getInternalString() {
        return get();
    }

    protected void setInternalString(String val) {
        set(val);
    }
    
    protected void setInternalObject(Object obj) {
        if (obj instanceof String) {
            set((String) obj);
        } else {
            set(obj == null ? null : obj.toString());
        }
    }
}

