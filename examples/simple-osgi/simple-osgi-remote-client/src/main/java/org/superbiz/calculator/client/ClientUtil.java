package org.superbiz.calculator.client;

import org.apache.openejb.client.RemoteInitialContextFactory;
import org.superbiz.osgi.calculator.CalculatorRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public final class ClientUtil {
    private ClientUtil() {
        // no-op
    }

    public static void invoke() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        properties.setProperty(Context.PROVIDER_URL, "ejbd://localhost:4201");
        Context remoteContext = new InitialContext(properties);
        CalculatorRemote calculator = (CalculatorRemote) remoteContext.lookup("CalculatorBeanRemote");
        System.out.println("Server answered: " + calculator.sayHello());
    }
}
