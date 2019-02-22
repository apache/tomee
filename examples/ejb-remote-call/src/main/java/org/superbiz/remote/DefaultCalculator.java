package org.superbiz.remote;

import javax.ejb.Remote;
import javax.ejb.Stateless;

@Stateless(name = "Calculator", description = "Calculator", mappedName = "Calculator")
@Remote(Calculator.class)
public class DefaultCalculator implements Calculator {
    @Override
    public int sum(int add1, int add2) {
        return add1 + add2;
    }

    @Override
    public int multiply(int mul1, int mul2) {
        return mul1 * mul2;
    }


}
