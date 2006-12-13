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

import org.apache.openejb.test.entity.CmrFactory;
import org.apache.openejb.test.entity.MultiValuedCmr;
import org.apache.openejb.test.entity.SingleValuedCmr;

import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;

public class CmrFactoryImpl implements CmrFactory {
    public <Bean extends EntityBean, Proxy extends EJBLocalObject> SingleValuedCmr<Bean, Proxy> createSingleValuedCmr(EntityBean source, String sourceProperty, Class<Bean> relatedType, String relatedProperty) {
        return new SingleValuedCmrImpl<Bean, Proxy>(source, sourceProperty, relatedType, relatedProperty);
    }

    public <Bean extends EntityBean, Proxy extends EJBLocalObject, PK> MultiValuedCmr<Bean, Proxy, PK> createMultiValuedCmr(EntityBean source, String sourceProperty, Class<Bean> relatedType, String property) {
        return new MultiValuedCmrImpl<Bean, Proxy, PK>(source, sourceProperty, relatedType, property);
    }
}
