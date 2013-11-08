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
 * Base exception for data store errors.
 *
 * @author Marc Prud'hommeaux
 * @since 0.2.5
 */
@SuppressWarnings("serial")
public class StoreException
    extends OpenJPAException {

    public static final int LOCK = 1;
    public static final int OBJECT_NOT_FOUND = 2;
    public static final int OPTIMISTIC = 3;
    public static final int REFERENTIAL_INTEGRITY = 4;
    public static final int OBJECT_EXISTS = 5;
    public static final int QUERY = 6;

    public StoreException(String msg) {
        super(msg);
    }

    public StoreException(Message msg) {
        super(msg.getMessage());
    }

    public StoreException(Throwable cause) {
        super(cause);
    }

    public StoreException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public StoreException(Message msg, Throwable cause) {
        super(msg.getMessage(), cause);
    }
    
    public int getType() {
        return STORE;
    }
}
