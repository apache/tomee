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

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;

public class NameNode implements Serializable {
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
    private boolean subTreeUnbound;

    public NameNode(final NameNode parent, final ParsedName name, final Object obj, final NameNode parentTree) {
        atomicName = name.getComponent();
        atomicHash = name.getComponentHashCode();
        this.parent = parent;
        this.parentTree = parentTree;
        if (name.next()) {
            subTree = new NameNode(this, name, obj, this);
        } else if (obj instanceof Context) {
            myObject = new Federation();
            ((Federation) myObject).add((Context) obj);
        } else {
            myObject = obj;
        }
    }

    void setMyContext(final IvmContext myContext) {
        this.myContext = myContext;
    }

    //TODO: probably can be removed, doesn't seem to be used anywhere
    public Object getBinding() {
        return getBinding(false);
    }
    
    public Object getBinding(boolean createReadOnlyContext) {
        if (myObject != null && !(myObject instanceof Federation)) {
            return myObject;// if NameNode has an object it must be a binding
        } else {
            if (myContext == null) {
                myContext = new IvmContext(this, createReadOnlyContext);
            }
            return myContext;
        }
    }

    public Object getObject() {
        return myObject;
    }
    
    //TODO: probably can be removed, doesn't seem to be used anywhere
    public Object resolve(final ParsedName name) throws NameNotFoundException {
        return resolve(name, false);
    }

    public Object resolve(final ParsedName name, boolean createReadOnlyContext) throws NameNotFoundException {
        final int compareResult = name.compareTo(atomicHash);
        NameNotFoundException n = null;
        final int pos = name.getPos();
        if (compareResult == ParsedName.IS_EQUAL && name.getComponent().equals(atomicName)) {
            // hashcodes and String values are equal
            if (name.next()) {
                if (subTree != null) {
                    try {
                        return subTree.resolve(name, createReadOnlyContext);
                    } catch (final NameNotFoundException e) {
                        n = e;
                    }
                } else if (!subTreeUnbound && !unbound && myContext != null && !Federation.class.isInstance(myObject)) {
                    try {
                        return myContext.mynode.resolve(name, createReadOnlyContext);
                    } catch (final NameNotFoundException e) {
                        n = e;
                    }
                }
            } else if (!unbound) {
                return getBinding(createReadOnlyContext);
            }
        } else if (compareResult == ParsedName.IS_LESS) {
            // parsed hash is less than
            if (lessTree != null) {
                return lessTree.resolve(name, createReadOnlyContext);
            }

        } else {
            //ParsedName.IS_GREATER
            if (grtrTree != null) {
                return grtrTree.resolve(name,createReadOnlyContext);
            }
        }
        if (myObject instanceof Federation) {
            name.reset(pos);
            final String nameInContext = compareResult != ParsedName.IS_EQUAL ? name.path() : name.remaining().path();
            Federation f = null;
            for (final Context c : (Federation) myObject) {
                try {
                    final Object o = c.lookup(nameInContext);
                    if (o instanceof Context) {
                        if (f == null) {
                            f = new Federation();
                        }
                        f.add((Context) o);
                    } else {
                        return o;
                    }
                } catch (final NamingException e) {
                    //ignore
                }
            }
            if (f != null) {
                final NameNode node = new NameNode(null, new ParsedName(""), f, null);
                return new IvmContext(node, createReadOnlyContext);
            }
        }
        if (n != null) {
            throw n;
        }
        throw new NameNotFoundException("Cannot resolve " + name);
    }

    public void bind(final ParsedName name, final Object obj) throws NameAlreadyBoundException {
        final int compareResult = name.compareTo(atomicHash);
        if (compareResult == ParsedName.IS_EQUAL && name.getComponent().equals(atomicName)) {
            if (name.next()) {
                if (myObject != null && !(myObject instanceof Federation)) {
                    throw new NameAlreadyBoundException();
                }
                if (subTree == null) {
                    subTree = new NameNode(this, name, obj, this);
                    subTreeUnbound = false;
                } else {
                    subTree.bind(name, obj);
                }
            } else {
                if (obj instanceof Context) {
                    if (myObject != null) {
                        if (!(myObject instanceof Federation)) {
                            throw new NameAlreadyBoundException(name.toString());
                        }
                    } else {
                        myObject = new Federation();
                    }
                    ((Federation) myObject).add((Context) obj);
                } else {
                    if (subTree != null) {
                        throw new NameAlreadyBoundException(name.toString());
                    }
                    if (myObject != null) {
                        throw new NameAlreadyBoundException(name.toString());
                    }
                    unbound = false;
                    myObject = obj;// bind the object to this node
                }
            }
        } else if (compareResult == ParsedName.IS_LESS) {
            if (lessTree == null) {
                lessTree = new NameNode(this.parent, name, obj, this);
            } else {
                lessTree.bind(name, obj);
            }
        } else {
            //ParsedName.IS_GREATER ...
            if (grtrTree == null) {
                grtrTree = new NameNode(this.parent, name, obj, this);
            } else {
                grtrTree.bind(name, obj);
            }
        }
    }

    public void tree(final String indent, final PrintStream out) {
        out.println(atomicName + " @ " + atomicHash + (myObject != null ? " [" + myObject + "]" : ""));

        if (grtrTree != null) {
            out.print(indent + " + ");
            grtrTree.tree(indent + "    ", out);
        }
        if (lessTree != null) {
            out.print(indent + " - ");
            lessTree.tree(indent + "    ", out);
        }
        if (subTree != null) {
            out.print(indent + " - ");
            subTree.tree(indent + "    ", out);
        }
    }


    public int compareTo(final int otherHash) {
        return Integer.compare(atomicHash, otherHash);
    }

    private void bind(final NameNode node) {
        int compareResult = node.compareTo(atomicHash);

        // This seems to be needed because of an inbalanced way
        // in which we use bind/unbind/lookup.  Bind/unbind require
        // the prefix of the node to be appended because lookup
        // will add the prefix when doing a lookup.  Seems if
        // we got rid of the prefix usage in lookup, we could
        // kill this if block and would have more balanced
        // btrees
        if (node.parent == this) {
            compareResult = ParsedName.IS_EQUAL;
        }

        if (compareResult == ParsedName.IS_EQUAL) {
            if (subTree == null) {
                subTree = node;
                subTreeUnbound = false;
                subTree.parentTree = this;
            } else {
                subTree.bind(node);
            }
        } else if (compareResult == ParsedName.IS_LESS) {
            if (lessTree == null) {
                lessTree = node;
                lessTree.parentTree = this;
            } else {
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

    public void unbind(final ParsedName name) throws NameAlreadyBoundException {
        final int compareResult = name.compareTo(atomicHash);
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

            if (grtrTree != null) {
                grtrTree.unbind(name);
            }
        }
    }

    private void unbind(final NameNode node) {
        if (subTree == node) {
            subTree = null;
            subTreeUnbound = true;
        } else if (grtrTree == node) {
            grtrTree = null;
        } else if (lessTree == node) {
            lessTree = null;
        }
        rebalance(node);
    }
    
    void setReadOnly(boolean isReadOnly) { 
        if(myContext != null) {
            myContext.readOnly = isReadOnly;
        }
        
        if(myObject instanceof Federation) {
            for (Context current : ((Federation) myObject)) {
                if (IvmContext.class.isInstance(current)) {
                    IvmContext.class.cast(current).setReadOnly(isReadOnly);
                }
            }
        }
        if (subTree != null) {
            subTree.setReadOnly(isReadOnly);;
        }
        if (lessTree != null) {
            lessTree.setReadOnly(isReadOnly);
        }
        if (grtrTree != null) {
            grtrTree.setReadOnly(isReadOnly);
        }
    }

    private void rebalance(final NameNode node) {
        if (node.subTree != null) {
            this.bind(node.subTree);
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

    private void prune(final NameNode until) {
        if (subTree != null) {
            subTree.prune(until);
        }
        if (lessTree != null) {
            lessTree.prune(until);
        }
        if (grtrTree != null) {
            grtrTree.prune(until);
        }

        if (this == until) {
            return;
        }

        if (!hasChildren() && myObject == null) {
            parentTree.unbind(this);
        }
    }

    private boolean hasChildren() {
        return hasChildren(this);
    }

    private boolean hasChildren(final NameNode node) {
        if (subTree != null && subTree.hasChildren(node)) {
            return true;
        }
        if (grtrTree != null && grtrTree.hasChildren(node)) {
            return true;
        }
        if (lessTree != null && lessTree.hasChildren(node)) {
            return true;
        }

        return parent == node;
    }

    protected void clearCache() {
        if (myContext != null) {
            myContext.fastCache.clear();
        }
        if (grtrTree != null) {
            grtrTree.clearCache();
        }
        if (lessTree != null) {
            lessTree.clearCache();
        }
        if (subTree != null) {
            subTree.clearCache();
        }
    }
    
    //TODO: probably can be removed, doesn't seem to be used anywhere
    public IvmContext createSubcontext(final ParsedName name) throws NameAlreadyBoundException {
        return createSubcontext(name, false);
    }

    public IvmContext createSubcontext(final ParsedName name, final boolean createReadOnlyContext) throws NameAlreadyBoundException {
        try {
            bind(name, null);
            name.reset();
            return (IvmContext) resolve(name, createReadOnlyContext);
        } catch (final NameNotFoundException exception) {
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

    public NameNode getParentTree() {
        return parentTree;
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

    public static class Federation extends ArrayList<Context> {
    }
}
