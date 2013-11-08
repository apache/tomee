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
package org.apache.openjpa.persistence.jdbc.sqlcache;

import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name="PERSON_PQC")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("PERSON")
@NamedQueries({
    @NamedQuery(name="JPQLNamedSelectPositionalParameter",
        query="select p from Person p where p.firstName=?2" +
              " and p.lastName=?1 and p.age=?3 and p.address=?4"),
    @NamedQuery(name="JPQLNamedSelectNamedParameter",
        query="select p from Person p where p.firstName=:first" +
              " and p.lastName=:last and p.age=:age and p.address=:addr"),
    @NamedQuery(name="JPQLNamedUpdatePositionalParameter",
        query="update Person p set p.firstName=?2, p.age=?3" +
              " WHERE p.lastName=?4 and p.address=?1"),
    @NamedQuery(name="JPQLNamedUpdateNamedParameter",
        query="update Person p set p.firstName=:first, p.age=:age" +
              " WHERE p.lastName=:last and p.address=:addr")
})
@NamedNativeQueries({
    @NamedNativeQuery(name="SQLNamedSelectPositionalParameter",
        query="select * from Person p where p.firstName=?2" +
                  " and p.lastName=?1 and p.age=?3 and p.address_id=?4"),
    @NamedNativeQuery(name="SQLNamedSelectNamedParameter",
        query="select * from Person p where p.firstName=:first" +
              " and p.lastName=:last and p.age=:age and p.address_id=:addr"),
    @NamedNativeQuery(name="SQLNamedUpdatePositionalParameter",
        query="update Person p set p.firstName=?2, p.age=?3" +
              " WHERE p.lastName=?4 and p.address_id=?1"),
    @NamedNativeQuery(name="SQLNamedUpdateNamedParameter",
        query="update Person p set p.firstName=:first, p.age=:age" +
              " WHERE p.lastName=:last and p.address_id=:addr")
})
public class Person {
    @Id
    private long id;
    
    private String firstName;
    private String lastName;
    private short age;
    private int   yob;
    
    @OneToOne
    private Address address;
    
    private static AtomicLong idCounter = new AtomicLong(System.currentTimeMillis());
    
    public Person() {
        this("?", "?", (short)0, 0);
    }
    
    public Person(String firstName, String lastName, short age, int yob) {
        super();
        this.id = idCounter.getAndAdd(1);
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.yob = yob;
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
    
    public short getAge() {
        return age;
    }
    
    public void setAge(short age) {
        this.age = age;
    }
    public int getBirthYear() {
        return yob;
    }
    
    public void setBirthYear(int yob) {
        this.yob = yob;
    }
    
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
}
