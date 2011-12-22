package org.superbiz.calculator;

import org.superbiz.osgi.calculator.CalculatorRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public final class RemoteClientMain {
    private RemoteClientMain() {
        // no-op
    }

    public static void main(String[] args) throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        properties.setProperty(Context.PROVIDER_URL, "ejbd://localhost:4201");
        Context remoteContext = new InitialContext(properties);
        CalculatorRemote calculator = (CalculatorRemote) remoteContext.lookup("CalculatorBeanRemote");
        System.out.println("Server answered: " + calculator.sayHello());
    }
}
