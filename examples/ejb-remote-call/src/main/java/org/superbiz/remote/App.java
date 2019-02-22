package org.superbiz.remote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class App {

    public static void main(String[] args) throws NamingException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        properties.put(Context.PROVIDER_URL, "http://localhost:8080/tomee/ejb");

        Context ctx = new InitialContext(properties);
        Object ref = ctx.lookup("global/ejb_remote_call_war/Calculator!org.superbiz.remote.Calculator");

        Calculator calculator = Calculator.class.cast(ref);
        System.out.println(calculator.sum(1, 2));


    }
}
