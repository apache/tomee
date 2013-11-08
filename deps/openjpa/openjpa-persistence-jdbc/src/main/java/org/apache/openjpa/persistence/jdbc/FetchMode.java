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

import org.apache.openjpa.jdbc.kernel.EagerFetchModes;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.persistence.OpenJPAEnum;

/**
 * Type of fetching to employ.
 *
 * @author Abe White
 * @since 0.4.0
 * @published
 */
public enum FetchMode implements OpenJPAEnum<FetchMode>{
    NONE(EagerFetchModes.EAGER_NONE, "none"),
    JOIN(EagerFetchModes.EAGER_JOIN, "join"),
    PARALLEL(EagerFetchModes.EAGER_PARALLEL, "parallel");

    private final int eagerFetchConstant;
    private final String[] _names;
    
    private FetchMode(int value, String... names) {
        eagerFetchConstant = value;
        _names = names;
    }

    public int toKernelConstant() {
        return eagerFetchConstant;
    }

    static FetchMode fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case EagerFetchModes.EAGER_NONE:
                return NONE;

            case EagerFetchModes.EAGER_JOIN:
                return JOIN;

            case EagerFetchModes.EAGER_PARALLEL:
                return PARALLEL;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }
    
    public int convertToKernelConstant(String s) {
        return FetchMode.toKernelConstantFromString(s);
    }
    
    public int convertToKernelConstant(int i) {
        if (i == FetchConfiguration.DEFAULT)
            return i;
        for (FetchMode mode : FetchMode.values()) {
            if (mode.eagerFetchConstant == i)
                return i;
        }
        throw new IllegalArgumentException(i + " is invalid value for FetchMode");
    }
    
    public static int toKernelConstantFromString(String s) {
        for (FetchMode level : FetchMode.values()) {
            for (String name : level._names)
               if (name.equalsIgnoreCase(s) || String.valueOf(level.toKernelConstant()).equals(s))
                   return level.toKernelConstant();
        }
        throw new IllegalArgumentException(s + " is not a valid name for " + FetchMode.class.getName());
    }
}
