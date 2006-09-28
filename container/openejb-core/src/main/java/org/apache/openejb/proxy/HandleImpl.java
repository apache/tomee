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

import java.io.ObjectStreamException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

public class HandleImpl implements java.io.Serializable, javax.ejb.HomeHandle, javax.ejb.Handle {
    
    public static final int HOMEHANDLE = 0 ;
    public static final int HANDLE = 1;
    
    public final int type;
    
    private final Object proxy;

    public HandleImpl() {
        this(null,0);
    }

    public HandleImpl(Object proxy, int type) {
        this.proxy = proxy;
        this.type = type;
    }
    
    public EJBHome getEJBHome( ) {
        return(EJBHome)proxy;
    }
    public EJBObject getEJBObject( ) {
        return(EJBObject)proxy;
    }       

    public Object getPrimaryKey() {
        return ((BaseEJB) proxy).getProxyInfo().getPrimaryKey();
    }
    protected Object writeReplace() throws ObjectStreamException{
        return SerializationHandler.writeReplace(this, ((BaseEJB)proxy).getProxyInfo());
    }

}