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
package javax.security.enterprise.credential;

public class UsernamePasswordCredential extends AbstractClearableCredential {

    private final String caller;
    private final Password password;

    public UsernamePasswordCredential(String callerName, String password) {
        this.caller = callerName;
        this.password = new Password(password);
    }

    public UsernamePasswordCredential(String callerName, Password password) {
        this.caller = callerName;
        this.password = password;
    }

    public Password getPassword() {
        return password;
    }

    public String getPasswordAsString() {
        return String.valueOf(getPassword().getValue());
    }

    @Override
    public void clearCredential() {
        password.clear();
    }

    public String getCaller() {
        return caller;
    }

    public boolean compareTo(String callerName, String password) {
        return getCaller().equals(callerName) && getPassword().compareTo(password);
    }
}
