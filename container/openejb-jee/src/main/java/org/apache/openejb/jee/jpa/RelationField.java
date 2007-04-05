/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee.jpa;

import java.util.List;

public interface RelationField {
    List<JoinColumn> getJoinColumn();

    JoinTable getJoinTable();

    void setJoinTable(JoinTable value);

    CascadeType getCascade();

    void setCascade(CascadeType value);

    FetchType getFetch();

    void setFetch(FetchType value);

    String getMappedBy();

    void setMappedBy(String value);

    String getName();

    void setName(String value);

    String getTargetEntity();

    void setTargetEntity(String value);

    /**
     * This is only used for xml converters and will normally return null.
     * Gets the field on the target entity for this relationship.
     * @return the field on the target entity for this relationship.
     */
    RelationField getRelatedField();

    /**
     * Gets the field on the target entity for this relationship.
     * @param value field on the target entity for this relationship.
     */
    void setRelatedField(RelationField value);

    /**
     * This is only used for xml converters and will normally return false.
     * A true value indicates that this field was generated for CMR back references.
     * @return true if this field was generated for CMR back references.
     */
    boolean isSyntheticField();

    /**
     * This is only used for xml converters and will normally return false.
     * A true value indicates that this field was generated for CMR back references.
     * @return true if this field was generated for CMR back references.
     */
    void setSyntheticField(boolean syntheticField);
}
