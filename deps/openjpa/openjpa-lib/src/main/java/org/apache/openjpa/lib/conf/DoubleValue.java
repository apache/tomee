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
 * A double {@link Value}.
 *
 * @author Marc Prud'hommeaux
 */
public class DoubleValue extends Value {

    private double value;

    public DoubleValue(String prop) {
        super(prop);
    }

    public Class<Double> getValueType() {
        return double.class;
    }

    /**
     * The internal value.
     */
    public void set(double value) {
        assertChangeable();
        double oldValue = this.value;
        this.value = value;
        if (oldValue != value)
            valueChanged();
    }

    /**
     * The internal value.
     */
    public Double get() {
        return value;
    }

    protected String getInternalString() {
        return String.valueOf(value);
    }

    protected void setInternalString(String val) {
        if (StringUtils.isEmpty(val))
            set(0D);
        else
            set(Double.parseDouble(val));
    }

    protected void setInternalObject(Object obj) {
        if (obj == null)
            set(0D);
        else
            set(((Number) obj).doubleValue());
    }
}
