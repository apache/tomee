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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.validator.GenericValidator;
import org.apache.webbeans.reservation.bindings.DatabaseLogin;
import org.apache.webbeans.reservation.controller.LoginController;
import org.apache.webbeans.reservation.entity.User;
import org.apache.webbeans.reservation.events.LoggedInEvent;
import org.apache.webbeans.reservation.util.JSFUtility;

/**
 * Contains login related managed bean functionality.
 */
@RequestScoped
@Named
public class LoginBean
{
    /**User name*/
    private String userName;
    
    /**Password*/
    private String password;

    /**Inject of the event instance*/
    private @Inject @Any Event<LoggedInEvent> loggedInEvent;
    
    /**Database related login controller*/
    private @Inject @DatabaseLogin LoginController loginController;

    /**
     * Check user login.
     * 
     * @return navigation result
     */
    public String login()
    {
        if(GenericValidator.isBlankOrNull(userName) || GenericValidator.isBlankOrNull(password))
        {
            JSFUtility.addErrorMessage("User name and password fields can not be empty", "");
            
            return null;
        }
        
        User result = loginController.checkLogin(userName, password);
        
        if(result == null)
        {
            JSFUtility.addErrorMessage("Login failed!,User name or password is not correct. Try again!", "");
            
            return null;
        }
        
        User user = result;
        
        //Fire Event
        LoggedInEvent event = new LoggedInEvent(user);
        loggedInEvent.fire(event);
        
        
        if(user.isAdmin())
        {
            return "adminHome";
        }
        
        return "userHome";
    }
    
    /**
     * Check user logout.
     * 
     * @return navigation result
     */
    public String logout()
    {
        return null;
    }

    /**
     * @return the userName
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * This method got introduced to test OWB-595
     * @return null and always throws a RuntimeException
     */
    public String createError()
    {
        throw new RuntimeException("Just to show OWB-595");
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
