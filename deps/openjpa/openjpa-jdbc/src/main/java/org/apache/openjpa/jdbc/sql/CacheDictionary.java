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
package org.apache.openjpa.jdbc.sql;

/**
 * Dictionary for Intersystems Cache.
 */
public class CacheDictionary
    extends DBDictionary {

    public CacheDictionary() {
        platform = "Intersystems Cache";
        supportsDeferredConstraints = false;
        supportsSelectForUpdate = true;
        validationSQL = "SET OPTION DUMMY=DUMMY";

        bigintTypeName = "NUMERIC";
        numericTypeName = "NUMERIC";
        clobTypeName = "LONGVARCHAR";
        blobTypeName = "LONGVARBINARY";

        // use Cache's objectscript for assigning auto-assigned values
        autoAssignClause = "DEFAULT OBJECTSCRIPT '$INCREMENT(^LogNumber)'";

        // there is no built-in function for getting the last generated
        // key in Cache; using MAX will have to suffice
        lastGeneratedKeyQuery = "SELECT MAX({0}) FROM {1}";
    }
}
