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
package org.apache.openjpa.jdbc.conf;

import org.apache.openjpa.jdbc.kernel.EagerFetchModes;
import org.apache.openjpa.lib.conf.IntValue;

/**
 * Value type used to represent fetch modes. This type is
 * defined separately so that it can be used both in the global configuration
 * and in class metadata with the same encapsulated configuration.
 *
 * @author Abe White
 * @nojavadoc
 */
public class FetchModeValue
    extends IntValue {

    public static final String EAGER_NONE = "none";
    public static final String EAGER_JOIN = "join";
    public static final String EAGER_PARALLEL = "parallel";

    private static String[] ALIASES = new String[]{
        EAGER_PARALLEL, String.valueOf(EagerFetchModes.EAGER_PARALLEL),
        EAGER_JOIN, String.valueOf(EagerFetchModes.EAGER_JOIN),
        EAGER_NONE, String.valueOf(EagerFetchModes.EAGER_NONE),
        // deprecated
        "multiple", String.valueOf(EagerFetchModes.EAGER_PARALLEL),
        "single", String.valueOf(EagerFetchModes.EAGER_JOIN),
    };

    public FetchModeValue(String prop) {
        super(prop);
        setAliases(ALIASES);
        setAliasListComprehensive(true);
    }
}
