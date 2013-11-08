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
package org.apache.openjpa.persistence.models.company;

import java.util.*;

public interface IEmployee extends IPerson {

    public void setManager(IFullTimeEmployee manager);
    public IFullTimeEmployee getManager();

    public void setCompany(ICompany company);
    public ICompany getCompany();

    public void setTitle(String title);
    public String getTitle();

    public void setHireDate(Date hireDate);
    public Date getHireDate();
}
