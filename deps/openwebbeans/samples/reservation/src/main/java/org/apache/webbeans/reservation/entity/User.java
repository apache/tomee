/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.reservation.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
public class User
{
    @Id
    @GeneratedValue
    private int id;
    
    @Column(length=64,nullable=false)
    private String name;
    
    @Column(length=64,nullable=false)
    private String surname;
    
    @Column
    private int age;
    
    @Column(length=50,nullable=false,unique=true)
    private String userName;
    
    @Column(nullable=false,length=20)
    private String password;
    
    @Temporal(value=TemporalType.DATE)
    private Date registerDate;
    
    @OneToMany(mappedBy="user",cascade={CascadeType.ALL})
    private Set<Reservation> reservations = new HashSet<Reservation>();
    
    @Version
    private int version;
    
    @Column
    private boolean admin;
    

    @Temporal(value=TemporalType.TIMESTAMP)
    private Date lastLoginDate;

    public User()
    {
        
    }

    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the surname
     */
    public String getSurname()
    {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname)
    {
        this.surname = surname;
    }

    /**
     * @return the age
     */
    public int getAge()
    {
        return age;
    }

    /**
     * @param age the age to set
     */
    public void setAge(int age)
    {
        this.age = age;
    }

    /**
     * @return the reservations
     */
    public Set<Reservation> getReservations()
    {
        return reservations;
    }

    /**
     * @param reservations the reservations to set
     */
    public void setReservations(Set<Reservation> reservations)
    {
        this.reservations = reservations;
    }

    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    public void addHotel(Reservation hotel)
    {
        this.reservations.add(hotel);
        
        hotel.setUser(this);
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }


    /**
     * @return the userName
     */
    public String getUserName()
    {
        return userName;
    }


    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }


    /**
     * @return the version
     */
    public int getVersion()
    {
        return version;
    }


    /**
     * @return the registerDate
     */
    public Date getRegisterDate()
    {
        return registerDate;
    }


    /**
     * @param registerDate the registerDate to set
     */
    public void setRegisterDate(Date registerDate)
    {
        this.registerDate = registerDate;
    }


    /**
     * @return the admin
     */
    public boolean isAdmin()
    {
        return admin;
    }


    /**
     * @param admin the admin to set
     */
    public void setAdmin(boolean admin)
    {
        this.admin = admin;
    }


    /**
     * @return the lastLoginDate
     */
    public Date getLastLoginDate()
    {
        return lastLoginDate;
    }


    /**
     * @param lastLoginDate the lastLoginDate to set
     */
    public void setLastLoginDate(Date lastLoginDate)
    {
        this.lastLoginDate = lastLoginDate;
    }
    
    
}
