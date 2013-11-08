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
package org.apache.openjpa.jdbc.kernel.exps;

import java.util.Map;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Tests whether the given path is an instance of the given class.
 *
 * @author Abe White
 */
class InstanceofExpression
    implements Exp {

    private final PCPath _path;
    private final Class _cls;

    /**
     * Constructor. Supply path and class to test for.
     */
    public InstanceofExpression(PCPath path, Class cls) {
        _path = path;
        _cls = cls;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        // note that we tell the path to go ahead and join to its related
        // object (if any) in order to access its class indicator
        ExpState pathState = _path.initialize(sel, ctx, Val.JOIN_REL);

        // does this path represent a relation?  if not, what class
        // is the field?
        ClassMapping relMapping = _path.getClassMapping(pathState);
        Class rel = null;
        if (relMapping == null) {
            FieldMapping field = _path.getFieldMapping(pathState);
            switch (field.getTypeCode()) {
                case JavaTypes.MAP:
                    if (_path.isKey())
                        rel = field.getKey().getDeclaredType();
                    // no break
                case JavaTypes.ARRAY:
                case JavaTypes.COLLECTION:
                    rel = field.getElement().getDeclaredType();
                    break;
                default:
                    rel = field.getDeclaredType();
            }
        } else
            rel = relMapping.getDescribedType();

        // if the path represents a relation, get its class indicator and
        // make sure it's joined down to its base type
        Discriminator discrim = (relMapping == null 
            || !relMapping.getDescribedType().isAssignableFrom(_cls)) 
            ? null : relMapping.getDiscriminator();
        ClassMapping mapping = null;
        Joins joins = pathState.joins;
        if (discrim != null) {
            // cache mapping for cast
            MappingRepository repos = ctx.store.getConfiguration().
                getMappingRepositoryInstance();
            mapping = repos.getMapping(_cls, ctx.store.getContext().
                getClassLoader(), false);

            // if not looking for a PC, don't bother with indicator
            if (mapping == null || !discrim.hasClassConditions(mapping, true))
                discrim = null;
            else {
                ClassMapping owner = discrim.getClassMapping();
                ClassMapping from, to;
                if (relMapping.getDescribedType().isAssignableFrom
                    (owner.getDescribedType())) {
                    from = owner;
                    to = relMapping;
                } else {
                    from = relMapping;
                    to = owner;
                }

                for (; from != null && from != to;
                    from = from.getJoinablePCSuperclassMapping())
                    joins = from.joinSuperclass(joins, false);
            }
        }
        return new InstanceofExpState(joins, pathState, mapping, discrim, rel);
    }

    /**
     * Expression state.
     */
    private static class InstanceofExpState
        extends ExpState {

        public final ExpState pathState;
        public final ClassMapping mapping;
        public final Discriminator discrim;
        public final Class rel;

        public InstanceofExpState(Joins joins, ExpState pathState, 
            ClassMapping mapping, Discriminator discrim, Class rel) {
            super(joins);
            this.pathState = pathState;
            this.mapping = mapping;
            this.discrim = discrim;
            this.rel = rel;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        // if no class indicator or a final class, just append true or false
        // depending on whether the cast matches the expected type
        InstanceofExpState istate = (InstanceofExpState) state;
        if (istate.discrim == null) {
            if (_cls.isAssignableFrom(istate.rel))
                sql.append("1 = 1");
            else
                sql.append("1 <> 1");
        } else {
            ctx.store.loadSubclasses(istate.discrim.getClassMapping());
            SQLBuffer buf = istate.discrim.getClassConditions(sel,
                istate.joins, istate.mapping, true);
            sql.append(buf);
        }
        sel.append(sql, istate.joins);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        InstanceofExpState istate = (InstanceofExpState) state;
        if (istate.discrim != null)
            sel.select(istate.discrim.getColumns(), istate.joins);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _path.acceptVisit(visitor);
        visitor.exit(this);
    }
}

