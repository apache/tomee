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
import org.apache.openjpa.kernel.exps.StringContains;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * Tests if the target contains the given argument. The argument must be
 * a constant.
 *  Examples:<br />
 * <code>"address.street.ext:stringContains (\"main\")"</code>
 *
 * @nojavadoc
 * @deprecated Use <code>matches()</code> instead.
 */
public class JDBCStringContains
    extends StringContains
    implements JDBCFilterListener {

    private static final Localizer _loc = Localizer.forPackage
        (JDBCStringContains.class);

    public void appendTo(SQLBuffer buf, FilterValue target, FilterValue[] args,
        ClassMapping type, JDBCStore store) {
        if (!args[0].isConstant())
            throw new UserException(_loc.get("const-only", TAG));

        Object val = args[0].getValue();
        target.appendTo(buf);
        if (val == null)
            buf.append(" IS ").appendValue(null);
        else
            buf.append(" LIKE ").appendValue("%" + val + "%");
    }
}
