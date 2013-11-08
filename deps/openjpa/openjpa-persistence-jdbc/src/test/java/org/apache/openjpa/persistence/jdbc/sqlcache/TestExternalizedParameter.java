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
package org.apache.openjpa.persistence.jdbc.sqlcache;

import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import junit.framework.TestCase;

import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.lib.rop.ResultList;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.OpenJPAPersistence;

/**
 * Tests that we can detect if a query is using query parameters for fields whose values are externalized.
 * 
 * @author Pinaki Poddar
 *
 */
public class TestExternalizedParameter extends TestCase {
    private static String RESOURCE = "META-INF/persistence.xml"; 
    private static String UNIT_NAME = "PreparedQuery";
    private static EntityManagerFactory emf;
    
    public void setUp() throws Exception {
        if (emf == null) {
            Properties config = new Properties();
            config.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true,SchemaAction='drop,add')");
            config.put("openjpa.jdbc.QuerySQLCache", "true");
            config.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
            config.put("openjpa.DynamicEnhancementAgent", "false");
            config.put("openjpa.Log", "SQL=WARN");
            emf = OpenJPAPersistence.createEntityManagerFactory(UNIT_NAME, RESOURCE, config);
        }
    }
    
    public void testNoFalseAlarmOnExternalizedParameterDetection() {
        String jpql = "select b from Book b where b.title=:title";
        EntityManager em = emf.createEntityManager();
        QueryExpressions[] exps = getExpressions(em.createQuery(jpql)
                .setParameter("title","XYZ")
                .getResultList());
        assertNotNull(exps);
        
        assertFalse(isUsingExternalizedParameter(exps[0]));
    }
    
    public void testCanDetectExternalizedSingleParameterValue() {
        String jpql = "select b from Book b where b.token=:token";
        EntityManager em = emf.createEntityManager();
        QueryExpressions[] exps = getExpressions(em.createQuery(jpql)
                .setParameter("token","MEDIUM")
                .getResultList());
        assertNotNull(exps);
        
        assertTrue(isUsingExternalizedParameter(exps[0]));
    }
    
    public void testCanDetectExternalizedMixedParameterValue() {
        String jpql = "select b from Book b where b.token=:token and b.title = :title";
        EntityManager em = emf.createEntityManager();
        QueryExpressions[] exps = getExpressions(em.createQuery(jpql)
                .setParameter("token","MEDIUM")
                .setParameter("token", "LARGE")
                .getResultList());
        assertNotNull(exps);
        
        assertTrue(isUsingExternalizedParameter(exps[0]));
    }
    
    public QueryExpressions[] getExpressions(List<?> result) {
        Object userObject = ((ResultList<?>)result).getUserObject();
        if (userObject == null || !userObject.getClass().isArray() || ((Object[])userObject).length != 2)
            return null;
        Object executor = ((Object[])userObject)[1];
        if (executor instanceof StoreQuery.Executor == false)
            return null;
        return ((StoreQuery.Executor)executor).getQueryExpressions();
    }
    
    boolean isUsingExternalizedParameter(QueryExpressions exp) {
        List<FieldMetaData> fmds = exp.getParameterizedFields();
        for (FieldMetaData fmd : fmds) {
            if (fmd.isExternalized())
                return true;
        }
        return false;
    }

}
