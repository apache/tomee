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
package org.apache.openjpa.persistence.inheritance.jointable;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.Index;

@Inheritance(strategy=InheritanceType.JOINED)
@Entity
@Table(name="WParttimeEmployee")
public class ParttimeEmployee extends Employee {
    @Column(name="PTEmpProp1",length=10)
    @Basic
    private String ptEmpProp1;


    @ManyToOne(optional=true,cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH},fetch=FetchType.LAZY)
    @JoinColumn(name="Dept_No",referencedColumnName="OID")
    @Index
    private Department dept;

    public ParttimeEmployee() {
    }

    public ParttimeEmployee(String desc) {
        setDescription(desc);
    }

    public String getPTEmpProp1() {
        return ptEmpProp1;
    }

    public void setPTEmpProp1(String ptEmpProp1) {
        this.ptEmpProp1 = ptEmpProp1;
    }

    public Department getDept() {
        return dept;
    }

    public void setDept(Department dept) {
        this.dept = dept;
    }

    public boolean equals(Object other) {
        if (other instanceof ParttimeEmployee) {
            ParttimeEmployee c = (ParttimeEmployee) other;
            if (c.getOID() == this.getOID() &&
                c.getDept() == this.getDept())
                return true;
        }
        return false;
    }
}

