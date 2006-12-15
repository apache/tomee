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
package org.apache.openejb.test.entity;

import javax.ejb.EntityBean;
import javax.ejb.EJBLocalObject;
import java.util.concurrent.Callable;

public interface CmrFactory {
    public static final CmrFactory cmrFactory = new Callable() {
        public CmrFactory call() {
            try {
                Class<?> clazz = CmrFactory.class.getClassLoader().loadClass("org.apache.openejb.core.cmp.jpa.CmrFactoryImpl");
                CmrFactory cmrFactory = (CmrFactory) clazz.newInstance();
                return cmrFactory;
            } catch (Exception e) {
                return new CmrFactory() {
                    public <Bean extends EntityBean, Proxy extends EJBLocalObject> SingleValuedCmr<Bean, Proxy> createSingleValuedCmr(EntityBean source, String sourceProperty, Class<Bean> relatedType, String property) {
                        return null;
                    }

                    public <Bean extends EntityBean, Proxy extends EJBLocalObject> MultiValuedCmr<Bean, Proxy> createMultiValuedCmr(EntityBean source, String sourceProperty, Class<Bean> relatedType, String property) {
                        return null;
                    }
                };
            }
        }
    }.call();

    <Bean extends EntityBean, Proxy extends EJBLocalObject> SingleValuedCmr<Bean, Proxy> createSingleValuedCmr(EntityBean source, String sourceProperty, Class<Bean> relatedType, String property);

    <Bean extends EntityBean, Proxy extends EJBLocalObject> MultiValuedCmr<Bean, Proxy> createMultiValuedCmr(EntityBean source, String sourceProperty, Class<Bean> relatedType, String property);
}
