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
package org.apache.openjpa.conf;

import org.apache.openjpa.kernel.TimeSeededSeq;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.meta.SequenceMetaData;

/**
 * Value type used to represent a sequence. This type is
 * defined separately so that it can be used both in the global configuration
 * and in class metadata with the same encapsulated configuration.
 *
 * @author Abe White
 * @nojavadoc
 */
public class SeqValue
    extends PluginValue {

    private static final String[] ALIASES = new String[]{
        SequenceMetaData.IMPL_TIME, TimeSeededSeq.class.getName(),
        SequenceMetaData.IMPL_NATIVE, TimeSeededSeq.class.getName(),
        // deprecated aliases
        "sjvm", TimeSeededSeq.class.getName(),
    };

    public SeqValue(String prop) {
        super(prop, true);
        setAliases(ALIASES);
        setDefault(ALIASES[0]);
        setClassName(ALIASES[1]);
    }
}
