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
package org.apache.openjpa.persistence.kernel.common.apps;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;

/**
 * @author <A HREF="mailto:pinaki.poddar@gmail.com>Pinaki Poddar</A>
 */
@Entity
@FetchGroup(name = "employee.department",
    attributes = @FetchAttribute(name = "department"))
public class PCEmployee extends PCPerson {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PCDepartment department;

    protected PCEmployee() {
        super();
    }

    public PCEmployee(String name) {
        super(name);
    }

    public PCDepartment getDepartment() {
        return department;
    }

    public void setDepartment(PCDepartment department) {
        this.department = department;
    }

    public static Object reflect(PCEmployee instance, String name) {
        if (instance == null)
            return null;
        try {
            return PCEmployee.class.getDeclaredField(name).get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
