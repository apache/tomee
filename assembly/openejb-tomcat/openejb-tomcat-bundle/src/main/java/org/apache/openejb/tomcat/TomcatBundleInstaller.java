/**
 *
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
package org.apache.openejb.tomcat;

import org.apache.openejb.tomcat.installer.Installer;
import org.apache.openejb.tomcat.installer.Paths;

import java.io.File;
import java.lang.reflect.Field;

public class TomcatBundleInstaller {
    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                System.out.println("Usage: TomcatBundleInstaller <catalina_home>");
            }

            String catalinaHome = args[0];
            System.out.println("Installing to: " + catalinaHome);
            Paths paths = new Paths(new File(catalinaHome));

            Installer installer = new Installer(paths, true);
            installer.installAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
