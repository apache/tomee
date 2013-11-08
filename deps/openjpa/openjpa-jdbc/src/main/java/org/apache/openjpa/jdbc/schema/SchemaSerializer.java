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

import org.apache.openjpa.lib.meta.MetaDataSerializer;

/**
 * Interface for schema serializers. Serializers work at the fine-grained
 * fine-grained table level to allow you to split schemas among multiple files.
 *
 * @author Abe White
 * @nojavadoc
 */
public interface SchemaSerializer
    extends MetaDataSerializer {

    /**
     * Return the set of tables that will be serialized.
     */
    public Table[] getTables();

    /**
     * Add the given table to the set of tables that will be serialized.
     */
    public void addTable(Table table);

    /**
     * Remove the given table from the set to be serialized.
     *
     * @return true if table was removed, false if not in set
     */
    public boolean removeTable(Table table);

    /**
     * Add the given schema's objects to the set of objects that will be
     * serialized.
     */
    public void addAll(Schema schema);

    /**
     * Add all the objects in the given group to the set of objects that
     * will be serialized.
     */
    public void addAll(SchemaGroup group);

    /**
     * Remove the given schema's objects from the set to be serialized.
     *
     * @return true if any objects in schema removed, false if none in set
     */
    public boolean removeAll(Schema schema);

    /**
     * Remove all schemas in the given group from the set to be serialized.
     *
     * @return true if any objects in the group were removed, false if
     * none in set
     */
    public boolean removeAll(SchemaGroup group);

    /**
     * Clear the set of objects to be serialized.
     */
    public void clear();
}
