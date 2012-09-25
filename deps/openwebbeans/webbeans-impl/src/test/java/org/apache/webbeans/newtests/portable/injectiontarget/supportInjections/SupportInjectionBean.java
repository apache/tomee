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
package org.apache.webbeans.newtests.portable.injectiontarget.supportInjections;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.New;
import javax.inject.Inject;

import org.apache.webbeans.newtests.promethods.common.Person;
import org.apache.webbeans.test.annotation.binding.Binding1;
import org.apache.webbeans.test.annotation.binding.Binding2;

public class SupportInjectionBean
{
    private @Inject @Binding1 Chair chair;
    
    private @Inject @Binding2 Table table;
    
    public static boolean POST_COSTRUCT = false;
    
    public static boolean PRE_DESTROY = false;
    
    private Person person;
    
    @Inject
    public SupportInjectionBean(@New Person person)
    {
        this.person = person;
    }
    
    public Person getPerson()
    {
        return this.person;
    }
    
    
    /**
     * @return the chair
     */
    public Chair getChair()
    {
        return chair;
    }



    /**
     * @param chair the chair to set
     */
    public void setChair(Chair chair)
    {
        this.chair = chair;
    }



    /**
     * @return the table
     */
    public Table getTable()
    {
        return table;
    }



    /**
     * @param table the table to set
     */
    public void setTable(Table table)
    {
        this.table = table;
    }



    @PostConstruct
    public void postConstruct()
    {
        POST_COSTRUCT = true;
    }
    
    @PreDestroy
    public void preDestroy()
    {
        PRE_DESTROY = true;
    }
        
}
