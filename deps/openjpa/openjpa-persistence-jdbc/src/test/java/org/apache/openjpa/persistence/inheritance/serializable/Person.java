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
package org.apache.openjpa.persistence.inheritance.serializable;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="PERSON_SERIAL")
public class Person implements Serializable {

    private static final long serialVersionUID = -862917178229746730L;

    private int id; 
    
    private String firstName;
    
    private String lastName;

    private int version;
    
    @Id
    @GeneratedValue
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
    
    @Version
    public int getVersion(){
        return version;
    }
    public void setVersion(int v){
        version = v;
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
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Person other = (Person) obj;
        if (getFirstName() == null) {
            if (other.getFirstName() != null)
                return false;
        } else if (!getFirstName().equals(other.getFirstName()))
            return false;
        if (getId() != other.getId())
            return false;
        if (getLastName() == null) {
            if (other.getLastName() != null)
                return false;
        } else if (!getLastName().equals(other.getLastName()))
            return false;
        return true;
    } 
}
