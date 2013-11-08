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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.openjpa.kernel.jpql.JPQLExpressionBuilder.ParsedJPQL;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * JPQL / Criteria Query Context
 * @since 2.0
 *
 */
public class Context implements Serializable {

    public final ParsedJPQL parsed;
    public ClassMetaData meta;
    public String schemaAlias;
    public Subquery subquery;
    public Expression from = null;
    public Context cloneFrom = null;
    private final Context parent;
    private List<Context> subsels = null;
    private Object select = null;
    protected int aliasCount = -1; 
    private Map<String,Value> variables = new HashMap<String,Value>();
    private Map<String,ClassMetaData> schemas =
        new HashMap<String,ClassMetaData>();

    public Context(ParsedJPQL parsed, Subquery subquery, Context parent) {
        this.parsed = parsed;
        this.subquery = subquery;
        this.parent = parent;
        if (subquery != null) {
            this.select = subquery.getSelect();
            parent.addSubselContext(this);
        }
    }

    public void setSubquery(Subquery subquery) {
        this.subquery = subquery;
        this.select = subquery.getSelect();
        parent.addSubselContext(this);
    }
    
    public ClassMetaData meta() {
        return meta;
    }

    public String schemaAlias() {
        return schemaAlias;
    }

    public Subquery subquery() {
        return subquery;
    }

    /**
     * Returns next table alias to be created.
     * @return
     */
    public int nextAlias() {
        Context p = this;
        while (p.subquery != null) {
            p = p.parent;
        }
        p.aliasCount++;
        return p.aliasCount;
    }

    /**
     * Reset alias count for prepared query cache
     *
     */
    public void resetAliasCount() {
        Context p = this;
        while (p.subquery != null) {
            p = p.parent;
        }
        p.aliasCount = -1;
    }

    /**
     * Register the select for this context.
     * @param select
     */
    public void setSelect(Object select) {
        this.select = select;
    }

    /**
     * Returns the select associated with this context.
     * @return
     */
    public Object getSelect() {
        return select;
    }

    /**
     * Register the subquery context in this context.
     * @param sub
     */
    public void addSubselContext(Context sub) {
        if (sub == null)
            return;
        if (subsels == null)
            subsels = new ArrayList<Context>();
        subsels.add(sub);
    }

    /**
     * Returns the subquery context.
     * @return
     */
    public List<Context> getSubselContexts() {
        return subsels;
    }

    /**
     * Returns the subquery in this context.
     * @return
     */
    public Subquery getSubquery() {
        return subquery;
    }

    public Context getParent() {
        return parent;
    }

    public void addVariable(String id, Value var) {
        variables.put(id.toLowerCase(), var);
    }

    public Map<String,Value> getVariables() {
        return variables;
    }

    public void setVariables(Map<String,Value> variables) {
        this.variables = variables;
    }

    public void addSchema(String id, ClassMetaData meta) {
        schemas.put(id.toLowerCase(), meta);
    }

    public ClassMetaData getSchema(String id) {
        if (id != null)
            return schemas.get(id.toLowerCase());
        return null;
    }

    public Map<String,ClassMetaData> getSchemas() {
        return schemas;
    }

    public void setSchemas(Map<String,ClassMetaData> schemas) {
        this.schemas = schemas;
    }

    /**
     * Given an alias and return its associated variable.
     * @param alias
     * @return
     */
    public Value getVariable(String alias) {
        Value variable = alias == null ? null 
            : variables.get(alias.toLowerCase());
        return variable;
    }

    /**
     * Given an alias find the context of its associated
     * variable where it is defined.
     * @param alias
     * @return
     */
    public Context findContext(String alias) {
        Value var = getVariable(alias);
        if (var != null)
            return this;
        for (Context p = parent; p != null; ) {
            var = p.getVariable(alias);
            if (var != null)
                return p;
            p = p.parent;
        }
        if (subsels != null) {
            for (Context subsel : subsels) {
                if (subsel != null) {
                    var = subsel.getVariable(alias);
                    if (var != null)
                        return subsel;
                }
            }
        }
        return null;
    }

    /**
     * Given an alias find the variable in JPQL contexts.
     * @param alias
     * @return
     */
    public Value findVariable(String alias) {
        Value var = getVariable(alias);
        if (var != null)
            return var;
        for (Context p = parent; p != null; ) {
            var = p.getVariable(alias);
            if (var != null)
                return var;
            p = p.parent;
        }
        return null;
    }
}

