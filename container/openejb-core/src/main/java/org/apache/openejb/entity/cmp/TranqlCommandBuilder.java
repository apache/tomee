/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.entity.cmp;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.tranql.builder.IdentityDefinerBuilder;
import org.tranql.builder.SQLQueryBuilder;
import org.tranql.cache.CacheFlushStrategyFactory;
import org.tranql.cache.CacheRowAccessor;
import org.tranql.cache.CacheTable;
import org.tranql.cache.EmptySlotLoader;
import org.tranql.cache.FaultHandler;
import org.tranql.cache.GlobalSchema;
import org.tranql.cache.QueryFaultHandler;
import org.tranql.ejb.CMPFieldAccessor;
import org.tranql.ejb.CMPFieldFaultTransform;
import org.tranql.ejb.CMPFieldIdentityExtractorAccessor;
import org.tranql.ejb.CMPFieldNestedRowAccessor;
import org.tranql.ejb.CMPFieldTransform;
import org.tranql.ejb.CMPMappedToCMRAccessor;
import org.tranql.ejb.CMRField;
import org.tranql.ejb.CMRMappedToInversePKCMP;
import org.tranql.ejb.CMRMappedToOwningPKCMP;
import org.tranql.ejb.EJB;
import org.tranql.ejb.FinderEJBQLQuery;
import org.tranql.ejb.LocalProxyTransform;
import org.tranql.ejb.ManyToManyCMR;
import org.tranql.ejb.ManyToOneCMR;
import org.tranql.ejb.MultiValuedCMRAccessor;
import org.tranql.ejb.MultiValuedCMRFaultHandler;
import org.tranql.ejb.OneToManyCMR;
import org.tranql.ejb.OneToOneCMR;
import org.tranql.ejb.ReadOnlyCMPFieldAccessor;
import org.tranql.ejb.SingleValuedCMRAccessor;
import org.tranql.ejb.SingleValuedCMRFaultHandler;
import org.tranql.field.FieldAccessor;
import org.tranql.field.FieldTransform;
import org.tranql.field.ReferenceAccessor;
import org.tranql.identity.IdentityDefiner;
import org.tranql.identity.IdentityTransform;
import org.tranql.intertxcache.CacheFaultHandler;
import org.tranql.intertxcache.CacheFieldFaultTransform;
import org.tranql.intertxcache.FindByPKCacheQueryCommand;
import org.tranql.intertxcache.FrontEndCache;
import org.tranql.ql.QueryException;
import org.tranql.query.QueryCommand;
import org.tranql.schema.Association;
import org.tranql.schema.AssociationEnd;
import org.tranql.schema.Attribute;
import org.tranql.schema.Entity;
import org.tranql.schema.FKAttribute;

/**
 * @version $Revision$ $Date$
 */
public class TranqlCommandBuilder {
    private final GlobalSchema globalSchema;
    private final TransactionManager transactionManager;
    private final SQLQueryBuilder queryBuilder;
    private final IdentityDefinerBuilder identityDefinerBuilder;
    private final CacheFlushStrategyFactory cacheFlushStrategyFactory;

    public TranqlCommandBuilder(GlobalSchema globalSchema, TransactionManager transactionManager, IdentityDefinerBuilder identityDefinerBuilder, SQLQueryBuilder queryBuilder) {
        this.identityDefinerBuilder = identityDefinerBuilder;
        this.globalSchema = globalSchema;
        this.transactionManager = transactionManager;
        this.queryBuilder = queryBuilder;
        cacheFlushStrategyFactory = globalSchema.getCacheFlushStrategyFactorr();
    }

    public CacheFlushStrategyFactory getCacheFlushStrategyFactory() {
        return cacheFlushStrategyFactory;
    }

    public IdentityDefinerBuilder getIdentityDefinerBuilder() {
        return identityDefinerBuilder;
    }

    public Map createSelectQueries(EJB ejb) throws QueryException {
        Map selects = queryBuilder.buildSelects(ejb.getName());
        return selects;
    }

    public Map createFinderQueries(EJB ejb, FrontEndCache frontEndCache) throws QueryException {
        Map finders = queryBuilder.buildFinders(ejb.getName());
        addFindByPrimaryKeyQuery(ejb, frontEndCache, finders);
        return finders;
    }

    private void addFindByPrimaryKeyQuery(EJB ejb, FrontEndCache frontEndCache, Map finders) throws QueryException {
        IdentityTransform primaryKeyTransform = identityDefinerBuilder.getPrimaryKeyTransform(ejb);

        // findByPrimaryKey
        QueryCommand localProxyLoad = queryBuilder.buildFindByPrimaryKey(ejb.getName(), true);
        localProxyLoad = new FindByPKCacheQueryCommand(frontEndCache, primaryKeyTransform, localProxyLoad);
        QueryCommand remoteProxyLoad = queryBuilder.buildFindByPrimaryKey(ejb.getName(), false);
        remoteProxyLoad = new FindByPKCacheQueryCommand(frontEndCache, primaryKeyTransform, remoteProxyLoad);

        Class pkClass;
        if (ejb.isUnknownPK()) {
            pkClass = Object.class;
        } else {
            pkClass = ejb.getPrimaryKeyClass();
        }

        FinderEJBQLQuery pkFinder = new FinderEJBQLQuery("findByPrimaryKey", new Class[]{pkClass}, "UNDEFINED");
        QueryCommand[] commands = new QueryCommand[]{localProxyLoad, remoteProxyLoad};
        boolean found = false;
        for (Iterator iter = finders.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            FinderEJBQLQuery query = (FinderEJBQLQuery) entry.getKey();
            if (query.equals(pkFinder)) {
                entry.setValue(commands);
                found = true;
                break;
            }
        }
        if (false == found) {
            finders.put(pkFinder, commands);
        }
    }

    public FaultHandler createLoadFault(EJB ejb, FrontEndCache frontEndCache) throws QueryException {
        List attributes = ejb.getAttributes();
        List pkAttributes = ejb.getPrimaryKeyFields();
        EmptySlotLoader[] slotLoaders = new EmptySlotLoader[pkAttributes.size()];
        String[] attributeNames = new String[pkAttributes.size()];
        int[] indexes = new int[pkAttributes.size()];
        for (int i = 0; i < pkAttributes.size(); i++) {
            Attribute attr = (Attribute) pkAttributes.get(i);
            attributeNames[i] = attr.getName();
            indexes[i] = attributes.indexOf(attr);
            slotLoaders[i] = new EmptySlotLoader(attributes.indexOf(attr), new FieldAccessor(i, attr.getType()));
        }
        QueryCommand loadCommand = queryBuilder.buildLoadEntity(ejb.getName(), attributeNames);
        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb);
        FaultHandler faultHandler = new QueryFaultHandler(loadCommand, identityDefiner, slotLoaders);
        faultHandler = new CacheFaultHandler(frontEndCache, faultHandler, indexes);
        return faultHandler;
    }


    public LinkedHashMap createCMPFieldAccessors(EJB ejb, FrontEndCache frontEndCache, LinkedHashMap cmrFieldAccessor) throws QueryException {
        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb);

        List attributes = ejb.getAttributes();
        List virtualAttributes = ejb.getVirtualCMPFields();
        LinkedHashMap cmpFieldAccessors = new LinkedHashMap(attributes.size());
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = (Attribute) attributes.get(i);
            if (virtualAttributes.contains(attribute)) {
                continue;
            }
            String name = attribute.getName();

            CMPFieldTransform accessor = new CMPFieldAccessor(new CacheRowAccessor(i, attribute.getType()), name);
            if (null != ejb.getAssociationEndDefiningFKAttribute(name)) {
                AssociationEnd end = ejb.getAssociationEndDefiningFKAttribute(name);
                CMPFieldTransform cmrAccessor = (CMPFieldTransform) cmrFieldAccessor.get(end.getName());

                EJB relatedEJB = (EJB) end.getEntity();
                if (relatedEJB.isCompoundPK()) {
                    IdentityDefiner relatedIdentityDefiner = identityDefinerBuilder.getIdentityDefiner(end.getEntity());
                    accessor = new CMPFieldIdentityExtractorAccessor(cmrAccessor, relatedIdentityDefiner);

                    int index = 0;
                    LinkedHashMap pkToFK = end.getAssociation().getJoinDefinition().getPKToFKMapping();
                    for (Iterator iter = pkToFK.entrySet().iterator(); iter.hasNext();) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        FKAttribute fkAttribute = (FKAttribute) entry.getValue();
                        if (fkAttribute.getName().equals(name)) {
                            accessor = new CMPFieldNestedRowAccessor(accessor, index);
                            break;
                        }
                        index++;
                    }

                    accessor = new ReadOnlyCMPFieldAccessor(accessor, attribute.getName());
                } else {
                    IdentityTransform relatedPrimaryKeyTransform = identityDefinerBuilder.getPrimaryKeyTransform(relatedEJB);
                    accessor = new CMPMappedToCMRAccessor(cmrAccessor, accessor, relatedPrimaryKeyTransform);
                }
            } else {
                QueryCommand command = queryBuilder.buildLoadAttribute(ejb.getName(), name, true);
                FieldTransform attAccessor = command.getQuery().getResultAccessors()[0];
                EmptySlotLoader[] loaders = new EmptySlotLoader[]{new EmptySlotLoader(i, attAccessor)};
                FaultHandler faultHandler = new QueryFaultHandler(command, identityDefiner, loaders);
                accessor = new CMPFieldFaultTransform(accessor, faultHandler, new int[]{i});
                accessor = new CacheFieldFaultTransform(frontEndCache, accessor, i);
            }
            // TODO: this breaks the CMP1 bridge.
//            if (attribute.isIdentity()) {
//                accessor = new PKFieldAccessCheck(accessor);
//            }

            cmpFieldAccessors.put(name, accessor);
        }
        return cmpFieldAccessors;
    }

    public CmrAccessors createCMRFieldAccessors(EJB ejb, FrontEndCache frontEndCache, boolean prefetch) throws QueryException {
        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(ejb);

        List associationEnds = ejb.getAssociationEnds();
        CmrAccessors cmrAccessors = new CmrAccessors();
        
        int offset = ejb.getAttributes().size();
        for (int i = offset; i < offset + associationEnds.size(); i++) {
            CMRField field = (CMRField) associationEnds.get(i - offset);

            String name = field.getName();
            Association association = field.getAssociation();
            CMRField relatedField = (CMRField) association.getOtherEnd(field);
            EJB relatedEJB = (EJB) field.getEntity();
            IdentityDefiner relatedIdentityDefiner = identityDefinerBuilder.getIdentityDefiner(relatedEJB);

            CMPFieldTransform accessor = new CMPFieldAccessor(new CacheRowAccessor(i, null), name);

            FaultHandler faultHandler = buildFaultHandler(queryBuilder, ejb, field, i, prefetch);
            accessor = new CMPFieldFaultTransform(accessor, faultHandler, new int[]{i});

            accessor = new CacheFieldFaultTransform(frontEndCache, accessor, i);

            int relatedIndex = relatedEJB.getAttributes().size() + relatedEJB.getAssociationEnds().indexOf(relatedField);
            FaultHandler relatedFaultHandler = buildFaultHandler(queryBuilder, relatedEJB, relatedField, relatedIndex, prefetch);
            CMPFieldTransform relatedAccessor = new CMPFieldAccessor(new CacheRowAccessor(relatedIndex, null), name);
            relatedAccessor = new CMPFieldFaultTransform(relatedAccessor, relatedFaultHandler, new int[]{relatedIndex});
            if (field.isOneToOne()) {
                accessor = new OneToOneCMR(accessor, identityDefiner, relatedAccessor, relatedIdentityDefiner);
                accessor = buildCMRMappedToPKCMP(relatedEJB, relatedField, accessor, false, relatedIndex);
                accessor = buildCMRMappedToPKCMP(ejb, field, accessor, true, i);
            } else if (field.isOneToMany()) {
                relatedAccessor = buildCMRMappedToPKCMP(relatedEJB, relatedField, relatedAccessor, true, relatedIndex);
                accessor = new ManyToOneCMR(accessor, identityDefiner, relatedAccessor, relatedIdentityDefiner);
            } else if (field.isManyToOne()) {
                accessor = new OneToManyCMR(accessor, relatedAccessor, relatedIdentityDefiner);
                accessor = buildCMRMappedToPKCMP(ejb, field, accessor, true, i);
            } else {
                CacheTable mtm = (CacheTable) globalSchema.getEntity(association.getManyToManyEntity().getName());
                boolean isRight = association.getRightJoinDefinition().getPKEntity() == ejb;
                accessor = new ManyToManyCMR(accessor, relatedAccessor, relatedIdentityDefiner, mtm, isRight);
            }

            cmrAccessors.rawAccessors.put(name, accessor);

            IdentityTransform relatedIdentityTransform = identityDefinerBuilder.getPrimaryKeyTransform(relatedEJB);
            if (field.isOneToOne() || field.isManyToOne()) {
                accessor = new SingleValuedCMRAccessor(accessor,
                        new LocalProxyTransform(relatedIdentityTransform, relatedEJB.getProxyFactory()));
            } else {
                accessor = new MultiValuedCMRAccessor(accessor,
                        transactionManager,
                        new LocalProxyTransform(relatedIdentityTransform, relatedEJB.getProxyFactory()),
                        relatedEJB.getProxyFactory().getLocalInterfaceClass());
            }

            cmrAccessors.fieldAccessors.put(name, accessor);
        }

        return cmrAccessors;
    }

    private CMPFieldTransform buildCMRMappedToPKCMP(Entity entity, AssociationEnd end, CMPFieldTransform accessor, boolean owning, int cmrSlot) {
        List pkFields = entity.getPrimaryKeyFields();
        for (Iterator iter = pkFields.iterator(); iter.hasNext();) {
            Attribute pkField = (Attribute) iter.next();
            if (end.hasFKAttribute(pkField.getName())) {
                if (owning) {
                    return new CMRMappedToOwningPKCMP(accessor, cmrSlot);
                }
                return new CMRMappedToInversePKCMP(accessor, cmrSlot);
            }
        }
        return accessor;
    }

    private FaultHandler buildFaultHandler(SQLQueryBuilder queryBuilder, EJB definingEJB, CMRField field, int slot, boolean prefetch) throws QueryException {
        Association association = field.getAssociation();
        CMRField relatedField = (CMRField) association.getOtherEnd(field);
        EJB relatedEJB = (EJB) field.getEntity();

        IdentityDefiner identityDefiner = identityDefinerBuilder.getIdentityDefiner(relatedField.getEntity());
        IdentityDefiner relatedIdentityDefiner =
                identityDefinerBuilder.getIdentityDefiner(relatedEJB, 0);

        QueryCommand faultCommand = queryBuilder.buildLoadAssociationEnd(definingEJB.getName(), field.getName(), prefetch);
        if (field.isOneToOne() || field.isManyToOne()) {
            return new SingleValuedCMRFaultHandler(faultCommand,
                    identityDefiner,
                    new EmptySlotLoader[]{new EmptySlotLoader(slot, new ReferenceAccessor(relatedIdentityDefiner))});
        }
        return new MultiValuedCMRFaultHandler(faultCommand,
                slot,
                identityDefiner,
                new ReferenceAccessor(relatedIdentityDefiner));
    }


    public static class CmrAccessors {
        public final LinkedHashMap rawAccessors = new LinkedHashMap();
        public final LinkedHashMap fieldAccessors = new LinkedHashMap();
    }
}
