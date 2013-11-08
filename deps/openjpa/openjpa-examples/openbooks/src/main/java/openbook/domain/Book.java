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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

/**
 * <A name="class"/>
 * An immutable persistent entity represents a Book.
 * <br>
 * The mutable properties of the book such as number of items in stock etc.
 * are factored out in a separate {@link Inventory} instance.
 * <br>
 * The state of inventory is mutable, but the relation to inventory is immutable. 
 * 
 * <LI><b>Identity</b>: Application-defined identity.
 * <LI><b>Mapping</b>: One-to-One bi-directional, immutable mapping to {@link Inventory}.
 * Many-to-Many bi-directional mapping to {@linkplain Author}.
 * <LI><b>Version</b>: No.
 * <p>
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
@Entity
public class Book implements Serializable {
    @Id
    private String ISBN;
    
    private String title;
    
    private double price;
    
    @OneToOne(mappedBy="book",
              fetch=FetchType.LAZY,
              cascade=CascadeType.ALL,
              optional=false,
              orphanRemoval=true)
    private Inventory inventory;
    
    /**
     * <A name="authors">
     * A many-to-many <em>eager</em> relation. 
     * By default, many-to-many relations are lazily fetched.
     */
    @ManyToMany(fetch=FetchType.EAGER)
    private List<Author> authors;
    
    /**
     * A no-arg constructor is required for JPA Specification.
     */
    public Book() {
    }
    
    /**
     * Construct a book with given parameters.
     * 
     * @param ISBN primary identity of this Book
     * @param title Title of the book.
     * @param price price of the book.
     * @param initialSupply initial inventory quantity.
     */
    public Book(String ISBN, String title, double price, int initialSupply) {
        this.ISBN = ISBN;
        this.title = title;
        this.price = price;
        inventory = new Inventory(this, initialSupply);
    }
    
    public String getISBN() {
        return ISBN;
    }
    
    public String getTitle() {
        return title;
    }
    
    public double getPrice() {
        return price;
    }
    
    public List<Author> getAuthors() {
        return authors;
    }
    
    public void addAuthor(Author...authors) {
        if (this.authors == null)
            this.authors = new ArrayList<Author>();
        for (Author a : authors) {
            if (!this.authors.contains(a))
                this.authors.add(a);
        }
    }
    
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    @Version
    private int version;
    
    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ISBN == null) ? 0 : ISBN.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Book other = (Book) obj;
        if (ISBN == null) {
            if (other.ISBN != null)
                return false;
        } else if (!ISBN.equals(other.ISBN))
            return false;
        return true;
    }
}
