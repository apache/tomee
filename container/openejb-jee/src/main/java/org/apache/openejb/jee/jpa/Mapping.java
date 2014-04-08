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

public interface Mapping {
    String getDescription();

    void setDescription(String value);

    IdClass getIdClass();

    void setIdClass(IdClass value);

    boolean isExcludeDefaultListeners();

    void setExcludeDefaultListeners(boolean value);

    boolean isExcludeSuperclassListeners();

    void setExcludeSuperclassListeners(boolean value);

    EntityListeners getEntityListeners();

    void setEntityListeners(EntityListeners value);

    PrePersist getPrePersist();

    void setPrePersist(PrePersist value);

    PostPersist getPostPersist();

    void setPostPersist(PostPersist value);

    PreRemove getPreRemove();

    void setPreRemove(PreRemove value);

    PostRemove getPostRemove();

    void setPostRemove(PostRemove value);

    PreUpdate getPreUpdate();

    void setPreUpdate(PreUpdate value);

    PostUpdate getPostUpdate();

    void setPostUpdate(PostUpdate value);

    PostLoad getPostLoad();

    void setPostLoad(PostLoad value);

    Attributes getAttributes();

    void setAttributes(Attributes value);

    AccessType getAccess();

    void setAccess(AccessType value);

    String getClazz();

    void setClazz(String value);

    Boolean isMetadataComplete();

    void setMetadataComplete(Boolean value);

    void addField(Field field);
}
