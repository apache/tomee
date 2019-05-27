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
package org.superbiz.injection.secure;

import org.apache.openejb.core.security.jaas.LoginProvider;

import javax.security.auth.login.FailedLoginException;
import java.util.Arrays;
import java.util.List;

public class MyLoginProvider implements LoginProvider {

    @Override
    public List<String> authenticate(String user, String password) throws FailedLoginException {
        if ("paul".equals(user) && "michelle".equals(password)) {
            return Arrays.asList("Manager", "rockstar", "beatle");
        }

        if ("eddie".equals(user) && "jump".equals(password)) {
            return Arrays.asList("Employee", "rockstar", "vanhalen");
        }

        throw new FailedLoginException("Bad user or password!");
    }
}
