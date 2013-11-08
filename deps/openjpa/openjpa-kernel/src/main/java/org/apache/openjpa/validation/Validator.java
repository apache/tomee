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
 * Basic validation interface which defines the contract for
 * event based validation.  Event values are defined in LifeCycleEvent.
 */
public interface Validator {

    /**
     * Validates a given instance
     * 
     * @param <T> The instance to validate
     * @param arg0 The class, of type T to validate
     * @param event The event id
     * @return ValidationException if the validator produces one or more
     *         constraint violations.
     */
    public <T> ValidationException validate(T arg0, int event);

    /**
     * Validates a property of a given instance
     * 
     * @param <T> The instance to validate
     * @param arg0 The class, of type T to validate
     * @param property The property to validate
     * @param event The event id
     * @return ValidationException if the validator produces one or more
     *         constraint violations.
     */
    public <T> ValidationException validateProperty(T arg0, 
            String property, int event); 

    /**
     * Validates a value based upon the constraints applied to a given class
     * attribute.
     * @param <T> The instance type to base validation upon
     * @param arg0 The class of type T to validate
     * @param arg1 The property to validate
     * @param arg2 The property value to validate
     * @param event The event id
     * @return ValidationException if the validator produces one or more
     *         constraint violations.
     */
    public <T> ValidationException validateValue(Class<T> arg0, 
        String arg1, Object arg2, int event);

    /**
     * Method for determining whether validation is active for the given 
     * type and event.
     * 
     * @param <T>
     * @param arg0 Type being validated
     * @param event event type
     * @return true if validation is active for the specified event
     */
    public <T> boolean validating(T arg0, int event);
}
