/**
 * 
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.openejb.persistence;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

public class FakeEntityManagerFactory implements EntityManagerFactory {

    private EntityManager em = null;
    private PersistenceUnitInfo pu = null;

    public FakeEntityManagerFactory(PersistenceUnitInfo pu){
       this.pu = pu;
    }

    public PersistenceUnitInfo getPersistenceUnitInfo(){
       return pu;
    }

    public EntityManager createEntityManager() {

        if (em == null)
            em = new FakeEntityManager(pu);

        return em;
    }

    public EntityManager createEntityManager(Map context) {
        return createEntityManager();
    }

    public EntityManager getEntityManager() {
        if (em != null)
            return em;

        return createEntityManager();
    }

    public void close() {
    }

    public boolean isOpen() {
        return false;
    }

}
