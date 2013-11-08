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
package org.apache.openjpa.kernel.jpql;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.ExpressionStoreQuery;
import org.apache.openjpa.kernel.FillStrategy;
import org.apache.openjpa.kernel.QueryContext;
import org.apache.openjpa.kernel.QueryOperations;
import org.apache.openjpa.kernel.ResultShape;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.kernel.exps.AbstractExpressionBuilder;
import org.apache.openjpa.kernel.exps.Context;
import org.apache.openjpa.kernel.exps.Expression;
import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.Literal;
import org.apache.openjpa.kernel.exps.Parameter;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Resolver;
import org.apache.openjpa.kernel.exps.Subquery;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.OrderedMap;
import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.UserException;


/**
 * Builder for JPQL expressions. This class takes the query parsed
 * in {@link JPQL} and converts it to an expression tree using
 * an {@link ExpressionFactory}. Public for unit testing purposes.
 *
 * @author Marc Prud'hommeaux
 * @author Patrick Linskey
 * @nojavadoc
 */
public class JPQLExpressionBuilder
    extends AbstractExpressionBuilder
    implements JPQLTreeConstants {

    private static final int VAR_PATH = 1;
    private static final int VAR_ERROR = 2;

    private static final Localizer _loc = Localizer.forPackage
        (JPQLExpressionBuilder.class);

    private final Stack<Context> contexts = new Stack<Context>();
    private OrderedMap<Object, Class<?>> parameterTypes;
    private int aliasCount = 0;
    private boolean inAssignSubselectProjection = false;
    private boolean hasParameterizedInExpression = false;

    /**
     * Constructor.
     *
     * @param factory the expression factory to use
     * @param query used to resolve variables, parameters,
     * and class names used in the query
     * @param parsedQuery the parsed query
     */
    public JPQLExpressionBuilder(ExpressionFactory factory,
        ExpressionStoreQuery query, Object parsedQuery) {
        super(factory, query.getResolver());

        contexts.push(new Context(parsedQuery instanceof ParsedJPQL
            ? (ParsedJPQL) parsedQuery
            : parsedQuery instanceof String
            ? getParsedQuery((String) parsedQuery)
            : null, null, null));

        if (ctx().parsed == null)
            throw new InternalException(parsedQuery + "");
    }

    protected Localizer getLocalizer() {
        return _loc;
    }

    protected ClassLoader getClassLoader() {
        // we don't resolve in the context of anything but ourselves
        return getClass().getClassLoader();
    }

    protected ParsedJPQL getParsedQuery() {
        return ctx().parsed;
    }

    protected ParsedJPQL getParsedQuery(String jpql) {
        return new ParsedJPQL(jpql);
    }

    private void setCandidate(ClassMetaData cmd, String schemaAlias) {
        addAccessPath(cmd);

        if (cmd != null)
            ctx().meta = cmd;

        if (schemaAlias != null)
            ctx().schemaAlias = schemaAlias;
    }

    private String nextAlias() {
        return "jpqlalias" + (++aliasCount);
    }

    protected ClassMetaData resolveClassMetaData(JPQLNode node) {
        // handle looking up alias names
        String schemaName = assertSchemaName(node);
        ClassMetaData cmd = getClassMetaData(schemaName, false);
        if (cmd != null)
            return cmd;

        // we might be referencing a collection field of a subquery's parent
        if (isPath(node)) {
            Path path = getPath(node);
            FieldMetaData fmd = path.last();
            cmd = getFieldType(fmd);
            if (cmd == null && fmd.isElementCollection())
                cmd = fmd.getDefiningMetaData();
            return cmd;
        }

        // now run again to throw the correct exception
        return getClassMetaData(schemaName, true);
    }

    private ClassMetaData getClassMetaData(String alias, boolean assertValid) {
        ClassLoader loader = getClassLoader();
        MetaDataRepository repos = resolver.getConfiguration().
            getMetaDataRepositoryInstance();

        // first check for the alias
        ClassMetaData cmd = repos.getMetaData(alias, loader, false);

        if (cmd != null)
            return cmd;

        // now check for the class name; this is not technically permitted
        // by the JPA spec, but is required in order to be able to execute
        // JPQL queries from other facades (like JDO) that do not have
        // the concept of entity names or aliases
        Class<?> c = resolver.classForName(alias, null);
        if (c != null)
            cmd = repos.getMetaData(c, loader, assertValid);
        else if (assertValid)
            cmd = repos.getMetaData(alias, loader, false);

        if (cmd == null && assertValid) {
            String close = repos.getClosestAliasName(alias);
            if (close != null)
                throw parseException(EX_USER, "not-schema-name-hint",
                    new Object[]{ alias, close, repos.getAliasNames() }, null);
            else
                throw parseException(EX_USER, "not-schema-name",
                    new Object[]{ alias, repos.getAliasNames() }, null);
        }

        return cmd;
    }

    private Class<?> getCandidateType() {
        return getCandidateMetaData().getDescribedType();
    }

    private ClassMetaData getCandidateMetaData() {
        if (ctx().meta != null)
            return ctx().meta;

        ClassMetaData cls = getCandidateMetaData(root());
        if (cls == null)
            throw parseException(EX_USER, "not-schema-name",
                new Object[]{ root() }, null);

        setCandidate(cls, null);
        return cls;
    }

    protected ClassMetaData getCandidateMetaData(JPQLNode node) {
        // examing the node to find the candidate query
        // ### this should actually be the primary SELECT instance
        // resolved against the from variable declarations
        JPQLNode from = node.findChildByID(JJTFROMITEM, true);
        if (from == null) {
            // OPENJPA-15 allow subquery without a FROMITEM
            if (node.id == JJTSUBSELECT) { 
                from = node.findChildByID(JJTFROM, true);
            }
            else {
                throw parseException(EX_USER, "no-from-clause", null, null);
            }
        }

        for (int i = 0; i < from.children.length; i++) {
            JPQLNode n = from.children[i];

            if (n.id == JJTABSTRACTSCHEMANAME) {
                // we simply return the first abstract schema child
                // as resolved into a class
                ClassMetaData cmd = resolveClassMetaData(n);

                if (cmd != null)
                    return cmd;

                // not a schema: treat it as a class
                String cls = assertSchemaName(n);
                if (cls == null)
                    throw parseException(EX_USER, "not-schema-name",
                        new Object[]{ root() }, null);

                return getClassMetaData(cls, true);
            }
            // OPENJPA-15 support subquery's from clause do not start with 
            // identification_variable_declaration()
            if (node.id == JJTSUBSELECT) {
                if (n.id == JJTINNERJOIN) {
                    n = n.getChild(0);
                }
                if (n.id == JJTPATH) {
                    Path path = getPath(n);
                    FieldMetaData fmd = path.last();
                    ClassMetaData cmd = getFieldType(fmd);
                    if (cmd == null && fmd.isElementCollection())
                        cmd = fmd.getDefiningMetaData();
                    if (cmd != null) {
                        return cmd;
                    }
                    else {
                        throw parseException(EX_USER, "no-alias", 
                                new Object[]{ n }, null);
                    }
                }
            }           
        }

        return null;
    }

    protected String currentQuery() {
        return ctx().parsed == null || root().parser == null ? null
            : root().parser.jpql;
    }

    QueryExpressions getQueryExpressions() {
        QueryExpressions exps = new QueryExpressions();
        exps.setContexts(contexts);

        evalQueryOperation(exps);

        Expression filter = null;
        Expression from = ctx().from;
        if (from == null)
            from = evalFromClause(root().id == JJTSELECT);
        filter = and(from, filter);
        filter = and(evalWhereClause(), filter);
        filter = and(evalSelectClause(exps), filter);

        exps.filter = filter == null ? factory.emptyExpression() : filter;

        evalGroupingClause(exps);
        evalHavingClause(exps);
        evalFetchJoins(exps);
        evalSetClause(exps);
        evalOrderingClauses(exps);

        if (parameterTypes != null)
            exps.parameterTypes = parameterTypes;

        exps.accessPath = getAccessPath();
        exps.hasInExpression = this.hasParameterizedInExpression;

        // verify parameters are consistent. 
        validateParameters();

        return exps;
    }

    private Expression and(Expression e1, Expression e2) {
        return e1 == null ? e2 : e2 == null ? e1 : factory.and(e1, e2);
    }

    private static String assemble(JPQLNode node) {
        return assemble(node, ".", 0);
    }

    /**
     * Assemble the children of the specific node by appending each
     * child, separated by the delimiter.
     */
    private static String assemble(JPQLNode node, String delimiter, int last) {
        StringBuilder result = new StringBuilder();
        JPQLNode[] parts = node.children;
        for (int i = 0; parts != null && i < parts.length - last; i++)
            result.append(result.length() > 0 ? delimiter : "").
                append(parts[i].text);

        return result.toString();
    }

    private Expression assignSubselectProjection(JPQLNode node,
        QueryExpressions exps) {
        inAssignSubselectProjection = true;
        exps.projections = new Value[1];
        exps.projectionClauses = new String[1];
        exps.projectionAliases = new String[1];

        Value val = getValue(node);
        exps.projections[0] = val;
        exps.projectionClauses[0] = 
            projectionClause(node.id == JJTSCALAREXPRESSION ?
                firstChild(node) : node);
        inAssignSubselectProjection = false;
        return null;
    }

    /**
     * Assign projections for NEW contructor in selection list.
     *     Example:  SELECT NEW Person(p.name) FROM Person p WHERE ...
     */
    private Expression assignProjections(JPQLNode parametersNode,
        QueryExpressions exps, List<Value> projections,
        List<String> projectionClauses, List<String> projectionAliases) {
        int count = parametersNode.getChildCount();

        Expression exp = null;
        for (int i = 0; i < count; i++) {
            JPQLNode parent = parametersNode.getChild(i);
            JPQLNode node = firstChild(parent);
            JPQLNode aliasNode = parent.children.length > 1 ? right(parent)
                : null; 
            Value proj = getValue(node);
            String alias = aliasNode != null ? aliasNode.text :
                projectionClause(node.id == JJTSCALAREXPRESSION ?
                        firstChild(node) : node);
            if (aliasNode != null)
                proj.setAlias(alias);
            projections.add(proj);
            projectionClauses.add(alias);
            projectionAliases.add(alias);
        }
        return exp;
    }

    private void evalProjectionsResultShape(JPQLNode selectionsNode,
        QueryExpressions exps,
        List<Value> projections,
        List<String> projectionClauses,
        List<String> projectionAliases) {
        int count = selectionsNode.getChildCount();
        Class<?> resultClass = null;
        ResultShape<?> resultShape = null;
        if (count > 1) {
            // muti-selection
            resultClass = Object[].class;
            resultShape = new ResultShape(resultClass, new FillStrategy.Array<Object[]>(Object[].class));
        }

        for (int i = 0; i < count; i++) {
            JPQLNode parent = selectionsNode.getChild(i);
            JPQLNode node = firstChild(parent);
            if (node.id == JJTCONSTRUCTOR) {
                // build up the fully-qualified result class name by
                // appending together the components of the children
                String resultClassName = assemble(left(node));
                Class<?> constructor = resolver.classForName(resultClassName, null);
                if (constructor == null) {
                    // try resolve it again using simple name
                    int n = left(node).getChildCount();
                    String baseName = left(node).getChild(n-1).text;
                    constructor = resolver.classForName(baseName, null);
                }
                if (constructor == null)
                    throw parseException(EX_USER, "no-constructor",
                            new Object[]{ resultClassName }, null);

                List<Value> terms = new ArrayList<Value>();
                List<String> aliases = new ArrayList<String>();
                List<String> clauses = new ArrayList<String>();
                // now assign the arguments to the select clause as the projections
                assignProjections(right(node), exps, terms, aliases, clauses);
                FillStrategy fill = new FillStrategy.NewInstance(constructor);
                ResultShape<?> cons = new ResultShape(constructor, fill);
                for (Value val : terms) {
                    Class<?> type = val.getType();
                    cons.nest(new ResultShape(type, new FillStrategy.Assign(), type.isPrimitive()));
                }
                if (count == 1) {
                    resultClass = constructor;
                    resultShape = cons;
                }
                else
                    resultShape.nest(cons);
                projections.addAll(terms);
                projectionAliases.addAll(aliases);
                projectionClauses.addAll(clauses);

            } else {
                JPQLNode aliasNode = parent.children.length > 1 ? right(parent)
                        : null; 
                Value proj = getValue(node);
                String alias = aliasNode != null ? aliasNode.text :
                    projectionClause(node.id == JJTSCALAREXPRESSION ?
                            firstChild(node) : node);
                if (aliasNode != null)
                    proj.setAlias(alias);
                projections.add(proj);
                projectionClauses.add(alias);
                projectionAliases.add(alias);
                Class<?> type = proj.getType();
                ResultShape<?> projShape = new ResultShape(type, new FillStrategy.Assign(), type.isPrimitive());

                if (count == 1)
                    resultShape = projShape;
                else
                    resultShape.nest(projShape);
            }
        }
        exps.shape = resultShape;
        exps.resultClass = resultClass;
    }

    private String projectionClause(JPQLNode node) {
        switch (node.id) {
        case JJTTYPE:
            return projectionClause(firstChild(node));
        default:
            return assemble(node);
        }
    }
    
    private void evalQueryOperation(QueryExpressions exps) {
        // determine whether we want to select, delete, or update
        if (root().id == JJTSELECT || root().id == JJTSUBSELECT)
            exps.operation = QueryOperations.OP_SELECT;
        else if (root().id == JJTDELETE)
            exps.operation = QueryOperations.OP_DELETE;
        else if (root().id == JJTUPDATE)
            exps.operation = QueryOperations.OP_UPDATE;
        else
            throw parseException(EX_UNSUPPORTED, "unrecognized-operation",
                new Object[]{ root() }, null);
    }

    private void evalGroupingClause(QueryExpressions exps) {
        // handle GROUP BY clauses
        JPQLNode groupByNode = root().findChildByID(JJTGROUPBY, false);

        if (groupByNode == null)
            return;

        int groupByCount = groupByNode.getChildCount();

        exps.grouping = new Value[groupByCount];

        for (int i = 0; i < groupByCount; i++) {
            JPQLNode node = groupByNode.getChild(i);
            Value val = getValue(node);
            if (val instanceof Path) {
                FieldMetaData fmd = ((Path) val).last();
                if (fmd != null && fmd.getValue().getTypeMetaData() != null && fmd.getValue().isEmbedded())
                    throw parseException(EX_USER, "cant-groupby-embeddable",
                        new Object[]{ node.getChildCount() > 1 ? assemble(node) : node.text }, null);
            }
            exps.grouping[i] = val;
        }
    }

    private void evalHavingClause(QueryExpressions exps) {
        // handle HAVING clauses
        JPQLNode havingNode = root().findChildByID(JJTHAVING, false);

        if (havingNode == null)
            return;

        exps.having = getExpression(onlyChild(havingNode));
    }

    private void evalOrderingClauses(QueryExpressions exps) {
        // handle ORDER BY clauses
        JPQLNode orderby = root().findChildByID(JJTORDERBY, false);
        if (orderby != null) {
            int ordercount = orderby.getChildCount();
            exps.ordering = new Value[ordercount];
            exps.orderingClauses = new String[ordercount];
            exps.orderingAliases = new String[ordercount];
            exps.ascending = new boolean[ordercount];
            for (int i = 0; i < ordercount; i++) {
                JPQLNode node = orderby.getChild(i);
                JPQLNode firstChild = firstChild(node);
                exps.ordering[i] = getValue(firstChild);
                exps.orderingClauses[i] = assemble(firstChild);
                exps.orderingAliases[i] = firstChild.text;

                // ommission of ASC/DESC token implies ascending
                exps.ascending[i] = node.getChildCount() <= 1 ||
                    lastChild(node).id == JJTASCENDING ? true : false;
            }
            // check if order by select item result alias
            for (int i = 0; i < ordercount; i++) {
                if (exps.orderingClauses[i] != null && 
                    !exps.orderingClauses[i].equals(""))
                    continue;
                for (int j = 0; j < exps.projections.length; j++) {
                    if (exps.projectionAliases[j].equalsIgnoreCase(
                        exps.orderingAliases[i])) {
                        exps.ordering[i] = exps.projections[j];
                        break;
                    }
                }
            }
        }
    }

    private Expression evalSelectClause(QueryExpressions exps) {
        if (exps.operation != QueryOperations.OP_SELECT)
            return null;

        JPQLNode selectNode = root();

        JPQLNode selectClause = selectNode.
            findChildByID(JJTSELECTCLAUSE, false);
        if (selectClause != null && selectClause.hasChildID(JJTDISTINCT))
            exps.distinct = QueryExpressions.DISTINCT_TRUE 
                          | QueryExpressions.DISTINCT_AUTO;
        else
            exps.distinct = QueryExpressions.DISTINCT_FALSE;

        // handle SELECT clauses
        JPQLNode expNode = selectNode.
        findChildByID(JJTSELECTEXPRESSIONS, true);
        if (expNode == null) {
            return null;
        }

        int selectCount = expNode.getChildCount();
        JPQLNode selectChild = firstChild(expNode);

        if (selectClause.parent.id == JJTSUBSELECT) {
            exps.distinct &= ~QueryExpressions.DISTINCT_AUTO;
            return assignSubselectProjection(onlyChild(selectChild), exps);
        }
        // if we are selecting just one thing and that thing is the
        // schema's alias, then do not treat it as a projection
        if (selectCount == 1 && selectChild != null &&
            selectChild.getChildCount() == 1 &&
            onlyChild(selectChild) != null) {
            JPQLNode child = onlyChild(selectChild);
            if (child.id == JJTSCALAREXPRESSION)
                child = onlyChild(child);
            if (assertSchemaAlias().equalsIgnoreCase(child.text)) {
                return null;
            }
        } 
        // JPQL does not filter relational joins for projections
        exps.distinct &= ~QueryExpressions.DISTINCT_AUTO;
        exps.projections = new Value[selectCount];
        List<Value> projections = new ArrayList<Value>();
        List<String> aliases = new ArrayList<String>();
        List<String> clauses = new ArrayList<String>();
        evalProjectionsResultShape(expNode, exps, projections, aliases, clauses);
        exps.projections = projections.toArray(new Value[projections.size()]);
        exps.projectionAliases = aliases.toArray(new String[aliases.size()]);
        exps.projectionClauses = clauses.toArray(new String[clauses.size()]);
        return null;
    }

    private String assertSchemaAlias() {
        String alias = ctx().schemaAlias;

        if (alias == null)
            throw parseException(EX_USER, "alias-required",
                new Object[]{ ctx().meta }, null);

        return alias;
    }

    protected Expression evalFetchJoins(QueryExpressions exps) {
        Expression filter = null;

        // handle JOIN FETCH
        Set<String> joins = null;
        Set<String> innerJoins = null;

        JPQLNode[] outers = root().findChildrenByID(JJTOUTERFETCHJOIN);
        for (int i = 0; outers != null && i < outers.length; i++)
            (joins == null ? joins = new TreeSet<String>() : joins).
                add(getPath(onlyChild(outers[i])).last().getFullName(false));

        JPQLNode[] inners = root().findChildrenByID(JJTINNERFETCHJOIN);
        for (int i = 0; inners != null && i < inners.length; i++) {
            String path = getPath(onlyChild(inners[i])).last()
                .getFullName(false);
            (joins == null ? joins = new TreeSet<String>() : joins).add(path);
            (innerJoins == null 
                    ? innerJoins = new TreeSet<String>() 
                    : innerJoins).add(path);
        }

        if (joins != null)
            exps.fetchPaths = (String[]) joins.
                toArray(new String[joins.size()]);
        if (innerJoins != null)
            exps.fetchInnerPaths = (String[]) innerJoins.
                toArray(new String[innerJoins.size()]);

        return filter;
    }

    protected void evalSetClause(QueryExpressions exps) {
        // handle SET field = value
        JPQLNode[] nodes = root().findChildrenByID(JJTUPDATEITEM);
        for (int i = 0; nodes != null && i < nodes.length; i++) {
            Path path = getPath(firstChild(nodes[i]));
            if (path.last().getValue().getEmbeddedMetaData() != null)
                throw parseException(EX_USER, "cant-bulk-update-embeddable",
                        new Object[]{assemble(firstChild(nodes[i]))}, null);

            JPQLNode lastChild = lastChild(nodes[i]);
            Value val = (lastChild.children == null) 
                      ? null : getValue(onlyChild(lastChild));
            exps.putUpdate(path, val);
        }
    }

    private Expression evalWhereClause() {
        // evaluate the WHERE clause
        JPQLNode whereNode = root().findChildByID(JJTWHERE, false);
        if (whereNode == null)
            return null;
        return (Expression) eval(whereNode);
    }

    private Expression evalFromClause(boolean needsAlias) {
        // build up the alias map in the FROM clause
        JPQLNode from = root().findChildByID(JJTFROM, false);
        if (from == null)
            throw parseException(EX_USER, "no-from-clause", null, null);
        return evalFromClause(from, needsAlias);
    }

    private Expression evalFromClause(JPQLNode from, boolean needsAlias) {
        Expression exp = null;

        for (int i = 0; i < from.children.length; i++) {
            JPQLNode node = from.children[i];

            if (node.id == JJTFROMITEM)
                exp = evalFromItem(exp, node, needsAlias);
            else if (node.id == JJTOUTERJOIN)
                exp = addJoin(node, false, exp);
            else if (node.id == JJTINNERJOIN)
                exp = addJoin(node, true, exp);
            else if (node.id == JJTINNERFETCHJOIN)
                ; // we handle inner fetch joins in the evalFetchJoins() method
            else if (node.id == JJTOUTERFETCHJOIN)
                ; // we handle outer fetch joins in the evalFetchJoins() method
            else
                throw parseException(EX_USER, "not-schema-name",
                    new Object[]{ node }, null);
        }

        return exp;
    }

    private Expression getSubquery(String alias, Path path, Expression exp) {
        Value var = getVariable(alias, true);
        // this bind is for validateMapPath to resolve alias
        Expression bindVar = factory.bindVariable(var, path);
        FieldMetaData fmd = path.last();
        ClassMetaData candidate = getFieldType(fmd);
        if (candidate == null && fmd.isElementCollection())
            candidate = fmd.getDefiningMetaData();

        setCandidate(candidate, alias);

        Context subContext = ctx();
        Subquery subquery = ctx().getSubquery();
        if (subquery == null){
            subquery = factory.newSubquery(candidate, true, alias);
            subContext.setSubquery(subquery);
        }
        else {
            subquery.setSubqAlias(alias);
        }

        Path subpath = factory.newPath(subquery);
        subpath.setSchemaAlias(path.getCorrelationVar());
        subpath.setMetaData(candidate);
        subquery.setMetaData(candidate);
        if (fmd.isElementCollection())
            exp = and(exp, bindVar);
        else
            exp = and(exp, factory.equal(path, subpath));

        return exp;
    }

    /**
     * Adds a join condition to the given expression.
     *
     * @param node the node to check
     * @param inner whether or not the join should be an inner join
     * @param exp an existing expression to AND, or null if none
     * @return the Expression with the join condition added
     */
    private Expression addJoin(JPQLNode node, boolean inner, Expression exp) {
        // the type will be the declared type for the field
        JPQLNode firstChild = firstChild(node);
        Path path = null;
        if (firstChild.id == JJTQUALIFIEDPATH)
            path = (Path) getQualifiedPath(firstChild);
        else
            path = getPath(firstChild, false, inner);

        JPQLNode alias = node.getChildCount() >= 2 ? right(node) : null;
        // OPENJPA-15 support subquery's from clause do not start with 
        // identification_variable_declaration()
        if (inner && ctx().getParent() != null && ctx().schemaAlias == null) {
            return getSubquery(alias.text, path, exp);
        }

        return addJoin(path, alias, exp);
    }

    private Expression addJoin(Path path, JPQLNode aliasNode,
        Expression exp) {
        FieldMetaData fmd = path.last();

        if (fmd == null)
            throw parseException(EX_USER, "path-no-meta",
                new Object[]{ path, null }, null);

        String alias = aliasNode != null ? aliasNode.text : nextAlias();

        Value var = getVariable(alias, true);
        var.setMetaData(getFieldType(fmd));

        Expression join = null;

        // if the variable is already bound, get the var's value and
        // do a regular contains with that
        boolean bound = isBound(var);
        if (bound) {
            var = getValue(aliasNode, VAR_PATH);
        } else {
            bind(var);
            join = and(join, factory.bindVariable(var, path));
        }

        if (!fmd.isTypePC()) // multi-valued relation
        {
            if (bound)
                join = and(join, factory.contains(path, var));

            setImplicitContainsTypes(path, var, CONTAINS_TYPE_ELEMENT);
        }

        return and(exp, join);
    }

    private Expression evalFromItem(Expression exp, JPQLNode node,
        boolean needsAlias) {
        ClassMetaData cmd = resolveClassMetaData(firstChild(node));

        String alias = null;

        if (node.getChildCount() < 2) {
            if (needsAlias)
                throw parseException(EX_USER, "alias-required",
                    new Object[]{ cmd }, null);
        } else {
            alias = right(node).text;
            JPQLNode left = left(node);
            addSchemaToContext(alias, cmd);

            // check to see if the we are referring to a path in the from
            // clause, since we might be in a subquery against a collection
            if (isPath(left)) {
                Path path = getPath(left);
                return getSubquery(alias, path, exp);
            } else {
                // we have an alias: bind it as a variable
                Value var = getVariable(alias, true);
                var.setMetaData(cmd);
                bind(var);
            }
        }

        // ### we assign the first FROMITEM instance we see as
        // the global candidate, which is incorrect: we should
        // instead be mapping this to the SELECTITEM to see
        // which is the desired candidate
        if (ctx().schemaAlias == null)
            setCandidate(cmd, alias);
        else  
            addAccessPath(cmd);

        return exp;
    }

    protected boolean isDeclaredVariable(String name) {
        // JPQL doesn't support declaring variables
        return false;
    }

    /**
     * Check to see if the specific node is a path (vs. a schema name)
     */
    boolean isPath(JPQLNode node) {
        if (node.getChildCount() < 2)
            return false;

        final String name = firstChild(node).text;
        if (name == null)
            return false;

        // handle the case where the class name is the alias
        // for the candidate (we don't use variables for this)
        if (getMetaDataForAlias(name) != null)
            return true;

        if (!isSeenVariable(name))
            return false;

        final Value var = getVariable(name, false);

        if (var != null)
            return isBound(var);

        return false;
    }

    private static ClassMetaData getFieldType(FieldMetaData fmd) {
        if (fmd == null)
            return null;

        ClassMetaData cmd = null;
        ValueMetaData vmd;

        if ((vmd = fmd.getElement()) != null)
            cmd = vmd.getDeclaredTypeMetaData();
        else if ((vmd = fmd.getKey()) != null)
            cmd = vmd.getDeclaredTypeMetaData();
        else if ((vmd = fmd.getValue()) != null)
            cmd = vmd.getDeclaredTypeMetaData();

        if (cmd == null || cmd.getDescribedType() == Object.class)
            cmd = fmd.getDeclaredTypeMetaData();

        return cmd;
    }

    /**
     * Identification variables in JPQL are case insensitive, so lower-case
     * all variables we are going to bind.
     */
    protected Value getVariable(String id, boolean bind) {
        if (id == null)
            return null;

        if (bind && getDefinedVariable(id) == null)
            return createVariable(id, bind);

        return super.getVariable(id.toLowerCase(), bind);
    }

    protected Value getDefinedVariable(String id) {
        return ctx().getVariable(id);
    }

    protected boolean isSeenVariable(String var) {
        Context c = ctx().findContext(var);
        if (c != null)
            return true;
        return false;
    }

    /**
     * Returns the class name using the children of the JPQLNode.
     */
    private String assertSchemaName(JPQLNode node) {
        if (node.id != JJTABSTRACTSCHEMANAME)
            throw parseException(EX_USER, "not-identifer",
                new Object[]{ node }, null);

        return assemble(node);
    }

    private void checkEmbeddable(Value val) {
        checkEmbeddable(val, currentQuery());
    }
    
    public static void checkEmbeddable(Value val, String currentQuery) {
        Path path = val instanceof Path ? (Path) val : null;
        if (path == null)
            return;

        FieldMetaData fmd = path.last();
        if (fmd == null)
            return;

        ValueMetaData vm = fmd.isElementCollection() ? fmd.getElement()
            : fmd.getValue();
        if (vm.getEmbeddedMetaData() != null) {
            //throw parseException(EX_USER, "bad-predicate",
            //    new Object[]{ currentQuery() }, null);
            String argStr = _loc.get("bad-predicate", 
                new Object[] {fmd.getName()}).getMessage();
            Message msg = _loc.get("parse-error", argStr, currentQuery);
            throw new UserException(msg, null);
        }
    }

    /**
     * Recursive helper method to evaluate the given node.
     */
    private Object eval(JPQLNode node) {
        Value val1 = null;
        Value val2 = null;
        Value val3 = null;

        boolean not = node.not;

        switch (node.id) {
            case JJTSCALAREXPRESSION:
                return eval(onlyChild(node));

            case JJTTYPE:
                return getType(onlyChild(node));

            case JJTTYPELITERAL:
                return getTypeLiteral(node);

            case JJTCLASSNAME:
                return getPathOrConstant(node);

            case JJTCASE:
                return eval(onlyChild(node));

            case JJTSIMPLECASE:
                return getSimpleCaseExpression(node);

            case JJTGENERALCASE:
                return getGeneralCaseExpression(node);

            case JJTWHEN:
                return getWhenCondition(node);

            case JJTWHENSCALAR:
                return getWhenScalar(node);

            case JJTCOALESCE:
                return getCoalesceExpression(node);

            case JJTNULLIF:
                return getNullIfExpression(node);

            case JJTWHERE: // top-level WHERE clause
                return getExpression(onlyChild(node));

            case JJTBOOLEANLITERAL:
                return factory.newLiteral("true".equalsIgnoreCase
                    (node.text) ? Boolean.TRUE : Boolean.FALSE,
                    Literal.TYPE_BOOLEAN);

            case JJTINTEGERLITERAL:
                // use BigDecimal because it can handle parsing exponents
                BigDecimal intlit = new BigDecimal
                    (node.text.endsWith("l") || node.text.endsWith("L")
                        ? node.text.substring(0, node.text.length() - 1)
                        : node.text).
                    multiply(new BigDecimal(negative(node)));
                return factory.newLiteral(Long.valueOf(intlit.longValue()),
                    Literal.TYPE_NUMBER);

            case JJTDECIMALLITERAL:
                BigDecimal declit = new BigDecimal
                    (node.text.endsWith("d") || node.text.endsWith("D") ||
                        node.text.endsWith("f") || node.text.endsWith("F")
                        ? node.text.substring(0, node.text.length() - 1)
                        : node.text).
                    multiply(new BigDecimal(negative(node)));
                return factory.newLiteral(declit, Literal.TYPE_NUMBER);

            case JJTSTRINGLITERAL:
            case JJTTRIMCHARACTER:
            case JJTESCAPECHARACTER:
                return factory.newLiteral(trimQuotes(node.text),
                    Literal.TYPE_SQ_STRING);

            case JJTSTRINGLITERAL2:
                return factory.newLiteral(trimDoubleQuotes(node.text),
                    Literal.TYPE_SQ_STRING);

            case JJTPATTERNVALUE:
                return eval(firstChild(node));

            case JJTNAMEDINPUTPARAMETER:
                return getParameter(onlyChild(node).text, false, false);

            case JJTPOSITIONALINPUTPARAMETER:
                return getParameter(node.text, true, false);

            case JJTCOLLECTIONPARAMETER:
                JPQLNode child = onlyChild(node);
                boolean positional = child.id == JJTPOSITIONALINPUTPARAMETER;
                if (!positional)
                    child = onlyChild(child);
                return getParameter(child.text, 
                    positional, true);

            case JJTOR: // x OR y
                return factory.or(getExpression(left(node)),
                    getExpression(right(node)));

            case JJTAND: // x AND y
                return and(getExpression(left(node)),
                    getExpression(right(node)));

            case JJTEQUALS: // x = y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, null);
                return factory.equal(val1, val2);

            case JJTNOTEQUALS: // x <> y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, null);
                return factory.notEqual(val1, val2);

            case JJTLESSTHAN: // x < y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, null);
                return factory.lessThan(val1, val2);

            case JJTLESSOREQUAL: // x <= y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, null);
                return factory.lessThanEqual(val1, val2);

            case JJTGREATERTHAN: // x > y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, null);
                return factory.greaterThan(val1, val2);

            case JJTGREATEROREQUAL: // x >= y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, null);
                return factory.greaterThanEqual(val1, val2);

            case JJTADD: // x + y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, TYPE_NUMBER);
                return factory.add(val1, val2);

            case JJTSUBTRACT: // x - y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, TYPE_NUMBER);
                return factory.subtract(val1, val2);

            case JJTMULTIPLY: // x * y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, TYPE_NUMBER);
                return factory.multiply(val1, val2);

            case JJTDIVIDE: // x / y
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, TYPE_NUMBER);
                return factory.divide(val1, val2);

            case JJTBETWEEN: // x.field [NOT] BETWEEN 5 AND 10
                val1 = getValue(child(node, 0, 3));
                val2 = getValue(child(node, 1, 3));
                val3 = getValue(child(node, 2, 3));
                setImplicitTypes(val1, val2, null);
                setImplicitTypes(val1, val3, null);
                return evalNot(not, and(factory.greaterThanEqual(val1, val2),
                    factory.lessThanEqual(val1, val3)));

            case JJTIN: // x.field [NOT] IN ('a', 'b', 'c')
                        // TYPE(x...) [NOT] IN (entityTypeLiteral1,...)
                Expression inExp = null;
                Iterator<JPQLNode> inIterator = node.iterator();
                // the first child is the path
                JPQLNode first = inIterator.next();
                val1 = getValue(first);
                while (inIterator.hasNext()) {
                    JPQLNode next = inIterator.next();
                    if (first.id == JJTTYPE && next.id == JJTTYPELITERAL)
                        val2 = getTypeLiteral(next);
                    else
                        val2 = getValue(next);
                    if (val2 instanceof Parameter) {
                        hasParameterizedInExpression = true;
                    }
                    // special case for <value> IN (<subquery>) or
                    // <value> IN (<single value>)
                    if (useContains(not, val1, val2, node))    
                        return evalNot(not, factory.contains(val2, val1)); 

                    // this is currently a sequence of OR expressions, since we
                    // do not have support for IN expressions
                    setImplicitTypes(val1, val2, null);
                    if (isVerticalTypeInExpr(val1, node) && not) {
                        if (inExp == null)
                            inExp = factory.notEqual(val1, val2);
                        else
                            inExp = factory.and(inExp, factory.notEqual(val1, val2));
                    } else {
                        if (inExp == null)
                            inExp = factory.equal(val1, val2);
                        else
                            inExp = factory.or(inExp, factory.equal(val1, val2));
                    }
                }
                

                // we additionally need to add in a "NOT NULL" clause, since
                // the IN behavior that is expected by the CTS also expects
                // to filter our NULLs
                if (isVerticalTypeInExpr(val1, node)) 
                    return inExp;
                else    
                    return and(evalNot(not, inExp),
                            factory.notEqual(val1, factory.getNull()));

            case JJTISNULL: // x.field IS [NOT] NULL
                val1 = getValue(onlyChild(node));
                checkEmbeddable(val1);
                if (not)
                    return factory.notEqual
                        (val1, factory.getNull());
                else
                    return factory.equal
                        (val1, factory.getNull());

            case JJTPATH:
                return getPathOrConstant(node);

            case JJTIDENTIFIER:
            case JJTIDENTIFICATIONVARIABLE:
                return getIdentifier(node);

            case JJTQUALIFIEDPATH:
                return getQualifiedPath(node);

            case JJTQUALIFIEDIDENTIFIER:
                // KEY(e), VALUE(e), ENTRY(e)
                return getQualifiedIdentifier(node);

            case JJTGENERALIDENTIFIER:
                // KEY(e), VALUE(e)
                if (node.parent.parent.id == JJTWHERE || node.parent.id == JJTGROUPBY)
                    return getGeneralIdentifier(onlyChild(node), true);
                return getQualifiedIdentifier(onlyChild(node));

            case JJTNOT:
                return factory.not(getExpression(onlyChild(node)));

            case JJTLIKE: // field LIKE '%someval%'
                val1 = getValue(left(node));
                val2 = getValue(right(node));

                setImplicitType(val1, TYPE_STRING);
                setImplicitType(val2, TYPE_STRING);

                // look for an escape character beneath the node
                String escape = null;
                JPQLNode escapeNode = right(node).
                    findChildByID(JJTESCAPECHARACTER, true);
                if (escapeNode != null)
                    escape = trimQuotes(onlyChild(escapeNode).text);

                if (not)
                    return factory.notMatches(val1, val2, "_", "%", escape);
                else
                    return factory.matches(val1, val2, "_", "%", escape);

            case JJTISEMPTY:
                return evalNot(not,
                    factory.isEmpty(getValue(onlyChild(node))));

            case JJTSIZE:
                return factory.size(getValue(onlyChild(node)));

            case JJTINDEX:
                return factory.index(getValue(onlyChild(node)));

            case JJTUPPER:
                val1 = getValue(onlyChild(node));
                setImplicitType(val1, TYPE_STRING);
                return factory.toUpperCase(val1);

            case JJTLOWER:
                return factory.toLowerCase(getStringValue(onlyChild(node)));

            case JJTLENGTH:
                return factory.stringLength(getStringValue(onlyChild(node)));

            case JJTABS:
                return factory.abs(getNumberValue(onlyChild(node)));

            case JJTSQRT:
                return factory.sqrt(getNumberValue(onlyChild(node)));

            case JJTMOD:
                val1 = getValue(left(node));
                val2 = getValue(right(node));
                setImplicitTypes(val1, val2, TYPE_NUMBER);
                return factory.mod(val1, val2);

            case JJTTRIM: // TRIM([[where] [char] FROM] field)
                val1 = getValue(lastChild(node));
                setImplicitType(val1, TYPE_STRING);

                Boolean trimWhere = null;

                JPQLNode firstTrimChild = firstChild(node);

                if (node.getChildCount() > 1) {
                    trimWhere =
                        firstTrimChild.id == JJTTRIMLEADING ? Boolean.TRUE
                            :
                            firstTrimChild.id == JJTTRIMTRAILING ? Boolean.FALSE
                                : null;
                }

                Value trimChar;

                // if there are 3 children, then we know the trim
                // char is the second node
                if (node.getChildCount() == 3)
                    trimChar = getValue(secondChild(node));
                    // if there are two children, then we need to check to see
                    // if the first child is a leading/trailing/both node,
                    // or the trim character node
                else if (node.getChildCount() == 2
                    && firstTrimChild.id != JJTTRIMLEADING
                    && firstTrimChild.id != JJTTRIMTRAILING
                    && firstTrimChild.id != JJTTRIMBOTH)
                    trimChar = getValue(firstChild(node));
                    // othwerwise, we default to trimming the space character
                else
                    trimChar = factory.newLiteral(" ", Literal.TYPE_STRING);

                return factory.trim(val1, trimChar, trimWhere);

            case JJTCONCAT:
                if (node.children.length < 2)
                	throw parseException(EX_USER, "less-child-count",
                        new Object[]{ Integer.valueOf(2), node,
                            Arrays.asList(node.children) }, null);

                val1 = getValue(firstChild(node));
                val2 = getValue(secondChild(node));
                setImplicitType(val1, TYPE_STRING);
                setImplicitType(val2, TYPE_STRING);
                Value concat = factory.concat(val1, val2);
                for (int i = 2; i < node.children.length; i++) {
                	val2 = getValue(node.children[i]);
                    setImplicitType(val2, TYPE_STRING);
                	concat = factory.concat(concat, val2);
                }
                return concat;

            case JJTSUBSTRING:
                // Literals are forced to be Integers because PostgreSQL rejects Longs in SUBSTRING parameters.
                // This however does not help if an expression like 1+1 is passed as parameter.
                val1 = getValue(firstChild(node));
                JPQLNode child2 = secondChild(node);
                if (child2.id == JJTINTEGERLITERAL) {
                    val2 = getIntegerValue(child2);
                } else {
                    val2 = getValue(child2);
                }
                if (node.getChildCount() == 3) {
                    JPQLNode child3 = thirdChild(node);
                    if (child3.id == JJTINTEGERLITERAL) {
                        val3 = getIntegerValue(child3);
                    } else {
                        val3 = getValue(child3);
                    }
                }
                setImplicitType(val1, TYPE_STRING);
                setImplicitType(val2, Integer.TYPE);
                if (node.children.length == 3)
                    setImplicitType(val3, Integer.TYPE);

                return convertSubstringArguments(factory, val1, val2, val3);

            case JJTLOCATE:
                Value locatePath = getValue(firstChild(node));
                Value locateSearch = getValue(secondChild(node));
                Value locateFromIndex = null;
                // Literals are forced to be Integers because PostgreSQL rejects Longs in POSITION parameters.
                // This however does not help if an expression like 1+1 is passed as parameter.
                if (node.getChildCount() > 2) { // optional start index arg
                    JPQLNode child3 = thirdChild(node);
                    if (child3.id == JJTINTEGERLITERAL) {
                        locateFromIndex = getIntegerValue(child3);
                    } else
                        locateFromIndex = getValue(child3);
                }
                setImplicitType(locatePath, TYPE_STRING);
                setImplicitType(locateSearch, TYPE_STRING);

                if (locateFromIndex != null)
                    setImplicitType(locateFromIndex, Integer.TYPE);

                return factory.indexOf(locateSearch,
                    locateFromIndex == null ? locatePath
                        : factory.newArgumentList(locatePath, locateFromIndex));

            case JJTAGGREGATE:
                // simply pass-through while asserting a single child
                return eval(onlyChild(node));

            case JJTCOUNT:
                JPQLNode c = lastChild(node);
                if (c.id == JJTIDENTIFIER)
                    // count(e)
                    return factory.count(getPath(node, false, true));
                return factory.count(getValue(c));

            case JJTMAX:
                return factory.max(getNumberValue(onlyChild(node)));

            case JJTMIN:
                return factory.min(getNumberValue(onlyChild(node)));

            case JJTSUM:
                return factory.sum(getNumberValue(onlyChild(node)));

            case JJTAVERAGE:
                return factory.avg(getNumberValue(onlyChild(node)));

            case JJTDISTINCTPATH:
                return factory.distinct(getValue(onlyChild(node)));

            case JJTEXISTS:
                return factory.isNotEmpty((Value) eval(onlyChild(node)));

            case JJTANY:
                return factory.any((Value) eval(onlyChild(node)));

            case JJTALL:
                return factory.all((Value) eval(onlyChild(node)));

            case JJTSUBSELECT:
                return getSubquery(node);

            case JJTMEMBEROF:
                val1 = getValue(left(node), VAR_PATH);
                val2 = getValue(right(node), VAR_PATH);
                checkEmbeddable(val2);
                setImplicitContainsTypes(val2, val1, CONTAINS_TYPE_ELEMENT);
                return evalNot(not, factory.contains(val2, val1));

            case JJTCURRENTDATE:
                return factory.getCurrentDate(Date.class);

            case JJTCURRENTTIME:
                return factory.getCurrentTime(Time.class);

            case JJTCURRENTTIMESTAMP:
                return factory.getCurrentTimestamp(Timestamp.class);

            case JJTSELECTEXTENSION:
                assertQueryExtensions("SELECT");
                return eval(onlyChild(node));

            case JJTGROUPBYEXTENSION:
                assertQueryExtensions("GROUP BY");
                return eval(onlyChild(node));

            case JJTORDERBYEXTENSION:
                assertQueryExtensions("ORDER BY");
                return eval(onlyChild(node));

            case JJTDATELITERAL:
                return factory.newLiteral(node.text, Literal.TYPE_DATE);

            case JJTTIMELITERAL:
                return factory.newLiteral(node.text, Literal.TYPE_TIME);

            case JJTTIMESTAMPLITERAL:    
                return factory.newLiteral(node.text, Literal.TYPE_TIMESTAMP);

            default:
                throw parseException(EX_FATAL, "bad-tree",
                    new Object[]{ node }, null);
        }
    }
    
    private boolean useContains(boolean not, Value val1, Value val2, JPQLNode node) {
        if (isVerticalTypeInExpr(val1, node) && not)
            return false;
        else
            return (!(val2 instanceof Literal) && node.getChildCount() == 2);
    }
    
    private boolean isVerticalTypeInExpr(Value val, JPQLNode node) {
        if (node.id != JJTIN)
            return false;
        return factory.isVerticalType(val);
    }
    
    private Value getIntegerValue(JPQLNode node) {
        BigDecimal bigdec = new BigDecimal
        (node.text.endsWith("l") || node.text.endsWith("L")
            ? node.text.substring(0, node.text.length() - 1)
            : node.text).
        multiply(new BigDecimal(negative(node)));
        return factory.newLiteral(Integer.valueOf(bigdec.intValue()),
                Literal.TYPE_NUMBER);        
    }
    
    /**
     * Converts JPQL substring() function to OpenJPA ExpressionFactory 
     * substring() arguments.
     * 
     * @param val1 the original String
     * @param val2 the 1-based start index as per JPQL substring() semantics
     * @param val3 the length of the returned string as per JPQL semantics
     * 
     */
    public static Value convertSubstringArguments(ExpressionFactory factory, 
    		Value val1, Value val2, Value val3) {
        if (val3 != null)
            return factory.substring(val1, factory.newArgumentList(val2, val3));
        else
            return factory.substring(val1, val2);
    }
    private void assertQueryExtensions(String clause) {
        OpenJPAConfiguration conf = resolver.getConfiguration();
        switch(conf.getCompatibilityInstance().getJPQL()) {
            case Compatibility.JPQL_WARN:
                // check if we've already warned for this query-factory combo
                StoreContext ctx = resolver.getQueryContext().getStoreContext();
                String query = currentQuery();
                if (ctx.getBroker() != null && query != null) {
                    String key = getClass().getName() + ":" + query;
                    BrokerFactory factory = ctx.getBroker().getBrokerFactory();
                    Object hasWarned = factory.getUserObject(key);
                    if (hasWarned != null)
                        break;
                    else
                        factory.putUserObject(key, Boolean.TRUE);
                }
                Log log = conf.getLog(OpenJPAConfiguration.LOG_QUERY);
                if (log.isWarnEnabled())
                    log.warn(_loc.get("query-extensions-warning", clause,
                        currentQuery()));
                break;
            case Compatibility.JPQL_STRICT:
                throw new ParseException(_loc.get("query-extensions-error",
                    clause, currentQuery()).getMessage());
            case Compatibility.JPQL_EXTENDED:
                break;
            default:
                throw new IllegalStateException(
                    "Compatibility.getJPQL() == "
                        + conf.getCompatibilityInstance().getJPQL());
        }
    }

    public void setImplicitTypes(Value val1, Value val2, 
        Class<?> expected) {
        String currQuery = currentQuery();
        setImplicitTypes(val1, val2, expected, resolver, parameterTypes, 
            currQuery);
    }
    
    
    public static void setImplicitTypes(Value val1, Value val2, 
        Class<?> expected, Resolver resolver, OrderedMap<Object,Class<?>> parameterTypes,
        String currentQuery) {
        AbstractExpressionBuilder.setImplicitTypes(val1, val2, expected, 
            resolver);

        // as well as setting the types for conversions, we also need to
        // ensure that any parameters are declared with the correct type,
        // since the JPA spec expects that these will be validated
        Parameter param = val1 instanceof Parameter ? (Parameter) val1
            : val2 instanceof Parameter ? (Parameter) val2 : null;
        Path path = val1 instanceof Path ? (Path) val1
            : val2 instanceof Path ? (Path) val2 : null;

        // we only check for parameter-to-path comparisons
        if (param == null || path == null || parameterTypes == null)
            return;

        FieldMetaData fmd = path.last();
        if (fmd == null)
            return;

        if (expected == null)
            checkEmbeddable(path, currentQuery);

        Class<?> type = path.getType();
        if (type == null)
            return;

        Object paramKey = param.getParameterKey();
        if (paramKey == null)
            return;

        // make sure we have already declared the parameter
        if (parameterTypes.containsKey(paramKey))
            parameterTypes.put(paramKey, type);
    }

    private Value getStringValue(JPQLNode node) {
        return getTypeValue(node, TYPE_STRING);
    }

    private Value getNumberValue(JPQLNode node) {
        return getTypeValue(node, TYPE_NUMBER);
    }

    private Value getTypeValue(JPQLNode node, Class<?> implicitType) {
        Value val = getValue(node);
        setImplicitType(val, implicitType);
        return val;
    }

    private Value getSubquery(JPQLNode node) {
        final boolean subclasses = true;

        // parse the subquery
        ParsedJPQL parsed = new ParsedJPQL(node.parser.jpql, node);
        Context subContext = new Context(parsed, null, ctx());
        contexts.push(subContext);

        ClassMetaData candidate = getCandidateMetaData(node);
        Subquery subq = subContext.getSubquery();
        if (subq == null) {
            subq = factory.newSubquery(candidate, subclasses, nextAlias());
            subContext.setSubquery(subq);
        }
        subq.setMetaData(candidate);
        
        // evaluate from clause for resolving variables defined in subquery
        JPQLNode from = node.getChild(1);
        subContext.from = evalFromClause(from, true);

        try {
            QueryExpressions subexp = getQueryExpressions();
            subq.setQueryExpressions(subexp);
            if (subexp.projections.length > 0)
                checkEmbeddable(subexp.projections[0]);
            return subq;
        } finally {
            // remove the subquery parse context
            contexts.pop();
        }
    }

    /**
     * Creates and records the names and order of parameters. The parameters are
     * identified by a key with its type preserved. The second argument
     * determines whether the first argument is used as-is or converted to
     * an Integer as parameter key. 
     * 
     * @param the text as it appears in the parsed node
     * @param positional if true the first argument is converted to an integer
     * @param isCollectionValued true for collection-valued parameters
     */
    private Parameter getParameter(String id, boolean positional, 
        boolean isCollectionValued) {
        if (parameterTypes == null)
            parameterTypes = new OrderedMap<Object, Class<?>>();
        Object paramKey = positional ? Integer.parseInt(id) : id;
        if (!parameterTypes.containsKey(paramKey))
            parameterTypes.put(paramKey, TYPE_OBJECT);

        ClassMetaData meta = null;
        int index;
        if (positional) {
            try {
                // indexes in JPQL are 1-based, as opposed to 0-based in
                // the core ExpressionFactory
                index = Integer.parseInt(id) - 1;
            } catch (NumberFormatException e) {
                throw parseException(EX_USER, "bad-positional-parameter",
                    new Object[]{ id }, e);
            }

            if (index < 0)
                throw parseException(EX_USER, "bad-positional-parameter",
                    new Object[]{ id }, null);
        } else {
            index = parameterTypes.indexOf(id);
        }
        Parameter param = isCollectionValued 
            ? factory.newCollectionValuedParameter(paramKey, TYPE_OBJECT) 
            : factory.newParameter(paramKey, TYPE_OBJECT);
        param.setMetaData(meta);
        param.setIndex(index);
        
        return param;
    }

    /**
     * Checks to see if we should evaluate for a NOT expression.
     */
    private Expression evalNot(boolean not, Expression exp) {
        return not ? factory.not(exp) : exp;
    }

    /**
     * Trim off leading and trailing single-quotes, and then
     * replace any internal '' instances with ' (since repeating the
     * quote is the JPQL mechanism of escaping a single quote).
     */
    private String trimQuotes(String str) {
        if (str == null || str.length() <= 1)
            return str;

        if (str.startsWith("'") && str.endsWith("'"))
            str = str.substring(1, str.length() - 1);

        int index = -1;

        while ((index = str.indexOf("''", index + 1)) != -1)
            str = str.substring(0, index + 1) + str.substring(index + 2);

        return str;
    }

    /**
     * Trim off leading and trailing double-quotes.
     */
    private String trimDoubleQuotes(String str) {
        if (str == null || str.length() <= 1)
            return str;

        if (str.startsWith("\"") && str.endsWith("\""))
            str = str.substring(1, str.length() - 1);

        return str;
    }

    /**
     * An IntegerLiteral and DecimalLiteral node will
     * have a child node of Negative if it is negative:
     * if so, this method returns -1, else it returns 1.
     */
    private short negative(JPQLNode node) {
        if (node.children != null && node.children.length == 1
            && firstChild(node).id == JJTNEGATIVE)
            return -1;
        else
            return 1;
    }

    private Value getIdentifier(JPQLNode node) {
        final String name = node.text;
        final Value val = getVariable(name, false);

        ClassMetaData cmd = getMetaDataForAlias(name);

        if (cmd != null) {
            // handle the case where the class name is the alias
            // for the candidate (we don't use variables for this)
            Value thiz = null;
            if (ctx().subquery == null || 
                ctx().getSchema(name.toLowerCase()) == null) {
                if (ctx().subquery != null && inAssignSubselectProjection)
                    thiz = factory.newPath(ctx().subquery);
                else
                    thiz = factory.getThis();
            } else {
                thiz = factory.newPath(ctx().subquery);
            }
            ((Path)thiz).setSchemaAlias(name);
            thiz.setMetaData(cmd);
            return thiz;
        } else if (val instanceof Path) {
            return (Path) val;
        } else if (val instanceof Value) {
            if (val.isVariable()) {
                // can be an entity type literal
                Class<?> c = resolver.classForName(name, null);
                if (c != null) {
                    Value lit = factory.newTypeLiteral(c, Literal.TYPE_CLASS);
                    Class<?> candidate = getCandidateType();
                    ClassMetaData can = getClassMetaData(candidate.getName(),
                            false);
                    ClassMetaData meta = getClassMetaData(name, false);
                    if (candidate.isAssignableFrom(c))
                        lit.setMetaData(meta);
                    else
                        lit.setMetaData(can);
                    return lit;
                }
            }
            return (Value) val;
        }

        throw parseException(EX_USER, "unknown-identifier",
            new Object[]{ name }, null);
    }

    private Path validateMapPath(JPQLNode node, JPQLNode id) {
        Path path = (Path) getValue(id);
        FieldMetaData fld = path.last();

        if (fld == null && ctx().subquery != null) {
            Value var = getVariable(id.text, false);
            if (var != null) {
                path = factory.newPath(var);
                fld = path.last();
            }
        }
        
        if (fld != null) {            
            // validate the field is of type java.util.Map
            if (fld.getDeclaredTypeCode() != JavaTypes.MAP) {
                String oper = "VALUE";
                if (node.id == JJTENTRY)
                    oper = "ENTRY";        
                else if (node.id == JJTKEY)
                    oper = "KEY";
                throw parseException(EX_USER, "bad-qualified-identifier",
                    new Object[]{ id.text, oper}, null);
            }
        }
        else
            throw parseException(EX_USER, "unknown-type",
                new Object[]{ id.text}, null);
            
        return path;
    }

    private Value getGeneralIdentifier(JPQLNode node, boolean verifyEmbeddable) {
        JPQLNode id = onlyChild(node);
        Path path = validateMapPath(node, id);

        if (node.id == JJTKEY)
            path = (Path) factory.getKey(path);
        FieldMetaData fld = path.last();
        ClassMetaData meta = fld.getKey().getTypeMetaData();
        if (verifyEmbeddable &&
            (node.id == JJTKEY && meta != null && fld.getKey().isEmbedded()) ||
            (node.id == JJTVALUE && fld.isElementCollection() &&
                 fld.getElement().getEmbeddedMetaData() != null)) { 
                 // check basic type
            if (node.parent.parent.id == JJTGROUPBY)
                throw parseException(EX_USER, "cant-groupby-key-value-embeddable",
                    new Object[]{ node.id == JJTVALUE ? "VALUE" : "KEY", id.text }, null);
            else
                throw parseException(EX_USER, "bad-general-identifier",
                    new Object[]{ node.id == JJTVALUE ? "VALUE" : "KEY", id.text }, null);
        }
        return path;
    }

    private Value getQualifiedIdentifier(JPQLNode node) {
        JPQLNode id = onlyChild(node);               
        Path path = validateMapPath(node, id);

        if (node.id == JJTVALUE)
            return path;

        Value value = getValue(id);
        if (node.id == JJTKEY)
            return factory.mapKey(path, value);
        else            
            return factory.mapEntry(path, value);
    }

    private Path getQualifiedPath(JPQLNode node) {
        return getQualifiedPath(node, false, true);
    }

    private Path getQualifiedPath(JPQLNode node, boolean pcOnly, boolean inner)
    {
        int nChild = node.getChildCount();
        JPQLNode firstChild = firstChild(node);
        JPQLNode id = firstChild.id == JJTKEY ? onlyChild(firstChild) :
               firstChild;               
        Path path = validateMapPath(firstChild, id);

        if (firstChild.id == JJTIDENTIFIER)
            return getPath(node);

        FieldMetaData fld = path.last();
        path = (Path) factory.getKey(path);
        ClassMetaData meta = fld.getKey().getTypeMetaData();

        if (meta == null)
            throw parseException(EX_USER, "bad-qualified-path",
                new Object[]{ id.text }, null);
        
        path.setMetaData(meta);

        // walk through the children and assemble the path
        boolean allowNull = !inner;
        for (int i = 1; i < nChild; i++) {
            path = (Path) traversePath(path, node.children[i].text, pcOnly,
                allowNull);

            // all traversals but the first one will always be inner joins
            allowNull = false;
        }
        return path;
    }

    private Value getTypeLiteral(JPQLNode node) {
        JPQLNode type = onlyChild(node);
        final String name = type.text;
        final Value val = getVariable(name, false);

        if (val instanceof Value && val.isVariable()) {
            Class<?> c = resolver.classForName(name, null);
            if (c != null) {
                Value typeLit = factory.newTypeLiteral(c, Literal.TYPE_CLASS);
                typeLit.setMetaData(getClassMetaData(name, false));
                return typeLit;
            }
        }

        throw parseException(EX_USER, "not-type-literal",
            new Object[]{ name }, null);
    }

    private Value getPathOrConstant(JPQLNode node) {
        // first check to see if the path is an enum or static field, and
        // if so, load it
        String className = assemble(node, ".", 1);
        Class<?> c = resolver.classForName(className, null);
        if (c != null) {
            String fieldName = lastChild(node).text;
            int type = (c.isEnum() ? Literal.TYPE_ENUM : Literal.TYPE_UNKNOWN);
            try {
                Field field = c.getField(fieldName);
                Object value = field.get(null);
                return factory.newLiteral(value, type);
            } catch (NoSuchFieldException nsfe) {
                if (node.inEnumPath)
                    throw parseException(EX_USER, "no-field",
                        new Object[]{ c.getName(), fieldName }, nsfe);
                else
                    return getPath(node, false, true);
            } catch (Exception e) {
                throw parseException(EX_USER, "unaccessible-field",
                    new Object[]{ className, fieldName }, e);
            }
        } else {
            return getPath(node, false, true);
        }
    }

    /**
     * Process type_discriminator
     *     type_discriminator ::=
     *         TYPE(general_identification_variable |
     *         single_valued_object_path_expression |
     *         input_parameter )
     */
    private Value getType(JPQLNode node) {
        switch (node.id) {
        case JJTIDENTIFIER:
            return factory.type(getValue(node));

        case JJTNAMEDINPUTPARAMETER:
            return factory.type(getParameter(node.text, false, false));

        case JJTPOSITIONALINPUTPARAMETER:
            return factory.type(getParameter(node.text, true, false));

        case JJTGENERALIDENTIFIER:
            return factory.type(getQualifiedIdentifier(onlyChild(node)));

        default:
            // TODO: enforce jpa2.0 spec rules.
            // A single_valued_object_field is designated by the name of
            // an association field in a one-to-one or many-to-one relationship
            // or a field of embeddable class type.
            // The type of a single_valued_object_field is the abstract schema
            // type of the related entity or embeddable class
            Value path = getPath(node, false, true);
            return factory.type(path);
        }
    }

    private Path getPath(JPQLNode node) {
        return getPath(node, false, true);
    }

    private Path getPath(JPQLNode node, boolean pcOnly, boolean inner) {
        // resolve the first element against the aliases map ...
        // i.e., the path "SELECT x.id FROM SomeClass x where x.id > 10"
        // will need to have "x" in the alias map in order to resolve
        Path path = null;

        final String name = firstChild(node).text;
        final Value val = getVariable(name, false);

        // handle the case where the class name is the alias
        // for the candidate (we don't use variables for this)
        if (name.equalsIgnoreCase(ctx().schemaAlias)) {
            if (ctx().subquery != null) {
                path = factory.newPath(ctx().subquery);
                path.setMetaData(ctx().subquery.getMetaData());
            } else {
                path = factory.newPath();
                path.setMetaData(ctx().meta);
            }
        } else if (getMetaDataForAlias(name) != null)
            path = newPath(null, getMetaDataForAlias(name));
        else if (val instanceof Path)
            path = (Path) val;
        else if (val.getMetaData() != null)
            path = newPath(val, val.getMetaData());
        else
            throw parseException(EX_USER, "path-invalid",
                new Object[]{ assemble(node), name }, null);

        path.setSchemaAlias(name);

        // walk through the children and assemble the path
        boolean allowNull = !inner;
        for (int i = 1; i < node.children.length; i++) {
            if (path.isXPath()) {
                for (int j = i; j <node.children.length; j++)
                    path = (Path) traverseXPath(path, node.children[j].text);
                return path;
            }
            path = (Path) traversePath(path, node.children[i].text, pcOnly,
                allowNull);
            if (ctx().getParent() != null && ctx().getVariable(path.getSchemaAlias()) == null) {
                path.setSubqueryContext(ctx(), name);
            }
        
            // all traversals but the first one will always be inner joins
            allowNull = false;
        }

        return path;
    }

    protected Class<?> getDeclaredVariableType(String name) {
        ClassMetaData cmd = getMetaDataForAlias(name);
        if (cmd != null)
            return cmd.getDescribedType();

        if (name != null && name.equals(ctx().schemaAlias))
            return getCandidateType();

        // JPQL has no declared variables
        return null;
    }

    /**
     * Returns an Expression for the given node by eval'ing it.
     */
    private Expression getExpression(JPQLNode node) {
        Object exp = eval(node);

        // check for boolean values used as expressions
        if (!(exp instanceof Expression))
            return factory.asExpression((Value) exp);
        return (Expression) exp;
    }

    /**
     * Returns a Simple Case Expression for the given node by eval'ing it.
     */
    private Value getSimpleCaseExpression(JPQLNode node) {
        Object caseOperand = eval(node.getChild(0));
        int nChild = node.getChildCount();

        Object val = eval(lastChild(node));
        Object exp[] = new Expression[nChild - 2];
        for (int i = 1; i < nChild - 1; i++)
            exp[i-1] = eval(node.children[i]);
        
        return factory.simpleCaseExpression((Value) caseOperand,
            (Expression[]) exp, (Value) val);
    }

    /**
     * Returns a General Case Expression for the given node by eval'ing it.
     */
    private Value getGeneralCaseExpression(JPQLNode node) {
        int nChild = node.getChildCount();

        Object val = eval(lastChild(node));
        Object exp[] = new Expression[nChild - 1];
        for (int i = 0; i < nChild - 1; i++)
            exp[i] = (Expression) eval(node.children[i]);
        
        return factory.generalCaseExpression((Expression[]) exp, (Value) val);
    }

    private Expression getWhenCondition(JPQLNode node) {
        Object exp = eval(firstChild(node));
        Object val = eval(secondChild(node));
        return factory.whenCondition((Expression) exp, (Value) val);
    }

    private Expression getWhenScalar(JPQLNode node) {
        Object val1 = eval(firstChild(node));
        Object val2 = eval(secondChild(node));
        return factory.whenScalar((Value) val1, (Value) val2);
    }

    private Value getCoalesceExpression(JPQLNode node) {
        int nChild = node.getChildCount();
        
        Object vals[] = new Value[nChild];
        for (int i = 0; i < nChild; i++)
            vals[i] = eval(node.children[i]);
        
        return factory.coalesceExpression((Value[]) vals);
    }

    private Value getNullIfExpression(JPQLNode node) {
        Object val1 = eval(firstChild(node));
        Object val2 = eval(secondChild(node));
        
        return factory.nullIfExpression((Value) val1, (Value) val2);
    }

    private Value getValue(JPQLNode node) {
        if (node.id == JJTQUALIFIEDIDENTIFIER)
            return getQualifiedIdentifier(onlyChild(node));
        return getValue(node, VAR_PATH);
    }

    private Path newPath(Value val, ClassMetaData meta) {
        Path path = val == null ? factory.newPath() : factory.newPath(val);
        if (meta != null)
            path.setMetaData(meta);
        return path;
    }

    /**
     * Returns a Value for the given node by eval'ing it.
     */
    private Value getValue(JPQLNode node, int handleVar) {
        Value val = (Value) eval(node);

        // determined how to evaluate a variable
        if (!val.isVariable())
            return val;
        else if (handleVar == VAR_PATH && !(val instanceof Path))
            return newPath(val, val.getMetaData());
        else if (handleVar == VAR_ERROR)
            throw parseException(EX_USER, "unexpected-var",
                new Object[]{ node.text }, null);
        else
            return val;
    }

    ////////////////////////////
    // Parse Context Management
    ////////////////////////////

    private Context ctx() {
        return  contexts.peek();
    }

    private JPQLNode root() {
        return ctx().parsed.root;
    }

    private ClassMetaData getMetaDataForAlias(String alias) {
        for (int i = contexts.size() - 1; i >= 0; i--) {
            Context context =  contexts.get(i);
            if (alias.equalsIgnoreCase(context.schemaAlias))
                return context.meta;
        }

        return null;
    }

    protected void addSchemaToContext(String id, ClassMetaData meta) {
        ctx().addSchema(id.toLowerCase(), meta);    
    }

    protected void addVariableToContext(String id, Value var) {
        ctx().addVariable(id, var);
    }

    protected Value getVariable(String var) {
        Context c = ctx();
        Value v = c.getVariable(var);
        if (v != null)
            return v;
        if (c.getParent() != null)
            return c.getParent().findVariable(var);

        return null;
    }

    ////////////////////////////
    // Node traversal utilities
    ////////////////////////////

    private JPQLNode onlyChild(JPQLNode node)
        throws UserException {
        JPQLNode child = firstChild(node);

        if (node.children.length > 1)
            throw parseException(EX_USER, "multi-children",
                new Object[]{ node, Arrays.asList(node.children) }, null);

        return child;
    }

    /**
     * Returns the left node (the first of the children), and asserts
     * that there are exactly two children.
     */
    private JPQLNode left(JPQLNode node) {
        return child(node, 0, 2);
    }

    /**
     * Returns the right node (the second of the children), and asserts
     * that there are exactly two children.
     */
    private JPQLNode right(JPQLNode node) {
        return child(node, 1, 2);
    }

    private JPQLNode child(JPQLNode node, int childNum, int assertCount) {
        if (node.children.length != assertCount)
            throw parseException(EX_USER, "wrong-child-count",
                new Object[]{ Integer.valueOf(assertCount), node,
                    Arrays.asList(node.children) }, null);

        return node.children[childNum];
    }

    private JPQLNode firstChild(JPQLNode node) {
        if (node.children == null || node.children.length == 0)
            throw parseException(EX_USER, "no-children",
                new Object[]{ node }, null);
        return node.children[0];
    }

    private static JPQLNode secondChild(JPQLNode node) {
        return node.children[1];
    }

    private static JPQLNode thirdChild(JPQLNode node) {
        return node.children[2];
    }

    private static JPQLNode lastChild(JPQLNode node) {
        return lastChild(node, 0);
    }

    /**
     * The Nth from the last child. E.g.,
     * lastChild(1) will return the second-to-the-last child.
     */
    private static JPQLNode lastChild(JPQLNode node, int fromLast) {
        return node.children[node.children.length - (1 + fromLast)];
    }

    /**
     * Base node that will be generated by the JPQLExpressionBuilder; base
     * class of the {@link SimpleNode} that is used by {@link JPQL}.
     *
     * @author Marc Prud'hommeaux
     * @see Node
     * @see SimpleNode
     */
    @SuppressWarnings("serial")
    protected abstract static class JPQLNode
        implements Node, Serializable {

        final int id;
        final JPQL parser;
        JPQLNode parent;
        JPQLNode[] children;
        String text;
        boolean not = false;
        boolean inEnumPath = false;

        public JPQLNode(JPQL parser, int id) {
            this.id = id;
            this.parser = parser;
            this.inEnumPath = parser.inEnumPath;
        }

        public void jjtOpen() {
        }

        public void jjtClose() {
        }

        JPQLNode[] findChildrenByID(int id) {
            Collection<JPQLNode> set = new LinkedHashSet<JPQLNode>();
            findChildrenByID(id, set);
            return set.toArray(new JPQLNode[set.size()]);
        }

        private void findChildrenByID(int id, Collection<JPQLNode> set) {
            for (int i = 0; children != null && i < children.length; i++) {
                if (children[i].id == id)
                    set.add(children[i]);

                children[i].findChildrenByID(id, set);
            }
        }

        boolean hasChildID(int id) {
            return findChildByID(id, false) != null;
        }

        JPQLNode findChildByID(int id, boolean recurse) {
            for (int i = 0; children != null && i < children.length; i++) {
                JPQLNode child = children[i];

                if (child.id == id)
                    return children[i];

                if (recurse) {
                    JPQLNode found = child.findChildByID(id, recurse);
                    if (found != null)
                        return found;
                }
            }

            // not found
            return null;
        }

        public void jjtSetParent(Node parent) {
            this.parent = (JPQLNode) parent;
        }

        public Node jjtGetParent() {
            return this.parent;
        }

        public void jjtAddChild(Node n, int i) {
            if (children == null) {
                children = new JPQLNode[i + 1];
            } else if (i >= children.length) {
                JPQLNode c[] = new JPQLNode[i + 1];
                System.arraycopy(children, 0, c, 0, children.length);
                children = c;
            }

            children[i] = (JPQLNode) n;
        }

        public Node jjtGetChild(int i) {
            return children[i];
        }

        public int getChildCount() {
            return jjtGetNumChildren();
        }

        public JPQLNode getChild(int index) {
            return (JPQLNode) jjtGetChild(index);
        }

        public Iterator<JPQLNode> iterator() {
            return Arrays.asList(children).iterator();
        }

        public int jjtGetNumChildren() {
            return (children == null) ? 0 : children.length;
        }

        void setText(String text) {
            this.text = text;
        }

        void setToken(Token t) {
            setText(t.image);
        }

        public String toString() {
            return JPQLTreeConstants.jjtNodeName[this.id];
        }

        public String toString(String prefix) {
            return prefix + toString();
        }

        /**
         * Debugging method.
         *
         * @see #dump(java.io.PrintStream,String)
         */
        public void dump(String prefix) {
            dump(System.out, prefix);
        }

        public void dump() {
            dump(" ");
        }

        /**
         * Debugging method to output a parse tree.
         *
         * @param out the stream to which to write the debugging info
         * @param prefix the prefix to write out before lines
         */
        public void dump(PrintStream out, String prefix) {
            dump(out, prefix, false);
        }

        public void dump(PrintStream out, String prefix, boolean text) {
            out.println(toString(prefix)
                + (text && this.text != null ? " [" + this.text + "]" : ""));
            if (children != null) {
                for (int i = 0; i < children.length; ++i) {
                    JPQLNode n = (JPQLNode) children[i];
                    if (n != null) {
                        n.dump(out, prefix + " ", text);
                    }
                }
            }
        }
    }

    /**
     * Public for unit testing purposes.
     * @nojavadoc
     */
    @SuppressWarnings("serial")
    public static class ParsedJPQL
        implements Serializable {

        // This is only ever used during parse; when ParsedJPQL instances
        // are serialized, they will have already been parsed.
        private final transient JPQLNode root;

        private final String query;
        
        // cache of candidate type data. This is stored here in case this  
        // parse tree is reused in a context that does not know what the 
        // candidate type is already. 
        private Class<?> _candidateType;

        ParsedJPQL(String jpql) {
            this(jpql, parse(jpql));
        }

        ParsedJPQL(String query, JPQLNode root) {
            this.root = root;
            this.query = query;
        }

        private static JPQLNode parse(String jpql) {
            if (jpql == null)
                jpql = "";

            try {
                return (JPQLNode) new JPQL(jpql).parseQuery();
            } catch (Error e) {
                // special handling for Error subclasses, which the
                // parser may sometimes (unfortunately) throw
                throw new UserException(_loc.get("parse-error",
                    new Object[]{ e.toString(), jpql }));
            }
        }

        void populate(ExpressionStoreQuery query) {
            QueryContext ctx = query.getContext();

            // if the owning query's context does not have
            // any candidate class, then set it here
            if (ctx.getCandidateType() == null) {
                if (_candidateType == null)
                    _candidateType = new JPQLExpressionBuilder
                        (null, query, this).getCandidateType();
                ctx.setCandidateType(_candidateType, true);
            }
        }
        
        /**
         * Public for unit testing purposes.
         */
        public Class<?> getCandidateType() {
            return _candidateType;
        }

        public String toString ()
		{
			return this.query;
		}
	}
    
    
    // throws an exception if there are numeric parameters which do not start with 1.
    private void validateParameters() {
        if (parameterTypes == null || parameterTypes.isEmpty()) {
            return;
        }

        boolean numericParms = false;
        boolean namedParms = false;

        for (Object key : parameterTypes.keySet()) {

            if (key instanceof Number) {
                if (namedParms) {
                    throw new UserException(_loc.get("mixed-parameter-types", resolver.getQueryContext()
                        .getQueryString(), parameterTypes.keySet().toString()));
                }
                numericParms = true;
            } else {
                if (numericParms) {
                    throw new UserException(_loc.get("mixed-parameter-types", resolver.getQueryContext()
                        .getQueryString(), parameterTypes.keySet().toString()));
                }
                namedParms = true;
            }
        }

        if (numericParms) {
            if (!parameterTypes.keySet().contains(1)) {
                throw new UserException(_loc.get("missing-positional-parameter", resolver.getQueryContext()
                    .getQueryString(), parameterTypes.keySet().toString()));
            }
        }
    }
}

