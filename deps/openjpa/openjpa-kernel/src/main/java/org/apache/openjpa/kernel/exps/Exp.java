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
package org.apache.openjpa.kernel.exps;

import java.util.Collection;

import org.apache.openjpa.kernel.StoreContext;

/**
 * An in-memory representation of an {@link Expression}.
 *
 * @author Abe White
 */
public class Exp
    implements Expression {

    /**
     * Evaluate the expression for the given candidate.
     */
    public final boolean evaluate(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        try {
            return eval(candidate, candidate, ctx, params);
        } catch (ClassCastException cce) {
            return false;
        } catch (NullPointerException npe) {
            return false;
        }
    }

    /**
     * Evaluate the expression for the given candidate group.
     */
    public final boolean evaluate(Collection candidates,
        StoreContext ctx, Object[] params) {
        try {
            return eval(candidates, ctx, params);
        } catch (ClassCastException cce) {
            return false;
        } catch (NullPointerException npe) {
            return false;
        }
    }

    /**
     * Evaluate the expression for the given context candidate and original
     * candidate.
     */
    protected boolean eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        return true;
    }

    /**
     * Evaluate the expression for the given group.
     */
    protected boolean eval(Collection candidates, StoreContext ctx,
        Object[] params) {
        return true;
	}

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        visitor.exit(this);
    }
}
