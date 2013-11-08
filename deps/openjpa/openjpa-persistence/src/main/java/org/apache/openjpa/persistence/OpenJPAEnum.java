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
package org.apache.openjpa.persistence;

/**
 * An interface to define conversion of a facade based enum to a kernel integer constant.
 * Facade specific enums implement this interface to convert user specified string or integer.
 * 
 * @author Pinaki Poddar
 * @since 2.0.0
 * 
 * @param <E> the enum type that needs to be converted. 
 */
public interface OpenJPAEnum<E extends Enum<?>> {
    /**
     * Convert this receiver to an equivalent kernel constant.
     */
    int toKernelConstant();
    
    /**
     * Convert the given integer to an equivalent kernel constant.
     * This method has a <em>static</em> semantics in the sense that it can be invoked on any enum instance, 
     * but the conversion is done w.r.t. all enums of the generic type. 
     * 
     * @exception throw IllegalArgumentException if no enum instance of the generic type matches the given integer. 
     */
    int convertToKernelConstant(int i);

    /**
     * Convert the given String to an equivalent kernel constant.
     * This method has a <em>static</em> semantics in the sense that it can be invoked on any enum instance, 
     * but the conversion is done w.r.t. all enums of the generic type. 
     * 
     * @exception throw IllegalArgumentException if no enum instance of the generic type matches the given String. 
     */
    int convertToKernelConstant(String s);
}
