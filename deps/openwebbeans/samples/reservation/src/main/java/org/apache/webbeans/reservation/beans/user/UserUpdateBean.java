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
package org.apache.webbeans.reservation.beans.user;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.reservation.controller.user.UserController;
import org.apache.webbeans.reservation.entity.User;
import org.apache.webbeans.reservation.session.SessionTracker;
import org.apache.webbeans.reservation.util.JSFUtility;

@Named
@RequestScoped
public class UserUpdateBean
{
    private String name;
    
    private String surname;
    
    private int age;
    
    private String userName;
    
    private String password;
    
    private @Inject @Default UserController controller;
    
    private @Inject @Default SessionTracker tracker;
    
    public UserUpdateBean()
    {
        
    }

    public String showInfo()
    {
        //Just show how can access session webbeans
        User user = this.controller.getUser(tracker.getUser().getId());
        
        setName(user.getName());
        setSurname(user.getSurname());
        setAge(user.getAge());
        setUserName(user.getUserName());
        setPassword(user.getPassword());
                
        return "toUpdatePage";
    }

    public String clear()
    {
        setName("");
        setSurname("");
        setAge(0);
        setUserName("");
        setPassword("");
        
        return null;
    }
    
    public String update()
    {
        this.controller.updateUserInfo(tracker.getUser().getId(), name, surname, age, userName, password);
        
        JSFUtility.addInfoMessage("Personal information is succesfully updated.", "");
        
        return null;
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
    
    
}
