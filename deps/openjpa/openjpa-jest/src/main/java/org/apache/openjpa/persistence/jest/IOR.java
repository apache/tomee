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

package org.apache.openjpa.persistence.jest;

import static org.apache.openjpa.persistence.jest.Constants.NULL_VALUE;

import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * String reference of a managed object.
 *  
 * @author Pinaki Poddar
 *
 */
public class IOR {
    public static final char DASH = '-';
    /**
     * Stringified representation of a managed instance identity.
     * The simple Java type name and the persistent identity separated by a {@link Constants#DASH dash}.
     *  
     * @param sm a managed instance.
     * @return
     */
    public static String toString(OpenJPAStateManager sm) {
        if (sm == null) return NULL_VALUE;
        return sm.getMetaData().getDescribedType().getSimpleName() + DASH + sm.getObjectId();
    }
}
