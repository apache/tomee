/**
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
package org.apache.openejb.jetty.common;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.security.SecurityServiceImpl;
import org.eclipse.jetty.server.Authentication;

import javax.security.auth.Subject;
import java.util.LinkedList;

public class JettySecurityService extends SecurityServiceImpl {

    static protected final ThreadLocal<LinkedList<Subject>> runAsStack = new ThreadLocal<LinkedList<Subject>>() {
        protected LinkedList<Subject> initialValue() {
            return new LinkedList<Subject>();
        }
    };
    
    public Object enterWebApp(Authentication.User principal, String runAs) {
        Identity oldIdentity = clientIdentity.get();
		Subject subject = principal.getUserIdentity().getSubject();

        Identity newIdentity = new Identity(subject, null);
        WebAppState webAppState = new WebAppState(oldIdentity, runAs != null);
        clientIdentity.set(newIdentity);

        if (runAs != null) {
            Subject runAsSubject = createRunAsSubject(runAs);
            runAsStack.get().addFirst(runAsSubject);
        }

        return webAppState;
    }

    public void exitWebApp(Object state) {
        if (state instanceof WebAppState) {
            WebAppState webAppState = (WebAppState) state;
            clientIdentity.set(webAppState.oldIdentity);
            if (webAppState.hadRunAs) {
                runAsStack.get().removeFirst();
            }
        }
    }

    protected Subject getRunAsSubject(BeanContext callingBeanContext) {
        Subject runAsSubject = super.getRunAsSubject(callingBeanContext);
        if (runAsSubject != null) return runAsSubject;

        LinkedList<Subject> stack = runAsStack.get();
        if (stack.isEmpty()) {
            return null;
        }
        return stack.getFirst();
    }

    private static class WebAppState {
        private final Identity oldIdentity;
        private final boolean hadRunAs;


        public WebAppState(Identity oldIdentity, boolean hadRunAs) {
            this.oldIdentity = oldIdentity;
            this.hadRunAs = hadRunAs;
        }
    }


}
