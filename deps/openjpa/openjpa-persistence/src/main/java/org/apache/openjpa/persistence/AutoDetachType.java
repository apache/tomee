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
package org.apache.openjpa.persistence;

import java.util.EnumSet;

import org.apache.openjpa.kernel.AutoDetach;

/**
 * The possible settings for the auto-detach behavior of an
 * {@link OpenJPAEntityManager}.
 *
 * @since 1.0.0
 * @published
 */
public enum AutoDetachType {
    NONE(AutoDetach.DETACH_NONE),
    CLOSE(AutoDetach.DETACH_CLOSE),
    COMMIT(AutoDetach.DETACH_COMMIT),
    NON_TRANSACTIONAL_READ(AutoDetach.DETACH_NONTXREAD),
    ROLLBACK(AutoDetach.DETACH_ROLLBACK);

    private final int autoDetachConstant;

    private AutoDetachType(int value) {
        autoDetachConstant = value;
    }

    public static EnumSet<AutoDetachType> toEnumSet(int autoDetach) {
        EnumSet<AutoDetachType> types = EnumSet.noneOf(AutoDetachType.class);
        if ((autoDetach & AutoDetach.DETACH_NONE) != 0) 
        	types.add(NONE);
        if ((autoDetach & AutoDetach.DETACH_CLOSE) != 0)
            types.add(CLOSE);
        if ((autoDetach & AutoDetach.DETACH_COMMIT) != 0)
            types.add(COMMIT);
        if ((autoDetach & AutoDetach.DETACH_NONTXREAD) != 0)
            types.add(NON_TRANSACTIONAL_READ);
        if ((autoDetach & AutoDetach.DETACH_ROLLBACK) != 0)
            types.add(ROLLBACK);
        return types;
    }

    public static int fromEnumSet(EnumSet<AutoDetachType> types) {
        int autoDetach = 0;
        for (AutoDetachType type : types)
            autoDetach |= type.autoDetachConstant;
        return autoDetach;
    }
}
