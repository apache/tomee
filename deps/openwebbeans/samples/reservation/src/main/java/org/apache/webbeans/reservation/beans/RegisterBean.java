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
package org.apache.webbeans.reservation.beans;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.logging.Log;
import org.apache.webbeans.reservation.bindings.ApplicationLog;
import org.apache.webbeans.reservation.controller.RegisterController;
import org.apache.webbeans.reservation.util.JSFUtility;

@Named(value="register")
@RequestScoped
public class RegisterBean
{
    private @Inject @ApplicationLog Log logger;
    
    private String name;
    
    private String surname;
    
    private Integer age;
    
    private String userName;
    
    private String password;
    
    private boolean admin;
    
    private @Inject @Default RegisterController personController;
    
    private @Inject @Default BeanManager manager; 
    
    public RegisterBean()
    {
        
    }
    
    public String register()
    {   
        
        logger.info("Registering the new user with user name : " + userName);
        
        personController.registerUser(userName, password, name, surname, age, admin);
        
        JSFUtility.addInfoMessage("User with name : " + userName + " is registered successfully.", "");
        
        return "login";

    }

    @PostConstruct
    public void init()
    {
        Bean<?> bean = manager.getBeans("logger").iterator().next();
        
        logger = (Log)manager.getReference(bean, null, manager.createCreationalContext(bean));
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
    public Integer getAge()
    {
        return age;
    }

    /**
     * @param age the age to set
     */
    public void setAge(Integer age)
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
    
    
}
