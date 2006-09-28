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
package org.apache.openejb.proxy;

import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;


public abstract class EntityEJBLocalObject extends EJBLocalObjectImpl {


    public EntityEJBLocalObject(EJBMethodInterceptor handler) {
        super(handler);
    }
    
    public Object getPrimaryKey() throws EJBException {
        return ejbHandler.getPrimaryKey();
    }

    public boolean isIdentical(EJBLocalObject obj) throws EJBException {
        try {
            if (!(obj instanceof EntityEJBLocalObject)) return false;
            
            Object thatID = ((EntityEJBLocalObject)obj).getProxyInfo().getContainerID();
            Object thisID = getProxyInfo().getContainerID();
            return thisID.equals(thatID) && getPrimaryKey().equals(obj.getPrimaryKey());
        } catch (Throwable t){
            return false;
        }
    }
}
