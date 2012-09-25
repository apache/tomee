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
package org.apache.webbeans.test.component.event.normal;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;

import org.apache.webbeans.test.annotation.binding.Check;
import org.apache.webbeans.test.event.LoggedInEvent;

@RequestScoped
public class ComponentWithObserves1
{
    private String userName;

    private String userNameWithMember;

    public void afterLoggedIn(@Observes @Any LoggedInEvent event)
    {
        this.userName = event.getUserName();
    }

    private void afterLoggedInWithMember(@Observes @Check(type = "CHECK") LoggedInEvent event)
    {
        this.userNameWithMember = event.getUserName();
    }

    public String getUserName()
    {
        return this.userName;
    }

    /**
     * @return the userNameWithMember
     */
    public String getUserNameWithMember()
    {
        return userNameWithMember;
    }

}
