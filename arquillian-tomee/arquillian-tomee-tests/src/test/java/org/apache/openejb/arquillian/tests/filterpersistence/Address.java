package org.apache.openejb.arquillian.tests.filterpersistence;

import javax.persistence.Entity;

@Entity
public class Address {
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    private String street = "123 Lakeview St.", city = "Paradise", state = "ZZ", zip = "00000";

    public String toString() {
        return "Street: " + street + ", City: " + city + ", State: " + state + ", Zip: " + zip;
    }
}