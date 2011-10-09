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

import org.apache.naming.ResourceRef;
import static org.apache.openejb.tomcat.common.NamingUtil.JNDI_NAME;
import static org.apache.openejb.tomcat.common.NamingUtil.getProperty;
import static org.apache.openejb.tomcat.common.NamingUtil.getStaticValue;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.Hashtable;

public class PersistenceUnitFactory extends AbstractObjectFactory {
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable environment) throws Exception {
        // ignore non resource-refs
        if (!(object instanceof ResourceRef)) {
            return null;
        }

        Reference ref = (Reference) object;

        Object value;
        if (getProperty(ref, JNDI_NAME) != null) {
            // lookup the value in JNDI
            value = super.getObjectInstance(object, name, context, environment);
        } else {
            // value is hard hard coded in the properties
            value = getStaticValue(ref);
        }

        return value;
    }

    protected String buildJndiName(Reference reference) throws NamingException {
        throw new UnsupportedOperationException();
    }
}
