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
package org.apache.openjpa.persistence.lockmgr;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;


@Entity
@Table(name="LOCK_PSN")
public class Person implements Externalizable {

    private int id;

    private String firstName;
    private String lastName;
    private List<PhoneNumber> phoneNumbers;
    
    @Id
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
    
    @ManyToMany(mappedBy = "owners")
    public List<PhoneNumber> getPhoneNumbers(){
        return phoneNumbers;
    }
    
    public void setPhoneNumbers(List<PhoneNumber> numbers){
        phoneNumbers = numbers;
    }

    public String toString() {
        return this.getClass().getName() + '@'
            + Integer.toHexString(System.identityHashCode(this)) + "[id="
            + getId() + "] first=" + getFirstName()
            + ", last=" + getLastName() + " phone numbers="+phoneNumbers.toString();
    }

    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
        id = in.readInt();
        firstName = (String) in.readObject();
        lastName = (String) in.readObject();
        phoneNumbers = (List<PhoneNumber>) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeObject(firstName);
        out.writeObject(lastName);
        out.writeObject(phoneNumbers);
    }
}
