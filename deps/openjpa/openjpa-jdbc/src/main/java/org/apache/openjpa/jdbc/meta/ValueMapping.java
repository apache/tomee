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

import java.io.Serializable;
import java.sql.SQLException;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.MetaDataContext;
import org.apache.openjpa.meta.ValueMetaData;

/**
 * Specialization of value metadata for relational databases.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface ValueMapping
    extends ValueMetaData, MetaDataContext, Serializable {

    /**
     * Standard forward join.
     */
    public static final int JOIN_FORWARD = 0;

    /**
     * Inverse join.
     */
    public static final int JOIN_INVERSE = 1;

    /**
     * Inverse join that is marked up as a forward join because the
     * backing mapping expects an inverse direction.
     */
    public static final int JOIN_EXPECTED_INVERSE = 2;

    /**
     * A fully polymorphic relation (the default).
     */
    public static final int POLY_TRUE = 0;

    /**
     * A non-polymorphic relation.
     */
    public static final int POLY_FALSE = 1;

    /**
     * A relation that can hold any joinable subclass type.
     */
    public static final int POLY_JOINABLE = 2;

    /**
     * Raw mapping data.
     */
    public ValueMappingInfo getValueInfo();

    /**
     * The handler used for this value, or null if none.
     */
    public ValueHandler getHandler();

    /**
     * The handler used for this value, or null if none.
     */
    public void setHandler(ValueHandler handler);

    /**
     * Convenience method to perform cast from
     * {@link ValueMetaData#getRepository}.
     */
    public MappingRepository getMappingRepository();

    /**
     * Convenience method to perform cast from
     * {@link ValueMetaData#getFieldMetaData}.
     */
    public FieldMapping getFieldMapping();

    /**
     * Convenience method to perform cast from
     * {@link ValueMetaData#getTypeMetaData}.
     */
    public ClassMapping getTypeMapping();

    /**
     * Convenience method to perform cast from
     * {@link ValueMetaData#getDeclaredTypeMetaData}.
     */
    public ClassMapping getDeclaredTypeMapping();

    /**
     * Convenience method to perform cast from
     * {@link ValueMetaData#getEmbeddedMetaData}.
     */
    public ClassMapping getEmbeddedMapping();

    /**
     * Convenience method to perform cast from
     * {@link ValueMetaData#getValueMappedByMetaData}.
     */
    public FieldMapping getValueMappedByMapping();

    /**
     * The columns that hold the data for this value.
     */
    public Column[] getColumns();

    /**
     * The columns that hold the data for this value.
     */
    public void setColumns(Column[] cols);

    /**
     * I/O information on the foreign key, or columns if this value doesn't
     * have a key.
     */
    public ColumnIO getColumnIO();

    /**
     * I/O information on the foreign key, or columns if this value doesn't
     * have a key.
     */
    public void setColumnIO(ColumnIO io);

    /**
     * If this value joins to another record, the foreign key.
     */
    public ForeignKey getForeignKey();

    /**
     * Return an equivalent of this value's foreign key, but joining to the
     * given target, which may be an unjoined subclass of this value's
     * related type.
     */
    public ForeignKey getForeignKey(ClassMapping target);

    /**
     * If this value joins to another record, the foreign key.
     */
    public void setForeignKey(ForeignKey fk);

    /**
     * The join direction.
     */
    public int getJoinDirection();

    /**
     * The join direction.
     */
    public void setJoinDirection(int direction);

    /**
     * Sets this value's foreign key to the given related object. The object
     * may be null.
     */
    public void setForeignKey(Row row, OpenJPAStateManager rel)
        throws SQLException;
    
    /**
     * Sets this value's foreign key to the given related object. The object
     * may be null. If the object is one of2or more foreign keys with the
     * same target, the targetNumber specifies the one to set.
     */
    public void setForeignKey(Row row, OpenJPAStateManager rel, int targetNumber)
        throws SQLException;

    /**
     * Sets this value's foreign key to the given related object. The object
     * may be null.
     */
    public void whereForeignKey(Row row, OpenJPAStateManager rel)
        throws SQLException;

    /**
     * Return all independently-mapped joinable types for this value, depending
     * on whether this value is polymorphic and how the related type is mapped.
     * Return an empty array if value type is not PC.
     */
    public ClassMapping[] getIndependentTypeMappings();

    /**
     * Return the {@link org.apache.openjpa.sql.Select} subclasses constant 
     * for loading this relation, based on how the related type is mapped, 
     * whether this relation is polymorphic, and whether it is configured to 
     * use class criteria.
     */
    public int getSelectSubclasses();

    /**
     * Unique constraint on this value's columns, or null if none.
     */
    public Unique getValueUnique();

    /**
     * Unique constraint on this value's columns, or null if none.
     */
    public void setValueUnique(Unique unq);

    /**
     * Index on this value's columns, or null if none.
     */
    public Index getValueIndex();

    /**
     * Index on this value's columns, or null if none.
     */
    public void setValueIndex(Index idx);

    /**
     * Whether to use class criteria when joining to related type.
     */
    public boolean getUseClassCriteria();

    /**
     * Whether to use class criteria when joining to related type.
     */
    public void setUseClassCriteria(boolean criteria);

    /**
     * The degree to which this relation is polymorphic.
     */
    public int getPolymorphic();

    /**
     * The degree to which this relation is polymorphic.
     */
    public void setPolymorphic(int polymorphic);

    /**
     * Increase the reference count on used schema components.
     */
    public void refSchemaComponents();

    /**
     * Map indexes and constraints for this value, using the current
     * {@link ValueMappingInfo}. The foreign key or columns of this value
     * must be set before calling this method.
     * @deprecated
     */
    public void mapConstraints(String name, boolean adapt);

    /**
     * Map indexes and constraints for this value, using the current
     * {@link ValueMappingInfo}. The foreign key or columns of this value
     * must be set before calling this method.
     */
    public void mapConstraints(DBIdentifier name, boolean adapt);

    /**
     * Clear mapping information, including strategy.
     */
    public void clearMapping();

    /**
     * Update {@link MappingInfo} with our current mapping information.
     */
    public void syncMappingInfo();

    /**
     * Copy mapping info from the given instance to this one.
     */
    public void copyMappingInfo(ValueMapping vm);
}
