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
package org.apache.openejb.tomcat.common;

import org.apache.openejb.loader.SystemInstance;
import static org.apache.openejb.tomcat.common.NamingUtil.COMPONENT_TYPE;
import static org.apache.openejb.tomcat.common.NamingUtil.getProperty;
import static org.apache.openejb.tomcat.common.NamingUtil.loadClass;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

public class SystemComponentFactory implements ObjectFactory {
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable environment) throws Exception {
        Reference ref = (Reference) object;

        // load the component type class
        String className = getProperty(ref, COMPONENT_TYPE);
        Class<?> clazz = loadClass(className);
        if (clazz == null) return null;

        // lookup the value
        Object value = SystemInstance.get().getComponent(clazz);
        return value;
    }
}
