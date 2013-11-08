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

import java.math.BigDecimal;

/**
 * @author <a href="mailto:mnachev@gmail.com">Miroslav Nachev</a>
 * @since 1.1.0
 */
public class BigDecimalId
    extends OpenJPAId {

    private final BigDecimal key;

    public BigDecimalId(Class cls, String key) {
        this(cls, (key == null) ? null : new BigDecimal(key));
    }

    public BigDecimalId(Class cls, BigDecimal key) {
        super(cls);
        this.key = key;
    }

    public BigDecimalId(Class cls, BigDecimal key, boolean subs) {
        super(cls, subs);
        this.key = key;
    }

    public BigDecimal getId() {
        return key;
    }

    public Object getIdObject() {
        return key;
    }

    public String toString() {
        if (key == null) {
            return "NULL";
        }
        return key.toString();
    }

    protected int idHash() {
        if (key != null) {
            return key.hashCode();
        }
        return 0;
    }

    protected boolean idEquals(OpenJPAId other) {
        if ((key == null) ||
            (!BigDecimalId.class.isAssignableFrom(other.getClass()))) {
            return false;
        }
        return key.equals(((BigDecimalId)other).key);
    }
}

