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

import org.apache.openjpa.jdbc.kernel.LRSSizes;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.persistence.OpenJPAEnum;

/**
 * Algorithm to use for computing the size of an LRS relation.
 *
 * @since 1.0.0
 * @published
 */
public enum LRSSizeAlgorithm implements OpenJPAEnum<Enum<?>> {
    UNKNOWN(LRSSizes.SIZE_UNKNOWN, "unknown"),
    LAST(LRSSizes.SIZE_LAST, "last"),
    QUERY(LRSSizes.SIZE_QUERY, "query");

    private final int lrsConstant;
    private final String[] _names;
    
    private LRSSizeAlgorithm(int value, String...aliases) {
        lrsConstant = value;
        _names = aliases;
    }

    public int toKernelConstant() {
        return lrsConstant;
    }

    static LRSSizeAlgorithm fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case LRSSizes.SIZE_UNKNOWN:
                return UNKNOWN;

            case LRSSizes.SIZE_LAST:
                return LAST;

            case LRSSizes.SIZE_QUERY:
                return QUERY;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }

    public int convertToKernelConstant(String s) {
        return LRSSizeAlgorithm.toKernelConstantFromString(s);
    }
    
    public int convertToKernelConstant(int i) {
        try {
            if (i == FetchConfiguration.DEFAULT)
                return i;
            return LRSSizeAlgorithm.values()[i].ordinal();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(i + " is invalid value for LRSSize Algorithm");
        }
    }
    
    public static int toKernelConstantFromString(String s) {
        for (LRSSizeAlgorithm level : LRSSizeAlgorithm.values()) {
            for (String name : level._names) {
                if (name.equalsIgnoreCase(s) || String.valueOf(level.toKernelConstant()).equals(s))
                    return level.toKernelConstant();
            }
        }
        throw new IllegalArgumentException(s + " is not a valid name for " + IsolationLevel.class.getName());
    }


}
