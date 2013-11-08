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


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Version;

/**
 * A persistent entity to represent an author of one or more Book.
 * <br>
 * <b>Notes</b>: No setter for identity value.
 * <br>
 * <LI><b>Identity</b>:Generated value as identity.
 * <LI><b>Mapping</b>:Many-to-Many mapping to Books.
 * <LI><b>Version</b>: Yes.
 *  
 * @author Pinaki Poddar
 *
 */
@Entity
public class Author {
    @Id
    @GeneratedValue
    private long id;
    
    private String name;
    
    @ManyToMany(mappedBy="authors")
    private Set<Book> books;
    
    @Version
    private int version;
    
    public long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<Book> getBooks() {
        return books;
    }
    
    public void addBook(Book book) {
        if (books == null)
            books = new HashSet<Book>();
        books.add(book);
    }
    
    public int getVersion() {
        return version;
    }
}
