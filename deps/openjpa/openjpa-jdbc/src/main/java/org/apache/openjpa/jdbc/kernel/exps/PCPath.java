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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStoreQuery;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.Strategy;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.strats.HandlerCollectionTableFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.HandlerHandlerMapTableFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.LRSMapFieldStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Schemas;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.kernel.exps.CandidatePath;
import org.apache.openjpa.kernel.exps.Context;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.XMLMetaData;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.UserException;

/**
 * A path represents a traversal into fields of a candidate object.
 *
 * @author Abe White
 */
public class PCPath
    extends CandidatePath
    implements JDBCPath {
	
    protected static final String TRUE = "1 = 1";
    protected static final String FALSE = "1 <> 1";

    private static final int PATH = 0;
    private static final int BOUND_VAR = 1;
    private static final int UNBOUND_VAR = 2;
    private static final int UNACCESSED_VAR = 3;
    private static final int XPATH = 4;
    private static final int OBJECT_PATH = 5;

    private static final Localizer _loc = Localizer.forPackage(PCPath.class);

    private final ClassMapping _candidate;
    private ClassMapping _class = null;
    private boolean _key = false;
    private int _type = PATH;
    private String _varName = null;
    private Class _cast = null;
    private boolean _cid = false;
    private FieldMetaData _xmlfield = null;
    private boolean _keyPath = false;
    private String _schemaAlias = null;

    /**
     * Return a path starting with the 'this' ptr.
     */
    public PCPath(ClassMapping type) {
        _candidate = type;
    }

    /**
     * Return a path starting from the given variable.
     */
    public PCPath(ClassMapping candidate, Variable var) {
        _candidate = candidate;
        _actions = new LinkedList();

        PCPath other = var.getPCPath();
        Action action = new Action();
        action.var = var.getName();
        if (other == null) {
            _type = UNBOUND_VAR;
            action.op = Action.UNBOUND_VAR;
            action.data = var;
            _schemaAlias = var.getName();
        } else {
            // bound variable; copy path
            _type = UNACCESSED_VAR;
            _actions.addAll(other._actions);
            _key = other._key;

            action.op = Action.VAR;
            action.data = var.getName();
            _schemaAlias = other._schemaAlias;
            _correlationVar = other._correlationVar;
        }
        _actions.add(action);
        _cast = var.getType(); // initial type is var type
    }

    /**
     * Return a path starting from the given subquery.
     */
    public PCPath(SubQ sub) {
        _candidate = sub.getCandidate();
        _actions = new LinkedList();

        Action action = new Action();
        action.op = Action.SUBQUERY;
        action.data = sub.getCandidateAlias();
        _actions.add(action);
        _cast = sub.getType(); // initial type is subquery type
        _varName = sub.getCandidateAlias();
    }

    public void setSchemaAlias(String schemaAlias) {
        if (_schemaAlias == null) 
            _schemaAlias = schemaAlias;
    }

    public String getSchemaAlias() {
        return _schemaAlias;
    }
    
    public void setSubqueryContext(Context context, String correlationVar) {
        Action action = lastFieldAction();
        if (action == null)
            return;
        action.context = context;
        _correlationVar = correlationVar;
    }
    
    /**
     * Set the path as a binding of the given variable.
     */
    public void addVariableAction(Variable var) {
        _varName = var.getName();
    }

    /**
     * Return true if this is a bound variable that has not been accessed
     * after binding. Useful for filters like
     * "coll.contains (var) &amp;&amp; var == null", which should really
     * just act like "coll.contains (null)".
     */
    public boolean isUnaccessedVariable() {
        return _type == UNACCESSED_VAR;
    }

    /**
     * Return whether this is a path involving a variable.
     */
    public boolean isVariablePath() {
        return _type != PATH;
    }

    /**
     * If this path is part of a contains clause, then alias it to the
     * proper contains id before initialization.
     */
    public synchronized void setContainsId(String id) {
        if (_cid)
            return;

        Action last = _actions == null ? null : (Action) _actions.getLast();
        if (last != null && last.op == Action.VAR && ((String)last.data).equals(last.var)) {
            _cid = true;
            return;
        }            
            
        // treat it just like a unique variable
        Action action = new Action();
        action.op = Action.VAR;
        action.data = id;
        if (_actions == null)
            _actions = new LinkedList();
        _actions.add(action);
        _cid = true;
    }

    public ClassMetaData getMetaData() {
        return _class;
    }

    public void setMetaData(ClassMetaData meta) {
        _class = (ClassMapping) meta;
    }

    public boolean isKey() {
        return _key;
    }
    
    public boolean isXPath() {
        return _type == XPATH;
    }
    
    public String getXPath() {
        StringBuilder xpath = new StringBuilder();
        Action action;
        Iterator itr = _actions.iterator();
        
        // Skip variable actions since they are not part of the xpath
        // until we reach the first xpath action.
        // The first xpath action maps to the root of an xml document.
        do 
            action = (Action) itr.next(); 
        while (action.op != Action.GET_XPATH);
        
        // Skip XmlRootElement:
        // We can't rely on the accuracy of the name of the root element,
        // because it could be set to some default by JAXB XML Binding.
        // The caller(DBDictionary) should start with "/*" or "/*/",
        // we build the remaining xpath that follows the root element.
        while (itr.hasNext()) {
            action = (Action) itr.next();
            if (((XMLMetaData) action.data).getXmlname() != null)
                xpath.append(((XMLMetaData) action.data).getXmlname());
            else
                xpath.append("*");
            if (itr.hasNext())
                xpath.append("/");
        }
        return xpath.toString();
    }
    
    public String getPCPathString() {
        if (_actions == null)
            return (_varName == null) ? "" : _varName + ".";

        StringBuilder path = new StringBuilder();
        Action action;
        for (Iterator itr = _actions.iterator(); itr.hasNext();) {
            action = (Action) itr.next();
            if (action.op == Action.VAR || action.op == Action.SUBQUERY)
                path.append(action.data);
            else if (action.op == Action.UNBOUND_VAR)
                path.append(((Variable) action.data).getName());
            else 
                path.append(((FieldMapping) action.data).getName());
            path.append('.');
        }
        if (_varName != null)
            path.append(_varName).append('.');
        return path.toString();
    }

    public ClassMapping getClassMapping(ExpState state) {
        PathExpState pstate = (PathExpState) state;
        if (pstate.field == null)
            return _class;
        if (_key) {
            if (pstate.field.getKey().getTypeCode() == JavaTypes.PC)
                return pstate.field.getKeyMapping().getTypeMapping();
            return null;
        }
        if (pstate.field.getElement().getTypeCode() == JavaTypes.PC) {
            if (pstate.field.isElementCollection() &&
                pstate.field.getElement().isEmbedded())
                pstate.isEmbedElementColl = true;
            return pstate.field.getElementMapping().getTypeMapping();
        }
        if (pstate.field.getTypeCode() == JavaTypes.PC)
            return pstate.field.getTypeMapping();
        return null;
    }

    public FieldMapping getFieldMapping(ExpState state) {
        return ((PathExpState) state).field;
    }

    public Column[] getColumns(ExpState state) {
        PathExpState pstate = (PathExpState) state;
        if (pstate.cols == null)
            pstate.cols = calculateColumns(pstate);
        return pstate.cols;
    }

    /**
     * The columns used by this path.
     */
    private Column[] calculateColumns(PathExpState pstate) {
        if (_key ||
            (_keyPath && pstate.field.getKey() != null &&
                !pstate.field.getKey().isEmbedded())) {
            if (!pstate.joinedRel 
                && pstate.field.getKey().getValueMappedBy() != null)
                joinRelation(pstate, _key, false, false);
            else if (pstate.joinedRel 
                && pstate.field.getKey().getTypeCode() == JavaTypes.PC)
                return pstate.field.getKeyMapping().getTypeMapping().
                    getPrimaryKeyColumns();
            return pstate.field.getKeyMapping().getColumns();
        }
        if (pstate.field != null) {
            switch (pstate.field.getTypeCode()) {
                case JavaTypes.MAP:
                case JavaTypes.ARRAY:
                case JavaTypes.COLLECTION:
                    ValueMapping elem = pstate.field.getElementMapping();
                    if (pstate.field.isElementCollection() && pstate.field.getElement().isEmbedded()) {
                        Strategy strategy = pstate.field.getStrategy();
                        if (strategy instanceof HandlerCollectionTableFieldStrategy) {
                            return ((HandlerCollectionTableFieldStrategy) strategy).
                                getElementColumns(elem.getTypeMapping());
                        } else if (strategy instanceof HandlerHandlerMapTableFieldStrategy) {
                            return ((HandlerHandlerMapTableFieldStrategy) strategy).
                            getValueColumns(elem.getTypeMapping());
                        }
                    }
                    if (pstate.joinedRel && elem.getTypeCode() == JavaTypes.PC)
                        return elem.getTypeMapping().getPrimaryKeyColumns();
                    if (elem.getColumns().length > 0)
                        return elem.getColumns();
                    return pstate.field.getColumns();
                case JavaTypes.PC:
                    if (pstate.joinedRel)
                        return pstate.field.getTypeMapping().
                            getPrimaryKeyColumns();
                    return pstate.field.getColumns();
                default:
                    return pstate.field.getColumns();
            }
        }
        return (_class == null) ? Schemas.EMPTY_COLUMNS
            : _class.getPrimaryKeyColumns();
    }

    public boolean isVariable() {
        if (_actions == null)
            return false;
        Action action = (Action) _actions.getLast();
        return action.op == Action.UNBOUND_VAR || action.op == Action.VAR; 
    }

    public void get(FieldMetaData field, boolean nullTraversal) {
        if (_actions == null)
            _actions = new LinkedList();
        Action action = new Action();
        action.op = (nullTraversal) ? Action.GET_OUTER : Action.GET;
        action.data = field;
        _actions.add(action);
        if (_type == UNACCESSED_VAR)
            _type = BOUND_VAR;
        _cast = null;
        _key = false;
    }
    
    public void get(FieldMetaData fmd, XMLMetaData meta) {
        if (_actions == null)
            _actions = new LinkedList();
        Action action = new Action();
        action.op = Action.GET_XPATH;
        action.data = meta;
        _actions.add(action);
        _cast = null;
        _key = false;
        _type = XPATH;
        _xmlfield = fmd;
    }
    
    public void get(XMLMetaData meta, String name) {
        Action action = new Action();
        action.op = Action.GET_XPATH;
        action.data = meta.getFieldMapping(name);
        _actions.add(action);
        _cast = null;
        _key = false;
        _type = XPATH;
    }
    
    public XMLMetaData getXmlMapping() {
        Action act = (Action) _actions.getLast();
        if (act != null)
            return (XMLMetaData) act.data;
        return null;
    }

    public synchronized void getKey() {
        if (_cid)
            return;

        // replace the last field action to a get key
        Action action = lastFieldAction();
        Action key = new Action();
        key.op = Action.GET_KEY;
        key.data = action.data;
        int pos = _actions.indexOf(action);
        _actions.remove(action);
        _actions.add(pos, key);
        _cast = null;
        _key = true;
        _type = PATH;
    }

    private void checkObjectPathInheritanceTypeJoined(PathExpState pstate) {
        // if this mapping is in InheritanceType.JOINED,
        // then add joins
         ClassMapping base = _class;
         while (base.getJoinablePCSuperclassMapping() != null)
             base = base.getJoinablePCSuperclassMapping();
         if (base != _class) {
             ClassMapping from = _class;
             ClassMapping to = base;
             _type = OBJECT_PATH;
             for (; from != null && from != to; from = from.getJoinablePCSuperclassMapping()) {
                 pstate.joins = from.joinSuperclass(pstate.joins, false);
             }
         }
    }

    public FieldMetaData last() {
        Action act = lastFieldAction();
        return (act == null) ? null : isXPath() ?
            _xmlfield : (FieldMetaData) act.data;
    }

    /**
     * Return the last action that gets a field.
     */
    private Action lastFieldAction() {
        if (_actions == null)
            return null;

        if (isXPath())
            return (Action) _actions.getLast();
        
        ListIterator itr = _actions.listIterator(_actions.size());
        Action prev;
        while (itr.hasPrevious()) {
            prev = (Action) itr.previous();
            if (prev.op == Action.GET || prev.op == Action.GET_OUTER
                || prev.op == Action.GET_KEY)
                return prev;
        }
        return null;
    }

    public Class getType() {
        if (_cast != null)
            return _cast;
        Action act = lastFieldAction();
        if (act != null && act.op == Action.GET_XPATH)
            return ((XMLMetaData) act.data).getType();

        FieldMetaData fld = (act == null) ? null : (FieldMetaData) act.data;
        boolean key = act != null && act.op == Action.GET_KEY;
        if (fld != null) {
            switch (fld.getDeclaredTypeCode()) {
                case JavaTypes.ARRAY:
                    if (fld.getDeclaredType() == byte[].class
                        || fld.getDeclaredType() == Byte[].class
                        || fld.getDeclaredType() == char[].class
                        || fld.getDeclaredType() == Character[].class)
                        return fld.getDeclaredType();
                    return fld.getElement().getDeclaredType();
                case JavaTypes.MAP:
                    if (key)
                        return fld.getKey().getDeclaredType();
                    return fld.getElement().getDeclaredType();
                case JavaTypes.COLLECTION:
                    return fld.getElement().getDeclaredType();
                default:
                    return fld.getDeclaredType();
            }
        }
        if (_class != null)
            return _class.getDescribedType();
        return Object.class;
    }

    public void setImplicitType(Class type) {
        _cast = type;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        PathExpState pstate = new PathExpState(sel.newJoins());
        boolean key = false;
        boolean forceOuter = false;
        ClassMapping rel = _candidate;

        sel.setSchemaAlias(_schemaAlias);

        // iterate to the final field
        ClassMapping owner;
        ClassMapping from, to;
        Action action;
        Variable var;
        Iterator itr = (_actions == null) ? null : _actions.iterator();
        FieldMapping field = null;
        Action prevaction = null;
        boolean isCorrelatedPath = false;
        boolean fromParentRootInSubselect = navigateFromParentRootInSubselect(sel);
                
        while (itr != null && itr.hasNext()) {
            action = (Action) itr.next();

            // treat subqueries like variables for alias generation purposes
            if (action.op == Action.VAR) {
                if (sel.getParent() != null && action.var != null &&
                    prevaction != null && prevaction.data != null &&
                    sel.ctx().getVariable(action.var) == null) {
                    isCorrelatedPath = true;
                    pstate.joins = pstate.joins.setCorrelatedVariable(action.var);
                } else 
                    pstate.joins = pstate.joins.setVariable((String) action.data);
            }
            else if (action.op == Action.SUBQUERY) {
                pstate.joins = pstate.joins.setSubselect((String) action.data);
            }
            else if (action.op == Action.UNBOUND_VAR) {
                // unbound vars are cross-joined to the candidate table
                var = (Variable) action.data;
                rel = (ClassMapping) var.getMetaData();
                if (rel == null)
                	throw new IllegalArgumentException(_loc.get(
                	    "invalid-unbound-var", var.getName()).toString());
                	    
                if (sel.getParent() != null && action.var != null &&
                    sel.ctx().getVariable(action.var) == null) {
                    //System.out.println("Correlated action var="+action.var);
                    isCorrelatedPath = true;
                    pstate.joins = pstate.joins.setCorrelatedVariable(var.getName());
                } else                 
                    pstate.joins = pstate.joins.setVariable(var.getName());

                pstate.joins = pstate.joins.crossJoin(_candidate.getTable(),
                    rel.getTable());
                if (!itr.hasNext() && isVariable()) {
                    checkObjectPathInheritanceTypeJoined(pstate);
                }
            } else {
                // move past the previous field, if any
                field = (FieldMapping) ((action.op == Action.GET_XPATH) ?
                    _xmlfield : action.data);

                if (pstate.field != null) {
                    // if this is the second-to-last field and the last is
                    // the related field this field joins to, no need to
                    // traverse: just use this field's fk columns
                    if (!itr.hasNext() && (flags & JOIN_REL) == 0
                        && isJoinedField(pstate.field, key, field)) {
                        pstate.cmpfield = field;
                        break;
                    }
                    
                    if (fromParentRootInSubselect) {
                        isCorrelatedPath = true;
                        pstate.joins = pstate.joins.setCorrelatedVariable(_schemaAlias);
                        pstate.joins.setJoinContext(null);
                    }
                    
                    rel = traverseField(pstate, key, forceOuter, false);
                }

                // mark if the next traversal should go through
                // the key rather than value
                key = action.op == Action.GET_KEY;
                forceOuter |= action.op == Action.GET_OUTER;
                
                if (key && itr.hasNext())
                    _keyPath = true;

                // get mapping for the current field
                pstate.field = field;

                owner = pstate.field.getDefiningMapping();
                if (pstate.field.getManagement() 
                    != FieldMapping.MANAGE_PERSISTENT)
                    throw new UserException(_loc.get("non-pers-field", 
                        pstate.field));

                // find the most-derived type between the declared relation
                // type and the field's owner, and join from that type to
                // the lesser derived type
                if (rel != owner && rel != null) {
                    if (rel.getDescribedType().isAssignableFrom
                        (owner.getDescribedType())) {
                        from = owner;
                        to = rel;
                    } else {
                        from = rel;
                        to = owner;
                    }

                    for (; from != null && from != to;
                        from = from.getJoinablePCSuperclassMapping()) {
                    	FieldMapping cast = from.getFieldMapping(pstate.field
                    			.getName());
                    	if (cast != null)
                    		pstate.field = cast;
                        pstate.joins = from.joinSuperclass(pstate.joins, false);
                    }
                }
                // nothing more to do from here on as we encountered an xpath
                // action
                if (action.op == Action.GET_XPATH)
                    break;
            }
            prevaction = action;
            if (prevaction != null && prevaction.context != null) {
                Context jCtx = JDBCStoreQuery.getThreadLocalContext(prevaction.context);
                pstate.joins = pstate.joins.setJoinContext(jCtx);
            }
        }
        if (_varName != null)
            pstate.joins = pstate.joins.setVariable(_varName);

        // if we're not comparing to null or doing an isEmpty, then
        // join into the data on the final field; obviously we can't do these
        // joins when comparing to null b/c the whole purpose is to see
        // whether the joins even exist
        if ((flags & NULL_CMP) == 0)
            traverseField(pstate, key, forceOuter, true);
        pstate.joinedRel = false;
        if ((flags & JOIN_REL) != 0)
            joinRelation(pstate, key, forceOuter || (flags & FORCE_OUTER) != 0,
                false);
        if (isCorrelatedPath) {
            // check if there are joins that belong to parent
            pstate.joins.moveJoinsToParent();
        }
        pstate.joins.setJoinContext(null);
        
        if (_actions == null) {
            String subqAlias = findSubqAlias(sel);
            pstate.joins = pstate.joins.setSubselect(subqAlias);
            pstate.joins.setCorrelatedVariable(_schemaAlias);
            checkObjectPathInheritanceTypeJoined(pstate);
        }
        
        return pstate;
    }
    
    private String findSubqAlias(Select sel) {
        Select pSel = sel.getParent();
        if (pSel == null)
            return null;
        Context pCtx = pSel.ctx();
        if (pCtx.subquery == null)
            return null;
        if (pCtx.getSchema(_schemaAlias) != null)
            return ((SubQ)pCtx.subquery).getCandidateAlias();
        return findSubqAlias(pSel);
    }

    /**
     * When a PCPath is in subselect, and it is simply a navigation
     * from the parent root, the joins involved in this PCPath
     * must happen in the main select.  
     */
    private boolean navigateFromParentRootInSubselect(Select sel) {
        if (sel.getParent() == null)
            return false;
        Iterator itr = (_actions == null) ? null : _actions.iterator();

        boolean hasVar = false;
        boolean startsWithSubquery = false;
        while (itr != null && itr.hasNext()) {
            Action action = (Action) itr.next();
            if (action.op == Action.VAR) 
                hasVar = true;
            else if (action.op == Action.SUBQUERY)
                startsWithSubquery = true;
        }
        return !hasVar && !startsWithSubquery && sel.ctx().getSchema(_schemaAlias) == null;
    }
    
    /**
     * Return whether the given source field joins to the given target field.
     */
    private static boolean isJoinedField(FieldMapping src, boolean key, 
        FieldMapping target) {
        ValueMapping vm;
        switch (src.getTypeCode()) {
            case JavaTypes.ARRAY:
            case JavaTypes.COLLECTION:
                vm = src.getElementMapping();
                break;
            case JavaTypes.MAP:
                vm = (key) ? src.getKeyMapping() : src.getElementMapping();
                break;
            default:
                vm = src;
        }
        if (vm.getJoinDirection() != ValueMapping.JOIN_FORWARD)
            return false;
        ForeignKey fk = vm.getForeignKey();
        if (fk == null)
            return false; 
        
        // foreign key must join to target columns
        Column[] rels = fk.getColumns();
        Column[] pks = target.getColumns(); 
        if (rels.length != pks.length)
            return false;
        for (int i = 0; i < rels.length; i++)
            if (fk.getPrimaryKeyColumn(rels[i]) != pks[i])
                return false;
        return true;
    }
    
    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        if (_actions == null)
            return candidate;

        Action action;
        OpenJPAStateManager sm;
        Broker tmpBroker;
        for (Iterator itr = _actions.iterator(); itr.hasNext();) {
            action = (Action)itr.next();
            sm = null;
            tmpBroker = null;
            if (ImplHelper.isManageable(candidate))
                sm = (OpenJPAStateManager) (ImplHelper.toPersistenceCapable(
                    candidate, ctx.getConfiguration())).
                    pcGetStateManager();
            if (sm == null) {
                tmpBroker = ctx.getBroker();
                tmpBroker.transactional(candidate, false, null);
                sm = tmpBroker.getStateManager(candidate);
            }
            if (action.op != Action.GET && action.op != Action.GET_OUTER)
                continue;
            try {
                candidate = sm.fetchField(
                        ((FieldMapping)action.data).getIndex(), true);
            } catch (ClassCastException cce) {
                throw new RuntimeException(action.data + " not a field path");
            } finally {
                // transactional does not clear the state, which is
                // important since tmpCandidate might be also managed by
                // another broker if it's a proxied non-pc instance
                if (tmpBroker != null)
                    tmpBroker.nontransactional(sm.getManagedInstance(), null);
            }
        }
        return candidate;
    }

    /**
     * Expression state.
     */
    public static class PathExpState
        extends ExpState {

        public FieldMapping field = null;
        public FieldMapping cmpfield = null;
        public Column[] cols = null;
        public boolean joinedRel = false;
        public boolean isEmbedElementColl = false;
        
        public PathExpState(Joins joins) {
            super(joins);
        }
    }

    /**
     * Traverse into the previous field of a relation path.
     *
     * @param last whether this is the last field in the path
     * @return the mapping of the related type, or null
     */
    private ClassMapping traverseField(PathExpState pstate, boolean key, 
        boolean forceOuter, boolean last) {
        if (pstate.field == null)
            return null;

        // traverse into field value
        if (key)
            pstate.joins = pstate.field.joinKey(pstate.joins, forceOuter);
        else
            pstate.joins = pstate.field.join(pstate.joins, forceOuter);

        // if this isn't the last field, traverse into the relation
        if (!last)
            joinRelation(pstate, key, forceOuter, true);

        // return the maping of the related type, if any
        if (key)
            return pstate.field.getKeyMapping().getTypeMapping();
        if (pstate.field.getElement().getTypeCode() == JavaTypes.PC)
            return pstate.field.getElementMapping().getTypeMapping();
        return pstate.field.getTypeMapping();
    }

    /**
     * Join into the relation represented by the current field, if any.
     */
    private void joinRelation(PathExpState pstate, boolean key, 
        boolean forceOuter, boolean traverse) {
        if (pstate.field == null)
            return;
        if (key)
            pstate.joins = pstate.field.joinKeyRelation(pstate.joins, 
                forceOuter, traverse);
        else
            pstate.joins = pstate.field.joinRelation(pstate.joins, forceOuter,
                traverse);
        pstate.joinedRel = true;
    }

    public Object toDataStoreValue(Select sel, ExpContext ctx, ExpState state, 
        Object val) {
        PathExpState pstate = (PathExpState) state;
        FieldMapping field = (pstate.cmpfield != null) ? pstate.cmpfield 
            : pstate.field;
        if (isXPath())
            return val;
        if (field != null) {
            if (_key)
                return field.toKeyDataStoreValue(val, ctx.store);
            if (field.getElement().getDeclaredTypeCode() != JavaTypes.OBJECT)
                return field.toDataStoreValue(val, ctx.store);

            val = field.getExternalValue(val, ctx.store.getContext());
            return field.toDataStoreValue(val, ctx.store);
        }
        return _class.toDataStoreValue(val, _class.getPrimaryKeyColumns(),
            ctx.store);
    }

    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        selectColumns(sel, ctx, state, pks);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        sel.setSchemaAlias(_schemaAlias);
        ClassMapping mapping = getClassMapping(state);
        PathExpState pstate = (PathExpState) state;
        if (_type != OBJECT_PATH && (mapping == null || !pstate.joinedRel ||
            pstate.isEmbedElementColl))
            sel.select(getColumns(state), pstate.joins);
        else if (_key && pstate.field.getKey().isEmbedded())
            selectEmbeddedMapKey(sel, ctx, state);
        else if (pks)
            sel.select(mapping.getPrimaryKeyColumns(), pstate.joins);
        else {
            // select the mapping; allow any subs because we know this must
            // be either a relation, in which case it will already be
            // constrained by the joins, or 'this', in which case the
            // JDBCExpressionFactory takes care of adding class conditions for
            // the candidate class on the select
            int subs = (_type == UNBOUND_VAR) ? Select.SUBS_JOINABLE
                : Select.SUBS_ANY_JOINABLE;
            sel.select(mapping, subs, ctx.store, ctx.fetch,
                JDBCFetchConfiguration.EAGER_NONE, sel.outer(pstate.joins));
        }
    }

    public void groupBy(Select sel, ExpContext ctx, ExpState state) {
        ClassMapping mapping = getClassMapping(state);
        PathExpState pstate = (PathExpState) state;
        if (mapping == null || !pstate.joinedRel)
            sel.groupBy(getColumns(state), sel.outer(pstate.joins));
        else {
            int subs = (_type == UNBOUND_VAR) ? Select.SUBS_JOINABLE
                : Select.SUBS_ANY_JOINABLE;
            sel.groupBy(mapping, subs, ctx.store, ctx.fetch, 
                sel.outer(pstate.joins));
        }
    }

    public void orderBy(Select sel, ExpContext ctx, ExpState state, 
        boolean asc) {
        sel.orderBy(getColumns(state), asc, sel.outer(state.joins), false);
    }

    public Object load(ExpContext ctx, ExpState state, Result res)
        throws SQLException {
        return load(ctx, state, res, false);
    }

    Object load(ExpContext ctx, ExpState state, Result res, boolean pks)
        throws SQLException {
        ClassMapping mapping = getClassMapping(state);
        PathExpState pstate = (PathExpState) state;
        if (mapping != null && (pstate.field == null 
            || !pstate.field.isEmbedded())) {
            if (pks)
                return mapping.getObjectId(ctx.store, res, null, true, 
                    pstate.joins);
            if (_key && pstate.field.getKey().isEmbedded())
                return loadEmbeddedMapKey(ctx, state, res);
            if (pstate.isEmbedElementColl)
                return pstate.field.loadProjection(ctx.store, ctx.fetch, res,
                    pstate.joins);
            return res.load(mapping, ctx.store, ctx.fetch, pstate.joins);
        }

        Object ret;
        if (_key) {
            // Map key is a java primitive type
            // example: Map<Integer, Employee> emps
            ret = res.getObject(pstate.cols[0], null, pstate.joins);
        } else {
            ret = pstate.field.loadProjection(ctx.store, ctx.fetch, res, pstate.joins);
        }
        
        if (pstate.field.isExternalized()) {
            ret = pstate.field.getFieldValue(ret, ctx.store.getContext());            
        }
        
        if (_cast != null)
            ret = Filters.convert(ret, _cast);
        return ret;
    }

    private void validateMapStrategy(Strategy strategy) {
        if (strategy == null ||
            !(strategy instanceof LRSMapFieldStrategy))
            throw new RuntimeException("Invalid map field strategy:"+strategy);
    }

    private void selectEmbeddedMapKey(Select sel, ExpContext ctx,
        ExpState state) {
        PathExpState pstate = (PathExpState) state;
        validateMapStrategy(pstate.field.getStrategy());
        LRSMapFieldStrategy strategy = (LRSMapFieldStrategy)
            pstate.field.getStrategy();
        ClassMapping mapping = pstate.field.getKeyMapping().getTypeMapping();
        strategy.selectKey(sel, mapping, null, ctx.store, ctx.fetch,
            pstate.joins);
    }

    private Object loadEmbeddedMapKey(ExpContext ctx, ExpState state,
        Result res) throws SQLException {
        PathExpState pstate = (PathExpState) state;
        validateMapStrategy(pstate.field.getStrategy());
        LRSMapFieldStrategy strategy =
            (LRSMapFieldStrategy) pstate.field.getStrategy();
        return strategy.loadKey(null, ctx.store, ctx.fetch, res,
            pstate.joins);
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        // we don't create the SQL b/c it forces the Select to cache aliases
        // for the tables we use, and these aliases might not ever be used if
        // we eventually call appendIsEmpty or appendIsNull rather than appendTo
    }

    public void verifyIndexedField() {
        Action lastAction = (Action) lastFieldAction();
        FieldMapping fm = (FieldMapping) lastAction.data;
        if (fm.getOrderColumn() == null)
            throw new UserException(_loc.get("no-order-column", fm.getName()));
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        return getColumns(state).length;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        Column[] cols = getColumns(state);
        for (int i = 0; i < cols.length; i++) {
            appendTo(sel, state, sql, cols[i]);
            if (i < cols.length -1)
            sql.append(", ");
        }
    }
    
    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        Column col = getColumns(state)[index];
        appendTo(sel, state, sql, col);
    }
    
    public void appendTo(Select sel, ExpState state, SQLBuffer sql, Column col) {
        if (sel != null)
            sel.setSchemaAlias(_schemaAlias);

        // if select is null, it means we are not aliasing columns
        // (e.g., during a bulk update)
        if (sel == null)
            sql.append(col.getIdentifier());
        else if (_type == XPATH)
            // if this is an xpath, append xpath string
            sql.append(getXPath());
        else
            sql.append(sel.getColumnAlias(col, state.joins));
    }

    public void appendIsEmpty(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        PathExpState pstate = (PathExpState) state;
        if (pstate.field == null)
            sql.append(FALSE);
        else
            pstate.field.appendIsEmpty(sql, sel, pstate.joins);
    }

    public void appendIsNotEmpty(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        PathExpState pstate = (PathExpState) state;
        if (pstate.field == null)
            sql.append(FALSE);
        else
            pstate.field.appendIsNotEmpty(sql, sel, pstate.joins);
    }

    public void appendIndex(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        PathExpState pstate = (PathExpState) state;
        if (pstate.field == null)
            sql.append("1");
        else
            pstate.field.appendIndex(sql, sel, pstate.joins);
    }

    public void appendType(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        Discriminator disc = null;
        ClassMapping sup = _class;
        while (sup.getMappedPCSuperclassMapping() != null)
            sup = sup.getMappedPCSuperclassMapping();

        disc = sup.getDiscriminator();

        Column[] cols = null;
        if (disc != null)
            cols = disc.getColumns();
        else
            cols = getColumns(state);

        if (cols == null || cols.length == 0) {
            sql.append("1");
            return;
        }

        for (int i = 0; i < cols.length; i++) {
            if (i > 0)
                sql.append(", ");
            sql.append(sel.getColumnAlias(cols[i], state.joins));
        }
    }

    public void appendSize(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        PathExpState pstate = (PathExpState) state;
        if (pstate.field == null)
            sql.append("1");
        else
            pstate.field.appendSize(sql, sel, pstate.joins);
    }

    public void appendIsNull(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        PathExpState pstate = (PathExpState) state;
        if (pstate.field == null)
            sql.append(FALSE);
        else
            pstate.field.appendIsNull(sql, sel, pstate.joins);
    }

    public void appendIsNotNull(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        PathExpState pstate = (PathExpState) state;
        if (pstate.field == null)
            sql.append(TRUE);
        else
            pstate.field.appendIsNotNull(sql, sel, pstate.joins);
    }
    
    public boolean isSubqueryPath() {
        if (_actions != null && _actions.size() == 1 && 
           ((Action)_actions.get(0)).op == Action.SUBQUERY)
            return true;
        return false;
    }

    public int hashCode() {
        if (_actions == null)
            return _candidate.hashCode();
        return _candidate.hashCode() ^ _actions.hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof PCPath))
            return false;
        PCPath path = (PCPath) other;
        return ObjectUtils.equals(_candidate, path._candidate)
            && ObjectUtils.equals(_actions, path._actions);
    }
    
    public int getId() {
        return Val.VAL;
    }


    /**
     * Helper class representing an action.
     */
    private static class Action
        implements Serializable {

        public static final int GET = 0;
        public static final int GET_OUTER = 1;
        public static final int GET_KEY = 2;
        public static final int VAR = 3;
        public static final int SUBQUERY = 4;
        public static final int UNBOUND_VAR = 5;
        public static final int CAST = 6;
        public static final int GET_XPATH = 7;

        public int op = -1;
        public Object data = null;
        public String var = null;
        public Context context = null;

        public String toString() {
            return op + "|" + data;
        }

        public int hashCode() {
            if (data == null)
                return op;
            return op ^ data.hashCode();
        }

        public boolean equals(Object other) {
            if (other == null)
                return false;
            if (other == this)
                return true;
            Action a = (Action) other;
            return op == a.op
                && ObjectUtils.equals(data, a.data);
        }
    }
}
