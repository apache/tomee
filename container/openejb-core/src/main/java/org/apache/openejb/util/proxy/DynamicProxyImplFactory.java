/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.util.proxy;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author rmannibucau
 */
public class DynamicProxyImplFactory {
    public static Object newProxy(BeanContext context) {
        List<Injection> injection = context.getInjections(); // the entity manager
        if (injection.size() < 1) {
            throw new RuntimeException("a dynamic bean should have at least one PersistenceContext annotation");
        }

        String emLookupName = injection.get(injection.size() - 1).getJndiName();
        EntityManager em;
        try {
            em = (EntityManager) context.getJndiEnc().lookup(emLookupName);
        } catch (NamingException e) {
            throw new RuntimeException("a dynamic bean should reference at least one correct PersistenceContext", e);
        }

        try {
            return ProxyManager.newProxyInstance(context.getLocalInterface(), new QueryProxy(em));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("illegal access", e);
        }
    }
}
