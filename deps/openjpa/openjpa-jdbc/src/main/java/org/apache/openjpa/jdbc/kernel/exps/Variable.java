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

import java.sql.SQLException;

import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * A variable in a filter. Typically, the {@link #initialize} and
 * {@link #getJoins} methods of this value are not called. They are
 * only called if the variable is bound but otherwise unused in the filter,
 * in which case we must at least make the joins to the variable because the
 * act of binding a variable should at least guarantee that an instance
 * representing the variable could exist (i.e. the binding collection is not
 * empty).
 *
 * @author Abe White
 */
class Variable
    extends AbstractVal {

    private final String _name;
    private Class _type;
    private ClassMetaData _meta;
    private PCPath _path = null;
    private Class _cast = null;

    /**
     * Constructor. Supply variable name and type.
     */
    public Variable(String name, Class type) {
        _name = name;
        _type = type;
    }

    /**
     * Return the variable name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Return true if the variable is bound.
     */
    public boolean isBound() {
        return _path != null;
    }

    /**
     * Return the path this variable is aliased to.
     */
    public PCPath getPCPath() {
        return _path;
    }

    public Path getPath() {
        return _path;
    }

    /**
     * Set the path this variable is aliased to.
     */
    public void setPCPath(PCPath path) {
        _path = path;
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public void setMetaData(ClassMetaData meta) {
        _meta = meta;
        if (meta != null)
            _type = meta.getDescribedType();
    }

    public boolean isVariable() {
        return true;
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        return _type;
    }

    public void setImplicitType(Class type) {
        _cast = type;
        if (_path != null)
            _path.setImplicitType(type);
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        if (_path != null) {
            _path.addVariableAction(this);
            return _path.initialize(sel, ctx, flags | JOIN_REL);
        }
        return ExpState.NULL;
    }

    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
    }

    public void groupBy(Select sel, ExpContext ctx, ExpState state) {
    }

    public void orderBy(Select sel, ExpContext ctx, ExpState state, 
        boolean asc) {
    }

    public Object load(ExpContext ctx, ExpState state, Result res)
        throws SQLException {
        return null;
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        if (_path != null)
            _path.calculateValue(sel, ctx, state, other, otherState);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return 0;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
    }

    public void appendIsEmpty(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
    }

    public void appendIsNotEmpty(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
    }

    public void appendSize(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
    }

    public void appendIsNull(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
    }

    public void appendIsNotNull(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        if (_path != null)
            _path.acceptVisit(visitor);
        visitor.exit(this);
    }
}
