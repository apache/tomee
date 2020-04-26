/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.superbiz.rest;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class BookBean {

    private HashMap<Integer,Book> bookStore;


    @PostConstruct
    public void bookBean() {
        bookStore = new HashMap();
    }

    public void addBook(Book newBook) {
        bookStore.put(newBook.getId(), newBook);
    }

    public void deleteBook(int id) {
        bookStore.remove(id);
    }

    public void updateBook(Book updatedBook) {
        bookStore.put(updatedBook.getId(),updatedBook);
    }

    public Book getBook(int id) {
        return bookStore.get(id);
    }

    public List getBooks() {
        Collection<Book> books = bookStore.values();
        return new ArrayList<Book>(books);

    }

}
