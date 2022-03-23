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
package org.apache.tomee.security;

import java.util.Map;

import jakarta.enterprise.context.Dependent;
import jakarta.security.enterprise.identitystore.PasswordHash;

@Dependent
public class TomEEPlaintextPasswordHash implements PasswordHash {

  @Override
  public void initialize(final Map<String, String> parameters) {

  }

  @Override
  public String generate(final char[] password) {
    return new String(password);
  }

  @Override
  public boolean verify(final char[] password, final String hashedPassword) {
    // don't bother with constant time comparison; more portable
    // this way, and algorithm will be used only for testing.
    return (password != null && password.length > 0 && hashedPassword != null
        && hashedPassword.length() > 0
        && hashedPassword.equals(new String(password)));
  }
}