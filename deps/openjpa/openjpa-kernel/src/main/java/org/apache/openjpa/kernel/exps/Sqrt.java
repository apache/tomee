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

/**
 * Take the square root of a number.
 *
 * @author Abe White
 */
class Sqrt
    extends UnaryMathVal {

    /**
     * Constructor. Provide the number whose square root to calculate.
     */
    public Sqrt(Val val) {
        super(val);
    }

    protected Class getType(Class c) {
        return double.class;
    }

    protected Object operate(Object o, Class c) {
        return Double.valueOf(Math.sqrt(((Number) o).doubleValue()));
    }
}
