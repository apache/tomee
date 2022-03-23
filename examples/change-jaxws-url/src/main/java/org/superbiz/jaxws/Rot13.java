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
package org.superbiz.jaxws;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.jws.WebService;

@Lock(LockType.READ)
@Singleton
@WebService
public class Rot13 {

    public String rot13(final String in) {
        final StringBuilder builder = new StringBuilder(in.length());
        for (int b : in.toCharArray()) {
            int cap = b & 32;
            b &= ~cap;
            if (Character.isUpperCase(b)) {
                b = (b - 'A' + 13) % 26 + 'A';
            } else {
                b = cap;
            }
            b |= cap;
            builder.append((char) b);
        }
        return builder.toString();
    }
}
