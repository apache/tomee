/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.kernel;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes the shape of a query result.
 * <br>
 * A shape is described as a Java class by the generic type argument T. A shape may contain zero or more shapes. 
 * A shape is categorized as follows: 
 * <LI>A <em>primitive</em> shape can not have child shapes e.g. Foo or float. 
 * <LI>A <em>compound</em> shape has zero or more child shapes e.g. Foo{} or Foo{String, int} or 
 * Foo{String,Bar{Double},int}. 
 * <LI>A <em>nesting</em> shape has one or more compound child shape(s).
 * For example,  Foo{String,Bar{Double},int}. On the other hand, Foo{String, int} is a compound shape but is not
 * nesting because all its child shapes are primitive.
 * <br>
 * A primitive category shape is declared during construction and immutable.
 * The category of a non-primitive shape is mutable. 
 * <br>
 * Notice that all nested shapes are compound shapes but not all compound shapes are nesting.
 * <br>
 * A compound shape can <em>add</em> other primitive shapes or <em>nest</em> other shapes to any arbitrary depth.
 * However, a shape does not allow recursive nesting of shapes.
 * <br> 
 * <B>Usage</B>: 
 * The purpose of a shape is to populate an instance of T from an array of input values where each
 * array element is further specified with a type and an alias. FillStrategy determines how a shape 
 * populates an instance of T by consuming the input array element values.
 * The input data is presented as an Object[] with a parallel array of types because the primitive
 * types (short, float etc.) are not preserved in the input array. For certain FillStrategy such as
 * MAP or BEAN, the alias of the input array element are used to identify the Map key or setter
 * methods respectively. 
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 *
 */
@SuppressWarnings("serial")
public class ResultShape<T> implements Serializable {
    private final Class<T> cls;        // the type of value this shape represents or populates
    private final boolean isPrimitive; // flags this shape as primitive
    private boolean isNesting;         // flags this shape as nesting
    private String alias;
    
    private final FillStrategy<T> strategy;   // the strategy to populate this shape
    private final List<ResultShape<?>> children; // children of this shape. null for primitives
    private Set<ResultShape<?>> parents;   // the shapes that have nested this shape

    /**
     * Construct a non-primitive shape with ASSIGN or ARRAY fill strategy. 
     */
    public ResultShape(Class<T> cls) {
        this(cls, false);
    }
    
    /**
     * Construct a primitive or non-primitive shape with ASSIGN or ARRAY fill strategy. 
     * If the shape is declared as primitive then the given class can not be an array.
     */
    public ResultShape(Class<T> cls, boolean primitive) {
        this(cls, cls.isArray() ? new FillStrategy.Array<T>(cls) : new FillStrategy.Assign<T>(), primitive);
        if (cls.isArray() && primitive)
            throw new IllegalArgumentException(cls.getSimpleName() + " can not be primitive shape");
    }
    
    /**
     * 
     * Construct a non-primitive shape with the given fill strategy. 
     */
    public ResultShape(Class<T> cls, FillStrategy<T> strategy) {
        this(cls, strategy, false);
    }
    
    /**
     * Construct a shape with the given fill strategy. 
     * 
     */
    public ResultShape(Class<T> cls, FillStrategy<T> strategy, boolean primitive) {
        if (cls == null) throw new NullPointerException();
        this.cls = cls;
        this.strategy = strategy;
        isPrimitive = primitive;
        children = isPrimitive ? null : new ArrayList<ResultShape<?>>();
    }
    
//    /**
//     * Construct a shape with the MAP fill strategy to invoke the given method. 
//     * 
//     */
//    public ResultShape(Class<T> cls, Method putMethod) {
//        if (cls == null) throw new NullPointerException();
//        this.cls = cls;
//        this.strategy = new FillStrategy.Map<T>(putMethod);
//        isPrimitive = true;
//        children = new ArrayList<ResultShape<?>>();
//    }
//
//    /**
//     * Construct a shape with the CONSTRUCTOR fill strategy to invoke the given constructor. 
//     * 
//     */
//    public ResultShape(Class<T> cls, Constructor<? extends T> cons) {
//        if (cls == null) throw new NullPointerException();
//        this.cls = cls;
//        this.strategy = new FillStrategy.NewInstance<T>(cons);
//        isPrimitive = false;
//        children = new ArrayList<ResultShape<?>>();
//    }
    
    /**
     * Gets the type of instance populated by this shape.
     */
    public Class<T> getType() {
        return cls;
    }
    
    public FillStrategy<T> getStrategy() {
        return strategy;
    }
    
    public ResultShape<T> setAlias(String alias) {
        this.alias = alias;
        return this;
    }
    
    public String getAlias() {
        return alias;
    }
    
    /**
     * Gets the list of classes to compose this shape and all its children.
     * For example, a shape Foo{String,Bar{int, Date}, Double} will return
     * {String, int, Date, Double}
     */
    public List<Class<?>> getCompositeTypes() {
        List<Class<?>> result = new ArrayList<Class<?>>();
        if (isPrimitive() || children.isEmpty()) {
            result.add(cls);
        } else {
            for (ResultShape<?> child : children) {
                result.addAll(child.getCompositeTypes());
            }
        }
        return result;
    }
    
    /**
     * Gets the list of classes to compose this shape only i.e. without expanding the children's shape.
     * For example, a shape Foo{String,Bar{int, Date}, Double} will return {String, Bar, Double}
     */
    public List<Class<?>> getTypes() {
        List<Class<?>> result = new ArrayList<Class<?>>();
        if (children.isEmpty()) {
            result.add(cls);
        } else {
            for (ResultShape<?> child : children) {
                result.add(child.getType());
            }
        }
        return result;
    }
    
    /**
     * Creates a new shape of type X with the given class arguments and nests
     * the new shape within this shape. 
     * 
     * @return newly created nested shape
     */
    public <X> ResultShape<X> nest(Class<X> cls, FillStrategy strategy, Class<?>... classes) {
        assertNotPrimitive();
        ResultShape<X> child = new ResultShape<X>(cls, strategy, true);
        this.nest(child.add(classes));
        return child;
    }

    /**
     * Nest the given shape. 
     * 
     * @param shape The given shape can not be a parent of this shape
     * to prohibit recursive nesting.
     * 
     * @return this shape itself
     */
    public ResultShape<T> nest(ResultShape<?> shape) {
        assertNotPrimitive();
        if (shape.isParent(this))
            throw new IllegalArgumentException(this + " can not nest recursive " + shape);
        children.add(shape);
        shape.addParent(this);
        isNesting |= !shape.isPrimitive(); 
        return this;
    }
    
    /**
     * Adds the given shape as one of the parents of this shape.
     * 
     */
    private void addParent(ResultShape<?> p) {
        if (parents == null)
            parents = new HashSet<ResultShape<?>>();
        parents.add(p);
    }
    
    /**
     * Adds the given classes as child shapes of this shape.
     * The child shapes are primitive shapes.
     */
    public ResultShape<T> add(Class<?>... classes) {
        assertNotPrimitive();
        for (Class<?> c : classes) {
            children.add(new ResultShape(c, true));
        }
        return this;
    }
    
    /**
     * Gets all the child shapes.
     */
    public List<ResultShape<?>> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    /**
     * Affirms if this shape can have child shapes.
     */
    public boolean isCompound() {
        return !isPrimitive;
    }
    
    /**
     * Affirms if this shape can not have any child shape.
     * A primitive shape uses ASSIGN strategy.
     */
    public boolean isPrimitive() {
        return isPrimitive;
    }
    
    /**
     * Affirms if at least one child shape of this shape is a compound shape.
     */
    public boolean isNesting() {
        return isNesting;
    }
    
    /**
     * Affirms if this shape is nested within other shapes.
     */
    public boolean isNested() {
        return parents != null;
    }
    
    /**
     * Affirms if the given shape is a parent (or grandparent) of this shape.
     */
    public boolean isParent(ResultShape<?> p) {
        if (p.getParents().contains(this))
            return true;
        if (children != null) {
            for (ResultShape<?> child : children) {
                if (child.isParent(p))
                    return true;
            }
        }
        return false;
    }
    
    void assertNotPrimitive() {
        if (isPrimitive)
            throw new UnsupportedOperationException("Can not add/nest shape to primitive shape " + this);
    }
    
    /**
     * Gets the immediate parents of this shape.
     */
    public Set<ResultShape<?>> getParents() {
        return parents == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(parents);
    }
    
    /**
     * Total number of arguments required to populate the shape and all its child shapes.
     */
    public int argLength() {
        if (isPrimitive() || children.isEmpty())
            return 1;
        int l = 0;
        for (ResultShape<?> child : children) {
            l += child.argLength();
        }
        return l;
    }
    
    /**
     * Number of arguments to populate this shape only.
     */
    public int length() {
        if (isPrimitive() || children.isEmpty())
            return 1;
        return children.size();
    }
    
    // ======================================================================================
    // Data Population Routines
    // ======================================================================================

    /**
     * Fill this shape and its children with the given array element values.
     * The parallel arrays contain the actual values, the types of these values and aliases.
     * The type and alias information are used for packing Map or invoking constructor. 
     * The type can be different from what can be determined from array elements because
     * of boxing of primitive types.
     * The actual constructor argument types are sourced from types[] array.
     */
    public T pack(Object[] values, Class<?>[] types, String[] aliases) {
        if (values.length < argLength()) // input can be longer than required
            throw new IndexOutOfBoundsException(values.length + " values are less than " + 
                    argLength() + " argumenets required to pack " + this); 
        Object[] args = new Object[length()];
        Class<?>[] argTypes = new Class[length()];
        String[] argAliases = new String[length()];
        if (isPrimitive() || children.isEmpty()) {
            args[0] = values[0];
            argTypes[0] = types[0];
            argAliases[0] = aliases[0];
        } else { // pack each children
            int start = 0;
            int i = 0;
            for (ResultShape<?> rs : children) {
                int finish = start + rs.argLength();
                args[i] = rs.pack(chop(values, start, finish), chop(types, start, finish), 
                        chop(aliases, start, finish));
                argTypes[i] = rs.getType();
                argAliases[0] = rs.getAlias();
                start = finish;
                i++;
            }
        }
        return (T)strategy.fill(args, argTypes, argAliases);
    }

    /**
     * Chop an array from start to finish.
     */
    <X> X[] chop(X[] values, int start, int finish) {
        X[] result = (X[])Array.newInstance(values.getClass().getComponentType(), finish-start);
        System.arraycopy(values, start, result, 0, finish-start);
        return result;
    }
    /**
     * Gets a human-readable representation of this shape.
     * 
     */
    public String toString() {
        StringBuilder buf = new StringBuilder(cls.getSimpleName());
        if (isPrimitive() || children.isEmpty())
            return buf.toString();
        int i = 0;
        for (ResultShape<?> child : children) {
            buf.append(i++ == 0 ? "{" : ", ");
            buf.append(child);
        }
        if (!children.isEmpty())
            buf.append("}");
        return buf.toString();
    }
}
