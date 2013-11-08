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
package org.apache.openjpa.persistence.jdbc;

import java.sql.ResultSet;

import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.persistence.OpenJPAEnum;

/**
 * Type of result set to use.
 *
 * @since 1.0.0
 * @published
 */
public enum ResultSetType implements OpenJPAEnum<ResultSetType>{
    FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
    SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
    SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);

    private final int resultSetConstant;

    private ResultSetType(int value) {
        resultSetConstant = value;
    }

    public int toKernelConstant() {
        return resultSetConstant;
    }

    static ResultSetType fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case ResultSet.TYPE_FORWARD_ONLY:
                return FORWARD_ONLY;

            case ResultSet.TYPE_SCROLL_INSENSITIVE:
                return SCROLL_INSENSITIVE;

            case ResultSet.TYPE_SCROLL_SENSITIVE:
                return SCROLL_SENSITIVE;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }
    
    public int convertToKernelConstant(String s) {
        return ResultSetType.toKernelConstantFromString(s);
    }
    
    public int convertToKernelConstant(int i) {
        if (i == FetchConfiguration.DEFAULT)
            return i;
        for (ResultSetType level : ResultSetType.values()) {
            if (level.resultSetConstant == i)
                return i;
        }
        throw new IllegalArgumentException(i + " is invalid value for ResultSetType");
    }
    
    public static int toKernelConstantFromString(String s) {
        for (ResultSetType level : ResultSetType.values()) {
            if (level.name().equalsIgnoreCase(s) || String.valueOf(level.toKernelConstant()).equals(s))
                return level.toKernelConstant();
        }
        throw new IllegalArgumentException(s + " is not a valid name for " + ResultSetType.class.getName());
    }
}
