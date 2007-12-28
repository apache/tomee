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
package org.apache.openejb.tomcat.naming;

import static org.apache.openejb.tomcat.naming.NamingUtil.NAME;
import static org.apache.openejb.tomcat.naming.NamingUtil.RESOURCE_ID;
import static org.apache.openejb.tomcat.naming.NamingUtil.getProperty;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.net.URL;
import java.util.Hashtable;

public class ResourceFactory extends AbstractObjectFactory {
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable environment) throws Exception {
        Reference reference = ((Reference) object);
        if (reference.getClassName().equals("java.net.URL")) {
            String resourceId = getProperty(reference, RESOURCE_ID);
            return new URL(resourceId);
        }
        return super.getObjectInstance(object, name, context, environment);
    }

    protected String buildJndiName(Reference reference) throws NamingException {
        // get and verify interface type
        String resourceId = getProperty(reference, RESOURCE_ID);
        if (resourceId == null) {
            resourceId = getProperty(reference, NAME);
        }
        if (resourceId == null) throw new NamingException("Resource reference id is null");

        // build jndi name using the deploymentId and interface type
        String jndiName = "java:openejb/Resource/" + resourceId;
        return jndiName;
    }

}
