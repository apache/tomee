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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UnsupportedException;

/**
 * In-memory Any implementation. Currently unsupported since
 * in-memory subqueries are not supported.
 *
 * @author Marc Prud'hommeaux
 */
class Any
    extends UnaryMathVal {

    private static final Localizer _loc = Localizer.forPackage(Any.class);

    public Any(Val val) {
        super(val);
    }

    protected Class getType(Class c) {
        Class wrap = Filters.wrap(c);
        if (wrap == Integer.class
            || wrap == Float.class
            || wrap == Double.class
            || wrap == Long.class
            || wrap == BigDecimal.class
            || wrap == BigInteger.class)
            return c;
        return int.class;
    }

    protected Object operate(Object o, Class c) {
        throw new UnsupportedException(_loc.get("in-mem-subquery"));
    }
}
