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

package org.apache.openejb.spi;

import org.apache.openejb.InterfaceType;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.security.Principal;

/**
 * The generic value T is any serializable token of the SecurityService
 * implementations choosing.   This token only needs to be understandable
 * by the SecurityService internally and need not be a publicly usable class
 * type.  No part of the outlying system will make any assumptions as to the
 * type of the object.  The use of a java generic type is to express the
 * required symmetry in the interface.
 */
public interface SecurityService<T> extends Service {
    /**
     *
     */
    T login(String user, String pass) throws LoginException;

    T login(String securityRealm, String user, String pass) throws LoginException;

    /**
     * Active
     */
    void associate(T securityIdentity) throws LoginException;

    /**
     * Active
     */
    T disassociate();

    /**
     * Active
     */
    void logout(T securityIdentity) throws LoginException;

    /**
     * Active
     */
    boolean isCallerInRole(String role);

    /**
     * Implementors are encouraged to return a java.security.Principal
     * object that implements org.apache.openejb.spi.CallerPrincipal
     *
     * JAAS LoginModule implementors are encouraged to use the CallerPrincipal
     * interface to denote the best fitting Principal for getCallerPrincipal.
     */
    Principal getCallerPrincipal();

    /**
     * Active
     */
    boolean isCallerAuthorized(Method method, InterfaceType type);

    // mainly for asynch handling
    void setState(Object o);

    Object currentState();

    /**
     * Called when request.logout() is triggered. Intended to remove context propagation.
     *
     * @param request the http request triggering the logout.
     */
    void onLogout(HttpServletRequest request);
}