/*
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

import org.apache.openejb.OpenEJBRuntimeException;

import javax.naming.*;
import java.io.PrintStream;
import java.util.ArrayList;

public class NameNode implements java.io.Serializable {
    private final String atomicName;
    private final int atomicHash;
    private NameNode lessTree;
    private NameNode grtrTree;
    private NameNode subTree;
    private NameNode parentTree;
    private final NameNode parent;
    private Object myObject;
    private transient IvmContext myContext;
    private boolean unbound;
    public NameNode(NameNode parent, ParsedName name, Object obj, NameNode parentTree) {
        atomicName = name.getComponent();
        atomicHash = name.getComponentHashCode();
        this.parent = parent;
        this.parentTree = parentTree;
        if (name.next())
            subTree = new NameNode(this, name, obj, this);
        else if (obj instanceof Context) {
            myObject = new Federation();
            ((Federation)myObject).add((Context) obj);
        } else {
            myObject = obj;
        }
    }

    void setMyContext(IvmContext myContext) {
        this.myContext = myContext;
    }

    public Object getBinding() {
        if (myObject != null && !(myObject instanceof Federation))
            return myObject;// if NameNode has an object it must be a binding
        else {
            if (myContext == null)
                myContext = new IvmContext(this);
            return myContext;
        }
    }

    public Object resolve(ParsedName name) throws javax.naming.NameNotFoundException {
        int compareResult = name.compareTo(atomicHash);
        javax.naming.NameNotFoundException n = null;
        int pos = name.getPos();
        if (compareResult == ParsedName.IS_EQUAL && name.getComponent().equals(atomicName)) {
            // hashcodes and String valuse are equal
            if (name.next()) {
                if (subTree != null) {
                    try {
                        return subTree.resolve(name);
                    } catch (NameNotFoundException e) {
                        n = e;
                    }
                }
            } else if (!unbound){
                return getBinding();
            }
        } else if (compareResult == ParsedName.IS_LESS) {
            // parsed hash is less than
            if (lessTree != null) {
                return lessTree.resolve(name);
            }

        } else {
            //ParsedName.IS_GREATER
            if (grtrTree != null) {
                return grtrTree.resolve(name);
            }
        }
        if (myObject instanceof Federation) {
            name.reset(pos);
            String nameInContext = name.remaining().path();
            Federation f = null;
            for (Context c: (Federation)myObject) {
                try {
                    Object o = c.lookup(nameInContext);
                    if (o instanceof Context) {
                        if (f == null) {
                            f = new Federation();
                        }
                        f.add((Context) o);
                    } else {
                        return o;
                    }
                } catch (javax.naming.NamingException e) {
                    //ignore
                }
            }
            if (f != null) {
                NameNode node = new NameNode(null, new ParsedName(""), f, null);
                return new IvmContext(node);
            }
        }
        if (n != null) throw n;
        throw new javax.naming.NameNotFoundException("Cannot resolve " + name);
    }

    public void bind(ParsedName name, Object obj) throws javax.naming.NameAlreadyBoundException {
        int compareResult = name.compareTo(atomicHash);
        if (compareResult == ParsedName.IS_EQUAL && name.getComponent().equals(atomicName)) {
            if (name.next()) {
                if (myObject != null && !(myObject instanceof Federation)) {
                    throw new javax.naming.NameAlreadyBoundException();
                }
                if (subTree == null)
                    subTree = new NameNode(this, name, obj, this);
                else
                    subTree.bind(name, obj);
            } else {
                if (obj instanceof Context) {
                    if (myObject != null) {
                        if (!(myObject instanceof Federation)) {
                            throw new javax.naming.NameAlreadyBoundException(name.toString());
                        }
                    } else {
                        myObject = new Federation();
                    }
                    ((Federation)myObject).add((Context) obj);
                } else {
                    if (subTree != null) {
                        throw new javax.naming.NameAlreadyBoundException(name.toString());
                    }
                    if (myObject != null) {
                        throw new javax.naming.NameAlreadyBoundException(name.toString());
                    }
                    unbound = false;
                    myObject = obj;// bind the object to this node
                }
            }
        } else if (compareResult == ParsedName.IS_LESS) {
            if (lessTree == null)
                lessTree = new NameNode(this.parent, name, obj, this);
            else
                lessTree.bind(name, obj);
        } else {
            //ParsedName.IS_GREATER ...
            if (grtrTree == null)
                grtrTree = new NameNode(this.parent, name, obj, this);
            else
                grtrTree.bind(name, obj);
        }
    }

    public void tree(String indent, PrintStream out){
        out.println(atomicName + " @ " + atomicHash + (myObject != null ? " [" + myObject + "]" : ""));

        if (grtrTree != null) {
            out.print(indent +" + ");
            grtrTree.tree(indent + "    ", out);
        }
        if (lessTree != null) {
            out.print(indent +" - ");
            lessTree.tree(indent + "    ", out);
        }
        if (subTree != null) {
            out.print(indent +" - ");
            subTree.tree(indent + "    ", out);
        }
    }


    public int compareTo(int otherHash) {
        if (atomicHash == otherHash)
            return 0;
        else if (atomicHash > otherHash)
            return 1;
        else
            return -1;
    }

    private void bind(NameNode node) {
        int compareResult = node.compareTo(atomicHash);

        // This seems to be needed because of an inbalanced way
        // in which we use bind/unbind/lookup.  Bind/unbind require
        // the prefix of the node to be appended because lookup
        // will add the prefix when doing a lookup.  Seems if
        // we got rid of the prefix usage in lookup, we could
        // kill this if block and would have more balanced
        // btrees
        if (node.parent == this){
            compareResult = ParsedName.IS_EQUAL;
        }

        if (compareResult == ParsedName.IS_EQUAL) {
            if (subTree == null){
                subTree = node;
                subTree.parentTree = this;
            } else {
                subTree.bind(node);
            }
        } else if (compareResult == ParsedName.IS_LESS) {
            if (lessTree == null) {
                lessTree = node;
                lessTree.parentTree = this;
            }
            else{
                lessTree.bind(node);
            }
        } else {//ParsedName.IS_GREATER ...

            if (grtrTree == null) {
                grtrTree = node;
                grtrTree.parentTree = this;
            } else {
                grtrTree.bind(node);
            }
        }
    }

    public void unbind(ParsedName name) throws javax.naming.NameAlreadyBoundException {
        int compareResult = name.compareTo(atomicHash);
        if (compareResult == ParsedName.IS_EQUAL && name.getComponent().equals(atomicName)) {
            if (name.next()) {
                if (subTree != null) {
                    subTree.unbind(name);
                }
            } else {
                unbound = true;
                myObject = null;
                parentTree.unbind(this);
            }
        } else if (compareResult == ParsedName.IS_LESS) {
            if (lessTree != null) {
                lessTree.unbind(name);
            }
        } else {//ParsedName.IS_GREATER ...

            if (grtrTree != null)
                grtrTree.unbind(name);
        }
    }

    private void unbind(NameNode node) {
        if (subTree == node) {
            subTree = null;
        } else if (grtrTree == node){
            grtrTree = null;
        } else if (lessTree == node){
            lessTree = null;
        }
        rebalance(this, node);
    }

    private void rebalance(NameNode tree, NameNode node) {
        if (node.subTree != null) {
            tree.bind(node.subTree);
        }
        if (node.lessTree != null) {
            this.bind(node.lessTree);
        }
        if (node.grtrTree != null) {
            this.bind(node.grtrTree);
        }
    }

    protected void prune() {
        prune(this);
    }

    private void prune(NameNode until) {
        if (subTree != null) {
            subTree.prune(until);
        }
        if (lessTree != null) {
            lessTree.prune(until);
        }
        if (grtrTree != null) {
            grtrTree.prune(until);
        }

        if (this == until) return;

        if (!hasChildren() && myObject == null){
            parentTree.unbind(this);
        }
    }

    private boolean hasChildren() {
        return hasChildren(this);
    }

    private boolean hasChildren(NameNode node) {
        if (subTree != null && subTree.hasChildren(node)) return true;
        if (grtrTree != null && grtrTree.hasChildren(node)) return true;
        if (lessTree != null && lessTree.hasChildren(node)) return true;

        return (parent == node);
    }

    protected void clearCache() {
        if (myContext != null) {
            myContext.fastCache.clear();
        }
        if (grtrTree != null) {
            grtrTree.clearCache();
        }
        if (lessTree != null){
            lessTree.clearCache();
        }
        if (subTree != null){
            subTree.clearCache();
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
            throw new OpenEJBRuntimeException(exception);
        }
    }

    public String getAtomicName() {
        return atomicName;
    }

    public NameNode getLessTree() {
        return lessTree;
    }

    public NameNode getGrtrTree() {
        return grtrTree;
    }

    public NameNode getSubTree() {
        return subTree;
    }

    public NameNode getParent() {
        return parent;
    }


    @Override
    public String toString() {
        return "NameNode{" +
                "atomicName='" + atomicName + '\'' +
                ", atomicHash=" + atomicHash +
                ", lessTree=" + (lessTree != null ? lessTree.atomicName : "null") +
                ", grtrTree=" + (grtrTree != null ? grtrTree.atomicName : "null") +
                ", subTree=" + (subTree != null ? subTree.atomicName : "null") +
                ", parentTree=" + (parentTree != null ? parentTree.atomicName : "null") +
                ", parent=" + (parent != null ? parent.atomicName : "null") +
                ", myObject=" + myObject +
                ", myContext=" + myContext +
                ", unbound=" + unbound +
                '}';
    }

    private static class Federation extends ArrayList<Context> {};
}
