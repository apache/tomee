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
package org.apache.openjpa.meta;

import java.io.Serializable;

/**
 * Holds metadata on a value; this could be a field value, key value, or
 * element value.
 *
 * @since 0.4.0
 * @author Abe White
 */
public interface ValueMetaData
    extends MetaDataContext, MetaDataModes, Serializable {

    /**
     * The operation is not cascaded to this field.
     */
    public int CASCADE_NONE = 0;

    /**
     * The operation is immediately cascaded to this field.
     */
    public int CASCADE_IMMEDIATE = 1;

    /**
     * Use automatic cascade behavior. Persistence-by-reachability,
     * delete-dependent, attach-if-detached, etc.
     */
    public int CASCADE_AUTO = 2;

    /**
     * Marker to set on {@link #setValueMappedBy} to denote that the map key
     * is mapped by the primary key field of the value.
     */
    public static final String MAPPED_BY_PK = "`pk`";

    /**
     * Return the owning field for this value.
     */
    public FieldMetaData getFieldMetaData();

    /**
     * The value class.
     */
    public Class getType();

    /**
     * The value class.
     */
    public void setType(Class type);

    /**
     * The type code of the value class.
     */
    public int getTypeCode();

    /**
     * The type code of the value class.
     */
    public void setTypeCode(int code);

    /**
     * Whether the type is a persistence capable instance.
     */
    public boolean isTypePC();

    /**
     * The metadata for the value class, if the type is persistent.
     */
    public ClassMetaData getTypeMetaData();

    /**
     * Return the declared class of the value. This can differ
     * from the return value of {@link #getType} if the user indicates
     * a different type or the value has an externalizer.
     */
    public Class getDeclaredType();

    /**
     * Set the declared class of the value.
     */
    public void setDeclaredType(Class type);

    /**
     * Return the declared type code of the value. This can differ
     * from the return value of {@link #getTypeCode} if the user indicates
     * a different type or the value has an externalizer.
     */
    public int getDeclaredTypeCode();

    /**
     * Set the type code for the value. The type code is usually
     * computed automatically, but it can be useful to set it explicitly
     * when creating metadatas from scratch.
     */
    public void setDeclaredTypeCode(int type);

    /**
     * Whether the type is a persistence capable instance.
     */
    public boolean isDeclaredTypePC();

    /**
     * Return metadata for the value's class, if the type is persistent.
     */
    public ClassMetaData getDeclaredTypeMetaData();

    /**
     * This attribute is a hint to the implementation to store this value
     * in the same structure as the class, rather than as a separate datastore
     * structure. Defaults to true if the field is not a collection or map
     * or persistence-capable object; defaults to false otherwise.
     * Implementations are permitted to ignore this attribute.
     */
    public boolean isEmbedded();

    /**
     * This attribute is a hint to the implementation to store this value
     * in the same structure as the class, rather than as a separate datastore
     * structure. Defaults to true if the field is not a collection or map
     * or persistence-capable objects; defaults to false otherwise.
     * Implementations are permitted to ignore this attribute.
     */
    public void setEmbedded(boolean embedded);

    /**
     * Whether this is an embedded persistence capable value.
     */
    public boolean isEmbeddedPC();

    /**
     * The embedded class metadata for the value.
     */
    public ClassMetaData getEmbeddedMetaData();

    /**
     * Add embedded metadata for this value.
     */
    public ClassMetaData addEmbeddedMetaData();

    /**
     * Add embedded metadata for this value with the given access type
     */
    public ClassMetaData addEmbeddedMetaData(int access);
    
    /**
     * Cascade behavior for delete operation. Only applies to
     * persistence-capable values. Options are:<br />
     * <ul>
     * <li><code>CASCADE_NONE</code>: No cascades.</li>
     * <li><code>CASCADE_IMMEDIATE</code>: Value is deleted immediately when
     * the owning object is deleted.</li>
     * <li><code>CASCADE_AUTO</code>: Value will be deleted on flush
     * if the owning object is deleted or if the value is removed from the
     * owning object, and if the value is not assigned to another relation in
     * the same transaction.</li>
     * </ul>
     */
    public int getCascadeDelete();

    /**
     * Cascade behavior for deletion.
     *
     * @see #getCascadeDelete
     */
    public void setCascadeDelete(int cascade);

    /**
     * Cascade behavior for persist operation. Only applies to
     * persistence-capable values. Options are:<br />
     * <ul>
     * <li><code>CASCADE_NONE</code>: No cascades. If a transient relation
     * is held at flush, an error is thrown.</li>
     * <li><code>CASCADE_IMMEDIATE</code>: Value is persisted immediately when
     * the owning object is persisted.</li>
     * <li><code>CASCADE_AUTO</code>: Value will be persisted on flush.</li>
     * </ul>
     */
    public int getCascadePersist();

    /**
     * Cascade behavior for persist operation.
     *
     * @see #getCascadePersist
     */
    public void setCascadePersist(int cascade);

    /**
     * Cascade behavior for attach operation. Only applies to
     * persistence-capable values. Options are:<br />
     * <ul>
     * <li><code>CASCADE_NONE</code>: No cascades of attach. Relation
     * remains detached.</li>
     * <li><code>CASCADE_IMMEDIATE</code>: Value is attached immediately.</li>
     * </ul>
     */
    public int getCascadeAttach();

    /**
     * Cascade behavior for attach operation.
     *
     * @see #getCascadeAttach
     */
    public void setCascadeAttach(int cascade);
    
    /**
     * Cascade behavior for detach operation. Only applies to
     * persistence-capable values. Options are:<br />
     * <ul>
     * <li><code>CASCADE_NONE</code>: No cascades of detach. Relation
     * remains attached.</li>
     * <li><code>CASCADE_IMMEDIATE</code>: Value is detached immediately.</li>
     * </ul>
     */
    public int getCascadeDetach();

    /**
     * Cascade behavior for detach operation.
     *
     * @see #getCascadeDetach
     */
    public void setCascadeDetach(int cascade);


    /**
     * Cascade behavior for refresh operation. Only applies to
     * persistence-capable values. Options are:<br />
     * <ul>
     * <li><code>CASCADE_NONE</code>: No cascades of refresh.</li>
     * <li><code>CASCADE_IMMEDIATE</code>: Persistent value object is also
     * refreshed.</li>
     * <li><code>CASCADE_AUTO</code>: Value will be refreshed if it is
     * in the current fetch groups.</li>
     * </ul>
     */
    public int getCascadeRefresh();

    /**
     * Cascade behavior for refresh operation.
     *
     * @see #getCascadeRefresh
     */
    public void setCascadeRefresh(int cascade);

    /**
     * Whether this value is serialized when stored.
     */
    public boolean isSerialized();

    /**
     * Whether this value is serialized when stored.
     */
    public void setSerialized(boolean serialized);

    /**
     * The field that this value shares a mapping with. Currently the only
     * supported use for a mapped-by value is when a map field key is
     * determined by a field of the persistence-capable map value.
     */
    public String getValueMappedBy();

    /**
     * The field that this value shares a mapping with. Currently the only
     * supported use for a mapped-by value is when a map field key is
     * determined by a field of the persistence-capable map value.
     */
    public void setValueMappedBy(String mapped);

    /**
     * The field that this value shares a mapping with. Currently the only
     * supported use for a mapped-by value is when a map field key is
     * determined by a field of the persistence-capable map value.
     */
    public FieldMetaData getValueMappedByMetaData();

    /**
     * User-supplied type overriding assumed type based on field.
     */
    public Class getTypeOverride();

    /**
     * User-supplied type overriding assumed type based on field.
     */
    public void setTypeOverride(Class type);

    /**
     * Resolve mode for metadata.
     */
    public int getResolve();

    /**
     * Resolve mode for metadata.
     */
    public void setResolve(int mode);

    /**
     * Resolve mode for metadata.
     */
    public void setResolve(int mode, boolean on);

    /**
     * Resolve and validate metadata. Return true if already resolved.
     */
    public boolean resolve(int mode);

    /**
     * Copy state from the given value to this one. Do not copy mapping
     * information.
     */
    public void copy(ValueMetaData vmd);
}
