/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.jdbc.meta;

import javax.persistence.*;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.jdbc.schema.Column;

public class TestBooleanValue extends SingleEMFTestCase {

    public void setUp() throws Exception {
        super.setUp(EntityBool.class);
    }

    public void testBooleanValue() {
        EntityManager em = emf.createEntityManager();
        EntityBool t0 = new EntityBool();
        t0.setDummy(false);
        em.getTransaction().begin();
        em.persist(t0);
        em.getTransaction().commit();
        Column boolCol = getMapping(EntityBool.class).getTable().getColumn("dummy");
        DBIdentifier boolColId = boolCol.getIdentifier();
        Query q =
            em.createNativeQuery("Select "
                    + ((JDBCConfiguration) emf.getConfiguration())
                        .getDBDictionaryInstance().getPlaceholderValueString(
                            boolCol)
                    + " FROM EntityBool a UNION ALL Select a." + boolColId.getName()  + " " +
                    		"FROM EntityBool a");
        q.getResultList();
        em.close();
    }
}
