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
package org.apache.webbeans.telephone;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.telephone.ejbs.Telephone;
import org.apache.webbeans.telephone.entity.Record;
import org.apache.webbeans.telephone.util.FacesMessageUtil;

@Named("operation")
@RequestScoped
public class OperationBean
{
    private @Inject Telephone operation;
    
    private @Inject FacesMessageUtil messageUtil;
    
    private String name;
    
    private String surname;
    
    private String telephone;
    
    private boolean business;
    
    private List<Record> records = new ArrayList<Record>();
    
    public OperationBean()
    {
        
    }
    
    @PostConstruct
    public void afterConstruct()
    {
        System.out.println("After instance creation!");
    }
    
    @PreDestroy
    public void beforeDestroy()
    {
        System.out.println("Before instance destroy!");
    }
    
    public String addRecord()
    {
        this.operation.addRecord(name, surname, telephone, business);
        
        this.messageUtil.addMessage(FacesMessage.SEVERITY_INFO, "Record added", "Record added");
        
        return null;
    }
    
    public String showList()
    {
        this.records = this.operation.getRecords();
        
        return null;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the surname
     */
    public String getSurname()
    {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname)
    {
        this.surname = surname;
    }

    /**
     * @return the telephone
     */
    public String getTelephone()
    {
        return telephone;
    }

    /**
     * @param telephone the telephone to set
     */
    public void setTelephone(String telephone)
    {
        this.telephone = telephone;
    }

    /**
     * @return the business
     */
    public boolean isBusiness()
    {
        return business;
    }

    /**
     * @param business the business to set
     */
    public void setBusiness(boolean business)
    {
        this.business = business;
    }

    /**
     * @return the records
     */
    public List<Record> getRecords()
    {
        return records;
    }

    /**
     * @param records the records to set
     */
    public void setRecords(List<Record> records)
    {
        this.records = records;
    }
    
    
}
