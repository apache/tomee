/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.telephone.ejbs;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.Dependent;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.webbeans.ejb.common.interceptor.OpenWebBeansEjbInterceptor;
import org.apache.webbeans.telephone.entity.Record;

@Stateless
@Dependent
@Interceptors(value=OpenWebBeansEjbInterceptor.class)
public class TelephoneBean implements Telephone
{
    private @PersistenceContext(name="pu") EntityManager entityManager; 

    @Override
    public Record addRecord(String name, String surname, String telephone, boolean business)
    {
        Record record = new Record();
        record.setName(name);
        record.setSurname(surname);
        record.setNumber(telephone);
        record.setBusiness(business);
        
        entityManager.persist(record);
        
        return record;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @SuppressWarnings("unchecked")
    public List<Record> getRecords()
    {
        Query query = this.entityManager.createQuery("select c from Record c");
        return  (List<Record>)query.getResultList();
        
    }
    
}
