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
package org.apache.openejb.core.ivm.naming;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.naming.*;

import org.apache.openejb.persistence.JtaEntityManager;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceContextReference extends Reference{

    private EntityManager em;

    public PersistenceContextReference(EntityManager em) {
        this.em = em;
    }

    public Object getObject() throws javax.naming.NamingException {
        return em;
    }

}
