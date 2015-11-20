/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.dynamicdatasourcerouting;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * OpenJPA create the table at the first query.
 * To avoid to have to create the table manunally this singleton will do it for us.
 */
@Startup
@Singleton
public class BoostrapUtility {

    @PersistenceContext(unitName = "db1")
    private EntityManager em1;

    @PersistenceContext(unitName = "db2")
    private EntityManager em2;

    @PersistenceContext(unitName = "db3")
    private EntityManager em3;

    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void initDatabase() {
        em1.find(Person.class, 0);
        em2.find(Person.class, 0);
        em3.find(Person.class, 0);
    }
}
