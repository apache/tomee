package org.superbiz.model;

import java.util.Objects;

import javax.mvc.binding.MvcBinding;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.FormParam;

@Embeddable
public class Address {

    @FormParam("country")
    @NotEmpty(message="can not be empty")
    @MvcBinding
    private String country;

    @FormParam("state")
    @NotEmpty(message="can not be empty")
    @MvcBinding
    private String state;

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address address = (Address) o;
        return Objects.equals(country, address.country) &&
                Objects.equals(state, address.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, state);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Address{");
        sb.append("country='").append(country).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append('}');
        return sb.toString();
    }
}