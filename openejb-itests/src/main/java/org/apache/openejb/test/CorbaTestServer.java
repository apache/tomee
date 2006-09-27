package org.apache.openejb.test;

import java.util.Properties;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class CorbaTestServer implements TestServer {
        
    Properties props;

    public void init(Properties props){
        /* TO DO:
         * Perform some test to see if the OpenEJB CORBA Server
         * is started.  If not, display the followding message
         * and exit.
         */
//      log("OpenEJB Test Suite with the OpenEJB CORBA Server");
//      log("");
//      log("Before running the OpenEJB test suite on the ");
//      log("OpenEJB CORBA Server, the MapNamingContext");
//      log("and CORBA Server must each be started in ");
//      log("seperate processes.");
//      log("");
//      log("1) Execute corba_naming_server.sh or .bat in a process.");
//      log("2) Execute corba_server.sh or .bat in another process.");
//      log("");

        this.props = props;
    }
    
    public void log(String s){
        System.out.println("[NOTE] "+s);
    }
    public void destroy(){
    }
    
    public void start(){
    }

    public void stop(){
       
    }

    public Properties getContextEnvironment(){
        return (Properties)props.clone();
    }

}
