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
package org.apache.openjpa.persistence.proxy.delayed;

import java.util.Collection;

public interface IDepartment { 

    public void setEmployees(Collection<IEmployee> employees);

    public Collection<IEmployee> getEmployees();
    
    public void setId(int id);

    public int getId();

    public void setLocations(Collection<Location> locations);

    public Collection<Location> getLocations();

    public void setProducts(Collection<Product> products);

    public Collection<Product> getProducts();

    public void setCertifications(Collection<Certification> certifications);

    public Collection<Certification> getCertifications();

    public void setAwards(Collection<Award> awards);

    public Collection<Award> getAwards();
}
