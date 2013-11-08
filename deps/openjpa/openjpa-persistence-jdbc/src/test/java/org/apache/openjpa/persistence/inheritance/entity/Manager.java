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
package org.apache.openjpa.persistence.inheritance.entity;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * Test case and domain classes were originally part of the reported issue
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-1536">OPENJPA-1536</A>
 *  
 * @author Jody Grassel
 * @author Fay Wang
 *
 */
@Entity
@DiscriminatorValue("3")
public class Manager extends FTEmployee {
    @OneToMany(mappedBy="manager")
    private List<Employee> managesList;

    public List<Employee> getManagesList() {
        return managesList;
    }
    
    public String toString() {
        String str = super.toString();
        return "Manager " + str;
    }

}
