/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.util;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import java.security.Principal;

@Stateless(name = "openejb/User")
@Remote(User.class)
public class UserEjb implements User {
    @Resource
    private SessionContext ctx;

    @RolesAllowed({"tomee-admin"})
    @Override
    public void adminOnly() {
        //no-op
    }

    @Override
    public String getUserName() {
        final Principal principal = this.ctx.getCallerPrincipal();
        if (principal == null) {
            return null;
        }
        return principal.getName();
    }
}
