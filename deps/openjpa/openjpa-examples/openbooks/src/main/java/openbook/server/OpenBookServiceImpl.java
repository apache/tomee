/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package openbook.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import openbook.domain.Author;
import openbook.domain.Author_;
import openbook.domain.Book;
import openbook.domain.Book_;
import openbook.domain.Customer;
import openbook.domain.Customer_;
import openbook.domain.Inventory;
import openbook.domain.Inventory_;
import openbook.domain.LineItem;
import openbook.domain.PurchaseOrder;
import openbook.domain.PurchaseOrder_;
import openbook.domain.Range;
import openbook.domain.ShoppingCart;
import openbook.util.PropertyHelper;
import openbook.util.Randomizer;

import org.apache.openjpa.persistence.criteria.OpenJPACriteriaBuilder;

/**
 * A demonstrative example of a transaction service with persistent entity using Java Persistence API.
 * <br>
 * This example service operates on a persistent domain model to browse {@linkplain Book books}, 
 * occasionally {@linkplain #placeOrder(ShoppingCart) placing} {@linkplain PurchaseOrder purchase orders}, 
 * while {@linkplain Inventory inventory} gets updated either by {@linkplain #deliver() delivery} or 
 * by {@linkplain #supply() supply}. 
 * <br>
 * The operational model as well as the persistent domain model is influenced by the fact that
 * a JPA based application can benefit from  
 * <LI>Mostly Immutable Persistent Data Model
 * <LI>Optimistic Transaction Model 
 * <br>for better scalability and throughput.
 * <br>  
 *  
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
class OpenBookServiceImpl extends PersistenceService implements OpenBookService {
    
    public static final int   CUSTOMER_COUNT     = 10;
    public static final int   BOOK_COUNT         = 100;
    public static final int   AUTHOR_COUNT       = 40;
    public static final int   AUTHOR_PER_BOOK    = 3;
    
    /**
     * Range of number of queries executed for a {@linkplain #shop() shopping} trip.
     */
    public static final Range<Double> PRICE_RANGE  = new Range<Double>(4.99, 120.99);
    public static final Range<Integer> STOCK_RANGE = new Range<Integer>(5, 50);
    public static final int REORDER_LEVEL          = 10;
    
    OpenBookServiceImpl(String unit, EntityManagerFactory emf, boolean managed,
            PersistenceContextType scope) {
        super(unit, emf, managed, scope);
    }
    
    /**
     * Initialize service by populating inventory of Books and Customers.
     * If the inventory exists, then returns immediately without creating any new inventory.
     * 
     * @return true if new inventory is created. false otherwise.
     */
    public boolean initialize(Map<String,Object> config) {
        if (isInitialized()) {
            return false;
        }
        EntityManager em = begin();
        if (config == null) {
            config = Collections.EMPTY_MAP;
        }
        int nCustomer  = PropertyHelper.getInteger(config, "openbook.Customer.Count",  CUSTOMER_COUNT);
        int nBook   = PropertyHelper.getInteger(config, "openbook.Book.Count",  BOOK_COUNT);
        int nAuthor = PropertyHelper.getInteger(config, "openbook.Author.Count",  AUTHOR_COUNT);
        int nAuthorPerBook = PropertyHelper.getInteger(config, "openbook.Book.Author.Count", AUTHOR_PER_BOOK);

        Double priceMax = PropertyHelper.getDouble(config, "openbook.Book.Price.Max",  PRICE_RANGE.getMaximum());
        Double priceMin = PropertyHelper.getDouble(config, "openbook.Book.Price.Min",  PRICE_RANGE.getMinimum());

        Integer stockMax = PropertyHelper.getInteger(config, "openbook.Inventory.Max", STOCK_RANGE.getMaximum());
        Integer stockMin = PropertyHelper.getInteger(config, "openbook.Inventory.Min", STOCK_RANGE.getMinimum());
        
        System.err.println("Creating " + nCustomer + " new Customer");
        for (int i = 1; i < nCustomer; i++) {
            Customer customer = new Customer();
            customer.setName("Customer-"+i);
            em.persist(customer);
        }

        List<Author> allAuthors = new ArrayList<Author>();
        System.err.println("Creating " + nAuthor + " new Authors");
        for (int i = 1; i <= nAuthor; i++) {
            Author author = new Author();
            author.setName("Author-"+i);
            allAuthors.add(author);
            em.persist(author);
        }
        System.err.println("Creating " + nBook + " new Books");
        System.err.println("Linking at most " + nAuthorPerBook + " Authors per Book");
        for (int i = 1; i <= nBook; i++) {
            Book book = new Book(Randomizer.randomString(4,2), 
                                 "Book-" + i, 
                                 Randomizer.random(priceMin, priceMax), 
                                 Randomizer.random(stockMin, stockMax));
            List<Author> authors = Randomizer.selectRandom(allAuthors, 
                    Math.max(1, Randomizer.random(nAuthorPerBook)));
            for (Author author : authors) {
                author.addBook(book);
                book.addAuthor(author);
            }
            em.persist(book);
        }
        
        
        commit();
        return true;
    }
    
    /**
     * Affirms whether the database is loaded with some records.
     */
    public boolean isInitialized() {
        return count(Book.class) > 0;
    }
    
    /**
     * <A name="login">
     * Provide a name to login a Customer.
     * If such a customer exists, return it. Otherwise creates a new one.
     * 
     * @param name name of an existing or a new Customer
     * 
     * @return a Customer
     */
    public Customer login(String name) {
        EntityManager em = begin();
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Customer customer = null;
        Root<Customer> root = q.from(Customer.class);
        ParameterExpression<String> pName = cb.parameter(String.class);
        q.where(cb.equal(root.get(Customer_.name), pName));
        List<Customer> customers = em.createQuery(q)
          .setParameter(pName, name)
          .getResultList();
        if (customers.isEmpty()) {
            Customer newCustomer = new Customer();
            newCustomer.setName(name);
            em.persist(newCustomer);
            customer = newCustomer;
        } else {
            customer = customers.get(0);
        }
        commit();
        return customer;
    }
    
    /**
     * Find books that match title and price range.
     * @param title
     * @param minPrice
     * @param maxPrice
     * @param author
     * @return
     */
    
    public List<Book> select(String title, Double minPrice, Double maxPrice, String author, 
            QueryDecorator...decorators) {
        CriteriaQuery<Book> q = buildQuery(title, minPrice, maxPrice, author);
        EntityManager em = begin();
        TypedQuery<Book> query = em.createQuery(q);
        List<Book> result = query.getResultList();
        commit();
        return result;
    }
    
    /**
     * <A name="getQuery">
     * Gets the string representation of a Criteria Query.
     * The string form of a Criteria Query is not specified in JPA specification.
     * But OpenJPA produces a readable form that is quite <em>similar</em> to 
     * equivalent JPQL.
     */
    public String getQuery(String title, Double minPrice, Double maxPrice, String author) {
        CriteriaQuery<Book> q = buildQuery(title, minPrice, maxPrice, author);
        return q.toString();
    }
   
    /**
     * <A name="buildQuery">
     * Creates a Query based on the values of the user input form. 
     * The user may or may not have filled a value for each form field
     * and accordingly the query will be different.<br>
     * This is typical of a form-based query. To account for all possible
     * combinations of field values to build a String-based JPQL can be
     * a daunting exercise. This method demonstrates how such dynamic,
     * conditional be alternatively developed using {@link CriteriaQuery}
     * introduced in JPA version 2.0.
     * <br>
     * 
     * @return a typed query
     */

    private CriteriaQuery<Book> buildQuery(String title, Double minPrice, Double maxPrice, String author) {
        // builder generates the Criteria Query as well as all the expressions
        CriteriaBuilder cb = getUnit().getCriteriaBuilder();
        // The query declares what type of result it will produce 
        CriteriaQuery<Book> q = cb.createQuery(Book.class);
        // Which type will be searched
        Root<Book> book = q.from(Book.class);
        // of course, the projection term must match the result type declared earlier
        q.select(book);
        
        // Builds the predicates conditionally for the filled-in input fields 
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (!isEmpty(title)) {
            Predicate matchTitle = cb.like(book.get(Book_.title), title);
            predicates.add(matchTitle);
        }
        if (!isEmpty(author)) {
            Predicate matchAuthor = cb.like(book.join(Book_.authors).get(Author_.name), "%"+author+"%");
            predicates.add(matchAuthor);
        }
        // for price fields, also the comparison operation changes based on whether
        // minimum or maximum price or both have been filled. 
        if (minPrice != null && maxPrice != null) {
            Predicate matchPrice = cb.between(book.get(Book_.price), minPrice, maxPrice);
            predicates.add(matchPrice);
        } else if (minPrice != null && maxPrice == null) {
            Predicate matchPrice = cb.ge(book.get(Book_.price), minPrice);
            predicates.add(matchPrice);
        } else if (minPrice == null && maxPrice != null) {
            Predicate matchPrice = cb.le(book.get(Book_.price), maxPrice);
            predicates.add(matchPrice);
        }
        // Sets the evaluation criteria     
        if (!predicates.isEmpty())
            q.where(predicates.toArray(new Predicate[predicates.size()]));
        
        return q;
    }
    
    boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty(); 
    }

    
    /**
     * <A name="deliver"/>
     * Delivers the given order, if it is pending.
     * Delivery of an order amounts to decrementing inventory for each line item
     * and eventually nullify the line items to demonstrate orphan delete feature.
     * <br>
     * The transactions may fail because of either insufficient inventory or
     * concurrent modification of the same inventory by {@link #supply(Book, int) the supplier}.
     */
    public PurchaseOrder deliver(PurchaseOrder o) {
        if (o.isDelivered())
            return o;
        EntityManager em = begin();
        o = em.merge(o);
        for (LineItem item : o.getItems()) {
            item.getBook().getInventory().decrement(item.getQuantity());
        }
        o.setDelivered();
        commit();
        return o;
    }
    
    public List<PurchaseOrder> getOrders(PurchaseOrder.Status status, Customer customer) {
        EntityManager em = begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PurchaseOrder> q = cb.createQuery(PurchaseOrder.class);
        Root<PurchaseOrder> order = q.from(PurchaseOrder.class);
        q.select(order);
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (status != null) {
            predicates.add(cb.equal(order.get(PurchaseOrder_.status), status));
        }
        if (customer != null) {
            predicates.add(cb.equal(order.get(PurchaseOrder_.customer), customer));
        }
        if (!predicates.isEmpty())
            q.where(predicates.toArray(new Predicate[predicates.size()]));
        q.orderBy(cb.desc(order.get(PurchaseOrder_.placedOn)));
        
        TypedQuery<PurchaseOrder> query = em.createQuery(q);
        List<PurchaseOrder> result = query.getResultList();
        commit();
        return result;
    }
    
    /**
     * <A name="placeOrder"/>
     * Creates a new {@linkplain PurchaseOrder} from the content of the given {@linkplain ShoppingCart}.
     * The content of the cart is cleared as a result.
     * <br>
     * The transaction is not expected to fail because the inventory is
     * not modified by placing an order.
     * 
     * @param cart a non-null Shopping cart.
     */
    public PurchaseOrder placeOrder(ShoppingCart cart) {
        EntityManager em = begin();
        PurchaseOrder order = new PurchaseOrder(cart);
        em.persist(order);
        commit();
        cart.clear();
        return order;
    }
    
    /**
     * Supply books that have low inventory.
     * <br>
     * Queries for books with low inventory and supply each book in separate
     * transaction. Some of the transactions may fail due to concurrent modification on
     * the {@linkplain Inventory} by the {@linkplain #deliver() delivery} process. 
     */
    public Book supply(Book b, int quantity) {
        EntityManager em = begin();
        b = em.merge(b);
        b.getInventory().increment(quantity);
        commit();
        return b;
    }
    
    public List<Inventory> getReorderableBooks(int limit) {
        EntityManager em = begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Inventory> q = cb.createQuery(Inventory.class);
        Root<Inventory> inv = q.from(Inventory.class);
        q.select(inv);
        Expression<Integer> inStock = cb.diff(
                inv.get(Inventory_.supplied), 
                inv.get(Inventory_.sold));
        q.orderBy(cb.asc(inStock));
        
        List<Inventory> result = em.createQuery(q)
                                   .setMaxResults(limit)
                                   .getResultList();
        commit();
        return result;
    }
    
    public long count(Class<?> cls) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> c = cb.createQuery(Long.class);
        Root<?> from = c.from(cls);
        c.select(cb.count(from));
        return em.createQuery(c).getSingleResult();
    }
    
    public List<Book> selectByExample(Book b, QueryDecorator...decorators) {
        return queryByTemplate(Book.class, b);
    }
    
    private <T> List<T> queryByTemplate(Class<T> type, T template) {
        EntityManager em = begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> c = cb.createQuery(type);
        c.where(((OpenJPACriteriaBuilder)cb).qbe(c.from(type), template));
        List<T> result = em.createQuery(c).getResultList();
        commit();
        return result;
    }
    
    public <T> List<T> getExtent(Class<T> entityClass) {
        EntityManager em = begin();
        CriteriaQuery<T> c = em.getCriteriaBuilder().createQuery(entityClass);
        c.from(entityClass);
        List<T> result =  em.createQuery(c).getResultList();
        commit();
        return result;
    }
    
    public <T> List<T> query(String jpql, Class<T> resultClass, QueryDecorator... decorators) {
        EntityManager em = begin();
        TypedQuery<T> query = em.createQuery(jpql, resultClass);
        if (decorators != null) {
            for (QueryDecorator decorator : decorators) {
                decorator.decorate(query);
            }
        }
        List<T> result =   query.getResultList();
        commit();
        return result;
    }
    
    public void clean() {
        EntityManager em = begin();
        Set<EntityType<?>> entities = em.getMetamodel().getEntities();
        for (EntityType<?> type : entities) {
            System.err.println("Deleting all instances of " + type.getName());
            em.createQuery("delete from " + type.getName() + " p").executeUpdate();
        }
        commit();
    }


}
