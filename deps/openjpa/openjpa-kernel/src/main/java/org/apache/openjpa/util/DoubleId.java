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
package org.apache.openjpa.util;

/**
 * {@link OpenJPAId} subclass appropriate for double fields.
 *
 * @author Abe White
 */
public final class DoubleId 
    extends OpenJPAId {

    private final double key;

    public DoubleId(Class cls, Double key) {
        this(cls, (key == null) ? 0D : key.doubleValue());
    }

    public DoubleId(Class cls, String key) {
        this(cls, (key == null) ? 0D : Double.parseDouble(key));
    }

    public DoubleId(Class cls, double key) {
        super(cls);
        this.key = key;
    }

    public DoubleId(Class cls, double key, boolean subs) {
        super(cls, subs);
        this.key = key;
    }

    public double getId() {
        return key;
    }

    public Object getIdObject() {
        return Double.valueOf(key);
    }

    public String toString() {
        return Double.toString(key);
    }

    protected int idHash() {
        return (int) (Double.doubleToLongBits(key) 
            ^ (Double.doubleToLongBits(key) >>> 32));
    }

    protected boolean idEquals(OpenJPAId o) {
        return key == ((DoubleId) o).key;
    }
}
