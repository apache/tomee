/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.myfaces.view;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;
import org.superbiz.myfaces.domain.User;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.New;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

//due to the enhanced entities it isn't possible to use them directly (due to final methods)
@WindowScoped
public class UserHolder implements Serializable {

    private static final long serialVersionUID = -7687528373042288584L;

    @Inject
    @New
    private User user;

    @Produces
    @Dependent
    @Named("currentUser")
    protected User createCurrentUser() {
        return this.user;
    }

    public void setCurrentUser(User user) {
        this.user = user;
    }

    public boolean isLoggedIn() {
        return this.user.getId() != null;
    }
}
