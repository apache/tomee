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

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.kernel.exps.Context;

/**
 * Tracks joins made when traversing relations in a select.
 *
 * @author Abe White
 */
public interface Joins {

    /**
     * Whether we have any joins.
     */
    public boolean isEmpty();

    /**
     * Whether this joins path results in outer joins.
     */
    public boolean isOuter();

    /**
     * Perform a cross join on the given tables.
     */
    public Joins crossJoin(Table localTable, Table foreignTable);

    /**
     * Join the columns of the given foreign key.
     */
    public Joins join(ForeignKey fk, boolean inverse, boolean toMany);

    /**
     * Join the columns of the given foreign key.
     */
    public Joins outerJoin(ForeignKey fk, boolean inverse, boolean toMany);

    /**
     * Join the columns of the given foreign key, which represents a relation
     * via the given field name.
     */
    public Joins joinRelation(String name, ForeignKey fk, ClassMapping target,
        int subclasses, boolean inverse, boolean toMany);

    /**
     * Join the columns of the given foreign key, which represents a relation
     * via the given field name.
     */
    public Joins outerJoinRelation(String name, ForeignKey fk, 
        ClassMapping target, int subclasses, boolean inverse, boolean toMany);

    /**
     * Set the variable name being traversed into with the next join.
     */
    public Joins setVariable(String var);

    /**
     * Set the subquery alias.
     */
    public Joins setSubselect(String alias);

    /**
     * Set subquery context when traversing into the next join is
     * in transition from parent context to subquery.
     * @param context
     */
    public Joins setJoinContext(Context context);
    
    /**
     * Set the correlated variable name being traversed into
     * with the next join.
     */
    public Joins setCorrelatedVariable(String var);

    /**
     * Return correlated variable name
     * @return
     */
    public String getCorrelatedVariable();

    /**
     * Move joins that belong to subquery's parent
     */
    public void moveJoinsToParent();
}
