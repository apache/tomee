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
package org.apache.openejb.core.ivm.naming;

public class NameNode implements java.io.Serializable {
    public String atomicName;
    public int atomicHash;
    public NameNode lessTree;
    public NameNode grtrTree;
    public NameNode subTree;
    public NameNode parent;
    public Object myObject;
    public transient IvmContext myContext;

    public NameNode(NameNode parent, ParsedName name, Object obj) {
        atomicName = name.getComponent();
        atomicHash = name.getComponentHashCode();
        this.parent = parent;
        if (name.next())
            subTree = new NameNode(this, name, obj);
        else
            myObject = obj;
    }

    public Object getBinding() {
        if (myObject != null)
            return myObject;// if NameNode has an object it must be a binding
        else {
            if (myContext == null)
                myContext = new IvmContext(this);
            return myContext;
        }
    }

    public Object resolve(ParsedName name) throws javax.naming.NameNotFoundException {
        int compareResult = name.compareTo(atomicHash);

        if (compareResult == ParsedName.IS_EQUAL && name.getComponent().equals(atomicName)) {// hashcodes and String valuse are equal
            if (name.next()) {
                if (subTree == null) throw new javax.naming.NameNotFoundException("Can not resolve " + name);
                return subTree.resolve(name);
            } else
                return getBinding();
        } else if (compareResult == ParsedName.IS_LESS) {// parsed hash is less than
            if (lessTree == null) throw new javax.naming.NameNotFoundException("Can not resolve " + name);
            return lessTree.resolve(name);

        } else {//ParsedName.IS_GREATER

            if (grtrTree == null) throw new javax.naming.NameNotFoundException("Can not resolve " + name);
            return grtrTree.resolve(name);
        }
    }

    public void bind(ParsedName name, Object obj) throws javax.naming.NameAlreadyBoundException {
        int compareResult = name.compareTo(atomicHash);
        if (compareResult == ParsedName.IS_EQUAL && name.getComponent().equals(atomicName)) {
            if (name.next()) {
                if (myObject != null) {
                    throw new javax.naming.NameAlreadyBoundException();
                }
                if (subTree == null)
                    subTree = new NameNode(this, name, obj);
                else
                    subTree.bind(name, obj);
            } else {
                if (subTree != null) {
                    throw new javax.naming.NameAlreadyBoundException();
                }
                myObject = obj;// bind the object to this node
            }
        } else if (compareResult == ParsedName.IS_LESS) {
            if (lessTree == null)
                lessTree = new NameNode(this.parent, name, obj);
            else
                lessTree.bind(name, obj);
        } else {//ParsedName.IS_GREATER ...

            if (grtrTree == null)
                grtrTree = new NameNode(this.parent, name, obj);
            else
                grtrTree.bind(name, obj);
        }
    }

    public IvmContext createSubcontext(ParsedName name) throws javax.naming.NameAlreadyBoundException {
        try {
            bind(name, null);
            name.reset();
            return (IvmContext) resolve(name);
        }
        catch (javax.naming.NameNotFoundException exception) {
            exception.printStackTrace();
            throw new RuntimeException();
        }
    }
}
