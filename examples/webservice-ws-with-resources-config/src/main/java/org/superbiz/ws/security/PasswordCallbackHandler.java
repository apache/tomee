/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.ws.security;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public class PasswordCallbackHandler implements CallbackHandler {

    private static boolean called = false;

    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        called = true;

        final WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
        if (pc.getIdentifier().equals("openejb")) {
            System.out.println("logged openejb user");
            pc.setPassword("tomee");
        }
    }

    public static boolean wasCalled() {
        return called;
    }
}
