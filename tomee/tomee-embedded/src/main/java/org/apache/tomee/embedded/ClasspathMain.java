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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.embedded;

// same as Main but for platforms not supporting parameters it is convenient
public final class ClasspathMain {
    public static void main(final String[] args) {
        try (final Container container = new Container(new Configuration().http(httpPort())).deployClasspathAsWebApp()) {
            container.await();
        }
    }

    private static int httpPort() {
        final String port = System.getenv("PORT");
        return port == null ? 8080 : Integer.parseInt(port);
    }

    private ClasspathMain() {
        // no-op
    }
}
