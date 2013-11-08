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
package org.apache.openjpa.persistence.embed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

@AllowFailure(message=
	"Multi-level embedding" + 
	"JPA 2.0 Access Style " + 
    "XML Metadata "         + 
    "Attribute Override "   +  
    " is not yet supported")
public class TestEmbeddableXml extends SingleEMFTestCase {
   
    public int numEmbeddables = 1;
    public int numBasicTypes = 10;
    public int ID = 1;
    public int deptId = 1;
    public int empId = 1;
    public int compId = 1;
    public int divId = 1;
    public int vpId = 1;
    public int newDivId = 100;
    public int newVpId = 100;
    public int numItems = 2;
    public int itemId = 1;
    public int cId = 1;
    public int oId = 1;

    public int numImagesPerItem = 3;
    public int numDepartments = 2;
    public int numEmployeesPerDept = 2;
    public int numCompany = 2;
    public int numDivisionsPerCo = 2;
    public int numCustomers = 1;
    public int numOrdersPerCustomer = 2;
    
    public void setUp() {
        setUp(CLEAR_TABLES);
    }
    
    @Override
    protected String getPersistenceUnitName() {
        return "embed-pu";
    }
    
    public void testJoinColumns() {
        createFeatureXml();
        EntityManager em = emf.createEntityManager();
        String jpql = "Select f from Feature f Join fetch f.attributes";
        Query q = em.createQuery(jpql);
        List<FeatureXml> fList = (List<FeatureXml>) q.getResultList();
        for (FeatureXml f : fList) {
            List<AttributeXml> aList = f.getAttributes();;
            assertEquals(1, aList.size());
        }
        em.close();
    }
    
    public void createFeatureXml() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        FeatureXml f = new FeatureXml();
        FeatureIdXml fid = new FeatureIdXml();
        fid.setIndex(1);
        fid.setOid("oid");
        f.setId(fid);
        AttributeXml a = new AttributeXml();
        a.setName("name1");
        a.setValue("value1");
        List<AttributeXml> aList = new ArrayList<AttributeXml>();
        aList.add(a);
        f.setAttributes(aList);
        em.persist(f);
        
        FeatureXml f1 = new FeatureXml();
        FeatureIdXml fid1 = new FeatureIdXml();
        fid1.setIndex(1);
        fid1.setOid("oid1");
        f1.setId(fid1);
        AttributeXml a1 = new AttributeXml();
        a1.setName("name1");
        a1.setValue("value1");
        List<AttributeXml> aList1 = new ArrayList<AttributeXml>();
        aList1.add(a1);
        f1.setAttributes(aList1);
        em.persist(f1);
        
        tran.commit();       
        em.close();
    }
    
    public void testEntityA_Coll_StringXml() {
        createEntityA_Coll_StringXml();
        queryEntityA_Coll_StringXml();
        findEntityA_Coll_StringXml();
    }

    public void testEntityA_Coll_Embed_Embed() {
        createEntityA_Coll_Embed_EmbedXml();
        queryEntityA_Coll_Embed_EmbedXml();
        findEntityA_Coll_Embed_EmbedXml();
    }

    /*
     * Create EntityA_Coll_StringXml
     */
    public void createEntityA_Coll_StringXml() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        createEntityA_Coll_StringXml(em, ID);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public void createEntityA_Coll_StringXml(EntityManager em, int id) {
        EntityA_Coll_StringXml a = new EntityA_Coll_StringXml();
        a.setId(id);
        a.setName("a" + id);
        a.setAge(id);
        for (int i = 0; i < numBasicTypes; i++)
            a.addNickName("nickName_" + id + i);
        em.persist(a);
    }

    /*
     * Create EntityA_Coll_Embed_EmbedXml
     */
    public void createEntityA_Coll_Embed_EmbedXml() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        createEntityA_Coll_Embed_EmbedXml(em, ID);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public void createEntityA_Coll_Embed_EmbedXml(EntityManager em, int id) {
        EntityA_Coll_Embed_EmbedXml a = new EntityA_Coll_Embed_EmbedXml();
        a.setId(id);
        a.setName("a" + id);
        a.setAge(id);
        for (int i = 0; i < numEmbeddables; i++) {
            Embed_EmbedXml embed = createEmbed_EmbedXml(em, id, i);
            a.addEmbed(embed);
        }
        em.persist(a);
    }

    public Embed_EmbedXml createEmbed_EmbedXml(EntityManager em, int id,
            int idx) {
        Embed_EmbedXml embed = new Embed_EmbedXml();
        embed.setIntVal1(id * 100 + idx * 10 + 1);
        embed.setIntVal2(id * 100 + idx * 10 + 2);
        embed.setIntVal3(id * 100 + idx * 10 + 3);
        EmbedXml embed1 = createEmbedXml(id, idx);
        embed.setEmbed(embed1);
        return embed;
    }

    public EmbedXml createEmbedXml(int id, int idx) {
        EmbedXml embed = new EmbedXml();
        embed.setIntVal1(id * 100 + idx * 10 + 4);
        embed.setIntVal2(id * 100 + idx * 10 + 5);
        embed.setIntVal3(id * 100 + idx * 10 + 6);
        return embed;
    }

    /*
     * Find EntityA_Coll_StringXml
     */
    public void findEntityA_Coll_StringXml() {
        EntityManager em = emf.createEntityManager();
        EntityA_Coll_StringXml a = em.find(EntityA_Coll_StringXml.class, ID);
        checkEntityA_Coll_StringXml(a);
        
        Query q = em.createNativeQuery("select count(*) from EntityA_Coll_StringXml_nickNames");
        Object obj = q.getSingleResult();
        // ensure that multiple rows are inserted into the table (the column is not serialized)
        assertEquals(numBasicTypes, obj);          
        
        
        
        em.close();
    }

    /*
     * Find EntityA_Coll_Embed_EmbedXml
     */
    public void findEntityA_Coll_Embed_EmbedXml() {
        EntityManager em = emf.createEntityManager();
        EntityA_Coll_Embed_EmbedXml a =
            em.find(EntityA_Coll_Embed_EmbedXml.class, ID);
        checkEntityA_Coll_Embed_EmbedXml(a);
        em.close();
    }

    /*
     * check EntityA_Coll_String
     */
    public void checkEntityA_Coll_StringXml(EntityA_Coll_StringXml a) {
        int id = a.getId();
        String name = a.getName();
        int age = a.getAge();
        assertEquals(1, id);
        assertEquals("a" + id ,name);
        assertEquals(1, age);
        Set<String> nickNames = a.getNickNames();
        for (String nickName : nickNames)
            assertEquals("nickName_" + id + "0", nickName);
    }

    /*
     * check EntityA_Coll_Embed_EmbedXml
     */
    public void checkEntityA_Coll_Embed_EmbedXml(
            EntityA_Coll_Embed_EmbedXml a) {
        int id = a.getId();
        String name = a.getName();
        int age = a.getAge();
        assertEquals(1, id);
        assertEquals("a" + id ,name);
        assertEquals(1, age);
        List<Embed_EmbedXml> embeds = a.getEmbeds();
        for (Embed_EmbedXml embed : embeds)
            checkEmbed_EmbedXml(embed);
    }

    public void checkEmbed_EmbedXml(Embed_EmbedXml embed) {
        int intVal1 = embed.getIntVal1();
        int intVal2 = embed.getIntVal2();
        int intVal3 = embed.getIntVal3();
        assertEquals(101, intVal1);
        assertEquals(102, intVal2);
        assertEquals(103, intVal3);
        EmbedXml embed1 = embed.getEmbed();
        checkEmbedXml(embed1);
    }

    public void checkEmbedXml(EmbedXml embed) {
        int intVal1 = embed.getIntVal1();
        int intVal2 = embed.getIntVal2();
        int intVal3 = embed.getIntVal3();
        assertEquals(104, intVal1);
        assertEquals(105, intVal2);
        assertEquals(106, intVal3);
    }

    /*
     * Query EntityA_Coll_StringXml
     */
    public void queryEntityA_Coll_StringXml() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select a from EntityA_Coll_StringXml a");
        List<EntityA_Coll_StringXml> as = q.getResultList();
        for (EntityA_Coll_StringXml a : as) {
            checkEntityA_Coll_StringXml(a);
        }
        tran.commit();
        em.close();
    }

    /*
     * Query EntityA_Coll_Embed_Embed
     */
    public void queryEntityA_Coll_Embed_EmbedXml() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select a from EntityA_Coll_Embed_EmbedXml a");
        List<EntityA_Coll_Embed_EmbedXml> as = q.getResultList();
        for (EntityA_Coll_Embed_EmbedXml a : as) {
            checkEntityA_Coll_Embed_EmbedXml(a);
        }
        tran.commit();
        em.close();
    }

     
    public void testMapKeyAnnotations(){
        createObj();
        queryObj();
        findObj();
    }

    public void createObj() {
        createDepartments();
        createCompanies();
        createItems();
    }

    public void findObj() {
        findDepartment();
        findCompany();
        findItem();
    }

    public void createDepartments() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numDepartments; i++)
            createDepartment(em, deptId++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public void createCompanies() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numCompany; i++)
            createCompany(em, compId++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public void createItems() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numItems; i++)
            createItem(em, itemId++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public void createItem(EntityManager em, int id) {
        ItemXml item = new ItemXml();
        item.setId(id);
        for (int i = 0; i < numImagesPerItem; i++) {
            item.addImage("image" + id + i, "file" + id + i);
        }
        em.persist(item);
    }

    public void createDepartment(EntityManager em, int id) {
        DepartmentXml d = new DepartmentXml();
        d.setDeptId(id);
        Map emps = new HashMap();
        for (int i = 0; i < numEmployeesPerDept; i++) {
            EmployeeXml e = createEmployee(em, empId++);
            d.addEmployee(e);
            emps.put(e.getEmpId(), e);
            e.setDepartment(d);
            em.persist(e);
        }
        em.persist(d);
    }

    public EmployeeXml createEmployee(EntityManager em, int id) {
        EmployeeXml e = new EmployeeXml();
        e.setEmpId(id);
        return e;
    }

    public void createCompany(EntityManager em, int id) {
        CompanyXml c = new CompanyXml();
        c.setId(id);
        for (int i = 0; i < numDivisionsPerCo; i++) {
            DivisionXml d = createDivision(em, divId++);
            VicePresidentXml vp = createVicePresident(em, vpId++);
            c.addToOrganization(d, vp);
            em.persist(d);
            em.persist(vp);
        }
        em.persist(c);
    }

    public DivisionXml createDivision(EntityManager em, int id) {
        DivisionXml d = new DivisionXml();
        d.setId(id);
        d.setName("d" + id);
        return d;
    }

    public VicePresidentXml createVicePresident(EntityManager em, int id) {
        VicePresidentXml vp = new VicePresidentXml();
        vp.setId(id);
        vp.setName("vp" + id);
        return vp;
    }

    public void findCompany() {
        EntityManager em = emf.createEntityManager();
        CompanyXml c = em.find(CompanyXml.class, 1);
        assertCompany(c);

        DivisionXml d = em.find(DivisionXml.class, 1);
        assertDivision(d);

        VicePresidentXml vp = em.find(VicePresidentXml.class, 1);
        assertVicePresident(vp);

        updateCompany(em, c);
        em.close();

        em = emf.createEntityManager();
        c = em.find(CompanyXml.class, 1);
        assertCompany(c);
        deleteCompany(em, c);
        em.close();
    }

    public void findDepartment() {
        EntityManager em = emf.createEntityManager();
        DepartmentXml d = em.find(DepartmentXml.class, 1);
        assertDepartment(d);

        EmployeeXml e = em.find(EmployeeXml.class, 1);
        assertEmployee(e);

        // updateObj by adding a new Employee
        updateDepartment(em, d);
        deleteDepartment(em, d);
        em.close();
    }

    public void findItem() {
        EntityManager em = emf.createEntityManager();
        ItemXml item = em.find(ItemXml.class, 1);
        assertItem(item);
        updateItem(em, item);
        deleteItem(em, item);
        em.close();
    }

    public void updateItem(EntityManager em, ItemXml item) {
        // remove an element
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        item.removeImage("image" + item.getId() + "0");
        em.persist(item);
        em.flush();
        tran.commit();

        // add an element
        String key = "image" + item.getId() + "new";
        tran.begin();
        item.addImage(key, "file" + item.getId() + "new");
        em.persist(item);
        em.flush();
        tran.commit();

        // modify an element
        tran.begin();
        String fileName = item.getImage(key);
        fileName = fileName + "newAgain";
        item.addImage(key, fileName);
        em.persist(item);
        em.flush();
        tran.commit();
    }

    public void deleteItem(EntityManager em, ItemXml item) {
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        em.remove(item);
        tran.commit();
    }

    public void updateCompany(EntityManager em, CompanyXml c) {
        EntityTransaction tran = em.getTransaction();
        // remove an element
        tran.begin();
        Map orgs = c.getOrganization();
        Set keys = orgs.keySet();
        for (Object key : keys) {
            DivisionXml d = (DivisionXml) key;
            c.removeFromOrganization(d);
            break;
        }
        em.persist(c);
        em.flush();
        tran.commit();

        // add an element
        tran.begin();
        DivisionXml d = createDivision(em, newDivId++);
        VicePresidentXml vp = createVicePresident(em, newVpId++);
        c.addToOrganization(d, vp);
        em.persist(d);
        em.persist(vp);
        em.persist(c);
        em.flush();
        tran.commit();

        // modify an element
        tran.begin();
        orgs = c.getOrganization();
        vp = c.getOrganization(d);
        vp.setName("newNameAgain");
        em.persist(c);
        em.persist(vp);
        em.flush();
        tran.commit();
    }

    public void deleteCompany(EntityManager em, CompanyXml c) {
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        em.remove(c);
        tran.commit();
    }

    public void updateDepartment(EntityManager em, DepartmentXml d) {
        EntityTransaction tran = em.getTransaction();

        // add an element
        tran.begin();
        EmployeeXml e =
            createEmployee(em, numDepartments * numEmployeesPerDept + 1);
        d.addEmployee(e);
        e.setDepartment(d);
        em.persist(d);
        em.persist(e);
        em.flush();
        tran.commit();

        // remove an element
        tran.begin();
        d.removeEmployee(e.getEmpId());
        e.setDepartment(null);
        em.persist(d);
        em.persist(e);
        em.flush();
        tran.commit();
    }

    public void deleteDepartment(EntityManager em, DepartmentXml d) {
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        em.remove(d);
        tran.commit();
    }

    public void queryObj() {
        queryDepartment();
        queryEmployee();
        queryCompany();
        queryDivision();
        queryVicePresident();
        queryItem();
    }

    public void queryDepartment() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select d from DepartmentXml d");
        List<DepartmentXml> ds = q.getResultList();
        for (DepartmentXml d : ds) {
            assertDepartment(d);
        }
        tran.commit();
        em.close();
    }

    public void queryEmployee() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select e from EmployeeXml e");
        List<EmployeeXml> es = q.getResultList();
        for (EmployeeXml e : es) {
            assertEmployee(e);
        }
        tran.commit();
        em.close();
    }

    public void queryCompany() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select c from CompanyXml c");
        List<CompanyXml> cs = q.getResultList();
        for (CompanyXml c : cs){
            assertCompany(c);
        }
        tran.commit();
        em.close();
    }

    public void queryDivision() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select d from DivisionXml d");
        List<DivisionXml> ds = q.getResultList();
        for (DivisionXml d : ds){
            assertDivision(d);
        }
        tran.commit();
        em.close();
    }

    public void queryVicePresident() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select vp from VicePresidentXml vp");
        List<VicePresidentXml> vps = q.getResultList();
        for (VicePresidentXml vp : vps){
            assertVicePresident(vp);
        }
        tran.commit();
        em.close();
    }

    public void queryItem() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        Query q = em.createQuery("select i from ItemXml i");
        List<ItemXml> is = q.getResultList();
        for (ItemXml item : is){
            assertItem(item);
        }
        tran.commit();
        em.close();
    }

    public void assertDepartment(DepartmentXml d) {
        int id = d.getDeptId();
        Map<Integer, EmployeeXml> es = d.getEmpMap();
        assertEquals(2, es.size());
        Set keys = es.keySet();
        for (Object obj : keys) {
            Integer empId = (Integer) obj;
            EmployeeXml e = es.get(empId);
            assertEquals(empId.intValue(), e.getEmpId());
        }
    }

    public void assertItem(ItemXml item) {
        int id = item.getId();
        Map images = item.getImages();
        assertEquals(numImagesPerItem, images.size());
    }

    public void assertEmployee(EmployeeXml e) {
        int id = e.getEmpId();
        DepartmentXml d = e.getDepartment();
        assertDepartment(d);
    }

    public void assertCompany(CompanyXml c) {
        int id = c.getId();
        Map organization = c.getOrganization();
        assertEquals(2,organization.size());
    }

    public void assertDivision(DivisionXml d) {
        int id = d.getId();
        String name = d.getName();
    }

    public void assertVicePresident(VicePresidentXml vp) {
        int id = vp.getId();
        String name = vp.getName();
    }
    
    public void createOrphanRemoval() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numCustomers; i++)
            createCustomer(em, cId++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public CustomerXml createCustomer(EntityManager em, int id) {
        CustomerXml c = new CustomerXml();
        c.setId(id);
        c.setName("name" + id);
        for (int i = 0; i < numOrdersPerCustomer; i++) {
            OrderXml order = createOrder(em, oId++);
            c.addOrder(order);
            order.setCust(c);
            em.persist(order);
        }
        em.persist(c);
        return c;
    }

    public OrderXml createOrder(EntityManager em, int id) {
        OrderXml o = new OrderXml();
        o.setId(id);
        em.persist(o);
        return o;
    }
    
    public void testOrphanRemovalTarget() {
        createOrphanRemoval();
        EntityManager em = emf.createEntityManager();
        int count = count(OrderXml.class);
        assertEquals(numOrdersPerCustomer * numCustomers, count);

        CustomerXml c = em.find(CustomerXml.class, 1);
        Set<OrderXml> orders = c.getOrders();
        assertEquals(numOrdersPerCustomer, orders.size());

        // OrphanRemoval: remove target: the order will be deleted from db
        for (OrderXml order : orders) {
            orders.remove(order);
            break;
        }
        em.getTransaction().begin();
        em.persist(c);
        em.flush();
        em.getTransaction().commit();
        em.clear();

        c = em.find(CustomerXml.class, 1);
        orders = c.getOrders();
        assertEquals(numOrdersPerCustomer - 1, orders.size());
        count = count(OrderXml.class);
        assertEquals(numOrdersPerCustomer * numCustomers - 1, count);
        em.close();
    }
    
    public void testOrphanRemovalTargetSetNull() {
        createOrphanRemoval();
        EntityManager em = emf.createEntityManager();
        CustomerXml c = em.find(CustomerXml.class, 1);
        c.setOrders(null);
        em.getTransaction().begin();
        em.persist(c);
        em.flush();
        em.getTransaction().commit();
        em.clear();

        int count = count(OrderXml.class);
        assertEquals(numOrdersPerCustomer * (numCustomers - 1), count);
        
        c = em.find(CustomerXml.class, 1);
        Set<OrderXml> orders = c.getOrders();
        if (orders != null)
            assertEquals(0, orders.size());
        em.close();
    }
    
    public void testOrphanRemovalSource() {
        createOrphanRemoval();
        EntityManager em = emf.createEntityManager();
        
        // OrphanRemoval: remove source
        CustomerXml c = em.find(CustomerXml.class, 1);
        em.getTransaction().begin();
        em.remove(c);
        em.flush();
        em.getTransaction().commit();
        em.clear();
        
        int count = count(OrderXml.class);
        assertEquals(numOrdersPerCustomer * (numCustomers - 1), count);
        
        em.close();
    }
}
