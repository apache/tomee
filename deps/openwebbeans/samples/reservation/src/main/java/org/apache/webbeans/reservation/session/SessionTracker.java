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
package org.apache.webbeans.reservation.session;


import java.io.Serializable;


import org.apache.webbeans.reservation.entity.User;
import org.apache.webbeans.reservation.events.LoggedInEvent;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;

@SessionScoped
@Named
public class SessionTracker implements Serializable
{
    private static final long serialVersionUID = 6365740106065427860L;

    private User user;
     
    /**
     * When event fires, this observer method is called
     * by the {@link javax.enterprise.inject.spi.BeanManager} interface.
     * 
     * @param loggedInEvent event 
     */
    public void userAdded(@Observes LoggedInEvent loggedInEvent)
    {
        this.user = loggedInEvent.getUser();
    }

    public User getUser()
    {
        return this.user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user)
    {
        this.user = user;
    }
    
    
}
