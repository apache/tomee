/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client.corba;

import org.omg.CORBA.ORB;

import javax.naming.InitialContext;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import java.io.IOException;
import java.rmi.Remote;

public class Corbas {
    private Corbas() {
        // no-op
    }

    public static Object toStub(final Object obj) throws IOException {
        final Tie tie = javax.rmi.CORBA.Util.getTie((Remote) obj);
        if (tie == null) {
            throw new IOException("Unable to serialize PortableRemoteObject; object has not been exported: " + obj);
        }
        final ORB orb = getORB();
        tie.orb(orb);
        return PortableRemoteObject.toStub((Remote) obj);
    }

    private static ORB getORB() throws IOException { // note: we can cache it if needed but needs to be contextual
        try {
            return ORB.class.cast(new InitialContext().lookup("java:comp/ORB"));
        } catch (final Throwable e) {
            try {
                // any orb will do if we can't get a context one.
                return ORB.init();
            } catch (final Throwable ex) {
                throw new IOException("Unable to connect PortableRemoteObject stub to an ORB, no ORB bound to java:comp/ORB");
            }
        }
    }

    public static Object connect(final Object obj) throws IOException {
        if (obj instanceof Stub) {
            final Stub stub = (Stub) obj;
            final ORB orb = getORB();
            stub.connect(orb);
        }
        return obj;
    }
}
