package org.superbiz.cdi.stereotype;

@Mock
public class SuperCar implements Vehicle {
    @Override
    public String name() {
        return "the fatest";
    }
}
