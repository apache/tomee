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
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.UserException;

/**
 * Simple listener which embeds its SQL argument into the query. Listens
 * on <code>sql</code>.
 *  Example:<br />
 * <code>"price &lt; sql(\"(SELECT AVG (PRICE) FROM PRODUCT_TABLE)\")"</code>
 *
 * @nojavadoc
 */
public class SQLEmbed
    implements JDBCFilterListener {

    public static String TAG = "sql";

    private static final Localizer _loc = Localizer.forPackage(SQLEmbed.class);

    public String getTag() {
        return TAG;
    }

    public boolean expectsArguments() {
        return true;
    }

    public boolean expectsTarget() {
        return false;
    }

    public Object evaluate(Object target, Class targetClass, Object[] args,
        Class[] argClasses, Object candidate, StoreContext ctx) {
        throw new UnsupportedException(_loc.get("no-in-mem", TAG));
    }

    public void appendTo(SQLBuffer buf, FilterValue target, FilterValue[] args,
        ClassMapping type, JDBCStore store) {
        if (!args[0].isConstant())
            throw new UserException(_loc.get("const-only", TAG));
        buf.append(args[0].getValue().toString());
    }

    public Class getType(Class targetClass, Class[] argClasses) {
        return Object.class;
    }
}
