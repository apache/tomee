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
package org.apache.openjpa.jdbc.kernel.exps;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.kernel.exps.WildcardMatch;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * Tests if the target matches the wildcard expression given in the
 * argument. The wildcard '?' is used to represent any single character,
 * while '*' is used to represent any series of 0 or more characters.
 *  Examples:<br />
 * <code>"address.street.ext:wildcardMatch (\"?ain*reet\")"</code>
 *
 * @nojavadoc
 * @deprecated Use <code>matches()</code> instead.
 */
public class JDBCWildcardMatch
    extends WildcardMatch
    implements JDBCFilterListener {

    private static final Localizer _loc = Localizer.forPackage
        (JDBCWildcardMatch.class);

    public void appendTo(SQLBuffer sql, FilterValue target, FilterValue[] args,
        ClassMapping type, JDBCStore store) {
        if (!args[0].isConstant())
            throw new UserException(_loc.get("const-only", TAG));

        Object val = args[0].getValue();
        target.appendTo(sql);
        if (val == null)
            sql.append(" IS ").appendValue(null);
        else {
            // create a DB wildcard string by replacing '*' with '%' and
            // '?' with '_'
            String wild = val.toString().replace('*', '%').replace('?', '_');
            sql.append(" LIKE ").appendValue(wild);
        }
    }
}
