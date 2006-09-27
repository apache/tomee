package org.apache.openejb.test;

import java.util.Properties;

import javax.naming.InitialContext;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class IvmTestServer implements TestServer {

    private Properties properties;

    public void init(Properties props){
        
        properties = props;
        
        try{
            props.put("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory");
            Properties p = new Properties(props);
            p.put("openejb.loader", "embed");
            new InitialContext( p );
            
        //OpenEJB.init(properties);
        }catch(Exception oe){
            System.out.println("=========================");
            System.out.println(""+oe.getMessage());
            System.out.println("=========================");
            oe.printStackTrace();
            throw new RuntimeException("OpenEJB could not be initiated");
        }
    }

    public void destroy(){
    }

    public void start(){
    }

    public void stop(){

    }

    public Properties getContextEnvironment(){
        return (Properties)properties.clone();
    }

}
