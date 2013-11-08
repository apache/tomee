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
package org.apache.openjpa.persistence.dynamicschema;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Entity with very long table and column names 
 * 
 * @author Tim McConnell
 * @since 2.0.0
 */
@Entity
@Table(name="Very______________________________________________" +
            "Long______________________________________________" +
            "Table_____________________________________________" +
            "Name______________________________________________" )
public class EntityVeryLongNames implements Serializable {

    @Id
    @Column(name="ID________________________________________________" +
                 "Very______________________________________________" +
                 "Long______________________________________________" +
                 "Column____________________________________________" +
                 "Name______________________________________________" )
    private int id;

    @Column(name="FirstName_________________________________________" +
                 "Very______________________________________________" +
                 "Long______________________________________________" +
                 "Column____________________________________________" +
                 "Name______________________________________________" )
    private String firstName;

    @Column(name="LastName__________________________________________" +
                 "Very______________________________________________" +
                 "Long______________________________________________" +
                 "Column____________________________________________" +
                 "Name______________________________________________" )
    private String lastName;

    public EntityVeryLongNames() {
    }

    public EntityVeryLongNames(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

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


    @Override
    public String toString() {
        return "EntityVeryLongNames: id: " + getId() + 
               " firstName: " + getFirstName() +
               " lastName: " + getLastName();
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityVeryLongNames other = (EntityVeryLongNames) obj;
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
        return true;
    } 
}
