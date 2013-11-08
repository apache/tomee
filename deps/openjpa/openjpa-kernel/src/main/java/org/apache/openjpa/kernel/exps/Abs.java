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

/**
 * Take the absolute value of a number.
 */
class Abs
    extends UnaryMathVal {

    /**
     * Constructor. Provide the number whose absolute value to calculate.
     */
    public Abs(Val val) {
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
            return Filters.unwrap(c);
        return int.class;
    }

    protected Object operate(Object o, Class c) {
        c = Filters.wrap(c);
        if (c == Integer.class)
            return Math.abs(((Number) o).intValue());
        if (c == Float.class)
            return Float.valueOf(Math.abs(((Number) o).floatValue()));
        if (c == Double.class)
            return Double.valueOf(Math.abs(((Number) o).doubleValue()));
        if (c == Long.class)
            return Math.abs(((Number) o).longValue());
        if (c == BigDecimal.class)
            return ((BigDecimal) o).abs();
        if (c == BigInteger.class)
            return ((BigInteger) o).abs();

        // default to int
        return Math.abs(((Number) o).intValue());
    }
}
