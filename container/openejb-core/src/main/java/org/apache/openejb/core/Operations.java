/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core;

public class Operations {
    final public static byte OP_BUSINESS = (byte) 1;
    final public static byte OP_AFTER_BEGIN = (byte) 2;
    final public static byte OP_AFTER_COMPLETION = (byte) 3;
    final public static byte OP_BEFORE_COMPLETION = (byte) 4;
    final public static byte OP_REMOVE = (byte) 5;
    final public static byte OP_SET_CONTEXT = (byte) 6;
    final public static byte OP_UNSET_CONTEXT = (byte) 7;
    final public static byte OP_CREATE = (byte) 8;
    final public static byte OP_POST_CREATE = (byte) 9;
    final public static byte OP_ACTIVATE = (byte) 10;
    final public static byte OP_PASSIVATE = (byte) 11;
    final public static byte OP_FIND = (byte) 12;
    final public static byte OP_HOME = (byte) 13;
    final public static byte OP_LOAD = (byte) 14;
    final public static byte OP_STORE = (byte) 15;

}