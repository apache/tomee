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
package org.apache.openjpa.kernel.exps;

import org.apache.openjpa.kernel.StoreContext;

/**
 * Tests if the target contains the given argument. The argument must be
 * a constant.
 *  Examples:<br />
 * <code> "address.street.ext:stringContains (\"main\")"
 * </code>
 *
 * @deprecated Use <code>matches()</code> instead.
 * @nojavadoc
 */
public class StringContains
    implements FilterListener {

    public static final String TAG = "stringContains";

    public String getTag() {
        return TAG;
    }

    public boolean expectsArguments() {
        return true;
    }

    public boolean expectsTarget() {
        return true;
    }

    public Object evaluate(Object target, Class targetClass, Object[] args,
        Class[] argClasses, Object candidate, StoreContext ctx) {
        if (target == null || args[0] == null)
            return Boolean.FALSE;
        if (target.toString().indexOf(args[0].toString()) != -1)
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public Class getType(Class targetClass, Class[] argClasses) {
        return boolean.class;
    }
}
