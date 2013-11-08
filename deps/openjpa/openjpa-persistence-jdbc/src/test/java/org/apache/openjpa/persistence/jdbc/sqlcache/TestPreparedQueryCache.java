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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.openjpa.kernel.PreparedQuery;
import org.apache.openjpa.kernel.PreparedQueryCache;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.lib.jdbc.AbstractJDBCListener;
import org.apache.openjpa.lib.jdbc.JDBCEvent;
import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.jdbc.sqlcache.Employee.Category;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

/**
 * Tests correctness and performance of queries with and without Prepared Query Cache.
 *  
 * This test uses a single EntityManagerFactory initialized with fixed set of entity classes 
 * and appropriate configuration parameters for Prepared Query Cache. 
 * 
 * The entity classes are specified in persistence unit <code>"PreparedQuery"</code> in a 
 * <code>META-INF/persistence.xml</code> that must be available to the classpath.
 * 
 * Configures the EntityManagerFactory with properties that are relevant to testing PreparedQuery caches.
 * Switches off runtime enhancement to avoid unintended consequences.
 * Provides facilities to run a JPQL query with and without cache and compute the performance delta. 
 * 
 * Uses a hard-coded data set initialized once for the entire suite. 
 * 
 * @author Pinaki Poddar
 * 
 */
public class TestPreparedQueryCache extends AbstractPersistenceTestCase {
    
    private static String RESOURCE = "META-INF/persistence.xml"; 
    private static String UNIT_NAME = "PreparedQuery";
    
    protected static final int SAMPLE_SIZE = 100;  // no. of observations for performance statistics 
    public static final boolean USE_CACHE  = true; // mnemonic for using cache
    private static Object[] NO_PARAMS      = null; // mnemonic for no query parameters
    public static final boolean IS_NAMED_QUERY  = true; // mnemonic for named query
    
    private static boolean FAIL_IF_PERF_DEGRADE = false; 

    
    private static Company IBM;
    public static final String[] COMPANY_NAMES    = {"IBM", "BEA", "acme.org" };
    public static final int[]    START_YEARS      = {1900, 2000, 2010 };
	public static final String[] DEPARTMENT_NAMES = {"Marketing", "Sales", "Engineering" };
    public static final String[] EMPLOYEE_NAMES   = {"Tom", "Dick", "Harray" };
	public static final String[] CITY_NAMES       = {"Tulsa", "Durban", "Harlem"};
	
    public static final String EXCLUDED_QUERY_1 = "select count(p) from Company p";
    public static final String EXCLUDED_QUERY_2 = "select count(p) from Department p";
	public static final String INCLUDED_QUERY   = "select p from Address p";
	
    public static final long[] BOOK_IDS     = {1000, 2000, 3000};
    public static final String[] BOOK_NAMES = {"Argumentative Indian", "Tin Drum", "Blink"};
    public static final long[] CD_IDS       = {1001, 2001, 3001};
    public static final String[] CD_LABELS  =  {"Beatles", "Sinatra", "Don't Rock My Boat"};
    
    protected static OpenJPAEntityManagerFactorySPI emf;
    protected static SQLAuditor auditor;
    protected static int TEST_COUNT = 0;
    private OpenJPAEntityManagerSPI em;
	
	/**
	 * Sets up the test suite with a statically initialized EntityManagerFactory.
	 * Creates data once for all other tests to use.
	 */
    @Override
	public void setUp() throws Exception {
        super.setUp();
        if (emf == null) {
            Properties config = new Properties();
            config.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true,SchemaAction='drop,add')");
            config.put("openjpa.jdbc.JDBCListeners", new JDBCListener[] { auditor = new SQLAuditor()});
            config.put("openjpa.jdbc.QuerySQLCache", "true(EnableStatistics=true)");
            config.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
            config.put("openjpa.DynamicEnhancementAgent", "false");
            emf = (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.createEntityManagerFactory(
                    UNIT_NAME, RESOURCE, config);
            em = emf.createEntityManager();
            createTestData();
        } else {
    		em = emf.createEntityManager();
            getPreparedQueryCache().clear();
        }
        TEST_COUNT++;
	}
	
	/**
	 * Create data for the entire test suite to use.
	 */
	void createTestData() {
	    em.getTransaction().begin();
	    for (int i = 0; i < COMPANY_NAMES.length; i++) {
	        Company company = new Company();
	        if (i == 0) 
	            IBM = company;
	        company.setName(COMPANY_NAMES[i]);
	        company.setStartYear(START_YEARS[i]);
	        em.persist(company);
	        for (int j = 0; j < DEPARTMENT_NAMES.length; j++) {
	            Department dept = new Department();
	            dept.setName(DEPARTMENT_NAMES[j]);
	            company.addDepartment(dept);
	            em.persist(dept);
	            for (int k = 0; k < EMPLOYEE_NAMES.length; k++) {
	                Employee emp = new Employee();
	                emp.setName(EMPLOYEE_NAMES[k]);
	                Address addr = new Address();
	                addr.setCity(CITY_NAMES[k]);
                    em.persist(emp);
	                em.persist(addr);
	                emp.setAddress(addr);
                    dept.addEmployees(emp);
	            }
	        }
	    }
        Person p1 = new Person("John", "Doe", (short)45, 1964);
        Person p2 = new Person("John", "Doe", (short)42, 1967);
        Person p3 = new Person("Harry", "Doe", (short)12, 1995);
        Person p4 = new Person("Barry", "Doe", (short)22, 1985);
        em.persist(p1);
        em.persist(p2);
        em.persist(p3);
        em.persist(p4);
        
        Author a1 = new Author("Author1", "", (short)40, 1960);
        Author a2 = new Author("Author2", "", (short)41, 1961);
        Author a3 = new Author("Author3", "", (short)42, 1962);
        Singer s1 = new Singer("Singer1", "", (short)21, 1991);
        Singer s2 = new Singer("Singer2", "", (short)22, 1992);
        
        long id = 100;
        Book   b1 = new Book("Book1");
        Book   b2 = new Book("Book2");
        CD     c1 = new CD("CD1");
        CD     c2 = new CD("CD2");
        
        b1.setId(id++); b1.setTitle("title-1"); b1.setToken("LARGE");
        b2.setId(id++); b2.setTitle("title-2"); b2.setToken("MEDIUM");
        c1.setId(id++);
        c2.setId(id++);
        b1.addAuthor(a1);
        b1.addAuthor(a2);
        b2.addAuthor(a2);
        b2.addAuthor(a3);
        c1.setSinger(s1);
        c2.setSinger(s2);
        
        em.persist(a1); em.persist(a2);   em.persist(a3);
        em.persist(s1); em.persist(s2);
        em.persist(b1); em.persist(b2);
        em.persist(c1); em.persist(c2);

        id = (int)System.currentTimeMillis();
        OrderJPA o1 = new OrderJPA();
        o1.setOrderId(id++);
        o1.setCustomerId(339);
        o1.setDistrictId(3);
        o1.setWarehouseId(23);
        
        OrderJPA o2 = new OrderJPA();
        o2.setOrderId(id++);
        o2.setCustomerId(2967);
        o2.setDistrictId(5);
        o2.setWarehouseId(22);
        
        em.persist(o1);
        em.persist(o2);
        
        for (int i = 1; i < 10; i++) {
            Parent parent = new Parent();
            parent.setId(i);
            parent.setName(new String("Parent "+i));
            Address addr = new Address();
            addr.setCity("Address "+i+i);
            parent.setAddrId(addr);
            em.persist(addr);
            for (int j = 1; j < 5; j++) {
                Child child = new Child();
                child.setName("Child "+i+j);
                child.setParent(parent);
                parent.add(child);
            }
            em.persist(parent);
        }

        em.getTransaction().commit();
	}

	@Override
	public void tearDown() throws Exception {
	    closeEM(em);
	    em = null;
	    if (TEST_COUNT >= 50) {
	        auditor.clear();
	        auditor = null;
	        closeEMF(emf);
	        emf = null;
	    }
		super.tearDown();
	}
    
    public void testCollectionValuedParameterOfEntities() {
        OpenJPAEntityManager em = emf.createEntityManager();
        String jpql1 = "select d from Department d where d.name in ('Marketing', 'Sales') order by d.name";
        String jpql2 = "select d from Department d where d.name in ('Engineering', 'Marketing') order by d.name";
        
        List<Department> param1 = (List<Department>) em.createQuery(jpql1).getResultList();
        List<Department> param2 = (List<Department>) em.createQuery(jpql2).getResultList();
        em.clear();
        
        String jpql = "select e from Employee e where e.department in :param";
        
        List<Employee> rs1 = em.createQuery(jpql).setParameter("param", param1).getResultList();

        for (int i = 0; i < rs1.size(); i++) {
            Employee e = (Employee) rs1.get(i);
            assertFalse(e.getDepartment().getName().equals("Engineering"));
        }
        
        List<Employee> rs2 = (List<Employee>) em.createQuery(jpql).setParameter("param", param2).getResultList();
        for (int i = 0; i < rs2.size(); i++) {
            Employee e = (Employee) rs2.get(i);
            assertFalse(e.getDepartment().getName().equals("Sales"));
        }

        em.clear();
        String jpql3 = "select e from Employee e where e.department in (:p1, :p2, :p3)";
        Query query = em.createQuery(jpql3);
        query.setParameter("p1", param1.get(0));
        query.setParameter("p2", param1.get(1));
        query.setParameter("p3", param1.get(2));
        List<Employee> rs3 = query.getResultList();
        for (int i = 0; i < rs3.size(); i++) {
            Employee e = (Employee) rs3.get(i);
            assertTrue(e.getDepartment().getName().equals("Marketing"));
        }

        em.clear();
        query = em.createQuery(jpql3);
        query.setParameter("p1", param2.get(0));
        query.setParameter("p2", param2.get(1));
        query.setParameter("p3", param2.get(2));
        List<Employee> rs4 = query.getResultList();
        for (int i = 0; i < rs4.size(); i++) {
            Employee e = (Employee) rs4.get(i);
            assertTrue(e.getDepartment().getName().equals("Engineering"));
        }

        em.clear();
        String jpql4 = "select p from Parent p where p.id < 3";
        String jpql5 = "select p from Parent p where p.id > 4";
        List<Parent> parm1 = em.createQuery(jpql4).getResultList();
        List<Parent> parm2 = em.createQuery(jpql5).getResultList();
        
        assertTrue("Size of two result list " + parm1.size() + " and " + parm2.size() + 
            " must not be same", parm1.size() != parm2.size());
        
        em.clear();
        String jpql6 = "select c from Child c where c.parent in ?1";
        Query qry = em.createQuery(jpql6);
        qry.setParameter(1, parm1);
        List<Child> c1 = qry.getResultList();
        for (int i = 0; i < c1.size(); i++) {
            Child child = (Child) c1.get(i);
            assertTrue(child.getParent().getId() < 3);
        }
        
        em.clear();
        qry = em.createQuery(jpql6);
        qry.setParameter(1, parm2);
        List<Child> c2 = qry.getResultList();
        for (int i = 0; i < c2.size(); i++) {
            Child child = (Child) c2.get(i);
            assertTrue(child.getParent().getId() > 4);
        }
        
    }
    
    public void testCollectionValuedParameterOfEntitiesWithEmptyList() {
        OpenJPAEntityManager em = emf.createEntityManager();
        String jpql1 =
            "select d from Department d where d.name in ('Marketing', 'Sales') order by d.name";
        List<Department> param1 =
            (List<Department>) em.createQuery(jpql1).getResultList();
        em.clear();

        String jpql = "select e from Employee e where e.department in :param";

        List<Employee> rs1 =
            em.createQuery(jpql).setParameter("param", param1).getResultList();

        for (int i = 0; i < rs1.size(); i++) {
            Employee e = (Employee) rs1.get(i);
            assertFalse(e.getDepartment().getName().equals("Engineering"));
        }

        // Prior to OPENJPA-2118, the following query would yeild a
        // 'ArithmeticException: divide
        // by zero' exception (see JIRA for details).
        try {
            // Pass an empty list to 'param'.
            em.createQuery(jpql).setParameter("param",
                new ArrayList<Department>()).getResultList();
        } catch (ArgumentException ae) {
            assertEquals(ae.getCause().getMessage(),
                "Input parameter \"param\" is empty.");
        }
    }
    
    public void testRepeatedParameterInSubqueryInDifferentOrderSubQLast() {
        OpenJPAEntityManager em = emf.createEntityManager();
       
        String jpql = "SELECT o from OrderJPA o " +
                "WHERE (o.CustomerId = :customerId) " +
                "AND (o.WarehouseId = :warehouseId) " +
                "AND (o.DistrictId = :districtId) " +
                "AND o.OrderId IN (SELECT MAX (o1.OrderId) from OrderJPA o1 " +
                    "WHERE ((o1.CustomerId = :customerId) " +
                    "AND    (o1.DistrictId = :districtId) " +
                    "AND    (o1.WarehouseId = :warehouseId)))";
        
        em.getTransaction().begin();
        TypedQuery<OrderJPA> q1 = em.createQuery(jpql, OrderJPA.class);
        q1.setParameter("customerId", 339)
          .setParameter("districtId", 3)
          .setParameter("warehouseId", 23);
                  
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q1).getLanguage());
        assertFalse(q1.getResultList().isEmpty());        
        
        TypedQuery<OrderJPA> q2 = em.createQuery(jpql, OrderJPA.class);
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, OpenJPAPersistence.cast(q2).getLanguage());
        q2.setParameter("customerId", 2967)
          .setParameter("districtId", 5)
          .setParameter("warehouseId", 22);
        
        assertFalse(q2.getResultList().isEmpty());
        em.getTransaction().rollback();
        
    }

	public void testPreparedQueryCacheIsActiveByDefault() {
		assertNotNull(getPreparedQueryCache());
	}
	
	public void testPreparedQueryCacheIsPerUnitSingleton() {
		PreparedQueryCache c1 = getPreparedQueryCache();
		PreparedQueryCache c2 = getPreparedQueryCache();
		assertSame(c1, c2);
	}
	
	public void testPreparedQueryIdentifierIsOriginalJPQLQuery() {
        String jpql = "select p from Company p";
        OpenJPAQuery<?> q1 = em.createQuery(jpql);
        q1.getResultList();
        PreparedQuery pq = getPreparedQueryCache().get(jpql);
        assertNotNull(pq);
        assertEquals(jpql, pq.getIdentifier());
        assertEquals(jpql, pq.getOriginalQuery());
	}
	
	public void testOriginalJPQLQueryStringIsSetOnPreparedQuery() {
        String jpql = "select p from Company p";
        OpenJPAQuery<?> q1 = em.createQuery(jpql);
        q1.getResultList();
        PreparedQuery pq = getPreparedQueryCache().get(jpql);
        assertNotNull(pq);
        OpenJPAQuery<?> q2 = em.createQuery(jpql);
        assertEquals(jpql,q2.getQueryString());
	}

	public void testOrderByElementsAbsentInProjection() {
	    String jpql = "select c.name from Company c ORDER BY c.startYear";
        OpenJPAQuery<?> q1 = em.createQuery(jpql);
        List l1 = q1.getResultList();
        
        PreparedQuery pq = getPreparedQueryCache().get(jpql);
        assertNotNull(pq);
        OpenJPAQuery<?> q2 = em.createQuery(jpql);
        List l2 = q2.getResultList();
        
        assertEquals(l1.size(), l2.size());
        assertEquals(l1.toString(), l2.toString());
	}
	
	public void testExclusionPattern() {
		OpenJPAQuery<?> q1 = em.createQuery(EXCLUDED_QUERY_1);
		q1.getResultList();
		assertNotCached(EXCLUDED_QUERY_1);
		
		OpenJPAQuery<?> q2 = em.createQuery(EXCLUDED_QUERY_2);
		q2.getResultList();
		assertNotCached(EXCLUDED_QUERY_2);
		
		OpenJPAQuery<?> q3 = em.createQuery(INCLUDED_QUERY);
		q3.getResultList();
		assertCached(INCLUDED_QUERY);
	}
	
	void assertLanguage(OpenJPAQuery<?> q, String lang) {
		assertEquals(lang, q.getLanguage());
	}
	
	void assertCached(String id) {
		PreparedQuery cached = getPreparedQueryCache().get(id);
		assertNotNull(getPreparedQueryCache() + ": " + getPreparedQueryCache().getMapView() + 
				" does not contain " + id, cached);
	}
	
	void assertNotCached(String id) {
	    PreparedQueryCache cache = getPreparedQueryCache();
		if (cache != null) {
			assertNull(cache.get(id));
		}
	}
	
	public void testPreparedQueryIsCachedOnExecution() {
		String jpql = "select p from Company p";
		OpenJPAQuery<?> q1 = em.createQuery(jpql);
		assertNotCached(jpql);
		assertLanguage(q1, JPQLParser.LANG_JPQL);
		
		q1.getResultList();
		assertCached(jpql);
		assertLanguage(q1, JPQLParser.LANG_JPQL);
		
		PreparedQuery cached = getPreparedQueryCache().get(jpql);
		assertEquals(jpql, cached.getIdentifier());
		assertFalse(jpql.equalsIgnoreCase(cached.getTargetQuery()));
	}

	public void testPreparedQueryIsCachedAcrossExecution() {
		String jpql = "select p from Company p";
		OpenJPAQuery<?> q1 = em.createQuery(jpql);
		assertNotCached(jpql);
		assertLanguage(q1, JPQLParser.LANG_JPQL);
		
		
		q1.getResultList();
		assertCached(jpql);
		assertLanguage(q1, JPQLParser.LANG_JPQL);
		
		// Create a new query with the same JPQL
		// This is not only cached, its language is different too
		OpenJPAQuery<?> q2 = em.createQuery(jpql);
		assertCached(jpql);
		assertLanguage(q2, QueryLanguages.LANG_PREPARED_SQL);
	}
	
	public void testInvalidatePreparedQueryWithHint() {
		String jpql = "select p from Company p";
		OpenJPAQuery<?> q1 = em.createQuery(jpql);
		assertNotCached(jpql);
		
		q1.getResultList();
		assertCached(jpql);
		assertLanguage(q1, JPQLParser.LANG_JPQL);
		
		// Create a new query with the same JPQL
		// This is cached on creation, its language is Prepared SQL
		OpenJPAQuery<?> q2 = em.createQuery(jpql);
		assertCached(jpql);
		assertLanguage(q2, QueryLanguages.LANG_PREPARED_SQL);
		q2.getResultList();
		
		// Now execute with hints to invalidate. 
		q2.setHint(QueryHints.HINT_INVALIDATE_PREPARED_QUERY, true);
		// Immediately it should be removed from the cache
		assertNotCached(jpql);
		assertEquals(JPQLParser.LANG_JPQL, q2.getLanguage());
		q2.getResultList();
		
		// Create a new query with the same JPQL
		// This is not cached on creation, its language is JPQL
		OpenJPAQuery<?> q3 = em.createQuery(jpql);
		assertNotCached(jpql);
		assertLanguage(q3, JPQLParser.LANG_JPQL);
	}
	
	public void testIgnorePreparedQueryWithHint() {
		String jpql = "select p from Company p";
		OpenJPAQuery<?> q1 = em.createQuery(jpql);
		assertNotCached(jpql);
		
		q1.getResultList();
		assertCached(jpql);
		assertLanguage(q1, JPQLParser.LANG_JPQL);
		
		// Create a new query with the same JPQL
		// This is cached on creation, its language is PREPARED SQL
		OpenJPAQuery<?> q2 = em.createQuery(jpql);
		assertCached(jpql);
		assertLanguage(q2, QueryLanguages.LANG_PREPARED_SQL);
		q2.getResultList();
		
		// Now execute with hints to ignore. 
		q2.setHint(QueryHints.HINT_IGNORE_PREPARED_QUERY, true);
		// It should remain in the cache
		assertCached(jpql);
		// But its language should be JPQL and not PREPARED SQL
		assertEquals(JPQLParser.LANG_JPQL, q2.getLanguage());
		q2.getResultList();
		
		// Create a new query with the same JPQL
		// This is cached on creation, its language is PREPARED SQL
		OpenJPAQuery<?> q3 = em.createQuery(jpql);
		assertCached(jpql);
		assertLanguage(q3, QueryLanguages.LANG_PREPARED_SQL);
	}

	public void testQueryStatistics() {
        QueryStatistics<String> stats = getPreparedQueryCache().getStatistics();
        stats.reset();

        String jpql1 = "select c from Company c";
        String jpql2 = "select c from Company c where c.name = 'PObject'";
		int N1 = 5;
		int N2 = 8;
		for (int i = 0; i < N1; i++) {
	        OpenJPAQuery<?> q1 = em.createQuery(jpql1);
			q1.getResultList();
		}
		for (int i = 0; i < N2; i++) {
	        OpenJPAQuery<?> q2 = em.createQuery(jpql2);
			q2.getResultList();
		}
		
		assertEquals(N1,      stats.getExecutionCount(jpql1));
		assertEquals(N2,      stats.getExecutionCount(jpql2));
		assertEquals(N1+N2,   stats.getExecutionCount());
		assertEquals(N1-1,    stats.getHitCount(jpql1));
		assertEquals(N2-1,    stats.getHitCount(jpql2));
		assertEquals(N1+N2-2, stats.getHitCount());
		
	}

	public void testResetQueryStatistics() {
        QueryStatistics<String> stats = getPreparedQueryCache().getStatistics();
        stats.reset();
        
		String jpql1 = "select c from Company c";
        String jpql2 = "select c from Company c where c.name = 'PObject'";
		int N10 = 4;
		int N20 = 7;
		for (int i = 0; i < N10; i++) {
	        OpenJPAQuery<?> q1 = em.createQuery(jpql1);
			q1.getResultList();
		}
		for (int i = 0; i < N20; i++) {
	        OpenJPAQuery<?> q2 = em.createQuery(jpql2);
			q2.getResultList();
		}
		
		assertEquals(N10,       stats.getExecutionCount(jpql1));
		assertEquals(N20,       stats.getExecutionCount(jpql2));
		assertEquals(N10+N20,   stats.getExecutionCount());
		assertEquals(N10-1,     stats.getHitCount(jpql1));
		assertEquals(N20-1,     stats.getHitCount(jpql2));
		assertEquals(N10+N20-2, stats.getHitCount());
		
		stats.reset();
		
		int N11 = 7;
		int N21 = 4;
		for (int i = 0; i < N11; i++) {
            OpenJPAQuery<?> q1 = em.createQuery(jpql1);
			q1.getResultList();
		}
		for (int i = 0; i < N21; i++) {
            OpenJPAQuery<?> q2 = em.createQuery(jpql2);
			q2.getResultList();
		}

		assertEquals(N11,     stats.getExecutionCount(jpql1));
		assertEquals(N21,     stats.getExecutionCount(jpql2));
		assertEquals(N11+N21, stats.getExecutionCount());
		assertEquals(N11,     stats.getHitCount(jpql1));
		assertEquals(N21,     stats.getHitCount(jpql2));
		assertEquals(N11+N21, stats.getHitCount());
		
//		assertEquals(N10+N11,     stats.getTotalExecutionCount(jpql1));
//		assertEquals(N20+N21,     stats.getTotalExecutionCount(jpql2));
//		assertEquals(N10+N11+N20+N21, stats.getTotalExecutionCount());
		assertEquals(N10+N11-1,     stats.getTotalHitCount(jpql1));
		assertEquals(N20+N21-1,     stats.getTotalHitCount(jpql2));
		assertEquals(N10+N11+N20+N21-2, stats.getTotalHitCount());
		
	}

	public void testQueryWithNoParameter() {
		String jpql = "select p from Company p";
		compare(jpql, !IS_NAMED_QUERY);
	}

	public void testQueryWithLiteral() {
        String jpql = "select p from Company p where p.name = " + literal(COMPANY_NAMES[0]);
		compare(jpql, !IS_NAMED_QUERY);
	}

	public void testQueryWithParameter() {
		String jpql = "select p from Company p where p.name = :param";
		Object[] params = {"param", COMPANY_NAMES[0]};
		compare(jpql, !IS_NAMED_QUERY,  params);
	}

	public void testQueryWithJoinsAndParameters() {
        String jpql = "select e from Employee e " + "where e.name = :emp"
					+ " and e.department.name = :dept"
                    + " and e.department.company.name LIKE  "
                    + literal(COMPANY_NAMES[0]) 
					+ " and e.address.city = :city";
		Object[] params = { "emp", EMPLOYEE_NAMES[0], 
                            "dept", DEPARTMENT_NAMES[0],
							"city", CITY_NAMES[0]};
		compare(jpql, !IS_NAMED_QUERY,  params);
	}

	public void testNamedQueryWithNoParameter() {
		String namedQuery = "Company.PreparedQueryWithNoParameter";
        compare(namedQuery, IS_NAMED_QUERY);
	}

	public void testNamedQueryWithLiteral() {
		String namedQuery = "Company.PreparedQueryWithLiteral";
		compare(namedQuery, IS_NAMED_QUERY);
	}

	public void testNamedQueryWithPositionalParameter() {
        String namedQuery = "Company.PreparedQueryWithPositionalParameter";
		Object[] params = {1, COMPANY_NAMES[0], 2, START_YEARS[0]};
		compare(namedQuery, IS_NAMED_QUERY,  params);
	}
	
	public void testNamedQueryWithNamedParameter() {
		String namedQuery = "Company.PreparedQueryWithNamedParameter";
        Object[] params = {"name", COMPANY_NAMES[0], "startYear",
                START_YEARS[0]};
		compare(namedQuery, IS_NAMED_QUERY,  params);
	}
	
	public void testPersistenceCapableParameter() {
        String jpql = "select e from Employee e " +
                "where e.department.company=:company";
	    Object[] params = {"company", IBM};
	    compare(jpql, !IS_NAMED_QUERY,  params);
	}
	
	/**
	 * Project results are returned with different types of ROP.
	 */
	public void testProjectionResult() {
        String jpql = "select e.name from Employee e " +
                "where e.address.city=:city";
        Object[] params = {"city", CITY_NAMES[0]};
        compare(jpql, !IS_NAMED_QUERY, params);
	}
	
	public void testCollectionValuedParameters() {
	    String jpql = "select e from Employee e where e.name in :names";
	    Object[] params1 = {"names",
	            Arrays.asList(new String[]{EMPLOYEE_NAMES[0],
	            EMPLOYEE_NAMES[1]})};
        Object[] params2 = {"names",
                Arrays.asList(new String[]{EMPLOYEE_NAMES[2]})};
        Object[] params3 = {"names", Arrays.asList(EMPLOYEE_NAMES)};
        
        int expectedCount = 2 * COMPANY_NAMES.length * DEPARTMENT_NAMES.length;
        run(jpql, !IS_NAMED_QUERY, params1, expectedCount, USE_CACHE, 1);
        
        expectedCount = 1 * COMPANY_NAMES.length * DEPARTMENT_NAMES.length;
        run(jpql, !IS_NAMED_QUERY, params2, expectedCount, USE_CACHE, 1);
        
        expectedCount = EMPLOYEE_NAMES.length * COMPANY_NAMES.length * DEPARTMENT_NAMES.length;
        run(jpql, !IS_NAMED_QUERY, params3, expectedCount, USE_CACHE, 1);
	}
	
    public void testQueryProjectionNotCandidateClass() {
        String jpql = "select e.department from Employee e";
        compare(jpql, !IS_NAMED_QUERY);
    }
    
    public void testQueryMultipleProjectionClass() {
        String jpql = "select d, e from Department d, in (d.employees) e";
        compare(jpql, !IS_NAMED_QUERY);
    }
    
    public void testQueryWithOrderByClause() {
        String jpql = "select e.name from Employee e order by e.id";
        compare(jpql, !IS_NAMED_QUERY);
    }
    
    public void testQueryCount() {
        String jpql = "select count(e),d from Department d join d.employees e group by d";
        compare(jpql, !IS_NAMED_QUERY);
    }
    
    public void testProjectRepeatsTerm() {
        String jpql = "select e.name, e.name from Employee e";
        compare(jpql, !IS_NAMED_QUERY);
    }
    
    public void testProjectEmbedded() {
        String jpql = "select e.address from Employee e";
        compare(jpql, !IS_NAMED_QUERY);
    }
    
    public void testNeedsTypeConversion() {
        String jpql = "select e.name, e.isManager from Employee e";
        compare(jpql, !IS_NAMED_QUERY);
    }    
	
	String literal(String s) {
	    return "'"+s+"'";
	}
	
    public void testPositional() {
        String jpql = "select p from Person p where p.firstName=?1" +
                      " and p.lastName='Doe' and p.age > ?2";
        EntityManager em = emf.createEntityManager();
        
        OpenJPAQuery<?> q1 = OpenJPAPersistence.cast(em.createQuery(jpql));
        assertEquals(JPQLParser.LANG_JPQL, q1.getLanguage());
        
        List<?> result1 = q1.setParameter(1, "John")
                       .setParameter(2, (short)40)
                       .getResultList();
        
        assertEquals(2, result1.size());
        
        OpenJPAQuery<?> q2 = OpenJPAPersistence.cast(em.createQuery(jpql));
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, q2.getLanguage());
        List<?> result2 = q2.setParameter(1, "Harry")
                  .setParameter(2, (short)10)
                  .getResultList();
        
        assertEquals(1, result2.size());
    }
    
    public void testNamed() {
        String jpql = "select p from Person p where p.firstName=:first" +
                      " and p.lastName='Doe' and p.age > :age";
        EntityManager em = emf.createEntityManager();
        
        OpenJPAQuery<?> q1 = OpenJPAPersistence.cast(em.createQuery(jpql));
        assertEquals(JPQLParser.LANG_JPQL, q1.getLanguage());
        
        List<?> result1 = q1.setParameter("first", "John")
                       .setParameter("age", (short)40)
                       .getResultList();
        
        assertEquals(2, result1.size());
        
        OpenJPAQuery<?> q2 = OpenJPAPersistence.cast(em.createQuery(jpql));
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, q2.getLanguage());
        List<?> result2 = q2.setParameter("first", "Barry")
                  .setParameter("age", (short)20)
                  .getResultList();
        
        assertEquals(1, result2.size());
    }
    
    public void testWrongParameterValueTypeThrowException() {
        String jpql = "select p from Person p where p.firstName=:first" 
                    + " and p.age > :age";
        EntityManager em = emf.createEntityManager();

        OpenJPAQuery<?> q1 = OpenJPAPersistence.cast(em.createQuery(jpql));
        try {
            List<?> result1 = q1.setParameter("first", (short)40)
                             .setParameter("age", "John")
                             .getResultList();
            fail("Expected to fail with wrong parameter value");
        } catch (IllegalArgumentException e) {
            // good
        }
    }
    
    public void testNullParameterValueForPrimitiveTypeThrowsException() {
        String jpql = "select p from Person p where p.firstName=:first" 
                    + " and p.age > :age";
        EntityManager em = emf.createEntityManager();

        OpenJPAQuery<?> q1 = OpenJPAPersistence.cast(em.createQuery(jpql));
        try {
            List<?> result1 = q1.setParameter("first", "John")
                             .setParameter("age", null)
                             .getResultList();
            fail("Expected to fail with null parameter value for primitives");
        } catch (RuntimeException e) {
            // good
        }
    }
    public void testQueryWithLazyRelationIsCached() {
        // Author is lazily related to Book
        String jpql = "select p from Author p";
        EntityManager em = emf.createEntityManager();
        
        Query q1 = em.createQuery(jpql);
        assertEquals(OpenJPAPersistence.cast(q1).getLanguage(),
                JPQLParser.LANG_JPQL);
        List<Author> authors1 = q1.getResultList();
        assertFalse(authors1.isEmpty());
        Author author1 = authors1.iterator().next();
        em.close(); // nothing will be loaded by chance
        
        assertNull(author1.getBooks());
        
        // do the same thing again, this time query should be cached
        em = emf.createEntityManager();
        Query q2 = em.createQuery(jpql);
        assertEquals(OpenJPAPersistence.cast(q2).getLanguage(),
                QueryLanguages.LANG_PREPARED_SQL);
        List<Author> authors2 = q2.getResultList();
        assertFalse(authors2.isEmpty());
        Author author2 = authors2.iterator().next();
        em.close();
        
        assertNull(author2.getBooks());
    }
    
    public void testQueryWithEagerRelationIsNotCached() {
        // Book is eagerly related to Author
        String jpql = "select b from Book b";
        EntityManager em = emf.createEntityManager();
        
        Query q1 = em.createQuery(jpql);
        assertEquals(OpenJPAPersistence.cast(q1).getLanguage(),
                JPQLParser.LANG_JPQL);
        List<Book> books = q1.getResultList();
        assertFalse(books.isEmpty());
        Book book1 = books.iterator().next();
        em.close(); // nothing will be loaded by chance
        
        assertNotNull(book1.getAuthors());
        assertFalse(book1.getAuthors().isEmpty());
        
        // do the same thing again, this time query should not be cached
        // because it requires multiple selects
        em = emf.createEntityManager();
        Query q2 = em.createQuery(jpql);
        assertEquals(OpenJPAPersistence.cast(q2).getLanguage(),
                JPQLParser.LANG_JPQL);
        List<Book> books2 = q2.getResultList();
        assertFalse(books2.isEmpty());
        Book book2 = books2.iterator().next();
        em.close();
        
        assertNotNull(book2.getAuthors());
        assertFalse(book2.getAuthors().isEmpty());
    }

    public void testQueryWithUserDefinedAndInternalParamtersInSubquery() {
        String jpql = "Select a From Address a Where Not Exists ("
            + "     Select s.id From Singer As s Where "
            + "        s.address = a  And "
            + "        Not ("
            + "              (s.firstName = :firstName) "
            + "              Or "
            + "              ("
            + "                  ("
            + "                      exists (select c.id from CD c where c.singer = s and c.status = 1) And "
            + "                      s.lastName = :lastName"
            + "                  ) "
            + "                  Or "
            + "                  ("
            + "                      not exists (Select c.id from CD c where c.singer = s and c.status = 2)"
            + "                  )"
            + "              )"
            + "            )"
            + "     )";
        
        Query jQ = em.createQuery(jpql);
        jQ.setParameter("lastName", "LastName");
        jQ.setParameter("firstName", "FirstName");
        List jList = jQ.getResultList();
        
        Query jQ1 = em.createQuery(jpql);
        jQ1.setParameter("lastName", "LastName1");
        jQ1.setParameter("firstName", "FirstName1");
        try {
            List jList1 = jQ1.getResultList();
        } catch (Exception e) {
            System.err.println(jQ1.getParameters());
            e.printStackTrace();
            fail("Fail to execute again - Parameters are messed up:" + e.getMessage());
        }
    }
    
    public void testPreparedQueryIgnoredWhenLockModeIsSet() {
        String jpql = "select p from Author p";
        EntityManager em = emf.createEntityManager();
        
        Query q1 = em.createQuery(jpql);
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q1).getLanguage());
        List<Author> authors1 = q1.getResultList();
        
        // do the same thing again, this time query should be cached
        em.getTransaction().begin();
        Query q2 = em.createQuery(jpql);
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, OpenJPAPersistence.cast(q2).getLanguage());
        LockModeType lmode1 = q2.getLockMode();
        q2.setLockMode(LockModeType.OPTIMISTIC);
        LockModeType lmode2 = q2.getLockMode();
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q2).getLanguage());
        assertFalse(lmode1.equals(lmode2));
        List<Author> authors2 = q2.getResultList();
        em.getTransaction().rollback();
    }
    
    public void testEnumParameter() {
        String jpql = "select e from Employee e where e.status=:current and e.hireStatus=:hire";
        EntityManager em = emf.createEntityManager();
        
        TypedQuery<Employee> q1 = em.createQuery(jpql, Employee.class);
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q1).getLanguage());
        List<Employee> emps = q1.setParameter("current", Category.PERMANENT)
                                .setParameter("hire", Category.CONTRACTOR).getResultList();
        
        // do the same thing again, this time query should be cached
        em.getTransaction().begin();
        TypedQuery<Employee> q2 = em.createQuery(jpql, Employee.class);
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, OpenJPAPersistence.cast(q2).getLanguage());
        List<Employee> emps2 = q2.setParameter("current", Category.PERMANENT)
                            .setParameter("hire", Category.CONTRACTOR).getResultList();
        em.getTransaction().rollback();
    }
    
    public void testMultithreadedAccess() {
        OpenJPAEntityManager em1 = emf.createEntityManager();
        String jpql = "select p from Author p where p.name=:name";
        int N = 5;
        Thread[] threads =  new Thread[N];
        QueryThread[] qts = new QueryThread[N];
        for (int i = 0; i < N; i++) {
            OpenJPAEntityManager emt = emf.createEntityManager();
            qts[i] = new QueryThread(emt, jpql);
            threads[i] = new Thread(qts[i]);
            threads[i].setDaemon(true);
        }
        for (Thread t : threads) {
            t.start(); 
        }
        for (int i = 0; i < N; i++) {
            try {
              threads[i].join();
              assertFalse(qts[i].isFailed());
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
        }
    }
    
    public void testParameterOnExternalizedFieldIsExcluded() {
        String jpql = "select b from Book b where b.title=:title and b.token=:token";
        Query q1 = em.createQuery(jpql)
          .setParameter("title", "title-1")
          .setParameter("token", "LARGE");
        // default fetches authors eagerly and thus creates multiple SQL and hence not caches anyway
        OpenJPAPersistence.cast(q1).getFetchPlan().removeFetchGroup("default");
        assertFalse(q1.getResultList().isEmpty());
        assertNotCached(jpql);
        Query q2 = em.createQuery(jpql)
                     .setParameter("title", "title-2")
                     .setParameter("token", "MEDIUM");
       assertFalse(q2.getResultList().isEmpty());
    }
    
    public void testNoParameterOnExternalizedFieldIsIncluded() {
        String jpql = "select b from Book b where b.title=:title";
        Query q1 = em.createQuery(jpql)
          .setParameter("title", "title-1");
        // default fetches authors eagerly and thus creates multiple SQL and hence not caches anyway
        OpenJPAPersistence.cast(q1).getFetchPlan().removeFetchGroup("default");
        assertFalse(q1.getResultList().isEmpty());
        assertCached(jpql);
        Query q2 = em.createQuery(jpql)
                     .setParameter("title", "title-2");
       assertFalse(q2.getResultList().isEmpty());
    }
    
    public void testSubqueryParameters() {
        EntityManager em = emf.createEntityManager();
        String query = "select e from Employee e "
            + "inner join e.department d "
            + "inner join d.company c "
            + "where mod(c.startYear, 100) = 0 "
            + "and exists (select e2 from Employee e2 "
                + "inner join e2.department d2 "
                + "inner join d2.company c2 "
                + "where e2.address.city = e.address.city "
                + "and e2.isManager = false "
                + "and d2.name = d.name "
                + "and c2.name = :companyName) "
            + "and d.name = :departmentName";

        em.getTransaction().begin();
        TypedQuery<Employee> q1 = em.createQuery(query, Employee.class);
        q1.setParameter("companyName", "acme.org");
        q1.setParameter("departmentName", "Engineering");
        assertEquals(q1.getResultList().size(), 6);

        TypedQuery<Employee> q2 = em.createQuery(query, Employee.class);
        q2.setParameter("companyName", "acme.org");
        q2.setParameter("departmentName", "Engineering");
        assertEquals(q2.getResultList().size(), 6);
        em.getTransaction().rollback();
    }

    public void testRepeatedParameterInSubqueryInDifferentOrder() {
        OpenJPAEntityManager em = emf.createEntityManager();
        String jpql =  "select o from OrderJPA o " 
                             + "where o.OrderId in (select max(o1.OrderId) from OrderJPA o1 "
                                   +  "where ((o1.CustomerId = :customerId) " 
                                   +  "and   (o1.DistrictId = :districtId) " 
                                   +  "and   (o1.WarehouseId = :warehouseId))) " 
                    +  "and (o.CustomerId = :customerId) "
                    +  "and (o.WarehouseId = :warehouseId) "
                    +  "and (o.DistrictId = :districtId)";

        em.getTransaction().begin();
        TypedQuery<OrderJPA> q1 = em.createQuery(jpql, OrderJPA.class);
        q1.setParameter("customerId", 339)
          .setParameter("districtId", 3)
          .setParameter("warehouseId", 23);
                  
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q1).getLanguage());
        assertFalse(q1.getResultList().isEmpty());
        
        
        TypedQuery<OrderJPA> q2 = em.createQuery(jpql, OrderJPA.class);
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, OpenJPAPersistence.cast(q2).getLanguage());
        q2.setParameter("customerId", 2967)
          .setParameter("districtId", 5)
          .setParameter("warehouseId", 22);
        
        assertFalse(q2.getResultList().isEmpty());
        em.getTransaction().rollback();
    }
    
    public void testRepeatedParameterInSubqueryInSameOrder() {
        OpenJPAEntityManager em = emf.createEntityManager();
        String jpql =  "select o from OrderJPA o " 
                             + "where o.OrderId in (select max(o1.OrderId) from OrderJPA o1 "
                             +  "where ((o1.CustomerId = :customerId) " 
                             +  "and   (o1.DistrictId = :districtId) " 
                             +  "and   (o1.WarehouseId = :warehouseId))) " 
                        +  "and (o.CustomerId = :customerId) "
                        +  "and (o.DistrictId = :districtId) "
                        +  "and (o.WarehouseId = :warehouseId)";

        em.getTransaction().begin();
        TypedQuery<OrderJPA> q1 = em.createQuery(jpql, OrderJPA.class);
        q1.setParameter("customerId", 339)
          .setParameter("districtId", 3)
          .setParameter("warehouseId", 23);
                  
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q1).getLanguage());
        assertFalse(q1.getResultList().isEmpty());
        
        
        TypedQuery<OrderJPA> q2 = em.createQuery(jpql, OrderJPA.class);
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, OpenJPAPersistence.cast(q2).getLanguage());
        q2.setParameter("customerId", 2967)
          .setParameter("districtId", 5)
          .setParameter("warehouseId", 22);
        
        assertFalse(q2.getResultList().isEmpty());
        em.getTransaction().rollback();
    }
    
    public void testPartiallyRepeatedParameterInSubquery() {
        OpenJPAEntityManager em = emf.createEntityManager();
        String jpql =  "select o from OrderJPA o " 
                             + "where o.OrderId in (select max(o1.OrderId) from OrderJPA o1 "
                             +  "where ((o1.CustomerId = :customerId) " 
                             +  "and   (o1.WarehouseId = :warehouseId))) " 
                        +  "and (o.CustomerId = :customerId) "
                        +  "and (o.DistrictId = :districtId) "
                        +  "and (o.WarehouseId = :warehouseId)";

        em.getTransaction().begin();
        TypedQuery<OrderJPA> q1 = em.createQuery(jpql, OrderJPA.class);
        q1.setParameter("customerId", 339)
          .setParameter("districtId", 3)
          .setParameter("warehouseId", 23);
                  
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q1).getLanguage());
        assertFalse(q1.getResultList().isEmpty());
        
        
        TypedQuery<OrderJPA> q2 = em.createQuery(jpql, OrderJPA.class);
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, OpenJPAPersistence.cast(q2).getLanguage());
        q2.setParameter("customerId", 2967)
          .setParameter("districtId", 5)
          .setParameter("warehouseId", 22);
        
        assertFalse(q2.getResultList().isEmpty());
        em.getTransaction().rollback();
    }
    
    public void testPartiallyRepeatedParameterInMainquery() {
        OpenJPAEntityManager em = emf.createEntityManager();
        String jpql =  "select o from OrderJPA o " 
                             + "where o.OrderId in (select max(o1.OrderId) from OrderJPA o1 "
                             +  "where ((o1.CustomerId = :customerId) " 
                             +  "and   (o1.DistrictId = :districtId) " 
                             +  "and   (o1.WarehouseId = :warehouseId))) " 
                        +  "and (o.CustomerId = :customerId) "
                        +  "and (o.WarehouseId = :warehouseId)";

        em.getTransaction().begin();
        TypedQuery<OrderJPA> q1 = em.createQuery(jpql, OrderJPA.class);
        q1.setParameter("customerId", 339)
          .setParameter("districtId", 3)
          .setParameter("warehouseId", 23);
                  
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q1).getLanguage());
        assertFalse(q1.getResultList().isEmpty());
        
        
        TypedQuery<OrderJPA> q2 = em.createQuery(jpql, OrderJPA.class);
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, OpenJPAPersistence.cast(q2).getLanguage());
        q2.setParameter("customerId", 2967)
          .setParameter("districtId", 5)
          .setParameter("warehouseId", 22);
        
        assertFalse(q2.getResultList().isEmpty());
        em.getTransaction().rollback();
    }

    public void testRangeIsExcluded() {
        List<Company> l = null;

        l = getAllCompaniesPaged(0, 1);
        assertEquals(1, l.size());
        assertEquals(1900, l.get(0).getStartYear());
        
        l = getAllCompaniesPaged(1, 1);
        assertEquals(1, l.size());
        assertEquals(2000, l.get(0).getStartYear());
        
        l = getAllCompaniesPaged(2, 1);
        assertEquals(1, l.size());
        assertEquals(2010, l.get(0).getStartYear());
    }

    public List<Company> getAllCompaniesPaged(int start, int max) {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select p from Company p order by p.startYear");
        q.setFirstResult(start);
        q.setMaxResults(max);
        return (List<Company>) q.getResultList();
    }
    
    PreparedQueryCache getPreparedQueryCache() {
        return emf.getConfiguration().getQuerySQLCacheInstance();
    }
    
    /**
     * Compare the result of execution of the non-parameterized query with and without PreparedQuery cache.
     * 
     * @param jpql a String representing either a JPQL or the name of a NamedQuery
     * @param isNamedQuery flags if the first input represents a JPQL string or a NamedQuery name
     */
    void compare(String jpql, boolean isNamed) {
        compare(jpql, isNamed, -1, NO_PARAMS);
    }

    
    /**
     * Compare the result of execution of the non-parameterized query with and without PreparedQuery cache.
     * 
     * @param jpql a String representing either a JPQL or the name of a NamedQuery
     * @param isNamedQuery flags if the first input represents a JPQL string or a NamedQuery name
     * @param expectedCount expected number of results
     */
    void compare(String jpql, boolean isNamed, int expectedCount) {
        compare(jpql, isNamed, expectedCount, NO_PARAMS);
    }
    
    /**
     * Compare the result of execution of the query with and without PreparedQuery cache.
     * 
     * 
     * @param jpql a String representing either a JPQL or the name of a NamedQuery
     * @param isNamedQuery flags if the first input represents a JPQL string or a NamedQuery name
     * @param params a even sized array whose even-indexed elements are keys and odd-indexed elements are corresponding
     * parameter value for the query. A null array denotes the query is not parameterized.
     */
    void compare(String jpql, boolean isNamed, Object... params) {
        compare(jpql, isNamed, -1, params);
    }
    /**
     * Compare the result of execution of the query with and without PreparedQuery cache.
     * 
     * 
     * @param jpql a String representing either a JPQL or the name of a NamedQuery
     * @param isNamedQuery flags if the first input represents a JPQL string or a NamedQuery name
     * @param params a even sized array whose even-indexed elements are keys and odd-indexed elements are corresponding
     * parameter value for the query. A null array denotes the query is not parameterized.
     * @param expectedCount expected number of results. Supply a negative number to ignore.
     */
    void compare(String query, boolean isNamed, int expectedCount, Object... params) {
        run(query, isNamed, params, expectedCount, !USE_CACHE, 1); // run the query once for warming up
        
        // run N times without cache
        auditor.clear();
        long without = run(query, isNamed, params, expectedCount, !USE_CACHE, SAMPLE_SIZE);
        List<String> originalSQLs = auditor.getSQLs();
        
        // run N times with cache
        auditor.clear();
        long with = run(query, isNamed, params, expectedCount, USE_CACHE, SAMPLE_SIZE);
        List<String> cachedSQLs = auditor.getSQLs();
        
        compareSQLs(originalSQLs, cachedSQLs);
        long delta = (without == 0) ? 0 : (without - with) * 100 / without;
        
        String jpql = getJPQLString(query, isNamed);
        System.err.println((delta < 0 ? "***WARN " : "") + Math.abs(delta) + "% " + 
                (delta < 0 ? "degradtion" : "improvement") + " for ["+ jpql + "]");
        assertTrue(Math.abs(delta) + "% degradtion for ["+ jpql + "]", !FAIL_IF_PERF_DEGRADE || delta > 0);
    }
    
    void compareSQLs(List<String> a, List<String> b) {
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i), b.get(i));
        }
    }

    /**
     * Create a query from the given string and execute it for given number of times.
     * 
     * @param jpql a String representing either a JPQL or the name of a NamedQuery
     * @param isNamedQuery flags if the first input represents a JPQL string or a NamedQuery name
     * @param params a even sized array whose even-indexed elements are keys and odd-indexed elements are corresponding
     * parameter value for the query. A null array denotes the query is not parameterized.
     * @param expectedCount expected number of results. Supply a negative number to ignore.
     * @param useCache flags if the PreparedQuery cache is to be activated.
     * @param N number of times the query is to be executed to calculate reasonable statistics.
     * 
     * @return median time to execute a query and iterate through its results 
     */
    long run(String jpql, boolean isNamedQuery, Object[] params, int expectedCount, boolean useCache, int N) {
        List<Long> stats = new ArrayList<Long>();
        String cacheKey = getJPQLString(jpql, isNamedQuery);
        QueryStatistics<String> cacheStats = getPreparedQueryCache().getStatistics();
        getPreparedQueryCache().clear();
        assertEquals(0, cacheStats.getExecutionCount(cacheKey));
        assertEquals(0, cacheStats.getHitCount(cacheKey));
        
        for (int i = 0; i < N; i++) {
            OpenJPAEntityManagerSPI em = (OpenJPAEntityManagerSPI)emf.createEntityManager();
            em.setQuerySQLCache(useCache);
            assertEquals(useCache, em.getQuerySQLCache());
            
            // measure time 
            long start = System.nanoTime();
            OpenJPAQuery<?> q = isNamedQuery ? em.createNamedQuery(jpql) : em.createQuery(jpql);
            parameterize(q, params);
            List<?> list = q.getResultList();
            if (expectedCount >= 0)
                assertEquals(expectedCount, list.size());
            else
                assertFalse(list.isEmpty());
            iterate(list);
            long end = System.nanoTime();   
            
            assertEquals(useCache ? i+1 : 0, cacheStats.getExecutionCount(cacheKey));
            assertEquals(useCache ? i : 0,   cacheStats.getHitCount(cacheKey));
            
            q.closeAll();
            stats.add(end - start);
            em.close();
        }
        assertEquals("Execution Count [" + cacheKey + "]", useCache ? N : 0, cacheStats.getTotalExecutionCount());
        assertEquals("Hit Count [" + cacheKey + "]", useCache ? N-1 : 0, cacheStats.getTotalHitCount());
        
        Collections.sort(stats);
        return stats.get(N/2);
    }   
    
    
    void parameterize(Query q, Object[] params) {
        if (params == null)
            return;
        for (int j = 0; params != null && j < params.length - 1; j += 2) {
            Object key = params[j];
            Object val = params[j + 1];
            if (key instanceof Integer)
                q.setParameter(((Number)key).intValue(), val); 
            else if (key instanceof String)
                q.setParameter(key.toString(), val); 
            else
                fail("key " + key + " is neither Number nor String");
        }
    }
    
    void iterate(List<?> list) {
        Iterator<?> i = list.iterator();
        while (i.hasNext())
            i.next();
    }
    
    /**
     * Gets the JPQL String of a NamedQuery of the given name.
     */
    String getJPQLString(String name, boolean isNamedQuery) {
        if (!isNamedQuery)
            return name;
        return emf.getConfiguration()
                  .getMetaDataRepositoryInstance()
                  .getQueryMetaData(null, name, null, true)
                  .getQueryString();
    }


    /**
     * A JDBC Listener to audit target SQL executed per JPQL query.
     *
     */
    public class SQLAuditor extends AbstractJDBCListener {
        private List<String> sqls = new ArrayList<String>();
    
        @Override
        public void beforeExecuteStatement(JDBCEvent event) {
            if (event.getSQL() != null && sqls != null) {
               sqls.add(event.getSQL());
            }
        }
        
        void clear() {
            sqls.clear();
        }
        
        List<String> getSQLs() {
            return new ArrayList<String>(sqls);
        }
    }
    
    public static class QueryThread implements Runnable {
        private final OpenJPAEntityManager em;
        private final String jpql;
        private boolean failed = false;
        public QueryThread(OpenJPAEntityManager em, String jpql) {
            super();
            this.em = em;
            this.jpql = jpql;
        }
        
        public void run() {
            try {
                for (int i = 0; i < 10 && !failed; i++) {
                    OpenJPAQuery q = em.createQuery(jpql);
                    q.setParameter("name", "Author-"+i);
                    q.getResultList();
                    if (i > 1) 
                        assertEquals(QueryLanguages.LANG_PREPARED_SQL, q.getLanguage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed = true;
            }
        }
        
        public boolean isFailed() {
            return failed;
        }
        
    }

}
