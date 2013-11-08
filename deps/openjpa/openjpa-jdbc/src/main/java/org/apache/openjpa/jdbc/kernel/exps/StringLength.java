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

import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;

/**
 * Returns the number of characters in a string.
 *
 * @author Marc Prud'hommeaux
 */
public class StringLength
    extends StringFunction {

    private Class _cast = null;

    /**
     * Constructor. Provide the string to operate on.
     */
    public StringLength(Val val) {
        super(val);
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        return int.class;
    }

    public void setImplicitType(Class type) {
        _cast = type;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf, int index) {
        DBDictionary dict = ctx.store.getDBDictionary();
        String func = dict.stringLengthFunction;
        dict.assertSupport(func != null, "StringLengthFunction");
        func = dict.getCastFunction(getValue(), func);
        
        int idx = func.indexOf("{0}");
        buf.append(func.substring(0, idx));
        getValue().appendTo(sel, ctx, state, buf, index);
        buf.append(func.substring(idx + 3));
    }

    public int getId() {
        return Val.LENGTH_VAL;
    }
}

