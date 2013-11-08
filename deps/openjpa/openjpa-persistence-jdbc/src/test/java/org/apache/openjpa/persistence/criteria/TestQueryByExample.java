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
package org.apache.openjpa.persistence.criteria;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Attribute;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;

/**
 * Tests different styles for query by example.
 * 
 * @author Pinaki Poddar
 *
 */
public class TestQueryByExample extends CriteriaTest {
    DBDictionary dict = null;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        // If using an Oracle DB, use sql92 syntax in order to get a correct
        // comparison of SQL.  This may not work on Oracle JDBC drivers
        // prior to 10.x
        OpenJPAEntityManagerSPI ojem = (OpenJPAEntityManagerSPI)em;
        dict = ((JDBCConfiguration) ojem.getConfiguration())
            .getDBDictionaryInstance();
        if (dict instanceof OracleDictionary) {
            dict.setJoinSyntax("sql92");
        }
    }
    
    @Override
    public void tearDown() throws Exception {
        dict = null;
        super.tearDown();
    }
    
    public void testBasicFieldsWithNonDefaultValue() {
        String jpql = "SELECT e FROM Employee e WHERE e.rating=1 AND e.salary=1100";
        
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        
        Employee example = new Employee();
        example.setSalary(1000+100);
        example.setRating(1);
        
        ComparisonStyle style = null;
        Attribute<?,?>[] excludes = null;
        q.where(cb.qbe(q.from(Employee.class), example, style, excludes));
        
        assertEquivalence(q, jpql);
    }
    
    public void testExcludeBasicFieldWithNonDefaultValue() {
        String jpql = "SELECT e FROM Employee e WHERE e.salary=1100";
        
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        
        Employee example = new Employee();
        example.setSalary(1000+100);
        example.setRating(1);
        
        ComparisonStyle style = null;
        Attribute<?,?>[] excludes = {Employee_.rating};
        q.where(cb.qbe(q.from(Employee.class), example, style, excludes));
        
        assertEquivalence(q, jpql);
    }
    
    public void testBasicFieldWithDefaultValueExcludedByDefaultStyle() {
        String jpql = "SELECT e FROM Employee e WHERE e.rating=1";
        
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        
        Employee example = new Employee();
        example.setRating(1);
        
        ComparisonStyle style = null;
        Attribute<?,?>[] excludes = null;
        q.where(cb.qbe(q.from(Employee.class), example, style, excludes));
        
        executeAndCompareSQL(q, "WHERE (t0.rating = ?)");        
        assertEquivalence(q, jpql);
    }
    
    public void testBasicFieldWithDefaultValueCanBeIncludedByStyle() {
        String jpql = "SELECT e FROM Employee e WHERE e.rating=1 AND e.salary=1100";
        
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        
        Employee example = new Employee();
        example.setRating(1);
        
        ComparisonStyle style = cb.qbeStyle();
        Attribute<?,?>[] excludes = null;
        q.where(cb.qbe(q.from(Employee.class), example, style.setExcludeDefault(false), excludes));
        
        executeAndCompareSQL(q, "WHERE (t0.rating = ? AND t0.salary = ?)");
        assertEquivalence(q, jpql);
    }
    
    public void testRelationFieldWithNonDefaultValue() {
        String jpql = "SELECT e FROM Employee e WHERE e.rating=1 AND e.salary=1100 AND e.department.name='ExampleDept'";
        
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        
        Employee example = new Employee();
        example.setSalary(1100);
        example.setRating(1);
        Department dept = new Department();
        dept.setName("ExampleDept");
        example.setDepartment(dept);
        
        
        ComparisonStyle style = cb.qbeStyle();
        Attribute<?,?>[] excludes = null;
        q.where(cb.qbe(q.from(Employee.class), example, style, excludes));
        
        executeAndCompareSQL(q, "WHERE (t1.name = ? AND t0.rating = ? AND t0.salary = ?)");
    }
    
    public void testRelationFieldWithNullValueIncluded() {
        String jpql = "SELECT e FROM Employee e WHERE e.rating=1 AND e.salary=1100 AND e.department IS NULL";

        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        
        Employee example = new Employee();
        example.setName("ExampleEmployee");
        example.setSalary(1100);
        example.setRating(1);
        example.setDepartment(null);
        
        ComparisonStyle style = cb.qbeStyle();
        Attribute<?,?>[] excludes = {Employee_.frequentFlierPlan, Employee_.manager, Employee_.spouse};
        q.where(cb.qbe(q.from(Employee.class), example, style.setExcludeNull(false).setExcludeDefault(false), 
                excludes));
        
        executeAndCompareSQL(q, "WHERE (1 <> 1 AND t0.DEPARTMENT_DEPTNO IS NULL " 
                + "AND t0.name = ? AND t0.rating = ? AND t0.salary = ?)");
    }
    
    public void testEmbeddedField() {
        String jpql = "SELECT e FROM Employee e WHERE e.rating=1 AND e.salary=1100 AND e.department IS NULL";

        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        
        Employee example = new Employee();
        example.setName("ExampleEmployee");
        example.setSalary(1100);
        example.setRating(1);
        Contact contact = new Contact();
        Address address = new Address();
        address.setCity("Plano");
        address.setState("TX");
        address.setCountry("USA");
        contact.setAddress(address);
        example.setContactInfo(contact);
        
        ComparisonStyle style = cb.qbeStyle();
        Attribute<?,?>[] excludes = {Employee_.department, Employee_.frequentFlierPlan, 
                Employee_.manager, Employee_.spouse};
        q.where(cb.qbe(q.from(Employee.class), example, style, excludes));
        
        executeAndCompareSQL(q, "WHERE (t1.city = ? AND t1.country = ? AND t1.state = ? " 
                + "AND t0.name = ? AND t0.rating = ? AND t0.salary = ?)");
    }
    
    void executeAndCompareSQL(CriteriaQuery<?> q, String expected) {
        auditor.clear();
        em.createQuery(q).getResultList();
        assertEquals(1,auditor.getSQLs().size());
        String actual = extract("WHERE", auditor.getSQLs().get(0));
        if (dict instanceof DB2Dictionary)
            return;
        assertEquals(expected, actual);
    }
    
    String extract(String key, String s) {
        int index = s.indexOf(key);
        return index == -1 ? "" : s.substring(index);
    }
}
