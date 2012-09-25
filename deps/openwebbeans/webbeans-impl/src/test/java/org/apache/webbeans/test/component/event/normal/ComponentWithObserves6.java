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

import org.apache.webbeans.test.annotation.binding.TestingIfExists;
import org.apache.webbeans.test.annotation.binding.TestingIfNonExists;
import org.apache.webbeans.test.annotation.binding.TestingNormal;
import org.apache.webbeans.test.event.LoggedInEvent;

@RequestScoped
public class ComponentWithObserves6
{
    private String userName = null;
    private String userIEName = null;
    private String userNIEName = null;

    public void afterLoggedIn(@Observes @TestingNormal LoggedInEvent event)
    {
        this.userName = event.getUserName();
    }

    public void afterLoggedIn2(@Observes @TestingIfExists LoggedInEvent event)
    {
        this.userIEName = event.getUserName();
    }

    public void afterLoggedIn3(@Observes @TestingIfNonExists LoggedInEvent event)
    {
        this.userNIEName = event.getUserName();
    }

    public String getUserName()
    {
        return this.userName;
    }

    public String getUserIEName()
    {
        return this.userIEName;
    }

    public String getUserNIEName()
    {
        return this.userNIEName;
    }

}
