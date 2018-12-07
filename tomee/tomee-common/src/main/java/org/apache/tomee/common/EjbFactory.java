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
package org.apache.tomee.common;

import org.apache.naming.EjbRef;
import org.apache.openejb.core.ivm.EjbObjectInputStream;
import org.apache.openejb.core.ivm.IntraVmCopyMonitor;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.assembler.classic.JndiBuilder;

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
    @Override
    public Object getObjectInstance(final Object object, final Name name, final Context context, final Hashtable environment) throws Exception {
        // ignore non ejb-refs
        if (!(object instanceof EjbRef)) {
            return null;
        }

        // lookup the value
        Object value = super.getObjectInstance(object, name, context, environment);

        // if this is an external reference, copy it into the local class loader
        if (NamingUtil.isPropertyTrue((Reference) object, NamingUtil.EXTERNAL)) {
            value = copy(value);
        }

        // done
        return value;
    }

    @Override
    protected String buildJndiName(final Reference reference) throws NamingException {
        final String jndiName;// get and verify deploymentId
        final String deploymentId = NamingUtil.getProperty(reference, NamingUtil.DEPLOYMENT_ID);
        if (deploymentId == null) {
            throw new NamingException("ejb-ref deploymentId is null");
        }

        // get and verify interface type
        InterfaceType type = InterfaceType.BUSINESS_REMOTE;
        String interfaceType = NamingUtil.getProperty(reference, NamingUtil.REMOTE);

        if (interfaceType == null) {
            type = InterfaceType.LOCALBEAN;
            interfaceType = NamingUtil.getProperty(reference, NamingUtil.LOCALBEAN);
        }
      
        if (interfaceType == null) {
            type = InterfaceType.BUSINESS_LOCAL;
            interfaceType = NamingUtil.getProperty(reference, NamingUtil.LOCAL);
        }
        if (interfaceType == null) {
            throw new NamingException("ejb-ref interface type is null");
        }

        // build jndi name using the deploymentId and interface type
        jndiName = "java:openejb/Deployment/" + JndiBuilder.format(deploymentId, interfaceType, type);
        return jndiName;
    }

    private static Object copy(final Object source) throws Exception {
        IntraVmCopyMonitor.preCrossClassLoaderOperation();
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            final ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(source);
            out.close();

            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream in = new EjbObjectInputStream(bais);
            final Object copy = in.readObject();
            return copy;
        } finally {
            IntraVmCopyMonitor.postCrossClassLoaderOperation();
        }
    }
}