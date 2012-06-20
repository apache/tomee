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
package org.apache.tomee.catalina;

import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.CallbackHandler;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.CombinedRealm;
import org.apache.catalina.realm.JAASRealm;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.ietf.jgss.GSSContext;

public class TomEERealm extends CombinedRealm {
    private final ThreadLocal<Request> requests = new ThreadLocal<Request>();

    @Override
    public Principal authenticate(String username, String password) {
        return logInTomEE(super.authenticate(username, password));
    }

    @Override
    public Principal authenticate(X509Certificate[] certs) {
        return logInTomEE(super.authenticate(certs));
    }

    @Override
    public Principal authenticate(String username, String clientDigest,
                                  String nonce, String nc, String cnonce, String qop,
                                  String realmName, String md5a2) {
        return logInTomEE(super.authenticate(username, clientDigest, nonce, nc, cnonce, qop, realmName, md5a2));
    }

    @Override
    public Principal authenticate(GSSContext gssContext, boolean storeCreds) {
        return logInTomEE(super.authenticate(gssContext, storeCreds));
    }

    private Principal logInTomEE(final Principal pcp) {
        if (pcp == null) {
            return null;
        }

        final TomcatSecurityService ss = (TomcatSecurityService) SystemInstance.get().getComponent(SecurityService.class);
        if (ss != null) {
            // normally we don't care about oldstate because the listener already contains one
            // which is the previous one
            // so no need to clean twice here
            if (requests.get() != null) {
                ss.enterWebApp(this, pcp, requests.get().getWrapper().getRunAs());
            } else {
                ss.enterWebApp(this, pcp, null);
            }
        }
        return pcp;
    }

    public void enter(final Request request) {
        requests.set(request);
    }

    public void exit() {
        requests.remove();
    }
}
