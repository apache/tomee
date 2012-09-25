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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.validator.GenericValidator;
import org.apache.webbeans.reservation.bindings.ApplicationLog;
import org.apache.webbeans.reservation.bindings.EntityManagerQualifier;
import org.apache.webbeans.reservation.bindings.intercep.Transactional;
import org.apache.webbeans.reservation.controller.api.IRegisterController;
import org.apache.webbeans.reservation.entity.User;
import org.apache.webbeans.reservation.util.CalendarUtil;

/**
 * Controller responsible for registering operations.
 */
@RequestScoped
public class RegisterController implements IRegisterController
{
    private @Inject @ApplicationLog Log logger;
    
    private @Inject @EntityManagerQualifier EntityManager entityManager;

    /**
     * Register the user.
     */
    @Transactional
    public User registerUser(String userName, String password, String name, String surname , int age, boolean admin)
    {
        logger.debug("Register a new user with user name : " + userName);
        
        if(GenericValidator.isBlankOrNull(userName) || GenericValidator.isBlankOrNull(password))
        {
            logger.debug("Registering is failed. User name and password can not be null");
            return null;
        }
        
        User user = new User();
        
        user.setUserName(userName);
        user.setPassword(password);
        user.setName(name);
        user.setSurname(surname);
        user.setAge(age);
        user.setRegisterDate(CalendarUtil.getCurrentDate());
        user.setAdmin(admin);
        
        entityManager.persist(user);
        
        
        return user;
    }

}
