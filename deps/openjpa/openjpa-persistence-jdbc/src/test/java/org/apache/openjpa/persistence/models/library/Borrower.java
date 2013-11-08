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
package org.apache.openjpa.persistence.models.library;

import java.util.*;

import javax.persistence.*;

@Entity
@Table(name="LIBBORROWER")
public class Borrower {
    @Id
    @GeneratedValue
    private int oid;

    // persistent fields
    @Basic
    private String name;

    @OneToMany(mappedBy = "borrower", fetch = FetchType.EAGER)
    private Set<Book> books;

    @OneToOne(mappedBy = "borrower", cascade = { CascadeType.PERSIST,
            CascadeType.REMOVE })
    private Volunteer volunteer;

    protected Borrower() {
        // used only by OpenJPA
    }

    public Borrower(String name) {
        if (name != null)
            name = name.trim();

        if ((name == null) || (name.length() <= 0))
            throw new IllegalArgumentException("name cannot be empty or null");

        this.name = name;
        books = new HashSet<Book>();
    }

    public String getName() {
        return name;
    }

    public List<Book> getBooks() {
        if (books == null)
            return null;
        return new ArrayList<Book>(books);
    }

    public void borrowBook(Book book) {
        if (book == null)
            return;

        books.add(book);
        book.setBorrower(this);
    }

    public void returnBook(Book book) {
        if (book == null)
            return;

        books.remove(book);
        book.clearBorrower();
    }

    void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public String toString() {
        return "borrower [" + oid + "] " + name;
    }

    public int hashCode() {
        return oid;
    }

    /**
     * Uses the object's persistent identity value to determine equivalence.
     */
    public boolean equals(Object other) {
        // standard fare
        if (other == this)
            return true;

        // if the oid is 0, then this object is not persistent.
        // in that case, it cannot be considered equivalent to
        // other managed or unmanaged objects
        if (oid == 0)
            return false;

        if (other instanceof Borrower) {
            Borrower ob = (Borrower) other;
            return oid == ob.oid;
        }

        return false;
    }
}
