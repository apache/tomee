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

/**
 * Abstract validation class which provides base validation methods.
 */
public abstract class AbstractValidator implements Validator {

    public abstract <T> ValidationException validate(T arg0, int event);

    public abstract <T> ValidationException validateProperty(T arg0, 
            String property, int event); 

    public abstract <T> ValidationException validateValue(Class<T> arg0, 
        String arg1, Object arg2, int event);

    public <T> boolean validating(T arg0, int event) {
        return false;
    }
}
