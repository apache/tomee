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
package org.apache.openjpa.persistence.criteria;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.TupleElement;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

import org.apache.openjpa.kernel.FillStrategy;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.persistence.TupleFactory;
import org.apache.openjpa.persistence.TupleImpl;

/**
 * Implements selection terms that are composed of other selection terms.
 *  
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 *
 */
class CompoundSelections {
    private static Localizer _loc = Localizer.forPackage(CompoundSelections.class);
    /**
     * Gets the strategy to fill a given compound selection.
     * 
     */
    static <X> FillStrategy<X> getFillStrategy(Selection<X> s) {
        if (s instanceof CompoundSelectionImpl) {
            return ((CompoundSelectionImpl<X>)s).getFillStrategy();
        } else {
            return new FillStrategy.Assign<X>();
        }
    }
    
    /**
     * Abstract implementation of a selection term composed of multiple selection terms.
     *
     */
    private abstract static class CompoundSelectionImpl<X> extends SelectionImpl<X> implements CompoundSelection<X> {
        private final List<Selection<?>> _args;
        
        public CompoundSelectionImpl(Class<X> cls, Selection<?>...args) {
            super(cls);
//            assertNoCompoundSelection(args);
            _args = args == null ? (List<Selection<?>>)Collections.EMPTY_LIST : Arrays.asList(args);
        }
        
        public final boolean isCompoundSelection() {
            return true;
        }
        
        /**
         * Return selection items composing a compound selection
         * @return list of selection items
         * @throws IllegalStateException if selection is not a compound
         *           selection
         */
        public final List<Selection<?>> getCompoundSelectionItems() {
            return Expressions.returnCopy(_args);
        }
        
        void assertNoCompoundSelection(Selection<?>...args) {
            if (args == null)
                return;
            for (Selection<?> s : args) {
                if (s.isCompoundSelection() && !(s.getClass() == NewInstance.class)) {
                    throw new IllegalArgumentException("compound selection " + s + " can not be nested in " + this);
                }
            }
        }

        abstract FillStrategy<X> getFillStrategy();
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < _args.size(); i++) {
                buffer.append((((CriteriaExpression)_args.get(i)).asValue(q)));
                if (i+1 != _args.size())
                    buffer.append(", ");
            }
            return buffer;
        }
        
        @Override
        public StringBuilder asProjection(AliasContext q) {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < _args.size(); i++) {
                buffer.append((((CriteriaExpression)_args.get(i)).asProjection(q)));
                if (i+1 != _args.size())
                    buffer.append(", ");
            }
            return buffer;
        }
    }
    
    /**
     * A compound selection which is an array of its component terms.
     *
     * @param <X> type must be an array
     */
    static class Array<X> extends CompoundSelectionImpl<X> {
        public Array(Class<X> cls, Selection<?>... terms) {
            super(cls, terms);
            if (!cls.isArray()) {
                throw new IllegalArgumentException(cls + " is not an array. " + this + " needs an array");
            }
        }
        
        public FillStrategy<X> getFillStrategy() {
            return new FillStrategy.Array<X>(getJavaType());
        }
    }
    
    /**
     * A compound selection which is an instance constructed of its component terms.
     *
     * @param <X> type of the constructed instance
     */
    static class NewInstance<X> extends CompoundSelectionImpl<X> {
        private FillStrategy.NewInstance<X> strategy;
        public NewInstance(Class<X> cls, Selection<?>... selections) {
            super(cls, selections);
            strategy = new FillStrategy.NewInstance<X>(findConstructor(cls, selections));
        }
        
        public FillStrategy<X> getFillStrategy() {
            return strategy;
        }
        
        private Constructor<X> findConstructor(Class<X> cls, Selection<?>... selections) {
            Class<?>[] types = selections == null ? null : new Class[selections.length];
            if (selections != null) {
                for (int i = 0; i < selections.length; i++) {
                    types[i] = selections[i].getJavaType();
                }
            }
            try {
                return cls.getConstructor(types);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(_loc.get("select-no-ctor", cls, 
                    types == null ? "[]" : Arrays.toString(types)).getMessage());
            }
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return new StringBuilder("NEW ").append(getJavaType().getName()).append("(")
               .append(super.asValue(q)).append(")");
        }
    }
    
    /**
     * A compound selection which is a Tuple composed of its component terms.
     *
     */
    static class Tuple extends CompoundSelectionImpl<javax.persistence.Tuple> {
        public Tuple(final Selection<?>[] selections) {
            super(javax.persistence.Tuple.class, selections);
        }
        
        public FillStrategy<javax.persistence.Tuple> getFillStrategy() {
            List<Selection<?>> terms = getCompoundSelectionItems();
            TupleFactory factory = new TupleFactory(terms.toArray(new TupleElement[terms.size()]));
            return new FillStrategy.Factory<javax.persistence.Tuple>(factory, TupleImpl.PUT);
        }
    }

    /**
     * A selection of terms that interprets its arguments based on target result type.
     *
     * @param <T> the target result type.
     */
    static class MultiSelection<T> extends CompoundSelectionImpl<T> {
        public MultiSelection(Class<T> result, final Selection<?>[] selections) {
            super(result, selections);
        }
        
        public FillStrategy<T> getFillStrategy() {
            Class<?> resultClass = getJavaType();
            List<Selection<?>> terms = getCompoundSelectionItems();
            FillStrategy<?> strategy = null;
            if (javax.persistence.Tuple.class.isAssignableFrom(resultClass)) {
                TupleFactory factory = new TupleFactory(terms.toArray(new TupleElement[terms.size()]));
                strategy = new FillStrategy.Factory<javax.persistence.Tuple>(factory,  TupleImpl.PUT);
           } else if (resultClass == Object.class) {
               if (terms.size() > 1) { 
                   resultClass = Object[].class;
                   strategy = new FillStrategy.Array<Object[]>(Object[].class);
               } else {
                   strategy = new FillStrategy.Assign();
               }
           } else {
               strategy = resultClass.isArray() 
                        ? new FillStrategy.Array(resultClass) 
                        : new FillStrategy.NewInstance(resultClass);
           } 
            return (FillStrategy<T>)strategy;
        }
    }
}
