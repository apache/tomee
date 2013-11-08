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
package org.apache.openjpa.util;

import org.apache.openjpa.lib.util.Localizer.Message;

/**
 * Base exception type for user errors.
 *
 * @author Marc Prud'hommeaux
 * @since 0.2.5
 */
public class UserException
    extends OpenJPAException {

    public static final int METADATA = 1;
    public static final int INVALID_STATE = 2;
    public static final int NO_TRANSACTION = 3;
    public static final int CALLBACK = 4;
    public static final int NO_RESULT = 5;
    public static final int NON_UNIQUE_RESULT = 6;

    public UserException() {
    }

    public UserException(String msg) {
        super(msg);
    }

    public UserException(Message msg) {
        super(msg);
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    public UserException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public UserException(Message msg, Throwable cause) {
        super(msg, cause);
    }

    public int getType() {
        return USER;
    }
}
