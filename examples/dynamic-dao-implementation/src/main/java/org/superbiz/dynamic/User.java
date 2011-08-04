package org.superbiz.dynamic;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author rmannibucau
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "dynamic-ejb-impl-test.query", query = "SELECT u FROM User AS u WHERE u.name LIKE :name"),
    @NamedQuery(name = "dynamic-ejb-impl-test.all", query = "SELECT u FROM User AS u")
})
public class User {
    @Id @GeneratedValue private long id;
    private String name;
    private int age;

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
