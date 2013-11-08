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
package org.apache.openjpa.validation;

import org.apache.openjpa.util.OpenJPAException;

@SuppressWarnings("serial")
public class ValidationUnavailableException extends OpenJPAException {

    public ValidationUnavailableException(String msg) {
        super(msg);
    }

    public ValidationUnavailableException(String msg, RuntimeException e) {
        super(msg, e);
    }

    public ValidationUnavailableException(String msg, RuntimeException e, 
        boolean fatal) {
        super(msg, e);
        setFatal(fatal);
    }
    
    @Override
    public int getType() {
        return UNAVAILABLE;
    }
}
