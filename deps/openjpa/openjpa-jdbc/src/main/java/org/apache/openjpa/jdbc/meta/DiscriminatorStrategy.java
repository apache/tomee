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
package org.apache.openjpa.jdbc.meta;

import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;

/**
 * Handles determining the object class of database records.
 *
 * @author Abe White
 */
public interface DiscriminatorStrategy
    extends Strategy {

    /**
     * Set the Discriminator that uses this strategy. This will be called
     * before use.
     */
    public void setDiscriminator(Discriminator owner);

    /**
     * Select the data for this Discriminator.
     *
     * @param mapping the known base class being selected; this may
     * not be the base class in the inheritance hierarchy
     * @return true if anything was selected; false otherwise
     */
    public boolean select(Select sel, ClassMapping mapping);

    /**
     * Load all subclasses of the owning class mapping into the JVM.
     */
    public void loadSubclasses(JDBCStore store)
        throws SQLException, ClassNotFoundException;

    /**
     * Return the class for the current result row.
     */
    public Class<?> getClass(JDBCStore store, ClassMapping base, Result result)
        throws SQLException, ClassNotFoundException;

    /**
     * Whether any class conditions are necessary.
     *
     * @see #getClassConditions
     */
    public boolean hasClassConditions(ClassMapping base, boolean subs);

    /**
     * Return SQL to limit the classes selected as much as possible to the
     * given base class, and optionally its subclasses. The select and joins 
     * instances are supplied in order to get column aliases.
     */
    public SQLBuffer getClassConditions(Select sel, Joins joins, 
        ClassMapping base, boolean subs);
}
