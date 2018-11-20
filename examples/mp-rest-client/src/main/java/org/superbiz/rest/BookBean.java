package org.superbiz.rest;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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
