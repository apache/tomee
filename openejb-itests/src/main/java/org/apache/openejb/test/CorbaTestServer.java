/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
