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
package org.apache.openejb.loader;

public class ClassPathFactory {
    public static ClassPath createClassPath(String name) {
        if (name.equalsIgnoreCase("tomcat")) {
            return new TomcatClassPath();
        } else if (name.equalsIgnoreCase("tomcat-common")) {
            return new TomcatClassPath();
        } else if (name.equalsIgnoreCase("tomcat-system")) {
            return new TomcatClassPath();
        } else if (name.equalsIgnoreCase("tomcat-webapp")) {
            return new WebAppClassPath();
        } else if (name.equalsIgnoreCase("bootstrap")) {
            return new SystemClassPath();
        } else if (name.equalsIgnoreCase("system")) {
            return new SystemClassPath();
        } else if (name.equalsIgnoreCase("thread")) {
            return new ContextClassPath();
        } else if (name.equalsIgnoreCase("context")) {
            return new ContextClassPath();
        } else {
            return new ContextClassPath();
        }
    }
}
