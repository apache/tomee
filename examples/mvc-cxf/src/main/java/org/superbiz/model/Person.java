package org.superbiz.model;

import java.util.Objects;

import javax.mvc.binding.MvcBinding;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.BeanParam;
import javax.ws.rs.FormParam;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @FormParam("id")
    private Long id;

    @FormParam("name")
    @NotEmpty(message="can not be empty")
    @Size(min = 1, max = 20)
    @MvcBinding
    private String name;

    @FormParam("age")
    @MvcBinding
    @Min(18)
    private int age;

    @BeanParam
    @Valid
    @Embedded
    private Address address;

    @FormParam("server")
    @NotNull
    @MvcBinding
    private String server;

    @FormParam("description")
    @Column
    @NotEmpty(message="can not be empty")
    @MvcBinding
    @Size(min = 10, max = 20)
    private String description;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Address getAddress() {
        return address;
    }

    public String getServer() {
        return server;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person person = (Person) o;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Person{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", age=").append(age);
        sb.append(", address=").append(address);
        sb.append(", server='").append(server).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}