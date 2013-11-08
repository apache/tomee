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
package org.apache.openjpa.jdbc.schema;

/**
 * Factory for {@link SchemaGroup}s. Users can plug in their own factory
 * implementation, or rely on the ones provided. Most schema factories
 * will probably implement {@link org.apache.openjpa.lib.conf.Configurable} to
 * receive the system configuration on construction.
 *
 * @author Abe White
 */
public interface SchemaFactory {

    /**
     * Return the schema group for the current object model and database.
     */
    public SchemaGroup readSchema();

    /**
     * Record the schema group after changes may have been made.
     *
     * @param schema the schema definition for the entire system
     */
    public void storeSchema(SchemaGroup schema);
}
