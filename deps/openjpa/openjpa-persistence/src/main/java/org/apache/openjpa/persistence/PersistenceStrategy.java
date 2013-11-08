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

///////////////////////////////////////////////////////////////////
// NOTE: when adding a new classification, make sure to update the
// table in PersistenceMetaDataDefaults
///////////////////////////////////////////////////////////////////

/**
 * Set of field persistence strategies used in JPA.
 *
 * @author Abe White
 * @nojavadoc
 */
public enum PersistenceStrategy {
    // order matters to match XML spec
    BASIC,
    MANY_ONE,
    ONE_MANY,
    ONE_ONE,
    MANY_MANY,
    ELEM_COLL,
    EMBEDDED,
    TRANSIENT,
    // openjpa extensions
    PERS,
    PERS_COLL,
    PERS_MAP,
}
