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
package org.apache.openejb.client;

public interface ResponseCodes {

    public static final int AUTH_GRANTED = 1;
    public static final int AUTH_REDIRECT = 2;
    public static final int AUTH_DENIED = 3;
    public static final int EJB_OK = 4;
    public static final int EJB_OK_CREATE = 5;
    public static final int EJB_OK_FOUND = 6;
    public static final int EJB_OK_FOUND_COLLECTION = 7;
    public static final int EJB_OK_NOT_FOUND = 8;
    public static final int EJB_APP_EXCEPTION = 9;
    public static final int EJB_SYS_EXCEPTION = 10;
    public static final int EJB_ERROR = 11;
    public static final int JNDI_OK = 12;
    public static final int JNDI_EJBHOME = 13;
    public static final int JNDI_CONTEXT = 14;
    public static final int JNDI_ENUMERATION = 15;
    public static final int JNDI_NOT_FOUND = 16;
    public static final int JNDI_NAMING_EXCEPTION = 17;
    public static final int JNDI_RUNTIME_EXCEPTION = 18;
    public static final int JNDI_ERROR = 19;
    public static final int EJB_OK_FOUND_ENUMERATION = 20;
}

