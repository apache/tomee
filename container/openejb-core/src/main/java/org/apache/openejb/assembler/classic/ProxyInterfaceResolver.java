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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class ProxyInterfaceResolver {

    public static List<Class> getInterfaces(Class implementation, Class mainInterface, List<Class> interfaces){
        List<Class> valid = new ArrayList<Class>();
        // The intended interface is safe to add
        valid.add(mainInterface);

        // Any interface the bean implements is safe (potentially)
        for (Class interfce : interfaces) {
            if (interfce.isAssignableFrom(implementation)){
                valid.add(interfce);
            }
        }


        // Here comes the trick, if any of them implement java.rmi.Remote
        // we have to check the "remote" methods against the other methods
        // of the same name and params to see if there are any conflicts in
        // the exception clauses.  If there are, then we need to remove the
        // conflicting interface(s).
        //
        // DETAILS:
        // The trick is that two nearly matching interface methods
        //   -InterfaceA: void doIt() throws Foo;
        //   -InterfaceB: void doIt() throws Bar;
        //
        // can be implemented in a class by leaving out exceptions from the
        // throws clause that aren't declared in both interfaces methods.
        //   -Implementation:  void doIt(){}
        //
        // This means the implementing class can not throw Foo or Bar.  The
        // same rule applies to proxies created from these two interfaces as
        // the proxy generating code will automatically remove exceptions
        // not shared by the two matching interface methods; eliminating
        // the proxy's and therefore container's ability to throw those
        // exceptions.
        //
        // The specific issue with java.rmi.Remote interfaces is that per
        // spec rules many runtime exceptions (container or connection issues)
        // are thrown to clients as java.rmi.RemoteException which is not
        // a runtime exception and must be throwable via the proxy.


        List<Class> remotes = new ArrayList<Class>();
        List<Class> nonremotes = new ArrayList<Class>();
        for (Class interfce : valid) {
            if (java.rmi.Remote.class.isAssignableFrom(interfce)){
                remotes.add(interfce);
            } else {
                nonremotes.add(interfce);
            }
        }

        // No remote interfaces, we're good to go
        if (remotes.size() == 0) return valid;

        // -----------------------------------------------------------
        // If we got here, we have potentially clashing interfaces
        // We sort of have to start over and go "slower" checking
        // methods for conflicts and not including those interfaces
        // -----------------------------------------------------------

        valid.clear();
        remotes.remove(mainInterface);
        nonremotes.remove(mainInterface);

        // Re-add the main interface
        valid.add(mainInterface);

        // Track the method signatures of the interfaces we add
        List<Signature> proxySignatures = getSignatures(mainInterface);


        // Show affinity for the remote interfaces if the main
        // interface is a java.rmi.Remote
        if (java.rmi.Remote.class.isAssignableFrom(mainInterface)){
            for (Class interfce : remotes) {
                addIfNotConflicting(interfce, valid, proxySignatures);
            }
            for (Class interfce : nonremotes) {
                addIfNotConflicting(interfce, valid, proxySignatures);
            }
        } else {
            for (Class interfce : nonremotes) {
                addIfNotConflicting(interfce, valid, proxySignatures);
            }
            for (Class interfce : remotes) {
                addIfNotConflicting(interfce, valid, proxySignatures);
            }
        }

        return valid;
    }

    /**
     * Adds the interface to the list of valid interfaces for the proxy
     * if the signatures on the interface do not conflict with the method
     * signatures already apart of the proxy's other interfaces.
     *
     * @param interfce
     * @param valid
     * @param proxySignatures
     */
    private static void addIfNotConflicting(Class interfce, List<Class> valid, List<Signature> proxySignatures) {

        List<Signature> interfaceSigs = getSignatures(interfce);


        for (Signature sig : interfaceSigs) {
            // Contains will return true if the
            // method signature exits *and* has
            // a different throws clause
            if (proxySignatures.contains(sig)){
                 return;  // conflicts and cannot be added
            }
        }


        // Does not conflict, add it and track the new signatures
        valid.add(interfce);
        proxySignatures.addAll(interfaceSigs);
    }

    private static List<Signature> getSignatures(Class mainInterface) {
        List<Signature> sigs = new ArrayList<Signature>();
        for (Method method : mainInterface.getMethods()) {
            sigs.add(new Signature(mainInterface, method));
        }
        return sigs;
    }

    public static class Signature {
        private final Class clazz;
        private final Method method;
        private final String sig;

        public Signature(Class clazz, Method method) {
            this.clazz = clazz;
            this.method = method;
            StringBuilder sb = new StringBuilder();
            sb.append(method.getName());
            sb.append('(');
            for (Class<?> type : method.getParameterTypes()) {
                sb.append(type.getName());
                sb.append(',');
            }
            sb.append(')');
            sig = sb.toString();
        }

        public Method getMethod() {
            return method;
        }

        // This equals returns true only if the method signatures
        // are the same *and* one is remote and one is not
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Signature signature = (Signature) o;

            if (!sig.equals(signature.sig)) return false;

            boolean aIsRemote = java.rmi.Remote.class.isAssignableFrom(clazz);
            boolean bIsRemote = java.rmi.Remote.class.isAssignableFrom(signature.clazz);

            return !(aIsRemote == bIsRemote);
        }

        public int hashCode() {
            return sig.hashCode();
        }

        public String toString() {
            return method.toString();
        }
    }

}
