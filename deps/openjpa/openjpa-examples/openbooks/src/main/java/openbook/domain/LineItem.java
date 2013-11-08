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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;

/**
 * An immutable persistent entity with complex primary key.
 * The primary key is combination of the primary identity of {@linkplain PurchaseOrder} and
 * an 1-based integer index.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
@Entity
@IdClass(LineItem.LineItemId.class)
public class LineItem implements Serializable {
    /**
     * <A name="order">
     * An example of a compound derived identity.
     */
    @Id
    @OneToOne
    private PurchaseOrder order;
    
    @Id
    @OrderColumn
    @Column(name="IDX") // index is keyword
    private int index;
    
    @ManyToOne(optional=false)
    private Book book;
    
    private int quantity;
    
    protected LineItem() {
        
    }
    
    /**
     * Constructed as a line item for the given PurchaseOrder for the given Book for the given quantity.
     * Package protected because only the PurchaseOrder can create its own LineItem.
     *  
     * @param order non-null PurchaseOrder
     * @param i the 1-based index of this line item in its parent PurchaseOrder 
     * @param book non-null Book of this line item
     * @param quantity no. of books must be greater than zero. 
     */
    LineItem(PurchaseOrder order, int i, Book book, int quantity) {
        if (order == null)
            throw new NullPointerException("Can not create LineItem for null PurchaseOrder");
        if (i < 1)
            throw new IllegalArgumentException("Can not create LineItem with index " + i + ". Must be > 0");
        if (book == null)
            throw new NullPointerException("Can not create LineItem for null Book");
        if (quantity < 1)
            throw new IllegalArgumentException("Can not create LineItem with quantity " + i + ". Must be > 0");
        
        this.order = order;
        this.index = i;
        this.book = book;
        this.quantity = quantity;
    }
    
    /**
     * Gets the Book for this line item.
     * @return non-null Book.
     */
    public Book getBook() {
        return book;
    }

    /**
     * Gets the quantity of the book for this line item.
     * @return a positive number.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the parent PurchaseOrder of this line item.
     * @return non-null PurchaseOrder.
     */
    public PurchaseOrder getOrder() {
        return order;
    }

    /**
     * Gets the 1-based index this line item in its parent PurchaseOrder.
     * @return index must be greater than zero.
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Separate identity class.
     * 
     * @author Pinaki Poddar
     *
     */
    public static class LineItemId implements Serializable {
        long order; 
        int index;
        
        @Override
        public boolean equals(Object other) {
            if (other == this)
                return true;
            if (other instanceof LineItemId) {
                LineItemId that = (LineItemId)other;
                return order == that.order && index == that.index;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return (int) (31 ^ order + index);
        }
    }
}
