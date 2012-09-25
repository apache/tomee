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
package org.apache.webbeans.reservation.intercept;


import javax.decorator.Delegate;
import javax.decorator.Decorator;
import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.webbeans.reservation.bindings.ApplicationLog;
import org.apache.webbeans.reservation.bindings.DatabaseLogin;
import org.apache.webbeans.reservation.controller.api.ILoginController;
import org.apache.webbeans.reservation.entity.User;

@Decorator
public class LoginDecorator implements ILoginController 
{
    @Inject @Delegate @DatabaseLogin ILoginController decorator;
    
    private @Inject @ApplicationLog Log logger;

    /* (non-Javadoc)
     * @see org.apache.webbeans.reservation.controller.LoginController#checkLogin(java.lang.String, java.lang.String)
     */
    public User checkLogin(String userName, String password)
    {
        logger.info("Login process is started. Use tries to login with user name : " + userName );
        
        return decorator.checkLogin(userName, password);
    }

    
}
