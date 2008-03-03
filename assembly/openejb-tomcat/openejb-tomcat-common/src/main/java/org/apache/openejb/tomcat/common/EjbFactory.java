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

import org.apache.naming.EjbRef;
import org.apache.openejb.core.ivm.EjbObjectInputStream;
import org.apache.openejb.core.ivm.IntraVmCopyMonitor;
import static org.apache.openejb.tomcat.common.NamingUtil.DEPLOYMENT_ID;
import static org.apache.openejb.tomcat.common.NamingUtil.EXTERNAL;
import static org.apache.openejb.tomcat.common.NamingUtil.LOCAL;
import static org.apache.openejb.tomcat.common.NamingUtil.REMOTE;
import static org.apache.openejb.tomcat.common.NamingUtil.getProperty;
import static org.apache.openejb.tomcat.common.NamingUtil.isPropertyTrue;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

public class EjbFactory extends AbstractObjectFactory {
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable environment) throws Exception {
        // ignore non ejb-refs
        if (!(object instanceof EjbRef)) {
            return null;
        }

        // lookup the value
        Object value = super.getObjectInstance(object, name, context, environment);

        // if this is an external reference, copy it into the local class loader
        if (isPropertyTrue((Reference) object, EXTERNAL)) {
            value = copy(value);
        }

        // done
        return value;
    }

    protected String buildJndiName(Reference reference) throws NamingException {
        String jndiName;// get and verify deploymentId
        String deploymentId = getProperty(reference, DEPLOYMENT_ID);
        if (deploymentId == null) throw new NamingException("ejb-ref deploymentId is null");

        // get and verify interface type
        String interfaceType = getProperty(reference, REMOTE);
        if (interfaceType == null) {
            interfaceType = getProperty(reference, LOCAL);
        }
        if (interfaceType == null) throw new NamingException("ejb-ref interface type is null");

        // build jndi name using the deploymentId and interface type
        jndiName = "java:openejb/Deployment/" + deploymentId + "/" + interfaceType;
        return jndiName;
    }

    private static Object copy(Object source) throws Exception {
        IntraVmCopyMonitor.preCrossClassLoaderOperation();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(source);
            out.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream in = new EjbObjectInputStream(bais);
            Object copy = in.readObject();
            return copy;
        } finally {
            IntraVmCopyMonitor.postCrossClassLoaderOperation();
        }
    }
}