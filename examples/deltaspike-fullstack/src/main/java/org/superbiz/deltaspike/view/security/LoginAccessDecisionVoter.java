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
package org.superbiz.deltaspike.view.security;

import org.apache.deltaspike.security.api.authorization.AbstractAccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.superbiz.deltaspike.WebappMessageBundle;
import org.superbiz.deltaspike.view.UserHolder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class LoginAccessDecisionVoter extends AbstractAccessDecisionVoter {
    private static final long serialVersionUID = -6332617547592896599L;

    @Inject
    private UserHolder userHolder;

    @Inject
    private WebappMessageBundle webappMessageBundle;

    @Override
    protected void checkPermission(AccessDecisionVoterContext accessDecisionVoterContext,
                                   Set<SecurityViolation> violations) {
        if (!this.userHolder.isLoggedIn()) {
            violations.add(newSecurityViolation(this.webappMessageBundle.msgAccessDenied()));
        }
    }
}
