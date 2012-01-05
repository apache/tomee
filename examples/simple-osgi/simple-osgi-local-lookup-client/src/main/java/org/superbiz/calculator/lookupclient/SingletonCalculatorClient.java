package org.superbiz.calculator.lookupclient;

import org.superbiz.osgi.calculator.CalculatorLocal;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

@Singleton
@Startup
public class SingletonCalculatorClient {
    @EJB
    private CalculatorLocal calculator;

    @PostConstruct
    public void logInit() {
        System.out.println();
        checkCalculator();
        tryLookup();
        System.out.println();
    }

    private void tryLookup() {
        Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        try {
            Context ctx = new InitialContext(p);
            CalculatorLocal local = (CalculatorLocal) ctx.lookup("CalculatorBeanLocal");
            System.out.println("lookup OK: " + local.sayHello());
        } catch (NamingException e) {
            System.out.println("can't lookup bean: " + e.getMessage());
        }
    }

    private void checkCalculator() {
        if (calculator == null) {
            System.out.println(calculator + " is null -> FAILED!");
        } else {
            System.out.println("calculator OK: " + calculator.sayHello());
        }
    }
}
