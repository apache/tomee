package org.apache.openejb.arquillian.tests.jaxws.openejbjar;

import javax.ejb.Stateless;
import javax.jws.WebService;

@WebService
@Stateless(name = "LengthCalculator")
public class LengthCalculator {
    public int length(String in) {
        return in.length();
    }
}
