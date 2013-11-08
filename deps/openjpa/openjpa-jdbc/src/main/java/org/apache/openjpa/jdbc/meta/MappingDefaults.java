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

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Unique;

/**
 * Generates default names for tables, columns, indexes, constraints, etc.
 *
 * @author Abe White
 */
public interface MappingDefaults {

    /**
     * Whether to fill in missing mapping information at runtime with the
     * default values supplied by this plugin. A value of false means that
     * all mapping information must be present at runtime.
     */
    public boolean defaultMissingInfo();

    /**
     * The default for whether relations use the related object's
     * expected class as part of the join criteria.
     */
    public boolean useClassCriteria();

    /**
     * Default mapping strategy when there is no explicit strategy
     * and no hierarchy strategy given.
     *
     * @param cls the class; will not be mapped, but superclass and raw
     * {@link MappingInfo} will be available
     * @param adapt whether we can adapt the mapping or schema
     * @return the strategy alias or a strategy instance, or null
     */
    public Object getStrategy(ClassMapping cls, boolean adapt);

    /**
     * Default version mapping strategy when there is no explicit strategy.
     *
     * @param vers the version; will not be mapped, but raw
     * {@link MappingInfo} will be available
     * @param adapt whether we can adapt the mapping or schema
     * @return the strategy alias or a strategy instance, or null
     */
    public Object getStrategy(Version vers, boolean adapt);

    /**
     * Default discriminator mapping strategy when there is no explicit
     * strategy.
     *
     * @param disc the discriminator; will not be mapped, but raw
     * {@link MappingInfo} will be available
     * @param adapt whether we can adapt the mapping or schema
     * @return the strategy alias or a strategy instance, or null
     */
    public Object getStrategy(Discriminator disc, boolean adapt);

    /**
     * Custom handler or strategy for the given field, or null if none
     * has been registered.
     *
     * @param vm the value mapping; will not be mapped, but raw
     * {@link MappingInfo} will be available
     * @param type the value type
     * @param adapt whether we can adapt the mapping or schema
     * @return the handler/strategy alias or instance, or null
     */
    public Object getStrategy(ValueMapping vm, Class<?> type, boolean adapt);

    /**
     * Return the default discriminator value for the given instance.
     */
    public Object getDiscriminatorValue(Discriminator disc, boolean adapt);

    /**
     * Return the default table name for the given class. This method is
     * only called for classes mapped to their own table.
     * @deprecated
     */
    public String getTableName(ClassMapping cls, Schema defaultSchema);

    /**
     * Return the default table name for the given class. This method is
     * only called for classes mapped to their own table.
     */
    public DBIdentifier getTableIdentifier(ClassMapping cls, Schema defaultSchema);

    /**
     * Return the default secondary table name for the given field. This
     * method is only called for fields whose strategy requires a secondary
     * table.
     * @deprecated
     */
    public String getTableName(FieldMapping fm, Schema defaultSchema);

    /**
     * Return the default secondary table name for the given field. This
     * method is only called for fields whose strategy requires a secondary
     * table.
     */
    public DBIdentifier getTableIdentifier(FieldMapping fm, Schema defaultSchema);

    /**
     * Fill in default information for the given datastore identity columns.
     * The columns' name and Java type will already be populated with generic
     * defaults that may be replaced.
     */
    public void populateDataStoreIdColumns(ClassMapping cls, Table table,
        Column[] cols);

    /**
     * Fill in default information for the given version columns.
     * The columns' name and Java type will already be populated with generic
     * defaults that may be replaced.
     */
    public void populateColumns(Version vers, Table table, Column[] cols);

    /**
     * Fill in default information for the given discriminator columns.
     * The columns' name and Java type will already be populated with generic
     * defaults that may be replaced.
     */
    public void populateColumns(Discriminator disc, Table table,
        Column[] cols);

    /**
     * Fill in default information for the given column used to join a class
     * to its superclass table. The column will be a clone of the target
     * column, or have its name and Java type set in the case of a constant
     * target.
     *
     * @param target the target of this column in the join; may be
     * another column or a constant value
     * @param pos the index of this column in the logical foreign key
     * @param cols the number of columns in the logical foreign key
     */
    public void populateJoinColumn(ClassMapping cm, Table local, Table foreign,
        Column col, Object target, int pos, int cols);

    /**
     * Fill in default information for the given column used to join a field
     * to its defining class' table. The column will be a clone of the target
     * column, or have its name and Java type set in the case of a constant
     * target.
     *
     * @param target the target of this column in the join; may be
     * another column or a constant value
     * @param pos the index of this column in the logical foreign key
     * @param cols the number of columns in the logical foreign key
     */
    public void populateJoinColumn(FieldMapping fm, Table local, Table foreign,
        Column col, Object target, int pos, int cols);

    /**
     * Fill in default information for the given column used to join a value
     * to its related type. The column will be a clone of the target
     * column, or have its name and Java type set in the case of a constant
     * target.
     *
     * @param name base name for value, as decided by mapping
     * @param target the target of this column in the join; may be
     * another column or a constant value
     * @param inverse whether this is an inverse foreign key
     * @param pos the index of this column in the logical foreign key
     * @param cols the number of columns in the logical foreign key
     * @deprecated
     */
    public void populateForeignKeyColumn(ValueMapping vm, String name,
        Table local, Table foreign, Column col, Object target, boolean inverse,
        int pos, int cols);

    /**
     * Fill in default information for the given column used to join a value
     * to its related type. The column will be a clone of the target
     * column, or have its name and Java type set in the case of a constant
     * target.
     *
     * @param name base name for value, as decided by mapping
     * @param target the target of this column in the join; may be
     * another column or a constant value
     * @param inverse whether this is an inverse foreign key
     * @param pos the index of this column in the logical foreign key
     * @param cols the number of columns in the logical foreign key
     */
    public void populateForeignKeyColumn(ValueMapping vm, DBIdentifier name,
        Table local, Table foreign, Column col, Object target, boolean inverse,
        int pos, int cols);

    /**
     * Fill in default information for the given value columns.
     * The columns' name and Java type will already be populated with generic
     * defaults that may be replaced.
     *
     * @param name base name for value, as decided by mapping
     * @deprecated
     */
    public void populateColumns(ValueMapping vm, String name, Table table,
        Column[] cols);

    /**
     * Fill in default information for the given value columns.
     * The columns' name and Java type will already be populated with generic
     * defaults that may be replaced.
     *
     * @param name base name for value, as decided by mapping
     */
    public void populateColumns(ValueMapping vm, DBIdentifier name, Table table,
        Column[] cols);

    /**
     * Fill in default information for the given order columns.
     * The columns' name and Java type will already be populated with generic
     * defaults that may be replaced.
     *
     * @return false if the given field should not have order columns
     * by default; fill in default information even when returning
     * false in case the user forces ordering
     */
    public boolean populateOrderColumns(FieldMapping fm, Table table,
        Column[] cols);

    /**
     * Fill in default information for the given null indicator columns.
     * The columns' name and Java type will already be populated with generic
     * defaults that may be replaced.
     *
     * @param name base name for value, as decided by mapping
     * @return false if the given value should not have null indicator
     * columns by default; fill in default information even
     * when returning false in case the user forces an indicator
     * @deprecated
     */
    public boolean populateNullIndicatorColumns(ValueMapping vm, String name,
        Table table, Column[] cols);

    /**
     * Fill in default information for the given null indicator columns.
     * The columns' name and Java type will already be populated with generic
     * defaults that may be replaced.
     *
     * @param name base name for value, as decided by mapping
     * @return false if the given value should not have null indicator
     * columns by default; fill in default information even
     * when returning false in case the user forces an indicator
     */
    public boolean populateNullIndicatorColumns(ValueMapping vm, DBIdentifier name,
        Table table, Column[] cols);

    /**
     * Return a default foreign key for the join from this class' table to its
     * superclass' table, or null for a logical foreign key only. Do not
     * add columns to the key or add the key to the table; only fill in
     * its information such as name, delete action, etc.
     */
    public ForeignKey getJoinForeignKey(ClassMapping cls, Table local,
        Table foreign);

    /**
     * Return a default foreign key for the join from this field's table to its
     * defining class' table, or null for a logical foreign key only. Do not
     * add columns to the key or add the key to the table; only fill in
     * its information such as name, delete action, etc.
     */
    public ForeignKey getJoinForeignKey(FieldMapping fm, Table local,
        Table foreign);

    /**
     * Return a default foreign key for the join from this value to its
     * related type, or null for a logical foreign key only. Do not
     * add columns to the key or add the key to the table; only fill in
     * its information such as name, delete action, etc.
     *
     * @param name base name for value, as decided by mapping
     * @param inverse whether this is an inverse key
     * @deprecated
     */
    public ForeignKey getForeignKey(ValueMapping vm, String name, Table local,
        Table foreign, boolean inverse);

    /**
     * Return a default foreign key for the join from this value to its
     * related type, or null for a logical foreign key only. Do not
     * add columns to the key or add the key to the table; only fill in
     * its information such as name, delete action, etc.
     *
     * @param name base name for value, as decided by mapping
     * @param inverse whether this is an inverse key
     */
    public ForeignKey getForeignKey(ValueMapping vm, DBIdentifier name, Table local,
        Table foreign, boolean inverse);

    /**
     * Return a default index for the join, or null if the
     * join columns should not be indexed by default. Do not
     * add columns to the index or add the index to the table; only fill in
     * its information such as name, uniqueness, etc.
     */
    public Index getJoinIndex(FieldMapping fm, Table table, Column[] cols);

    /**
     * Return a default index for the value, or null if the value columns
     * should not be indexed by default. Do not add columns to the index or
     * add the index to the table; only fill in its information such as name,
     * uniqueness, etc.
     *
     * @param name base name for value, as decided by mapping
     * @deprecated
     */
    public Index getIndex(ValueMapping vm, String name, Table table,
        Column[] cols);

    /**
     * Return a default index for the value, or null if the value columns
     * should not be indexed by default. Do not add columns to the index or
     * add the index to the table; only fill in its information such as name,
     * uniqueness, etc.
     *
     * @param name base name for value, as decided by mapping
     */
    public Index getIndex(ValueMapping vm, DBIdentifier name, Table table,
        Column[] cols);

    /**
     * Return a default index for the version, or null if the
     * version columns should not be indexed by default. Do not
     * add columns to the index or add the index to the table; only fill in
     * its information such as name, uniqueness, etc.
     */
    public Index getIndex(Version vers, Table table, Column[] cols);

    /**
     * Return a default index for the discriminator, or null if the
     * discriminator columns should not be indexed by default. Do not
     * add columns to the index or add the index to the table; only fill in
     * its information such as name, uniqueness, etc.
     */
    public Index getIndex(Discriminator disc, Table table, Column[] cols);

    /**
     * Return a default constraint for the join, or null if the join columns
     * should not be constrained by default. Do not add columns to the
     * constraint or add the constraint to the table; only fill in its
     * information such as name, deferrability, etc.
     */
    public Unique getJoinUnique(FieldMapping fm, Table table, Column[] cols);

    /**
     * Return a default constraint for the value, or null if the value columns
     * should not be constrained by default. Do not add columns to the
     * constraint or add the constraint to the table; only fill in its
     * information such as name, deferrability, etc.
     *
     * @param name base name for value, as decided by mapping
     * @deprecated
     */
    public Unique getUnique(ValueMapping vm, String name, Table table,
        Column[] cols);

    /**
     * Return a default constraint for the value, or null if the value columns
     * should not be constrained by default. Do not add columns to the
     * constraint or add the constraint to the table; only fill in its
     * information such as name, deferrability, etc.
     *
     * @param name base name for value, as decided by mapping
     */
    public Unique getUnique(ValueMapping vm, DBIdentifier name, Table table,
        Column[] cols);

    /**
     * Return the name of the primary key for the table of the given class,
     * or null for database default.
     * @deprecated
     */
    public String getPrimaryKeyName(ClassMapping cm, Table table);

    /**
     * Return the name of the primary key for the table of the given class,
     * or null for database default.
     */
    public DBIdentifier getPrimaryKeyIdentifier(ClassMapping cm, Table table);

    /**
     * If desired, install a primary key on the given secondary table.
     */
    public void installPrimaryKey(FieldMapping fm, Table table);
}
