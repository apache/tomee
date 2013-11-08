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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.kernel.exps.Context;

/**
 * Abstraction of a SQL SELECT statement.
 *
 * @author Abe White
 */
public interface Select
    extends SelectExecutor {

    /**
     * Constant indicating to batch the select using an inner join.
     */
    public static final int EAGER_INNER = 0;

    /**
     * Constant indicating to batch the select using an outer join.
     */
    public static final int EAGER_OUTER = 1;

    /**
     * Constant indicating to use a separate select executed in parallel.
     */
    public static final int EAGER_PARALLEL = 2;

    /**
     * Constant indicating a select can be made without joins.
     */
    public static final int TYPE_JOINLESS = 3;

    /**
     * Constant indicating a two-part select and load.
     */
    public static final int TYPE_TWO_PART = 4;

    /**
     * Constant indicating to add conditions to the selcet to select this
     * class and joinable subclasses only.
     */
    public static final int SUBS_JOINABLE = 1;

    /**
     * Constant indicating to add conditions to the select to select this
     * class only.
     */
    public static final int SUBS_NONE = 2;

    /**
     * Constant indicating to select subclasses but without adding any
     * class conditions to the select.
     */
    public static final int SUBS_ANY_JOINABLE = 3;

    /**
     * Constant indicating to select this class but without adding any
     * class conditions to the select.
     */
    public static final int SUBS_EXACT = 4;

    /**
     * The alias to use for the from select, if any.
     */
    public static final String FROM_SELECT_ALIAS = "s";

    /**
     * The index of this select within the UNION, or 0.
     */
    public int indexOf();

    /**
     * Return this select's subselects, or empty collection if none.
     */
    public List getSubselects();

    /**
     * Return the parent of this select, if it is a subselect.
     */
    public Select getParent();

    /**
     * Return the subselect path for this select, if it is a subselect.
     */
    public String getSubselectPath();

    /**
     * Turn this select into a subselect of the given instance.
     */
    public void setParent(Select parent, String path);

    /**
     * Another select instance that creates a temporary table from which
     * this select pulls data.
     */
    public Select getFromSelect();

    /**
     * Another select instance that creates a temporary table from which
     * this select pulls data.
     */
    public void setFromSelect(Select sel);

    /**
     * Whether this select has an eager join of the specified type.
     */
    public boolean hasEagerJoin(boolean toMany);

    /**
     * Whether this select has a join of the specified type.
     */
    public boolean hasJoin(boolean toMany);

    /**
     * Return whether the given table is being used in this select.
     */
    public boolean isSelected(Table table);

    /**
     * Return the set of all used table aliases.
     */
    public Collection getTableAliases();

    /**
     * Return the actual {@link Val}s and {@link Column}s that were
     * selected, in the order that they were selected.
     *
     * @since 1.1.0
     */
    public List getSelects();

    /**
     * Return the aliases of all selected columns and all selected buffers,
     * in the order they were selected. Each alias may be either a string
     * or a {@link SQLBuffer}.
     */
    public List getSelectAliases();

    /**
     * Get the aliases for identifier columns that can be used in COUNT
     * selects to find the number of matches. Each alias will be a
     * string. If no identifier columns have been nominated, then all
     * column alises are returned.
     */
    public List getIdentifierAliases();

    /**
     * Return the ordering SQL for this select.
     */
    public SQLBuffer getOrdering();

    /**
     * Return the grouping SQL for this select.
     */
    public SQLBuffer getGrouping();

    /**
     * Return the WHERE clause, minus any necessary end joins.
     */
    public SQLBuffer getWhere();

    /**
     * Return the HAVING clause, or null if none.
     */
    public SQLBuffer getHaving();

    /**
     * Apply class conditions from relation joins.  This may affect the return
     * values of {@link #getJoins}, {@link #getJoinIterator}, and
     * {@link #getWhere}.
     */
    public void addJoinClassConditions();

    /**
     * Return the top-level joins for this select.
     */
    public Joins getJoins();

    /**
     * Return the top-level {@link Join} elements for this select.
     */
    public Iterator getJoinIterator();

    /**
     * The result start index.
     */
    public long getStartIndex();

    /**
     * The result end index.
     */
    public long getEndIndex();

    /**
     * Set the result range for this select.
     */
    public void setRange(long start, long end);

    /**
     * Return the alias for the given column.
     */
    public String getColumnAlias(Column col);

    /**
     * Return the alias for the given column.
     */
    public String getColumnAlias(Column col, Joins joins);

    /**
     * Return the alias for the given column.
     */
    public String getColumnAlias(String col, Table table);

    /**
     * Return the alias for the given column.
     */
    public String getColumnAlias(String col, Table table, Joins joins);

    /**
     * Return true if this is an aggregate select.
     */
    public boolean isAggregate();

    /**
     * Set to true for aggregate selects.
     */
    public void setAggregate(boolean agg);

    /**
     * Return true if this select includes a LOB.
     */
    public boolean isLob();

    /**
     * Set to true for selects that include LOB columns.
     */
    public void setLob(boolean lob);

    /**
     * Clear the existing column selects.
     */
    public void clearSelects();

    /**
     * Select the given SQL as a placeholder for a UNION element.
     */
    public void selectPlaceholder(String sql);

    /**
     * Select the given SQL; the given id object is an identifier
     * to use when retrieving the corresponding value from a {@link Result}.
     *
     * @return true if selected
     */
    public boolean select(SQLBuffer sql, Object id);

    /**
     * Select the given SQL; the given id object is an identifier
     * to use when retrieving the corresponding value from a {@link Result}.
     *
     * @return true if selected
     */
    public boolean select(SQLBuffer sql, Object id, Joins joins);

    /**
     * Select the given SQL; the given id object is an identifier
     * to use when retrieving the corresponding value from a {@link Result}.
     *
     * @return true if selected
     */
    public boolean select(String sql, Object id);

    /**
     * Select the given SQL; the given id object is an identifier
     * to use when retrieving the corresponding value from a {@link Result}.
     *
     * @return true if selected
     */
    public boolean select(String sql, Object id, Joins joins);

    /**
     * Select the given column.
     *
     * @return true if selected
     */
    public boolean select(Column col);

    /**
     * Select the given column.
     *
     * @return true if selected
     */
    public boolean select(Column col, Joins joins);

    /**
     * Select the given columns.
     *
     * @return bit set of indexes of columns that were selected
     */
    public int select(Column[] cols);

    /**
     * Select the given columns.
     *
     * @return bit set of indexes of columns that were selected
     */
    public int select(Column[] cols, Joins joins);

    /**
     * Select the columns of the given mapping, possibly including subclasses.
     * This method should be called after all where conditions are added in
     * case the given mapping batches other selects.
     */
    public void select(ClassMapping mapping, int subclasses,
        JDBCStore store, JDBCFetchConfiguration fetch, int eager);

    /**
     * Select the columns of the given mapping, possibly including subclasses.
     * This method should be called after all where conditions are added in
     * case the given mapping batches other selects.
     */
    public void select(ClassMapping mapping, int subclasses,
        JDBCStore store, JDBCFetchConfiguration fetch, int eager,
        Joins joins);

    /**
     * Select the given column as one that can be used to get a count of
     * distinct matches. It is not necessary to designate distinct identifiers
     * when eagerly traversing the entire result of the select or when
     * not using an LRSSize setting of <code>count</code>.
     *
     * @return true if selected
     */
    public boolean selectIdentifier(Column col);

    /**
     * Select the given column as one that can be used to get a count of
     * distinct matches. It is not necessary to designate distinct identifiers
     * when eagerly traversing the entire result of the select or when
     * not using an LRSSize setting of <code>count</code>.
     *
     * @return true if selected
     */
    public boolean selectIdentifier(Column col, Joins joins);

    /**
     * Select the given columns as ones that can be used to get a count of
     * distinct matches. It is not necessary to designate distinct identifiers
     * when eagerly traversing the entire result of the select or when
     * not using an LRSSize setting of <code>count</code>.
     *
     * @return bit set of indexes of columns that were selected
     */
    public int selectIdentifier(Column[] cols);

    /**
     * Select the given columns as ones that can be used to get a count of
     * distinct matches. It is not necessary to designate distinct identifiers
     * when eagerly traversing the entire result of the select or when
     * not using an LRSSize setting of <code>count</code>.
     *
     * @return bit set of indexes of columns that were selected
     */
    public int selectIdentifier(Column[] cols, Joins joins);

    /**
     * Select the columns of the given mapping, possibly including subclasses.
     * This method should be called after all where conditions are added in
     * case the given mapping batches other selects.
     * The primary key columns of the mapping can be used to get a count of
     * distinct matches. It is not necessary to designate distinct identifiers
     * when eagerly traversing the entire result of the select or when
     * not using an LRSSize setting of <code>count</code>.
     */
    public void selectIdentifier(ClassMapping mapping, int subclasses,
        JDBCStore store, JDBCFetchConfiguration fetch, int eager);

    /**
     * Select the columns of the given mapping, possibly including subclasses.
     * This method should be called after all where conditions are added in
     * case the given mapping batches other selects.
     * The primary key columns of the mapping can be used to get a count of
     * distinct matches. It is not necessary to designate distinct identifiers
     * when eagerly traversing the entire result of the select or when
     * not using an LRSSize setting of <code>count</code>.
     */
    public void selectIdentifier(ClassMapping mapping, int subclasses,
        JDBCStore store, JDBCFetchConfiguration fetch, int eager,
        Joins joins);

    /**
     * Select the primary key columns of the given mapping, joining to
     * superclasses as necessary to get all columns needed to construct
     * an object id.
     *
     * @return bit set of indexes of pk columns that were selected
     */
    public int selectPrimaryKey(ClassMapping mapping);

    /**
     * Select the primary key columns of the given mapping, joining to
     * superclasses as necessary to get all columns needed to construct
     * an object id.
     *
     * @return bit set of indexes of pk columns that were selected
     */
    public int selectPrimaryKey(ClassMapping mapping, Joins joins);

    /**
     * Clear odering conditions.
     */
    public void clearOrdering();

    /**
     * Order on the primary key columns of the given mapping,
     * joining to superclasses as necessary to get all columns needed to
     * construct an object id.
     * Optionally selects ordering data if not already selected.
     */
    public int orderByPrimaryKey(ClassMapping mapping, boolean asc,
        boolean sel);

    /**
     * Select and order on the primary key columns of the given mapping,
     * joining to superclasses as necessary to get all columns needed to
     * construct an object id.
     * Optionally selects ordering data if not already selected.
     */
    public int orderByPrimaryKey(ClassMapping mapping, boolean asc,
        Joins joins, boolean sel);

    /**
     * Order by the given column.
     * Optionally selects ordering data if not already selected.
     */
    public boolean orderBy(Column col, boolean asc, boolean sel);

    /**
     * Order by the given column.
     * Optionally selects ordering data if not already selected.
     */
    public boolean orderBy(Column col, boolean asc, Joins joins,
        boolean sel);

    /**
     * Order by the given columns.
     * Optionally selects ordering data if not already selected.
     */
    public int orderBy(Column[] cols, boolean asc, boolean sel);

    /**
     * Order by the given columns.
     * Optionally selects ordering data if not already selected.
     */
    public int orderBy(Column[] cols, boolean asc, Joins joins, boolean sel);

    /**
     * Add an ORDER BY clause.
     * Optionally selects ordering data if not already selected.
     */
    public boolean orderBy(SQLBuffer sql, boolean asc, boolean sel,
            Value selAs);

    /**
     * Add an ORDER BY clause.
     * Optionally selects ordering data if not already selected.
     */
    public boolean orderBy(SQLBuffer sql, boolean asc, Joins joins,
        boolean sel, Value selAs);

    /**
     * Add an ORDER BY clause.
     * Optionally selects ordering data if not already selected.
     */
    public boolean orderBy(String sql, boolean asc, boolean sel);

    /**
     * Add an ORDER BY clause.
     * Optionally selects ordering data if not already selected.
     */
    public boolean orderBy(String sql, boolean asc, Joins joins, boolean sel);

    /**
     * Add where conditions setting the mapping's primary key to the given
     * oid values. If the given mapping does not use oid values for its
     * primary key, we will recursively join to its superclass until we find
     * an ancestor that does.
     */
    public void wherePrimaryKey(Object oid, ClassMapping mapping,
        JDBCStore store);

    /**
     * Add where conditions setting the given foreign key to the given
     * oid values.
     *
     * @see #wherePrimaryKey
     */
    public void whereForeignKey(ForeignKey fk, Object oid,
        ClassMapping mapping, JDBCStore store);

    /**
     * Add the given where conditions.
     */
    public void where(Joins joins);

    /**
     * Add the given where conditions.
     */
    public void where(SQLBuffer sql);

    /**
     * Add the given where conditions.
     */
    public void where(SQLBuffer sql, Joins joins);

    /**
     * Add the given where conditions.
     */
    public void where(String sql);

    /**
     * Add the given where conditions.
     */
    public void where(String sql, Joins joins);

    /**
     * Add the given having conditions.
     */
    public void having(SQLBuffer sql);

    /**
     * Add the given having conditions.
     */
    public void having(SQLBuffer sql, Joins joins);

    /**
     * Add the given having conditions.
     */
    public void having(String sql);

    /**
     * Add the given having conditions.
     */
    public void having(String sql, Joins joins);

    /**
     * Group by the given column.
     */
    public void groupBy(Column col);

    /**
     * Group by the given column.
     */
    public void groupBy(Column col, Joins joins);

    /**
     * Group by the given columns.
     */
    public void groupBy(Column[] cols);

    /**
     * Group by the given columns.
     */
    public void groupBy(Column[] cols, Joins joins);

    /**
     * Add a GROUP BY clause.
     */
    public void groupBy(SQLBuffer sql);

    /**
     * Add a GROUP BY clause.
     */
    public void groupBy(SQLBuffer sql, Joins joins);

    /**
     * Add a GROUP BY clause.
     */
    public void groupBy(String sql);

    /**
     * Add a GROUP BY clause.
     */
    public void groupBy(String sql, Joins joins);

    /**
     * Group by the columns of the given mapping, possibly including subclasses.
     * Assumes EAGER_NONE.
     */
    public void groupBy(ClassMapping mapping, int subclasses, JDBCStore store, 
        JDBCFetchConfiguration fetch);

    /**
     * Group by the columns of the given mapping, possibly including subclasses.
     * Assumes EAGER_NONE.
     */
    public void groupBy(ClassMapping mapping, int subclasses, JDBCStore store, 
        JDBCFetchConfiguration fetch, Joins joins);

    /**
     * Return a SELECT with the same joins and where conditions as this one.
     *
     * @param sels number of selects to UNION together; ignored if &lt;= 1
     */
    public SelectExecutor whereClone(int sels);

    /**
     * Return a SELECT that is a complete clone of this one.
     *
     * @param sels number of selects to UNION together; ignored if &lt;= 1
     */
    public SelectExecutor fullClone(int sels);

    /**
     * Return a select that will be eagerly executed with this one, or null if
     * the	select cannot be created for the given key and join type.
     * If the join type is inner or outer, then this select instance will be
     * returned. Otherwise, the returned select will have a clone of this
     * select's where conditions and joins but will be independent.
     *
     * @param key the key for the eager select
     * @param eagerType one of the EAGER_* constants
     * @param toMany whether the eager join is to-many
     * @param sels number of selects to UNION together; ignored if &lt;= 1
     */
    public SelectExecutor eagerClone(FieldMapping key, int eagerType,
        boolean toMany, int sels);

    /**
     * Return the eager select for the given key.
     */
    public SelectExecutor getEager(FieldMapping key);

    /**
     * Return a new instance to use for joining.
     */
    public Joins newJoins();

    /**
     * Return a new instance to use for outer joining.
     */
    public Joins newOuterJoins();

    /**
     * Append the given joins to the given buffer.
     */
    public void append(SQLBuffer buf, Joins joins);

    /**
     * AND the given joins together. The given joins will be hollowed in the
     * process.
     */
    public Joins and(Joins joins1, Joins joins2);

    /**
     * OR the given joins together. The common joins will be removed in the
     * process.
     */
    public Joins or(Joins joins1, Joins joins2);

    /**
     * Return a join set making the given joins outer joins.
     */
    public Joins outer(Joins joins);

    /**
     * Implement toString to generate SQL string for profiling/debuggging.
     */
    public String toString();

    /**
     * Return the alias for the given column, without creating new table alias
     */
    public String getColumnAlias(Column col, Object path);

    /**
     * Set JPQL query context for this select
     * @param context
     */
    public void setContext(Context context);

    /**
     * Return the JPQL query context of this select
     */
    public Context ctx();

    /**
     * Record the initial schemaAlias of a join path
     * @param schemaAlias
     */
    public void setSchemaAlias(String schemaAlias);
    
    /**
     * Set the flag to indicate whether this Select has
     * internally generated subselect 
     */
    public void setHasSubselect(boolean hasSub);
    
    /**
     * Return the flag to indicate whether this Select has
     * internally generated subselect
     * @return
     */
    public boolean getHasSubselect();
   
    /**
     * Extended trace that logs eager relations
     */
    public void logEagerRelations();

    /**
     * Set table-per-class metadata for polymorphic queries
     */
    public void setTablePerClassMeta(ClassMapping meta);

    /**
     * get table-per-class metadata for polymorphic queries
     */
    public ClassMapping getTablePerClassMeta();

    /**
     * Set joined table metadatas for polymorphic queries
     */
    public void setJoinedTableClassMeta(List meta);

    /**
     * get joined table metadatas for polymorphic queries
     */
    public List getJoinedTableClassMeta();

    /**
     * Set joined table metadatas excluded for polymorphic queries
     */
    public void setExcludedJoinedTableClassMeta(List meta);

    /**
     * get joined table metadatas excluded for polymorphic queries
     */
    public List getExcludedJoinedTableClassMeta();
    
    public DBDictionary getDictionary() ; 
}
