package org.apache.openejb.arquillian.tests.filterenventry;

public class Car {
    private final String make = "Lexus", model = "IS 350";
    private final int year = 2011;

    public String drive(String name) {
        return name + " is on the wheel of a " + year + " " + make + " " + model;
    }
}