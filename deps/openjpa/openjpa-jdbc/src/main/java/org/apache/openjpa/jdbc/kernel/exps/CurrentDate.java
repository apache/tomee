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

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.util.InternalException;

/**
 * A literal current DATE/TIME/TIMESTAMP value in a filter.
 *
 * @author Marc Prud'hommeaux
 */
class CurrentDate
    extends Const {

    private final Class<? extends Date> _type;

    public CurrentDate(Class<? extends Date> type) {
        _type = type;
    }

    public Class<? extends Date> getType() {
        return _type;
    }

    public void setImplicitType(Class type) {
    }

    public Object load(ExpContext ctx, ExpState state, Result res) throws SQLException {
        if (Timestamp.class.isAssignableFrom(_type)) {
            return res.getTimestamp(this, null);
        } else if (Time.class.isAssignableFrom(_type)) {
            return res.getTime(this, null);
        } else if (Date.class.isAssignableFrom(_type)) {
            return res.getDate(this, null);
        } else {
            throw new InternalException();
        }
    }
    
    public Object getValue(Object[] params) {
        try {
            return _type.getConstructor(long.class).newInstance(System.currentTimeMillis());
        } catch (Exception e) {
            return new Date();
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, SQLBuffer sql, int index) {
        if (Timestamp.class.isAssignableFrom(_type)) {
            sql.append(ctx.store.getDBDictionary().currentTimestampFunction);
        } else if (Time.class.isAssignableFrom(_type)) {
            sql.append(ctx.store.getDBDictionary().currentTimeFunction);
        } else if (Date.class.isAssignableFrom(_type)) {
            sql.append(ctx.store.getDBDictionary().currentDateFunction);
        } else {
            throw new InternalException();
        }
    }
}
