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
package org.apache.openejb.core.security;

import java.security.Policy;

/**
 * A utility class to manage the Java Security Policy in a thread-safe manner.
 * This class provides methods to get and set the security policy, ensuring
 * that changes are synchronized across threads.
 */
public class PolicyJDK24 {

    private static volatile Policy policy;

    /**
     * @return the policy
     */
    public static synchronized Policy getPolicy() {
        return policy;
    }

    /**
     * @param policy the policy to set
     */
    public static synchronized void setPolicy(Policy policy) {
        PolicyJDK24.policy = policy;
    }



}
