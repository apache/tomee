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
package org.apache.openejb.client;

import javax.naming.InitialContext;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class Main {
    public static void main(String[] args) throws Exception {
        args = siftArgs(args);

        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, "org.apache.openejb.client");

        InitialContext initialContext = new InitialContext();


        String path = (String) initialContext.lookup("java:comp/path");
        // TODO: Download the file
        File file = new File(path);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        classLoader = new URLClassLoader(new URL[]{file.toURL()}, classLoader);

        String mainClassName = (String) initialContext.lookup("java:comp/mainClass");

        Class mainClass = classLoader.loadClass(mainClassName);
        Method mainMethod = mainClass.getMethod("main", args.getClass());
        mainMethod.invoke(args);

    }

    private static String[] siftArgs(String[] args) {
        List<String> argsList = new ArrayList();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.indexOf("-D") == -1) {
                argsList.add(arg);
            } else {
                String prop = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                String val = arg.substring(arg.indexOf("=") + 1);
                System.setProperty(prop, val);
            }
        }
        return (String[]) argsList.toArray(new String[argsList.size()]);
    }


}
