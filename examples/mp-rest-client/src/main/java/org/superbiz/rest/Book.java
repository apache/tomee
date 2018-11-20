package org.superbiz.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Book {

    private int id;
    private String name;

    public Book() {}

    public Book(int bookId, String name) {
        this.id = bookId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
