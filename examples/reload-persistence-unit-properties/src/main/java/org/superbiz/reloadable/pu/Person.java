package org.superbiz.reloadable.pu;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author rmannibucau
 */
@Entity
public class Person {
    @Id private long id;
    private String name;

    public Person() {
        // no-op
    }

    public Person(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
