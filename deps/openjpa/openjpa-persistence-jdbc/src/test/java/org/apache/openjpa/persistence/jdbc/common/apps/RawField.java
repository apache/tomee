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
package org.apache.openjpa.persistence.jdbc.common.apps;

import org.apache.openjpa.jdbc.kernel.*;
import org.apache.openjpa.jdbc.sql.*;
import org.apache.openjpa.jdbc.meta.*;
import org.apache.openjpa.jdbc.meta.strats.*;

public class RawField {

    private String str;

    public void setString(String s) {
        str = s;
    }

    public String getString() {
        return str;
    }

    public static class RawMapping
        extends ImmutableValueHandler {

        public Object toDataStoreValue(ValueMapping vm, Object val,
            JDBCStore store) {
            return new Raw("'" + val + "FOO'");
        }
    }
}
