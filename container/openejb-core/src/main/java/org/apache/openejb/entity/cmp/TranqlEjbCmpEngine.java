/**
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
package org.apache.openejb.entity.cmp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.NoSuchEntityException;
import javax.ejb.RemoveException;

import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.transaction.CmpTxData;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.proxy.ProxyInfo;
import org.tranql.builder.IdentityDefinerBuilder;
import org.tranql.cache.CacheFlushStrategy;
import org.tranql.cache.CacheFlushStrategyFactory;
import org.tranql.cache.CacheRow;
import org.tranql.cache.CacheRowState;
import org.tranql.cache.CacheTable;
import org.tranql.cache.DuplicateIdentityException;
import org.tranql.cache.FaultHandler;
import org.tranql.cache.InTxCache;
import org.tranql.ejb.CMPFieldTransform;
import org.tranql.ejb.EJB;
import org.tranql.ejb.FinderEJBQLQuery;
import org.tranql.ejb.SelectEJBQLQuery;
import org.tranql.identity.GlobalIdentity;
import org.tranql.identity.IdentityDefiner;
import org.tranql.identity.IdentityTransform;
import org.tranql.intertxcache.FrontEndCache;
import org.tranql.intertxcache.InTxCacheTracker;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.ql.QueryException;
import org.tranql.query.QueryCommand;
import org.tranql.schema.AssociationEnd;

/**
 * @version $Revision$ $Date$
 */
public class TranqlEjbCmpEngine implements EjbCmpEngine {
    private final EJB ejb;
    private final Class beanClass;
    private final ProxyInfo proxyInfo;
    private final FrontEndCache frontEndCache;
    private final TranqlCommandBuilder tranqlCommandBuilder;
    private final IdentityDefinerBuilder identityDefinerBuilder;

    private final String ejbName;
    private final CacheTable cacheTable;
    private final PrimaryKeyGenerator keyGenerator;

    private final Set cmpFields;
    private final Set selectQueries;

    private final IdentityDefiner identityDefiner;
    private final IdentityTransform primaryKeyTransform;

    private final List cascadeOneDeleteFields = new ArrayList();
    private final List cascadeManyDeleteFields = new ArrayList();
    private final List cmrOneFields = new ArrayList();
    private final List cmrManyFields = new ArrayList();

    private final FaultHandler loadFault;
    

    public TranqlEjbCmpEngine(EJB ejb, Class beanClass, ProxyInfo proxyInfo, FrontEndCache frontEndCache, CacheTable cacheTable, TranqlCommandBuilder tranqlCommandBuilder) throws Exception {
        this.ejb = ejb;
        this.beanClass = beanClass;
        this.proxyInfo = proxyInfo;
        this.frontEndCache = frontEndCache;
        this.cacheTable = cacheTable;
        this.tranqlCommandBuilder = tranqlCommandBuilder;

        ejbName = ejb.getName();
        keyGenerator = ejb.getPrimaryKeyGenerator();

        // Identity Transforms
        identityDefinerBuilder = tranqlCommandBuilder.getIdentityDefinerBuilder();
        identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb);
        primaryKeyTransform = identityDefinerBuilder.getPrimaryKeyTransform(ejb);

        // Load all fields command
        this.loadFault = tranqlCommandBuilder.createLoadFault(ejb, frontEndCache);

        //
        // Create the cmpFields... this is a single set containing both cmpFields and cmrFields
        //
        this.cmpFields = createFields();

        //
        // Create the selectQueries... this is a single set containing both ejbSelect methods and finders
        //
        this.selectQueries = createQueries();

        // cascade delete index
        Map noLoadCmrAccessors = tranqlCommandBuilder.createCMRFieldAccessors(ejb, frontEndCache, false).fieldAccessors;
        for (Iterator iter = cacheTable.getAssociationEnds().iterator(); iter.hasNext();) {
            AssociationEnd end = (AssociationEnd) iter.next();
            CMPFieldTransform accessor = (CMPFieldTransform) noLoadCmrAccessors.get(end.getName());
            if (accessor == null) {
                throw new IllegalArgumentException("No CMR accessor for association end " + end.getName());
            }
            if (end.isSingle()) {
                cmrOneFields.add(accessor);
                if (end.isCascadeDelete()) {
                    cascadeOneDeleteFields.add(accessor);
                }
            } else {
                cmrManyFields.add(accessor);
                if (end.isCascadeDelete()) {
                    cascadeManyDeleteFields.add(accessor);
                }
            }
        }
    }

    private SortedSet createFields() throws QueryException {
        SortedSet fields = new TreeSet();

        // CMRs
        TranqlCommandBuilder.CmrAccessors cmrAccessors = tranqlCommandBuilder.createCMRFieldAccessors(ejb, frontEndCache, true);
        LinkedHashMap realCmrFields = cmrAccessors.fieldAccessors;
        for (Iterator iter = realCmrFields.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            if (ejb.getAssociationEnd(name).isVirtual()) {
                iter.remove();
            }
        }
        SortedSet cmrFields = createTranqlCmpFields(realCmrFields);
        fields.addAll(cmrFields);

        // CMP fields
        LinkedHashMap cmpFieldAccessors = tranqlCommandBuilder.createCMPFieldAccessors(ejb, frontEndCache, cmrAccessors.rawAccessors);
        fields.addAll(createTranqlCmpFields(cmpFieldAccessors));

        return Collections.unmodifiableSortedSet(fields);
    }

    private SortedSet createTranqlCmpFields(LinkedHashMap fields) {
        SortedSet cmpFields = new TreeSet();
        for (Iterator iterator = fields.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String fieldName = (String) entry.getKey();
            CMPFieldTransform fieldTransform = (CMPFieldTransform) entry.getValue();

            try {
                String baseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                Method getter = beanClass.getMethod("get" + baseName, null);
                cmpFields.add(new TranqlCmpField(fieldName, getter.getReturnType(), fieldTransform));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing accessor for field " + fieldName + " on class " + beanClass.getName());
            }
        }
        return cmpFields;
    }

    private Set createQueries() throws QueryException {
        Set selectQueries = new LinkedHashSet();

        // ejbSelectMethods
        Map selects = tranqlCommandBuilder.createSelectQueries(ejb);
        selectQueries.addAll(createEjbSelectQueries(selects));

        // finders
        Map finders = tranqlCommandBuilder.createFinderQueries(ejb, frontEndCache);
        selectQueries.addAll(createFinderQueries(finders));

        return Collections.unmodifiableSet(selectQueries);
    }

    private SortedSet createEjbSelectQueries(Map selects) throws IllegalArgumentException {
        SortedSet queries = new TreeSet();
        for (Iterator iterator = selects.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            SelectEJBQLQuery query = (SelectEJBQLQuery) entry.getKey();

            InterfaceMethodSignature signature = new InterfaceMethodSignature(query.getMethodName(), query.getParameterTypes(), true);
            QueryCommand command = (QueryCommand) entry.getValue();

            Method method = signature.getMethod(beanClass);
            if (method == null) {
                throw new IllegalArgumentException("Could not find select for signature: " + signature);
            }

            String returnType = method.getReturnType().getName();
            if (returnType.equals("java.util.Collection")) {
                queries.add(new TranqlCollectionValuedQuery(query, command, null, query.getSelectedEJB(), identityDefinerBuilder));
            } else if (returnType.equals("java.util.Set")) {
                queries.add(new TranqlSetValuedQuery(query, command, null, query.getSelectedEJB(), identityDefinerBuilder));
            } else {
                queries.add(new TranqlSingleValuedQuery(query, command, null, query.getSelectedEJB(), identityDefinerBuilder));
            }
        }
        return queries;
    }

    private Set createFinderQueries(Map finders) {
        Class homeInterface = proxyInfo.getHomeInterface();
        Class localHomeInterface = proxyInfo.getLocalHomeInterface();

        Set queries = new LinkedHashSet();
        for (Iterator iterator = finders.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            FinderEJBQLQuery query = (FinderEJBQLQuery) entry.getKey();

            InterfaceMethodSignature signature = new InterfaceMethodSignature(query.getMethodName(), query.getParameterTypes(), true);
            QueryCommand[] commands = (QueryCommand[]) entry.getValue();

            Method method = signature.getMethod(homeInterface);
            if (method == null) {
                method = signature.getMethod(localHomeInterface);
            }
            if (method == null) {
                throw new IllegalArgumentException("Could not find method for signature: " + signature);
            }

            String returnType = method.getReturnType().getName();
            if (returnType.equals("java.util.Collection")) {
                queries.add(new TranqlCollectionValuedQuery(query, commands[0], commands[1], ejb, identityDefinerBuilder));
            } else if (returnType.equals("java.util.Set")) {
                queries.add(new TranqlSetValuedQuery(query, commands[0], commands[1], ejb, identityDefinerBuilder));
            } else if (returnType.equals("java.util.Enumeration")) {
                queries.add(new TranqlEnumerationValuedQuery(query, commands[0], commands[1], ejb, identityDefinerBuilder));
            } else {
                queries.add(new TranqlSingleValuedQuery(query, commands[0], commands[1], ejb, identityDefinerBuilder));
            }
        }
        return queries;
    }

    public CmpTxData createCmpTxData() {
        CacheFlushStrategyFactory strategyFactory = tranqlCommandBuilder.getCacheFlushStrategyFactory();
        CacheFlushStrategy strategy = strategyFactory.createCacheFlushStrategy();
        strategy = new InTxCacheTracker(frontEndCache, strategy);
        CmpTxData cmpTxData = new TranqlCmpTxData(strategy);
        return cmpTxData;
    }

    private static class TranqlCmpTxData extends InTxCache implements CmpTxData {
        public TranqlCmpTxData(CacheFlushStrategy strategy) {
            super(strategy);
        }
    }

    public Set getCmpFields() {
        return cmpFields;
    }

    public Set getSelectQueries() {
        return selectQueries;
    }

    public void beforeCreate(CmpInstanceContext ctx) {
        // Assign a new row to the context before calling the create method
        CacheRow cacheRow = cacheTable.newRow();
        ctx.setCmpData(cacheRow);
    }

    public void afterCreate(CmpInstanceContext ctx, EjbTransactionContext ejbTransactionContext) throws DuplicateKeyException, Exception {
        CacheRow cacheRow = (CacheRow) ctx.getCmpData();
        if (cacheRow == null) {
            throw new EJBException("Internal Error: CMP data is null");
        }

        // define the global id in the cache row using either the key generator or
        // the data defined in the cacheRow during the ejbCreate callback
        GlobalIdentity globalId;
        if (keyGenerator != null) {
            Object opaque = keyGenerator.getNextPrimaryKey(cacheRow);
            globalId = primaryKeyTransform.getGlobalIdentity(opaque);
        } else {
            // define identity (may require insert to database)
            globalId = identityDefiner.defineIdentity(cacheRow);
        }

        // cache insert
        InTxCache cache = (InTxCache) ejbTransactionContext.getCmpTxData();

        try {
            if (keyGenerator != null) {
                cacheRow = keyGenerator.updateCache(cache, globalId, cacheRow);

                // CacheRow slots do not define the identity in this case; Inject it.
                identityDefiner.injectIdentity(cacheRow);
            } else {
                // add the row to the cache (returning a new row containing identity)
                cacheRow = cacheTable.addRow(cache, globalId, cacheRow);
            }
        } catch (DuplicateIdentityException e) {
            Object pk = primaryKeyTransform.getDomainIdentity(globalId);
            throw new DuplicateKeyException("ejbName=" + ejbName + ", primaryKey=" + pk);
        }

        ctx.setCmpData(cacheRow);
        ctx.setId(primaryKeyTransform.getDomainIdentity(globalId));
    }

    public void afterRemove(CmpInstanceContext ctx, EjbTransactionContext ejbTransactionContext) throws RemoveException {
        CacheRow cacheRow = (CacheRow) ctx.getCmpData();
        if (cacheRow == null) {
            throw new EJBException("Internal Error: CMP data is null");
        }

        InTxCache cache = (InTxCache) ejbTransactionContext.getCmpTxData();
        if (cache == null) {
            throw new EJBException("Internal Error: CMP cache is null");
        }


        // get the entities to be deleted as part of a cascade delete
        Collection cascadeDeleteEntities = new ArrayList();
        for (Iterator iterator = cascadeOneDeleteFields.iterator(); iterator.hasNext();) {
            CMPFieldTransform fieldTransform = (CMPFieldTransform) iterator.next();
            EJBLocalObject entity = (EJBLocalObject) fieldTransform.get(cache, cacheRow);
            if (null != entity) {
                cascadeDeleteEntities.add(entity);
            }
        }
        for (Iterator iterator = cascadeManyDeleteFields.iterator(); iterator.hasNext();) {
            CMPFieldTransform fieldTransform = (CMPFieldTransform) iterator.next();
            Collection entities = (Collection) fieldTransform.get(cache, cacheRow);
            cascadeDeleteEntities.addAll(entities);
        }

        // delete this row in the persistence engine
        cacheRow.markRemoved();
        cache.remove(cacheRow);

        // remove entity from all relationships
        for (Iterator iterator = cmrOneFields.iterator(); iterator.hasNext();) {
            CMPFieldTransform fieldTransform = (CMPFieldTransform) iterator.next();
            fieldTransform.set(cache, cacheRow, null);
        }
        for (Iterator iterator = cmrManyFields.iterator(); iterator.hasNext();) {
            CMPFieldTransform fieldTransform = (CMPFieldTransform) iterator.next();
            fieldTransform.set(cache, cacheRow, Collections.EMPTY_SET);
        }


        // clear id and row data from the instance
        ctx.setId(null);
        ctx.setCmpData(null);

        // cascade delete
        for (Iterator iter = cascadeDeleteEntities.iterator(); iter.hasNext();) {
            EJBLocalObject entity = (EJBLocalObject) iter.next();
            entity.remove();
        }
    }

    public void beforeLoad(CmpInstanceContext ctx) throws Exception {
        EjbTransactionContext ejbTransactionContext = ctx.getEjbTransactionData();
        if (ejbTransactionContext == null) {
            throw new EJBException("Internal Error: transaction context is null");
        }

        InTxCache cache = (InTxCache) ejbTransactionContext.getCmpTxData();
        if (cache == null) {
            throw new EJBException("Internal Error: CMP cache is null");
        }

        // locate the cache row for this instance
        GlobalIdentity globalId = primaryKeyTransform.getGlobalIdentity(ctx.getId());
        CacheRow cacheRow = cache.get(globalId);

        // if we don't already have the row execute the load fault handler
        if (cacheRow == null) {
            loadFault.rowFault(cache, globalId);
            cacheRow = cache.get(globalId);
        }

        // if we still don't have a row, we can only assume that they have an old ref to the ejb
        if (cacheRow == null) {
            // how to get the name of the bean
            throw new NoSuchEntityException("Entity " + this + " not found");
        }

        // check that the row is not tagged as removed
        if (cacheRow.getState() == CacheRowState.REMOVED) {
            throw new NoSuchEntityException("Entity " + this + " has been removed");
        }

        ctx.setCmpData(cacheRow);
    }

    public void afterStore(CmpInstanceContext ctx) throws Exception {
    }
}
