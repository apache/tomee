/*
 * TestFetchPlan.java
 *
 * Created on October 16, 2006, 3:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
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
package org.apache.openjpa.persistence.kernel;

import java.util.Iterator;
import java.util.List;
import java.util.Set;


import org.apache.openjpa.persistence.kernel.common.apps.PCAddress;
import org.apache.openjpa.persistence.kernel.common.apps.PCCompany;
import org.apache.openjpa.persistence.kernel.common.apps.PCCountry;
import org.apache.openjpa.persistence.kernel.common.apps.PCDepartment;
import org.apache.openjpa.persistence.kernel.common.apps.PCDirectory;
import org.apache.openjpa.persistence.kernel.common.apps.PCEmployee;
import org.apache.openjpa.persistence.kernel.common.apps.PCFile;
import org.apache.openjpa.persistence.kernel.common.apps.PCPerson;

import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestFetchPlan extends BaseKernelTest {

    static Object _rootDirId;
    static Object _rootCompanyId;

    static final int MAX_DEPTH = 5; // Maximum depth of the directories
    static final int MAX_CHILD = 3; // Maximum number of files/directory
    static final String quote = "\"";
    private static boolean firstTime = true;

    /**
     * Creates a new instance of TestFetchPlan
     */
    public TestFetchPlan() {
    }

    public TestFetchPlan(String name) {
        super(name);
    }

    /**
     * Clears past data and creates new data for test.
     * Clear test data before and not <em>after</em> such that one can analyze
     * the database for test failures.
     */
    public void setUp() throws Exception {
        if (firstTime) {
            firstTime = false;
            clearTestData();
            createTestData();
        }
    }

    /**
     * Create a directory tree of MAX_DEPTH with each directory having a single
     * directory and MAX_CHILD files.
     * Creates typical Employee-Department-Company-Address instances.
     *
     * @return the persitent identifier of the root directory.
     */
    void createTestData() {
        // create a tree of directories with files in them
        PCDirectory rootDir = new PCDirectory(getDirectoryName(0));
        PCDirectory parent = rootDir;
        for (int i = 1; i <= MAX_DEPTH; i++) {
            PCDirectory dir = new PCDirectory(getDirectoryName(i));
            parent.add(dir);

            for (int j = 0; j < MAX_CHILD; j++)
                parent.add(getFileName(j));

            parent = dir;
        }

        // create a graph
        //      | ---address-country
        //      |
        //  company-dept-employee-address-country
        //
        PCCountry country1 = new PCCountry("100", "Employee 1 Country");
        PCCountry country2 = new PCCountry("200", "Employee 2 Country");
        PCCountry ccountry = new PCCountry("300", "Company Country");

        PCCompany company = new PCCompany("Company");

        PCDepartment dept1 = new PCDepartment("Department1");
        PCDepartment dept2 = new PCDepartment("Department2");

        PCEmployee emp1 = new PCEmployee("Employee1");
        PCEmployee emp2 = new PCEmployee("Employee2");

        PCAddress addr1 = new PCAddress("Street1", "city1", country1);
        PCAddress addr2 = new PCAddress("Street2", "city2", country2);
        PCAddress caddr = new PCAddress("Street3", "city3", ccountry);

        dept1.addEmployee(emp1);
        dept2.addEmployee(emp2);

        company.addDepartment(dept1);
        company.addDepartment(dept2);

        company.setAddress(caddr);

        emp1.setAddress(addr1);
        emp2.setAddress(addr2);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(rootDir);
        pm.persist(company);
//        _rootDirId     = pm.getObjectId(rootDir);
        _rootDirId = rootDir.getId();
        assertNotNull(_rootDirId);
//        _rootCompanyId = pm.getObjectId(company);
        _rootCompanyId = company.getId();
        assertNotNull(_rootCompanyId);
        endTx(pm);
        endEm(pm);
    }

    /**
     * Test that the single valued field (_parent) is not traversed when the
     * fecth group selects only the _name field.
     */
    public void testZeroRecursionDepthSingleValuedField() {
        genericTestForSingleValuedRecursiveField("name", 4, 0);
    }

    /**
     * Test that the single valued field (_parent) is traversed once and only
     * once when the fecth group selects the _parent field with recursion depth
     * of 1 (default).
     */
    public void testOneRecursionDepthSingleValuedField() {
        genericTestForSingleValuedRecursiveField("name+parent", 4, 1);
    }

    /**
     * Test that the single valued field (_parent) is traversed twice and only
     * twice when the fecth group selects the _parent field with recursion depth
     * of 2.
     */
    public void testTwoRecursionDepthSingleValuedField() {
        genericTestForSingleValuedRecursiveField("name+parent+grandparent",
            4, 2);
    }

    public void testThreeRecursionDepthSingleValuedField() {
        genericTestForSingleValuedRecursiveField
            ("name+parent+grandparent+greatgrandparent", 4, 3);
    }

    public void testInfiniteRecursionDepthSingleValuedField() {
        genericTestForSingleValuedRecursiveField("allparents", 4, -1);
    }

    /**
     * Generically tests recursive traversal of single-valued parent field.
     *
     * @param plan a plan that fetches L parents and no children
     * @param rd the recursion depth of directory from the root
     * @param fd the fetch depth = number of parents fetched
     */
    public void genericTestForSingleValuedRecursiveField(String plan, int rd,
        int fd) {
        PCDirectory result = queryDirectoryWithPlan(plan, rd, fd);

        checkParents(result, rd, fd);

        Object children = PCDirectory.reflect(result, "_children");
        assertNull(children);
    }

    /**
     * Query to obtain a single directory at the given depth.
     * The directory name is constructed by the depth it occurs (d0 for root,
     * d1 for depth 1 and so on).<BR>
     * Checks the result for for matching name and size of the result (must
     * be one).
     *
     * @param plan name of a fetch plan
     * @param depth depth of the directory to be queried
     * @return the selected directory.
     */
    PCDirectory queryDirectoryWithPlan(String plan, int rd, int fd) {
        OpenJPAEntityManager pm = getPM();
        pm.getFetchPlan().addFetchGroup(plan);
        if (fd != 0)
            pm.getFetchPlan().setMaxFetchDepth(fd);

//        String filter = "_name == " + quoted(getDirectoryName(rd));        
//        OpenJPAQuery query = pm.createNativeQuery(filter,PCDirectory.class);
//        List result = (List) query.getResultList();

        String query = "SELECT o FROM PCDirectory o WHERE o._name = '" +
            getDirectoryName(rd) + "'";
        List fresult = ((OpenJPAQuery) pm.createQuery(query)).getResultList();

        assertEquals(1, fresult.size());
        PCDirectory dir = (PCDirectory) fresult.get(0);

        return dir;
    }

    /**
     * Asserts that
     * <LI> the given directory name matches the directory name at depth D.
     * <LI> the parents upto L recursion is not null and beyond is
     * null.
     *
     * @param result a directory to test
     * @param D depth at which this directory appears
     * @param L the number of live (fetched) parents. -1 denotes infinite
     */
    void checkParents(PCDirectory result, int D, int L) {

        assertEquals("ge", getDirectoryName(D),
            PCDirectory.reflect(result, "_name"));
        PCDirectory[] parents = getParents(result, D);
        int N = (L == -1) ? D : L;
        for (int i = 0; i < N; i++) {
            assertNotNull(i + "-th parent at depth " + D + " is null",
                parents[i]);
            assertEquals(getDirectoryName(D - i - 1),
                PCDirectory.reflect(parents[i], "_name"));
        }
        for (int i = N; i < D; i++)
            assertNull(i + "-th parent at depth " + D + " is not null " +
                parents[i], parents[i]);
    }

    /**
     * Gets an array of parents of the given directory. The zeroth element
     * is the parent of the given directory and (i+1)-th element is the
     * parent of the i-th element. Uses reflection to ensure that the
     * side-effect does not cause a database access for the field.
     *
     * @param dir a starting directory
     * @param depth depth to recurse. must be positive.
     * @return
     */
    PCDirectory[] getParents(PCDirectory dir, int depth) {
        PCDirectory[] result = new PCDirectory[depth];
        PCDirectory current = dir;
        for (int i = 0; i < depth; i++) {
            result[i] = (PCDirectory) PCDirectory.reflect(current, "_parent");
            current = result[i];
        }
        return result;
    }

    /**
     * Checks that the first L elements of the given array is non-null and
     * the rest are null.
     *
     * @param depth
     */
    void assertNullParent(PCDirectory[] parents, int L) {
        for (int i = 0; i < L; i++)
            assertNotNull(parents[i]);
        for (int i = L; i < parents.length; i++)
            assertNull(parents[i]);
    }

    String getDirectoryName(int depth) {
        return "d" + depth;
    }

    String getFileName(int depth) {
        return "f" + depth;
    }

    String quoted(String s) {
        return quote + s + quote;
    }

    /**
     * Defines a fetch plan that has several fetch groups to traverse a chain
     * of relationships.
     * After getting the root by an extent query, checks (by reflection) that
     * all the relations in the chain are fetched.
     * The fetch depth is kept infinite, so what would be fetched is essentially
     * controlled by the fetch groups.
     */
    public void testRelationTraversal() {
        OpenJPAEntityManager pm = getPM();
        FetchPlan plan = pm.getFetchPlan();
        pm.getFetchPlan().setMaxFetchDepth(-1);
        plan.addFetchGroup("employee.department");
        plan.addFetchGroup("department.company");
        plan.addFetchGroup("company.address");
        plan.addFetchGroup("address.country");

        Iterator employees = pm.createExtent(PCEmployee.class, true).iterator();
        while (employees.hasNext()) {
            PCEmployee emp = (PCEmployee) employees.next();

            PCDepartment dept = (PCDepartment) PCEmployee.reflect(emp,
                "department");
            assertNotNull(dept);

            PCCompany company = (PCCompany) PCDepartment.reflect(dept,
                "company");
            assertNotNull(company);

            PCAddress addr = (PCAddress) PCCompany.reflect(company, "address");
            assertNotNull(addr);

            PCCountry country = (PCCountry) PCAddress.reflect(addr, "country");
            assertNotNull(country);
        }
    }

    /**
     * Defines a fetch plan that has several fetch groups to traverse a chain
     * of relationships but truncated at the last relation.
     * After getting the root by an extent query, checks (by reflection) that
     * all but the last relation in the chain are fetched.
     * The fetch depth is kept infinite, so what would be fetched is essentially
     * controlled by the fetch groups.
     */
    public void testRelationTraversalTruncated() {
        OpenJPAEntityManager pm = getPM();
        FetchPlan plan = pm.getFetchPlan();
        pm.getFetchPlan().setMaxFetchDepth(-1);
        plan.addFetchGroup("employee.department");
        plan.addFetchGroup("department.company");
        plan.addFetchGroup("company.address");

        Iterator employees = pm.createExtent(PCEmployee.class, true).iterator();
        while (employees.hasNext()) {
            PCEmployee emp = (PCEmployee) employees.next();

            PCDepartment dept = (PCDepartment) PCEmployee.reflect(emp,
                "department");
            assertNotNull(dept);

            PCCompany company = (PCCompany) PCDepartment.reflect(dept,
                "company");
            assertNotNull(company);

            PCAddress addr = (PCAddress) PCCompany.reflect(company, "address");
            assertNotNull(addr);

            PCCountry country = (PCCountry) PCAddress.reflect(addr, "country");
            assertNull(country);
        }
    }

    /**
     * Gets a Compnay object by getObjectById() method as opposed to query.
     * The active fetch groups should bring in the multi-valued relationships.
     * The address->country relationship can be reached in two alternate
     * paths -- one as company->address->country and the other is
     * company->department->employee->address->country.
     * Though active fetch groups allow both the paths -- the max fetch depth
     * is set such that the shorter path is taken but not the longer one.
     * Hence the company's address->country should be loaded but not the
     * employee's.
     */
    public void testRelationTraversalWithCompanyAsRoot() {
        OpenJPAEntityManager pm = getPM();
        FetchPlan plan = pm.getFetchPlan();

        plan.setMaxFetchDepth(2);
        plan.addFetchGroup("company.departments");
        plan.addFetchGroup("company.address");
        plan.addFetchGroup("department.employees");
        plan.addFetchGroup("person.address");
        plan.addFetchGroup("address.country");

        PCCompany company =
            (PCCompany) pm.find(PCCompany.class, _rootCompanyId);
        Set departments = (Set) PCCompany.reflect(company, "departments");
        assertNotNull("department is null", departments);
        assertEquals("exp. depart size is not 2", 2, departments.size());
        PCDepartment dept = (PCDepartment) departments.iterator().next();
        assertNotNull("dept is null", dept);
        Set employees = (Set) PCDepartment.reflect(dept, "employees");
        assertNotNull("employees is null", employees);
        assertEquals(1, employees.size());
        PCEmployee emp = (PCEmployee) employees.iterator().next();
        assertNotNull("emp is not null", emp);
        PCAddress eaddr = (PCAddress) PCPerson.reflect(emp, "address");
        PCAddress caddr = (PCAddress) PCCompany.reflect(company, "address");
        assertNull("eaddr is not null", eaddr);
        assertNotNull("caddr is null", caddr);
        PCCountry country = (PCCountry) PCAddress.reflect(caddr, "country");
        assertNotNull("country is null", country);
    }

    /**
     * Same as above but the root compnay instance is detached.
     */
    public void testDetachedRelationTraversalWithCompanyAsRoot() {
        OpenJPAEntityManager pm = getPM();
        FetchPlan plan = pm.getFetchPlan();
        pm.getFetchPlan().setMaxFetchDepth(2);
        plan.addFetchGroup("company.departments");
        plan.addFetchGroup("company.address");
        plan.addFetchGroup("department.employees");
        plan.addFetchGroup("person.address");
        plan.addFetchGroup("address.country");

        PCCompany company1 =
            (PCCompany) pm.find(PCCompany.class, _rootCompanyId);

        PCCompany company = (PCCompany) pm.detachCopy(company1);
        assertTrue("company is equal company1", company != company1);
        Set departments = (Set) PCCompany.reflect(company, "departments");
        assertNotNull("department is null", departments);
        assertEquals("department size is not 2", 2, departments.size());
        PCDepartment dept = (PCDepartment) departments.iterator().next();
        assertNotNull("dept is null", dept);
        Set employees = (Set) PCDepartment.reflect(dept, "employees");
        assertNotNull("employee is null", employees);
        assertEquals("employees size not 1", 1, employees.size());
        PCEmployee emp = (PCEmployee) employees.iterator().next();
        assertNotNull("emp is null", emp);
        PCAddress eaddr = (PCAddress) PCPerson.reflect(emp, "address");
        PCAddress caddr = (PCAddress) PCCompany.reflect(company, "address");
        assertNull("eaddr is not null", eaddr);
        assertNotNull("caddr is null", caddr);
        PCCountry country = (PCCountry) PCAddress.reflect(caddr, "country");
        assertNotNull("country is null", country);
    }

    public void testDefaultFetchGroup() {
        OpenJPAEntityManager pm = getPM();

        String squery =
            "SELECT DISTINCT o FROM PCEmployee o WHERE o.name = 'Employee1'";
        OpenJPAQuery q = pm.createQuery(squery);

        //FIXME jthomas
        PCEmployee person = (PCEmployee) q.getSingleResult();
        assertEquals("Exp. String is not employee1", "Employee1",
            PCPerson.reflect(person, "name"));
    }

    public void testDefaultFetchGroupExistsByDefault() {
        OpenJPAEntityManager pm = getPM();
        assertTrue("pm does not contain default fetchplan",
            pm.getFetchPlan().getFetchGroups().contains(
                FetchPlan.GROUP_DEFAULT));
    }

    public void testDefaultFetchGroupCanBeRemoved() {
        OpenJPAEntityManager pm = getPM();
        assertTrue("does not contain default fetchplan",
            pm.getFetchPlan().getFetchGroups().contains(
                FetchPlan.GROUP_DEFAULT));

        pm.getFetchPlan().removeFetchGroup(FetchPlan.GROUP_DEFAULT);
        assertFalse("does contain default fetchplan",
            pm.getFetchPlan().getFetchGroups().contains(
                FetchPlan.GROUP_DEFAULT));

        OpenJPAEntityManager pm2 = getPM();
        assertTrue("pm2 does not contain default fetchplan",
            pm2.getFetchPlan().getFetchGroups().contains(
                FetchPlan.GROUP_DEFAULT));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    private void clearTestData() throws Exception {
//        OpenJPAEntityManagerFactory pmf =
//            (OpenJPAEntityManagerFactory) getEmf();
//        OpenJPAConfiguration conf=pmf.getConfiguration();
//        
//        Class.forName(pmf.getConfiguration().getConnection2DriverName());
//        String url=conf.getConnection2URL();
//        String user=conf.getConnection2UserName();
//        String pass=conf.getConnection2Password();
//        
//        Connection con = DriverManager.getConnection(
//                url,
//                user,
//                pass);
//        con.setAutoCommit(true);
//        con.prepareStatement("DELETE FROM PCDIRECTORY").executeUpdate();
//        con.prepareStatement("DELETE FROM PCFILE").executeUpdate();
//        con.prepareStatement("DELETE FROM PCPERSON").executeUpdate();
//        con.prepareStatement("DELETE FROM PCDEPARTMENT").executeUpdate();
//        con.prepareStatement("DELETE FROM PCCOMPANY").executeUpdate();
//        con.prepareStatement("DELETE FROM PCADDRESS").executeUpdate();
//        con.prepareStatement("DELETE FROM PCCOUNTRY").executeUpdate();

        deleteAll(PCDirectory.class);
        deleteAll(PCFile.class);
        deleteAll(PCPerson.class);
        deleteAll(PCDepartment.class);
        deleteAll(PCCompany.class);
        deleteAll(PCAddress.class);
        deleteAll(PCCountry.class);
        deleteAll(PCEmployee.class);
    }
}
