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
package org.apache.openejb.assembler.spring;

import java.net.URLClassLoader;
import java.net.URL;

import org.springframework.beans.factory.FactoryBean;

/**
 * @org.apache.xbean.XBean element="urlClassLoader"
 */
public class UrlClassLoaderFactory implements FactoryBean {
    private URL[] urls;
    private ClassLoader parent;

    public URL[] getUrls() {
        return urls;
    }

    public void setUrls(URL[] urls) {
        this.urls = urls;
    }

    public ClassLoader getParent() {
        return parent;
    }

    public void setParent(ClassLoader parent) {
        this.parent = parent;
    }

    public Class getObjectType() {
        return URLClassLoader.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public URLClassLoader getObject() {
        if (urls == null) {
            throw new NullPointerException("urls is null");
        }

        ClassLoader parent = this.parent;
        if (parent == null) {
            parent = Thread.currentThread().getContextClassLoader();
        }
        if (parent == null) {
            parent = org.apache.openejb.OpenEJB.class.getClassLoader();
        }
        return new URLClassLoader(this.urls, parent);
    }
}
