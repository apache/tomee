/**
 *
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
package org.apache.openejb.core.cmp.jpa;

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.test.entity.MultiValuedCmr;

import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class MultiValuedCmrImpl<Bean extends EntityBean, Proxy extends EJBLocalObject, PK> implements MultiValuedCmr<Bean, Proxy, PK> {
    private final EntityBean source;
//    private final CoreDeploymentInfo sourceInfo;
    private final Field relatedField;
    private final CoreDeploymentInfo relatedInfo;

    public MultiValuedCmrImpl(EntityBean source, Class<Bean> relatedType, String property) {
        if (source == null) throw new NullPointerException("source is null");
        if (relatedType == null) throw new NullPointerException("relatedType is null");
        if (property == null) throw new NullPointerException("property is null");

        this.source = source;
        try {
            relatedField = relatedType.getField(property);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Related type " + relatedType.getName() + " does not contain a property " + property);
        }
        if (!relatedField.getType().isAssignableFrom(source.getClass())) {
            throw new IllegalArgumentException("Related type is " + relatedType.getName() + " but field " +
                    property + " type is " + relatedField.getType().getName());
        }

        try {
            Field deploymentInfoField = relatedType.getField("deploymentInfo");
            relatedInfo = (CoreDeploymentInfo) deploymentInfoField.get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("EntityBean class " + relatedType.getName() +
                    " does not contain a deploymentInfo field.  Is this a generated CMP 2 entity implementation?");
        }
//
//        try {
//            Field deploymentInfoField = source.getClass().getField("deploymentInfo");
//            sourceInfo = (CoreDeploymentInfo) deploymentInfoField.get(null);
//        } catch (Exception e) {
//            throw new IllegalArgumentException("EntityBean class " + source.getClass().getName() +
//                    " does not contain a deploymentInfo field.  Is this a generated CMP 2 entity implementation?");
//        }
    }

    public Set<Proxy> get(Map<PK, Bean> others) {
        if (others == null) throw new NullPointerException("others is null");
        Set<Proxy> cmrSet = new CmrSet<Bean, Proxy, PK>(source, relatedField, relatedInfo, others);
        return cmrSet;
    }

    public Map<PK, Bean> set(Set<Proxy> others) {
        return null;
    }
}
