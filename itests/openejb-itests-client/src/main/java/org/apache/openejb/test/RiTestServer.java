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

import java.io.DataInputStream;
import java.io.File;
import java.net.URL;
import java.util.Properties;

import javax.naming.Context;

/**
 * The Client test suite needs the following environment variables
 * to be set before it can be run.
 * 
 * <code>test.home</code>
 * <code>server.classpath</code>
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class RiTestServer implements TestServer {

    protected Process server;
    protected boolean startServerProcess;
    protected String configFile;
    protected String serverClassName = " org.apache.openejb.ri.server.Server ";
    protected String classPath;
    protected DataInputStream in;
    protected DataInputStream err;
    protected String testHomePath;
    protected File testHome;

    /**
     * The environment variable <code>test.home</code> sould be set 
     * to the base directory where the test suite is located.
     */
    public static final String TEST_HOME = "test.home";
    public static final String SERVER_CLASSPATH = "server.classpath";
    public static final String SERVER_CONFIG = "test.server.config";
    public static final String START_SERVER_PROCESS = "test.start.server.process";
    public static final String BAD_ENVIRONMENT_ERROR = "The following environment variables must be set before running the test suite:\n";
    
    
    static{
        System.setProperty("noBanner", "true");
    }
        
    public RiTestServer(){}

    public void init(Properties props){
        try{
            /* [DMB] Temporary fix  */
            try{
                System.setSecurityManager(new TestSecurityManager());
            } catch (Exception e){
                e.printStackTrace();
            }
            /* [DMB] Temporary fix  */
            
            String tmp = props.getProperty(START_SERVER_PROCESS, "true").trim();
            startServerProcess = "true".equalsIgnoreCase(tmp);
                        
            /* If we will not be starting process for the 
             * server than we don't need to read in the other
             * properties 
             */
            if (!startServerProcess) return;

            testHomePath = props.getProperty(TEST_HOME);
            classPath = props.getProperty(SERVER_CLASSPATH);
            configFile = props.getProperty(SERVER_CONFIG);

            checkEnvironment();
            
            testHome = new File(testHomePath);
            testHome  = testHome.getAbsoluteFile();
            testHomePath = testHome.getAbsolutePath();

            prepareServerClasspath();
        }catch (Exception e){
            e.printStackTrace ();
            System.exit(-1);
        }
    }

    public void destroy(){

    }

    /**
     * Starts and Ri Server with the configuration file from
     * the properties used to create this RiTestServer.
     * 
     * @param confFileName
     */
    public void start(){
        
        if (!startServerProcess) return;
        
        String command = "java -classpath "+classPath+" "+serverClassName +" "+configFile;
        try{
            server = Runtime.getRuntime().exec( command );
            in = new DataInputStream( server.getInputStream());
            err = new DataInputStream( server.getErrorStream());
            while(true){
                try{
                    String line = in.readLine();
                        System.out.println(line);
                    if (line == null || "[RI Server] Ready!".equals(line)) break;
                    
                } catch (Exception e){ break; }
            }

            Thread t = new Thread(new Runnable(){
                public void run(){
                    while(true){
                        try{
                            String line = in.readLine();
                            if ( line == null ) break;
                                System.out.println(line);
                        } catch (Exception e){ break; }
                    }
                
                }
            });
            t.start();
            Thread t2 = new Thread(new Runnable(){
                public void run(){
                    while(true){
                        try{
                            String line = err.readLine();
                            if ( line == null ) break;
//                                System.out.println(line);
                        } catch (Exception e){ break; }
                    }
                
                }
            });
            t2.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop(){
        if (!startServerProcess) return;
        
        if (server != null) server.destroy();
        server = null;
        try{
            in.close();
            err.close();
        } catch (Exception e){}
    }
    
    public Properties getContextEnvironment(){
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.ri.server.RiInitCtxFactory");

        try{
        properties.put(Context.PROVIDER_URL, new URL("http","127.0.0.1",1098,""));
        } catch (Exception e){}
        
        //properties.put(Context.SECURITY_PRINCIPAL, "STATEFUL_TEST_CLIENT");
        //properties.put(Context.SECURITY_CREDENTIALS, "STATEFUL_TEST_CLIENT");

        return properties;
    }

    //==========================================
    //  Methods supporting this implementation
    //  of the TestServer interface
    // 
    private String getConfFilePath(String confFileName){
        String str = getConfFile(confFileName).getAbsolutePath();
        return str;
    }

    private File getConfFile(String confFileName){
        return new File(testHome, confFileName);
    }
    
    private void checkEnvironment(){
        
        if ( testHomePath == null || classPath == null || configFile == null ) {
            String error = BAD_ENVIRONMENT_ERROR;
            error += ( testHomePath == null )? TEST_HOME       +"\n" : "";
            error += ( classPath    == null )? SERVER_CLASSPATH+"\n" : "";
            error += ( configFile   == null )? SERVER_CONFIG   +"\n" : "";
            throw new RuntimeException(error);
        }
    }

    private void prepareServerClasspath(){
        char PS = File.pathSeparatorChar;
        char FS = File.separatorChar;

        String javaTools = System.getProperty("java.home")+FS+"lib"+FS+"tools.jar";
        classPath = classPath.replace('/', FS);
        classPath = classPath.replace(':', PS);
        classPath+= PS + javaTools;
    }
    // 
    //  Methods supporting this implementation
    //  of the TestServer interface
    //==========================================

}
