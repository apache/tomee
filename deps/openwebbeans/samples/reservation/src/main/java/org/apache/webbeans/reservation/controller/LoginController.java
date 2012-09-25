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
package org.apache.webbeans.reservation.controller;

import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.validator.GenericValidator;
import org.apache.webbeans.reservation.bindings.DatabaseLogin;
import org.apache.webbeans.reservation.bindings.EntityManagerQualifier;
import org.apache.webbeans.reservation.bindings.intercep.Transactional;
import org.apache.webbeans.reservation.controller.api.ILoginController;
import org.apache.webbeans.reservation.entity.User;

@DatabaseLogin
@RequestScoped
@Named
public class LoginController implements ILoginController
{
    private Logger logger = Logger.getLogger(LoginController.class.getName());
    
    /**Injection of the request scope entity manager*/
    private @Inject @EntityManagerQualifier EntityManager entityManager;

    /**
     * Returns true if user exist else false
     * 
     * @param userName user name
     * @param password user password
     * @return true if user exist else false
     */
    @Transactional
    public User checkLogin(String userName, String password)
    {
        if(GenericValidator.isBlankOrNull(userName) || GenericValidator.isBlankOrNull(password))
        {
            logger.log(Level.WARNING, "UserName and Password is null");
            
            return null;
        }
        
        Query query = entityManager.createQuery("select p from User p where  " +
                                                "p.userName=:userName and " +
                                                "p.password=:password");
        
        query.setParameter("userName", userName);
        query.setParameter("password", password);
        
        User value = null;
        try
        {
            value =(User)query.getSingleResult();
            value.setLastLoginDate(GregorianCalendar.getInstance().getTime());
        }
        catch(Exception e)
        {
            return null;
        }
                
        
        return value;
    }
    

}
