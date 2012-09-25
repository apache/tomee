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
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.webbeans.test.annotation.binding.NotAny;
import org.apache.webbeans.test.annotation.binding.TestingNormal;
import org.apache.webbeans.test.event.LoggedInEvent;

@RequestScoped
public class ComponentWithObserves4
{
    private String userName;
    private @Inject @TestingNormal Event<LoggedInEvent> myLIE;

    public void afterLoggedIn(@Observes @NotAny LoggedInEvent event)
    {
        this.userName = event.getUserName();
        myLIE.fire(new LoggedInEvent(this.userName));
    }

    public String getUserName()
    {
        return this.userName;
    }

}
