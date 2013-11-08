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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

/**
 * A mutable persistent entity.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
@Entity
public class Inventory implements Serializable {
    @Id
    @OneToOne(fetch=FetchType.EAGER, optional=false)
    private Book book;
    
    private int supplied;
    
    private int sold;
    
    protected Inventory() {
        
    }
    /**
     * Construct with the given Book and initial inventory count.
     * Package protected because only a Book can create its own inventory.
     * 
     * @param book non-null Book.
     * @param initialSupply must be greater than zero.
     */
    Inventory(Book book, int initialSupply) {
        if (book == null)
            throw new NullPointerException("Can not create inventory for null Book");
        if (initialSupply < 1)
            throw new IllegalArgumentException("Can not create inventory " + initialSupply + " for " + book +
                    " Initial inventory must be greater than zero.");
        this.book = book;
        increment(initialSupply);
    }
    
    /**
     * Gets the Book that this inventory represents.
     * 
     * @return non-null Book.
     */
    public Book getBook() {
        return book;
    }
    
    /**
     * Gets the available quantity.
     * This is an <em>in-flight</em> value representing the difference between the quantity supplied and quantity sold
     * so far.
     */
    public int getInStock() {
        return supplied - sold;
    }
    
    /**
     * Gets the quantity supplied so far.
     * 
     * @return a monotonically increasing positive number.
     */
    public int getSupplied() {
        return supplied;
    }
    
    /**
     * Gets the quantity sold so far.
     * 
     * @return a monotonically increasing positive number.
     */
    public int getSold() {
        return sold;
    }
    
    /**
     * Increment this inventory by the given quantity.
     * 
     * @param supplied must be positive.
     */
    public void increment(int supplied) {
        if (supplied < 1)
            throw new IllegalArgumentException("Can not add " + supplied + " supplies to " + this + 
                    " Suuplied quanlity must be greater than zero.");
        this.supplied += supplied;
    }
    
    /**
     * Decrement this inventory by the given quantity.
     * 
     * @param sold must be positive.
     */
    public void decrement(int sold) {
        if (sold < 1)
            throw new IllegalArgumentException("can not sell " + sold + "quantity to " + this 
                    + "Sold quantity Must be greater than zero.");
        this.sold += sold;
    }
    
    @Version
    private int version;
    
    public int getVersion() {
        return version;
    }

}
