/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acme;

import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 * The legacy EJB 2.1 compatable interfaces are refered to
 * in the EJB 3.0 spec as "component" interfaces.
 *
 * EJB 3.0 beans are not required to have component interfaces, but
 * OpenEJB doesn't yet support the new Business Interfaces that replace
 * Component Interfaces.  You could get by with just a local interface
 * or just a remote interface, but for the sake of a more complete example
 * let's just create both.  
 *
 * I very much dislike having seeing lots of useless interfaces floating
 * around, so I tuck all the interfaces into one.  Keeps the view of my
 * code in my editor looking nice and clean.
 *
 * We could just put these four inner interfaces right in the FriendlyPerson
 * interface, but who wants to pollute that with javax.ejb.* dependencies.
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public interface FriendlyPersonComponent {

    public interface Remote extends FriendlyPerson, javax.ejb.EJBObject {
    }

    public interface Home extends javax.ejb.EJBHome {
        Remote create() throws RemoteException, CreateException;
    }

    public interface Local extends FriendlyPerson, javax.ejb.EJBLocalObject {
    }

    public interface LocalHome extends javax.ejb.EJBLocalHome {
        Local create() throws CreateException;
    }
}
