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
package org.apache.openjpa.persistence.enhance;

import javax.persistence.Persistence;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.openjpa.persistence.annotations.TestAnnotationBasics;
import org.apache.openjpa.persistence.annotations.TestEmbeddedId;
import org.apache.openjpa.persistence.annotations.TestEnumerated;
import org.apache.openjpa.persistence.annotations.TestFlatInheritance;
import org.apache.openjpa.persistence.annotations.TestGenerators;
import org.apache.openjpa.persistence.annotations.TestJoinedInheritance;
import org.apache.openjpa.persistence.annotations.TestManyToMany;
import org.apache.openjpa.persistence.annotations.TestMapKey;
import org.apache.openjpa.persistence.annotations.TestMappedSuperClass;
import org.apache.openjpa.persistence.annotations.TestOneToOne;
import org.apache.openjpa.persistence.annotations.TestPersistentCollection;
import org.apache.openjpa.persistence.annotations.TestSerializedLobs;
import org.apache.openjpa.persistence.annotations.TestTablePerClassInheritance;
import org.apache.openjpa.persistence.datacache.TestDataCacheBehavesIdentical;
import org.apache.openjpa.persistence.datacache.TestQueryResultSize;
import org.apache.openjpa.persistence.detach.TestDetachNoCascade;
import org.apache.openjpa.persistence.detachment.
    TestGetReferenceAndImplicitDetachment;
import org.apache.openjpa.persistence.enhance.identity.
    TestMultipleLevelDerivedIdentity;
import org.apache.openjpa.persistence.enhance.identity.
    TestMultipleLevelDerivedIdentity1;
import org.apache.openjpa.persistence.identity.TestFloatingPointIds;
import org.apache.openjpa.persistence.identity.TestGenerationType;
import org.apache.openjpa.persistence.identity.TestSQLBigDecimalId;
import org.apache.openjpa.persistence.identity.TestSQLBigIntegerId;
import org.apache.openjpa.persistence.identity.TestSQLDateId;
import org.apache.openjpa.persistence.jdbc.annotations.TestEJBEmbedded;
import org.apache.openjpa.persistence.jdbc.annotations.TestEmbeddableSuperclass;
import org.apache.openjpa.persistence.jdbc.annotations.TestOneToMany;
import org.apache.openjpa.persistence.jdbc.annotations.TestVersion;
import org.apache.openjpa.persistence.jdbc.mapping.TestPrecisionMapping;
import org.apache.openjpa.persistence.jdbc.maps.m2mmapex2.TestMany2ManyMapEx2;
import org.apache.openjpa.persistence.jdbc.maps.m2mmapex6.TestMany2ManyMapEx6;
import org.apache.openjpa.persistence.jpql.clauses.TestEJBClauses;
import org.apache.openjpa.persistence.jpql.clauses.TestEJBDeleteUpdateImpl;
import org.apache.openjpa.persistence.jpql.expressions.TestEntityTypeExpression;
import org.apache.openjpa.persistence.kernel.TestExtents;
import org.apache.openjpa.persistence.kernel.TestProxies2;
import org.apache.openjpa.persistence.kernel.TestSavepoints;
import org.apache.openjpa.persistence.kernel.TestStateManagerImplData;
import org.apache.openjpa.persistence.kernel.TestStoreBlob;
import org.apache.openjpa.persistence.meta.TestMetamodel;
import org.apache.openjpa.persistence.query.TestComplexQueries;
import org.apache.openjpa.persistence.query.TestNamedQueries;
import org.apache.openjpa.persistence.query.TestQueryResults;
import org.apache.openjpa.persistence.relations.
    TestBulkUpdatesAndEmbeddedFields;
import org.apache.openjpa.persistence.relations.
    TestCascadingOneManyWithForeignKey;
import org.apache.openjpa.persistence.relations.TestChainEntities;
import org.apache.openjpa.persistence.relations.TestEagerBidiSQL;
import org.apache.openjpa.persistence.relations.TestHandlerCollections;
import org.apache.openjpa.persistence.relations.TestHandlerToHandlerMaps;
import org.apache.openjpa.persistence.relations.TestHandlerToRelationMaps;
import org.apache.openjpa.persistence.relations.TestIdOrderedOneMany;
import org.apache.openjpa.persistence.relations.TestInverseEagerSQL;
import org.apache.openjpa.persistence.relations.TestLRS;
import org.apache.openjpa.persistence.relations.TestLazyManyToOne;
import org.apache.openjpa.persistence.relations.TestManyEagerSQL;
import org.apache.openjpa.persistence.relations.TestManyOneAsId;
import org.apache.openjpa.persistence.relations.TestMapCollectionToBlob;
import org.apache.openjpa.persistence.relations.
    TestMultipleSameTypedEmbeddedWithEagerRelations;
import org.apache.openjpa.persistence.relations.TestOneOneNulls;
import org.apache.openjpa.persistence.relations.
    TestRelationFieldAsPrimaryKeyAndForeignKey;
import org.apache.openjpa.persistence.relations.TestRelationToHandlerMaps;
import org.apache.openjpa.persistence.relations.TestRelationToRelationMaps;
import org.apache.openjpa.persistence.relations.TestTargetedIFaceRelations;
import org.apache.openjpa.persistence.simple.TestBasicAnnotation;
import org.apache.openjpa.persistence.simple.TestCaseInsensitiveKeywordsInJPQL;
import org.apache.openjpa.persistence.simple.TestEntityManagerClear;
import org.apache.openjpa.persistence.simple.TestEntityManagerFactory;
import org.apache.openjpa.persistence.simple.TestEntityManagerMerge;
import org.apache.openjpa.persistence.simple.
    TestEntityManagerMethodsThrowAfterClose;
import org.apache.openjpa.persistence.simple.TestFlushBeforeDetach;
import org.apache.openjpa.persistence.simple.TestJoin;
import org.apache.openjpa.persistence.simple.TestPersistence;
import org.apache.openjpa.persistence.simple.TestPropertiesMethods;
import org.apache.openjpa.persistence.simple.TestRefresh;
import org.apache.openjpa.persistence.simple.TestSerializedFactory;
import org.apache.openjpa.persistence.simple.
    TestTableNamesDefaultToEntityNames;
import org.apache.openjpa.persistence.spring.TestLibService;
import org.apache.openjpa.persistence.xml.TestSimpleXmlEntity;
import org.apache.openjpa.persistence.xml.TestXmlOverrideEntity;

public class DynamicEnhancementSuite extends TestCase {
    static {
        Persistence.createEntityManagerFactory("test", System.getProperties());
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();

        // Setting the property -DdynamicTest allows you to run a single test
        // with the dynamic enhaner.
        String test = System.getProperty("dynamicTest");
        if (test != null) {
            suite.addTestSuite(Class.forName(test));
        } else {

            // Subclassing failing tests
            suite.addTestSuite(TestComplexQueries.class);
            suite.addTestSuite(TestNamedQueries.class);
            suite.addTestSuite(TestQueryResults.class);
            suite.addTestSuite(TestMetamodel.class);
            suite.addTestSuite(TestLibService.class);
            suite.addTestSuite(TestOneOneNulls.class);
            suite.addTestSuite(TestProxies2.class);
            suite.addTestSuite(TestStoreBlob.class);
            suite.addTestSuite(TestEntityTypeExpression.class);
            suite.addTestSuite(TestSimpleXmlEntity.class);
            suite.addTestSuite(TestDataCacheBehavesIdentical.class);
            suite.addTestSuite(TestQueryResultSize.class);
            suite.addTestSuite(TestEJBClauses.class);
            suite.addTestSuite(TestEJBDeleteUpdateImpl.class);
            suite.addTestSuite(TestOneToMany.class);
            suite.addTestSuite(TestOneToOne.class);
            suite.addTestSuite(TestGetReferenceAndImplicitDetachment.class);
            suite.addTestSuite(TestMultipleLevelDerivedIdentity.class);
            suite.addTestSuite(TestMultipleLevelDerivedIdentity1.class);
            suite.addTestSuite(TestEJBEmbedded.class);
            suite.addTestSuite(TestEmbeddableSuperclass.class);
            suite.addTestSuite(TestFlatInheritance.class);
            suite.addTestSuite(TestVersion.class);
            suite.addTestSuite(TestMany2ManyMapEx2.class);
            suite.addTestSuite(TestMany2ManyMapEx6.class);
            suite.addTestSuite(TestOneOneNulls.class);
            suite.addTestSuite(TestTargetedIFaceRelations.class);
            suite.addTestSuite(TestExtents.class);
            suite.addTestSuite(TestProxies2.class);
            suite.addTestSuite(TestSavepoints.class);
            suite.addTestSuite(TestStateManagerImplData.class);
            suite.addTestSuite(TestStoreBlob.class);
            suite.addTestSuite(TestEntityTypeExpression.class);
            suite.addTestSuite(TestSimpleXmlEntity.class);
            suite.addTestSuite(TestXmlOverrideEntity.class);
            suite.addTestSuite(TestDataCacheBehavesIdentical.class);
            suite.addTestSuite(TestQueryResultSize.class);
            suite.addTestSuite(TestQueryResultSize.class);
            suite.addTestSuite(TestDetachNoCascade.class);
            // end Subclassing failing tests

            // org.apache.openjpa.persistence.enhance
            suite.addTestSuite(TestMultipleLevelDerivedIdentity.class);
            suite.addTestSuite(TestClone.class);

            // excluded via pom
            // suite.addTestSuite(TestDynamicStorageGenerator.class);
            // suite.addTestSuite(TestNoNoArgs.class);
            // suite.addTestSuite(TestSubclassedBehavior.class);

            // org.apache.openjpa.persistence.relations
            suite.addTestSuite(TestBulkUpdatesAndEmbeddedFields.class);
            suite.addTestSuite(TestCascadingOneManyWithForeignKey.class);
            suite.addTestSuite(TestChainEntities.class);
            suite.addTestSuite(TestEagerBidiSQL.class);
            suite.addTestSuite(TestHandlerCollections.class);
            suite.addTestSuite(TestHandlerToHandlerMaps.class);
            suite.addTestSuite(TestHandlerToRelationMaps.class);
            suite.addTestSuite(TestIdOrderedOneMany.class);
            suite.addTestSuite(TestInverseEagerSQL.class);
            suite.addTestSuite(TestLazyManyToOne.class);
            suite.addTestSuite(TestLRS.class);
            suite.addTestSuite(TestManyEagerSQL.class);
            suite.addTestSuite(TestManyOneAsId.class);
            suite.addTestSuite(TestMapCollectionToBlob.class);
            suite.addTestSuite(
                TestMultipleSameTypedEmbeddedWithEagerRelations.class);
            suite.addTestSuite(TestOneOneNulls.class);
            suite
                .addTestSuite(TestRelationFieldAsPrimaryKeyAndForeignKey.class);
            suite.addTestSuite(TestRelationToHandlerMaps.class);
            suite.addTestSuite(TestRelationToRelationMaps.class);
            suite.addTestSuite(TestTargetedIFaceRelations.class);
            // org.apache.openjpa.persistence.simple
            suite.addTestSuite(TestBasicAnnotation.class);
            suite.addTestSuite(TestCaseInsensitiveKeywordsInJPQL.class);
            suite.addTestSuite(TestEntityManagerClear.class);
            suite.addTestSuite(TestEntityManagerFactory.class);
            suite.addTestSuite(TestEntityManagerMerge.class);
            suite.addTestSuite(TestEntityManagerMethodsThrowAfterClose.class);
            suite.addTestSuite(TestFlushBeforeDetach.class);
            suite.addTestSuite(TestJoin.class);
            // TODO -- figure out why this test fails.
            // suite.addTestSuite(TestMissingMetaData.class);
            suite.addTestSuite(TestPersistence.class);
            suite.addTestSuite(TestPropertiesMethods.class);
            suite.addTestSuite(TestRefresh.class);
            suite.addTestSuite(TestSerializedFactory.class);
            suite.addTestSuite(TestTableNamesDefaultToEntityNames.class);

            // org.apache.openjpa.persistence.jdbc.mapping
            // excluded via pom
            // suite.addTestSuite(TestCompositeIdTraversalInSQLMapping.class);
            // suite.addTestSuite(TestNativeQueries.class);
            suite.addTestSuite(TestPrecisionMapping.class);

            // org.apache.openjpa.persistence.identity
            suite.addTestSuite(TestFloatingPointIds.class);
            suite.addTestSuite(TestGenerationType.class);
            suite.addTestSuite(TestSQLBigDecimalId.class);
            suite.addTestSuite(TestSQLBigIntegerId.class);
            suite.addTestSuite(TestSQLDateId.class);

            // org.apache.openjpa.persistence.annotations
            suite.addTestSuite(TestAnnotationBasics.class);
            suite.addTestSuite(TestEmbeddableSuperclass.class);
            suite.addTestSuite(TestEmbeddedId.class);
            suite.addTestSuite(TestEnumerated.class);
            suite.addTestSuite(TestFlatInheritance.class);
            suite.addTestSuite(TestGenerators.class);
            suite.addTestSuite(TestJoinedInheritance.class);
            suite.addTestSuite(TestManyToMany.class);
            suite.addTestSuite(TestMapKey.class);
            suite.addTestSuite(TestMappedSuperClass.class);
            suite.addTestSuite(TestOneToMany.class);
            suite.addTestSuite(TestOneToOne.class);
            suite.addTestSuite(TestPersistentCollection.class);
            suite.addTestSuite(TestSerializedLobs.class);
            suite.addTestSuite(TestTablePerClassInheritance.class);
            
            // excluded via pom
            // suite.addTestSuite(TestPropertyAccess.class);
            // suite.addTestSuite(TestVersion.class);
            // suite.addTestSuite(TestAdvAnnot.class);
            // suite.addTestSuite(TestDDCallbackMethods.class);
            // suite.addTestSuite(TestEJBEmbedded.class);
            // suite.addTestSuite(TestEntityListenerAnnot.class);
            // suite.addTestSuite(TestEntityOrderBy.class);
        }
        return suite;
    }
}
