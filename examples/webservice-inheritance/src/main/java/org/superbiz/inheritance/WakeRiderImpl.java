/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.inheritance;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.Query;
import java.util.List;

/**
 * This is an EJB 3 style pojo stateless session bean Every stateless session
 * bean implementation must be annotated using the annotation @Stateless This
 * EJB has a single interface: {@link WakeRiderWs} a webservice interface.
 */
@Stateless
@WebService(
        portName = "InheritancePort",
        serviceName = "InheritanceWsService",
        targetNamespace = "http://superbiz.org/wsdl",
        endpointInterface = "org.superbiz.inheritance.WakeRiderWs")
public class WakeRiderImpl implements WakeRiderWs {

    @PersistenceContext(unitName = "wakeboard-unit", type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;

    public void addItem(Item item) throws Exception {
        entityManager.persist(item);
    }

    public void deleteMovie(Item item) throws Exception {
        entityManager.remove(item);
    }

    public List<Item> getItems() throws Exception {
        Query query = entityManager.createQuery("SELECT i FROM Item i");
        List<Item> items = query.getResultList();
        return items;
    }
}
