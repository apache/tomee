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

package org.apache.openejb;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.ArrayList;

public class AppClassLoader extends URLClassLoader {
    List<String> addedClasses = new ArrayList<String>();

    public AppClassLoader(URL[] urls, ClassLoader classLoader) {
        super(urls, classLoader);
    }

    public AppClassLoader(URL[] urls) {
        super(urls);
    }

    public AppClassLoader(URL[] urls, ClassLoader classLoader, URLStreamHandlerFactory urlStreamHandlerFactory) {
        super(urls, classLoader, urlStreamHandlerFactory);
    }

    public void addClass(byte[] cls, String clsName) {
        defineClass(clsName, cls, 0, cls.length);
        addedClasses.add(clsName);
    }

    public boolean classDefined(String clsName) {
        return addedClasses.contains(clsName);
    }
}
