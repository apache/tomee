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

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import openbook.domain.Book;
import openbook.domain.Customer;
import openbook.domain.Inventory;
import openbook.domain.PurchaseOrder;
import openbook.domain.ShoppingCart;

/**
 * A simple service to select Books, purchase them and manage their inventory.
 * A service handle can be obtained from {@link ServiceFactory#getService(String)
 * Service Factory}. 
 * 
 * @author Pinaki Poddar
 *
 */
public interface OpenBookService {
    public static final String DEFAULT_UNIT_NAME = "OpenBooks";
    
    /**
     * Starts a session for the given named Customer.
     * If no record of the given name exists, a new Customer record is created.
     * 
     * @param name name of a possibly existing customer. Or a new one.
     * 
     * @return a Customer
     */
    public Customer login(String name);
    
    /**
     * Selects a list of Books matching the given conditions.
     * Each of the conditional parameter can be null.
     * A null parameter implies that the resultant query should ignore that parameter.
     * 
     * @param title title of the Book
     * @param min minimum price
     * @param max maximum price
     * @param author name of author
     * @param decorators to modify the executable query such as its range.
     * @return
     */
    public List<Book> select(String title, 
            Double min, Double max, 
            String author, 
            QueryDecorator...decorators);
    /**
     * Gets the query String for the given parameters.
     * Each of the conditional parameter can be null.
     * A null parameter implies that the resultant query should ignore that parameter.
     * 
     * @param title title of the Book
     * @param min minimum price
     * @param max maximum price
     * @param author name of author
     * @param decorators to modify the executable query such as its range.
     * @return
     */
    public String getQuery(String title, 
            Double min, Double max, 
            String author);
    
    /**
     * Runs an arbitrary JPQL query to return a list of result.
     * 
     * @param jpql a valid JPQL query string.
     * @param resultClass type of the result.
     * @param decorators zero or more QueryDecorators to be applied before Query is executed.
     * @return the selected instances.
     */
    <T> List<T> query(String jpql, Class<T> resultClass, QueryDecorator...decorators);
    <T> List<T> getExtent(Class<T> entity);

    /**
     * Buys the content of the given cart.
     * 
     * @param cart a non-empty cart.
     * @return a PurchaseOrder for the content of the cart.
     */
    public PurchaseOrder placeOrder(ShoppingCart cart); 
    
    /**
     * Delivers the given order. Delivery changes the status of the order, decrements
     * inventory and finally removes the line items.
     *  
     * @param order a PENDING order to be delivered.
     * @return the PurchaseOrder after delivery.
     * 
     */
    public PurchaseOrder deliver(PurchaseOrder order);
    
    /**
     * Add inventory of the given Book by the given quantity.
     * 
     * @param b a Book whose inventory is to be incremented
     * @param quantity positive number.
     * 
     * @return the Book after incrementing its inventory.
     */
    public Book supply(Book b, int quantity);
    
    /**
     * Gets the list of orders of given status.
     * 
     * @param status status of the orders. null implies all orders.
     * 
     * @return list of orders sorted by their placement dates.
     */
    public List<PurchaseOrder> getOrders(PurchaseOrder.Status status, Customer customer);
    
    
    /**
     * Gets the list of Books whose inventory is lower than the given limit.
     * 
     * @param limit reorder limit. null implies all inventory.
     * 
     * @return list of Books with inventory lower than the given limit.
     */
    public List<Inventory> getReorderableBooks(int limit);
    
    /**
     * Count the number of instances of the given persistent type.
     * 
     * @param cls a persistent type.
     * @return number of persistent entity of the given type.
     */
    public long count(Class<?> cls);
    
    /**
     * Populates the underlying data repository with sample values, only if
     * the data repository is empty.
     * 
     *  @param loadParameters control the number of Books etc. to be created.
     *  null is allowed.
     *  
     * @return true if the repository is initialized by this invocation.
     */
    public boolean initialize(Map<String,Object> loadParameters);
    
    /**
     * Cleans everything. Be careful.
     */
    public void clean();
    
    
    /**
     * Gets the underlying persistence unit.
     * 
     * @return
     */
    public EntityManagerFactory getUnit();
    
    /**
     * Gets the name of the underlying persistence unit.
     * 
     * @return
     */
    public String getUnitName();
    
    /**
     * Affirms if the transaction on this persistence unit is managed by a container.
     * 
     * @return
     */
    public boolean isManaged();
    
    
}
