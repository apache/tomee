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

/**
 * Interface supplying additional exception information. All OpenJPA
 * exceptions implement this interface.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface ExceptionInfo {

    public static final int GENERAL = 0;
    public static final int INTERNAL = 1;
    public static final int STORE = 2;
    public static final int UNSUPPORTED = 3;
    public static final int USER = 4;
    public static final int WRAPPED = 5;
    public static final int UNAVAILABLE = 6;


    /**
     * Exception message.
     *
     * @see Throwable#getMessage
     */
    public String getMessage();

    /**
     * Returns the first {@link Throwable} from {@link #getNestedThrowables}
     * in order to conform to {@link Throwable#getCause} in Java 1.4+.
     *
     * @see Throwable#getCause
     */
    public Throwable getCause();

    /**
     * Stack.
     *
     * @see Throwable#printStackTrace
     */
    public void printStackTrace();

    /**
     * Exception type.
     */
    public int getType();

    /**
     * Exception subtype.
     */
    public int getSubtype();

    /**
     * Whether this error is fatal.
     */
    public boolean isFatal();

    /**
     * The nested throwables.
     */
    public Throwable[] getNestedThrowables();

    /**
     * The failed object.
     */
    public Object getFailedObject();
}

