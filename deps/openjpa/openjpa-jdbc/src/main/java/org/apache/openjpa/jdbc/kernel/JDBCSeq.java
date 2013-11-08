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
package org.apache.openjpa.jdbc.kernel;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.kernel.Seq;

/**
 * Specialization of the the {@link Seq} interface to provide information
 * on the schema needed by this sequence. Only sequences that require special
 * tables that must be created by OpenJPA tools need to implement this
 * interface.
 *
 * @author Abe White
 */
public interface JDBCSeq
    extends Seq {

    /**
     * Add any tables, etc needed by this factory for the given mapping
     * to the given schema group, if they do not exist already.
     */
    public void addSchema(ClassMapping mapping, SchemaGroup group);
}
