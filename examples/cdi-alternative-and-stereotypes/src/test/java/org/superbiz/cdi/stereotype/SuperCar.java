package org.superbiz.cdi.stereotype;

/**
 * @author rmannibucau
 */
@Mock
public class SuperCar implements Vehicle {
    @Override
    public String name() {
        return "the fatest";
    }
}
