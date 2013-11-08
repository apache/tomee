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
package org.apache.openjpa.persistence.sequence;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author Tim McConnell
 * @since 2.0.0
 */
@Entity
@Table(name="ENTEMPLOYEE")
public class EntityEmployee implements Serializable {

    private static final long serialVersionUID = 2961572787273807912L;
    
    @Id
    @SequenceGenerator(name="SeqEmployee", sequenceName="test_native_sequence")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SeqEmployee")
    private int id; 
    private String firstName;
    private String lastName;
    private float salary;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "EntityEmployee: Employee id: " + getId() + 
               " firstName: " + getFirstName() +
               " lastName: " + getLastName() +
               " salary: " + getSalary();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((getFirstName() == null) ? 0 : getFirstName().hashCode());
        result = prime * result + getId();
        result = prime * result
            + ((getLastName() == null) ? 0 : getLastName().hashCode());
        result = prime * result + Float.floatToIntBits(getSalary());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityEmployee other = (EntityEmployee) obj;
        if (getId() != other.getId()) {
            return false;
        }
        if (getFirstName() == null) {
            if (other.getFirstName() != null) {
                return false;
            }
        }
        else if (!getFirstName().equals(other.getFirstName())) {
            return false;
        }
        if (getLastName() == null) {
            if (other.getLastName() != null) {
                return false;
            }
        }
        else if (!getLastName().equals(other.getLastName())) {
            return false;
        }
        if (Float.floatToIntBits(getSalary()) != Float.floatToIntBits(other
            .getSalary())) {
            return false;
        }
        return true;
    }
}
