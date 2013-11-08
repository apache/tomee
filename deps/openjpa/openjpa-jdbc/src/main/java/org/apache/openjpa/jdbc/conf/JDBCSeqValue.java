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

import org.apache.openjpa.conf.SeqValue;
import org.apache.openjpa.jdbc.kernel.ClassTableJDBCSeq;
import org.apache.openjpa.jdbc.kernel.NativeJDBCSeq;
import org.apache.openjpa.jdbc.kernel.TableJDBCSeq;
import org.apache.openjpa.jdbc.kernel.ValueTableJDBCSeq;
import org.apache.openjpa.jdbc.meta.SequenceMapping;
import org.apache.openjpa.kernel.TimeSeededSeq;

/**
 * Adds additional aliases to base {@link SeqValue}. This subclass is
 * not added to the configuration object because it is not visible to it.
 * Therefore, this class should not attempt to alter sequence instantiation
 * behavior. The aliases defined by this subclass are added to the
 * configuration, however, and this subclass may also be instantiated by
 * other components for creation of sequences without manual alias setting.
 *
 * @author Abe White
 * @nojavadoc
 */
public class JDBCSeqValue
    extends SeqValue {

    static final String[] ALIASES = new String[]{
        SequenceMapping.IMPL_TABLE, TableJDBCSeq.class.getName(),
        SequenceMapping.IMPL_VALUE_TABLE, ValueTableJDBCSeq.class.getName(),
        SequenceMapping.IMPL_CLASS_TABLE, ClassTableJDBCSeq.class.getName(),
        SequenceMapping.IMPL_NATIVE, NativeJDBCSeq.class.getName(),
        SequenceMapping.IMPL_TIME, TimeSeededSeq.class.getName(),
        // deprecated aliases
        "db", TableJDBCSeq.class.getName(),
        "db-class", ClassTableJDBCSeq.class.getName(),
        "sjvm", TimeSeededSeq.class.getName(),
    };

    public JDBCSeqValue(String prop) {
        super(prop);
        setAliases(ALIASES);
        setDefault(ALIASES[0]);
        setClassName(ALIASES[1]);
    }
}
