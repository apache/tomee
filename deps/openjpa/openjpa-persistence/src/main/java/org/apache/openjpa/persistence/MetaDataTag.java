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

/////////////////////////////////////////////////////////
// NOTE: when adding a new type, make sure to update the
// table in PersistenceMetaDataParser
/////////////////////////////////////////////////////////

/**
 * Set of metadata tags used in JPA.
 *
 * @author Abe White
 * @nojavadoc
 */
public enum MetaDataTag {
    // sorted by XML order
    ACCESS,
    CACHEABLE,
    MAPPED_SUPERCLASS,
    ENTITY,
    EMBEDDABLE,
    ENTITY_LISTENERS,
    FLUSH_MODE,
    GENERATED_VALUE,
    ID,
    EMBEDDED_ID,
    EXCLUDE_DEFAULT_LISTENERS,
    EXCLUDE_SUPERCLASS_LISTENERS,
    ID_CLASS,
    LOB,
    MAP_KEY,
    MAP_KEY_CLASS,
    MAPPED_BY_ID,
    NATIVE_QUERIES,
    NATIVE_QUERY,
    QUERY_STRING,
    ORDER_BY,
    ORDER_COLUMN,
    QUERIES,
    QUERY,
    QUERY_HINT,
    POST_LOAD,
    POST_PERSIST,
    POST_REMOVE,
    POST_UPDATE,
    PRE_PERSIST,
    PRE_REMOVE,
    PRE_UPDATE,
    SEQ_GENERATOR,
    VERSION,
    // openjpa extensions
    DATA_CACHE,
    DATASTORE_ID,
    DEPENDENT,
    DETACHED_STATE,
    ELEM_DEPENDENT,
    ELEM_TYPE,
    EXTERNAL_VALS,
    EXTERNAL_VAL,
    EXTERNALIZER,
    FACTORY,
    FETCH_GROUP,
    FETCH_GROUPS,
    FETCH_ATTRIBUTE,
    REFERENCED_FETCH_GROUP,
    INVERSE_LOGICAL,
    KEY_DEPENDENT,
    KEY_TYPE,
    LOAD_FETCH_GROUP,
    LRS,
    MANAGED_INTERFACE,
    READ_ONLY,
    TYPE,
    REPLICATED,
    OPENJPA_VERSION
}
