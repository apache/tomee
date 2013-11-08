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
@Table(name="LIBBOOK")
public class Book {
    private static final int WEEKS_TIME_MS = 1000 * 60 * 60 * 24 * 7;

    @Id
    @GeneratedValue
    private int oid;

    // persistent fields
    @Basic
    private String title;

    @Basic
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date dueDate;

    @ManyToOne
    private Borrower borrower;

    @ManyToMany(mappedBy = "books", cascade = CascadeType.PERSIST)
    private Set<Subject> subjects;

    protected Book() {
        // used only by OpenJPA
    }

    public Book(String title) {
        if (title != null)
            title = title.trim();

        if ((title == null) || (title.length() <= 0))
            throw new IllegalArgumentException("Title cannot be empty or null");

        this.title = title;
        subjects = new HashSet<Subject>();
    }

    public String getTitle() {
        return title;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    void setBorrower(Borrower borrower) {
        if (borrower != null) {
            this.borrower = borrower;
            // set the due date, one week from today
            long one_week_out = System.currentTimeMillis() + WEEKS_TIME_MS;
            dueDate = new Date(one_week_out);
        }
    }

    void clearBorrower() {
        borrower = null;
        dueDate = null;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public boolean addSubject(Subject subject) {
        boolean retv = false;

        if ((subject != null) && subject.addBook(this)) {
            retv = true;
            subjects.add(subject);
        }

        return retv;
    }

    public boolean removeSubject(Subject subject) {
        boolean retv = false;

        if ((subject != null) && subject.removeBook(this)) {
            retv = true;
            subjects.remove(subject);
        }

        return retv;
    }

    public List<Subject> getSubjects() {
        if (subjects == null)
            return null;
        return new ArrayList<Subject>(subjects);
    }

    public String toString() {
        return "book [" + oid + "] \"" + title + "\""
                + ((dueDate == null) ? "" : (" due back: " + dueDate));
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

        if (other instanceof Book) {
            Book ob = (Book) other;
            return oid == ob.oid;
        }

        return false;
    }
}
