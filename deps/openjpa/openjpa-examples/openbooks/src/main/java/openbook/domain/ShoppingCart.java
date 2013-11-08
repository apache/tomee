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
package openbook.domain;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <A name="non-persistent"/>
 * A non-persistent entity holds the content of a shopping session for a {@linkplain Customer}.
 * Used to create a persistent PurchaseOrder.
 * Books can be added or removed from this cart.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class ShoppingCart implements Serializable {
    /**
     * The owner of this cart.
     */
    private Customer customer;
    
    /**
     * <A name="items"/>
     * The items in the cart and their respective quantity.
     */
    private Map<Book, Integer> items;
    
    /**
     * Construct a cart for the given Customer.
     *  
     * @param c non-null Customer.
     */
    ShoppingCart(Customer c) {
        customer = c;
        items = new HashMap<Book, Integer>();
    }
    
    /**
     * Gets the Customer who owns this cart.
     * 
     * @return non-null Customer.
     */
    public Customer getCustomer() {
        return customer;
    }
    
    /**
     * Gets the books with their corresponding quantity in this cart.
     * 
     * @return
     */
    public Map<Book, Integer> getItems() {
        return items;
    }
    
    /**
     * Add the given book with the given quantity in the cart.
     * If the book already exists then the quantity is added to the existing quantity.
     * 
     * @param book non-null Book
     * @param quantity a positive quantity.  
     */
    public void addItem(Book book, int quantity) {
        if (book == null)
            throw new NullPointerException("Can not add null Book to " + this);
        if (quantity < 1)
            throw new IllegalArgumentException("Can not add " + quantity + " " + book + " to " + this + 
               " Added qunatity must be greater than zero");
        int current = items.containsKey(book) ? items.get(book) : 0;
        items.put(book, current + quantity);
    }
    
    /**
     * Change the quantity of the given book by the given delta.
     * 
     * @param book a non-null Book that must exist in this cart.
     * @param delta no. of quantity to change. Can be positive or negative.
     * If the resultant quantity becomes zero of negative, the book is removed from the cart.
     */
    public void changeQuantity(Book book, int delta) {
        if (book == null)
            throw new NullPointerException("Can not change quantity for null Book in " + this);
        if (!items.containsKey(book))
            throw new IllegalArgumentException("Can not change quantity for " + book + " becuase the book does not " +
                    "exist in " + this);
        int current = items.containsKey(book) ? items.get(book) : 0;
        if (current + delta <= 0) {
            items.remove(book);
        } else {
            items.put(book, current + delta);
        }
    }
    
    /**
     * Removes the given book from this cart.
     * 
     * @param book book a non-null Book that must exist in this cart.
     */
    public void remove(Book book) {
        if (book == null)
            throw new NullPointerException("Can not remove null Book from " + this);
        if (!items.containsKey(book))
            throw new IllegalArgumentException("Can not remove " + book + " becuase the book does not " +
                    "exist in " + this);
        items.remove(book);
    }
    
    public void clear() {
        items.clear();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public int getTotalCount() {
        int sum = 0;
        for (Integer q : items.values())
            sum += q.intValue();
        return sum;
    }

}
