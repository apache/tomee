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
package org.superbiz.bookstore.model;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class BooksBean {

    private Map<Integer, Book> store = new ConcurrentHashMap<>();

    public void addBook(Book book) {
        store.put(book.getId(), book);
    }

    public List<Book> getAll() {
        return new ArrayList<>(store.values());
    }

    public Book getBook(int id) {
        return store.get(id);
    }

    public void updateBook(Book book) {
        store.put(book.getId(), book);
    }

    public void deleteBook(int id) {
        store.remove(id);
    }
}
