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

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.ArrayList;

import org.apache.xbean.spring.context.SpringApplicationContext;
import org.apache.openejb.OpenEJBException;

/**
 * @version $Revision$ $Date$
 */
public class AssemblerUtil {
    public static <T> List<T> asList(T[] array) {
        List<T> list;
        if (array != null) {
            list = Arrays.asList(array);
        } else {
            list = Collections.emptyList();
        }
        return list;
    }

    public static void addSystemJndiProperties() {
        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String str = systemProperties.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            String naming = "org.apache.openejb.core.ivm.naming";
            if (str == null) {
                str = naming;
            } else if (str.indexOf(naming) == -1) {
                str = naming + ":" + str;
            }
            systemProperties.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(SpringApplicationContext factory, Class<T> type) throws OpenEJBException {
        // get the main service from the configuration file
        String[] names = factory.getBeanNamesForType(type);
        if (names.length == 0) {
            throw new OpenEJBException("No bean of type: " + type.getName() + " found in the bootstrap file: " + factory.getDisplayName());
        }
        T bean = (T) factory.getBean(names[0]);
        return bean;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getBeans(SpringApplicationContext factory, Class<T> type) {
        // get the main service from the configuration file
        String[] names = factory.getBeanNamesForType(type);
        ArrayList<T> beans = new ArrayList<T>(names.length);
        for (String name : names) {
            beans.add((T) factory.getBean(name));
        }
        return beans;
    }
}
