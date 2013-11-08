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

import org.apache.openjpa.lib.meta.MetaDataParser;

/**
 * Interface class for parers that read schema information. Parsers
 * will place all parsed schemas into the current {@link SchemaGroup}, set
 * via the {@link #setSchemaGroup} method. This allows parsing of
 * multiple files into a single schema group.
 *
 * @author Abe White
 * @nojavadoc
 */
public interface SchemaParser
    extends MetaDataParser {

    /**
     * Delay resolution of foreign key constraints until
     * {@link #resolveConstraints} is called. This allows you to parse
     * multiple resources where a foreign key in one resource might refer
     * to a table in another.
     */
    public boolean getDelayConstraintResolve();

    /**
     * Delay resolution of foreign key constraints until
     * {@link #resolveConstraints} is called. This allows you to parse
     * multiple resources where a foreign key in one resource might refer
     * to a table in another.
     */
    public void setDelayConstraintResolve(boolean delay);

    /**
     * Return the current schema group.
     */
    public SchemaGroup getSchemaGroup();

    /**
     * Set the current schema group; this clears all state from the last group.
     */
    public void setSchemaGroup(SchemaGroup group);

    /**
     * If this parser is in delayed resolve mode, resolve all constraints.
     */
    public void resolveConstraints();
}
