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
 * Interface for any literal value.
 *
 * @author Abe White
 * @nojavadoc
 */
public interface Literal
    extends Value, Constant {

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_NUMBER = 1;
    public static final int TYPE_BOOLEAN = 2;
    public static final int TYPE_STRING = 3;
    public static final int TYPE_SQ_STRING = 4; // single-quoted string
    public static final int TYPE_CLASS = 5;
    public static final int TYPE_ENUM = 6;
    public static final int TYPE_COLLECTION = 7;
    public static final int TYPE_DATE = 8;
    public static final int TYPE_TIME = 9;
    public static final int TYPE_TIMESTAMP = 10;

    /**
     * The value of this literal.
     */
    public Object getValue();

    /**
     * The value of this literal.
     */
    public void setValue(Object val);

    /**
     * The type the literal was parsed as.
     */
    public int getParseType();
}

