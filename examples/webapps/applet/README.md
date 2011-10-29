[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: Signed Applet EJB Client 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ applet ---
[INFO] Deleting /Users/dblevins/examples/webapps/applet/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ applet ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-dependency-plugin:2.1:copy (copy) @ applet ---
[INFO] Configured Artifact: org.apache.openejb:openejb-client:4.0.0-beta-1:jar
[INFO] Configured Artifact: org.apache.openejb:javaee-api:6.0-2:jar
[INFO] Copying openejb-client-4.0.0-beta-1.jar to /Users/dblevins/examples/webapps/applet/target/applet/openejb-client.jar
[INFO] Copying javaee-api-6.0-2.jar to /Users/dblevins/examples/webapps/applet/target/applet/javaee-api.jar
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ applet ---
[INFO] Compiling 3 source files to /Users/dblevins/examples/webapps/applet/target/classes
[INFO] 
[INFO] --- maven-antrun-plugin:1.5:run (default) @ applet ---
[WARNING] Parameter tasks is deprecated, use target instead
[INFO] Executing tasks

main:
      [jar] Building jar: /Users/dblevins/examples/webapps/applet/target/applet/app.jar
  [signjar] Signing JAR: /Users/dblevins/examples/webapps/applet/target/applet/app.jar to /Users/dblevins/examples/webapps/applet/target/applet/app.jar as mykey
  [signjar] jarsigner error: java.lang.RuntimeException: keystore load: /Users/dblevins/.keystore (No such file or directory)
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.765s
[INFO] Finished at: Fri Oct 28 17:03:48 PDT 2011
[INFO] Final Memory: 11M/81M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-antrun-plugin:1.5:run (default) on project applet: An Ant BuildException has occured: jarsigner returned: 1 -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.applet;
    
    import javax.ejb.Remote;
    
    @Remote
    public interface Calculator {
        public double add(double x, double y);
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.applet;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.naming.NamingException;
    import javax.rmi.PortableRemoteObject;
    import javax.swing.*;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.util.Properties;
    
    public class CalculatorApplet extends JApplet {
        JTextArea area;
    
        JTextField field1;
        JTextField field2;
        JLabel label1;
        JLabel label2;
        JButton button;
        JLabel label3;
        Context ctx;
    
        public void init() {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        createUI();
                    }
                });
            } catch (Exception e) {
                System.err.println("createGUI didn't successfully complete");
            }
    
        }
    
        private void createUI() {
            field1 = new JTextField();
            field2 = new JTextField();
            label1 = new JLabel("Enter first number");
            label2 = new JLabel("Enter second number");
            label3 = new JLabel("RESULT=");
            button = new JButton("Add");
    
            setLayout(new GridLayout(3, 2));
            add(label1);
            add(field1);
            add(label2);
            add(field2);
            add(button);
            add(label3);
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put(Context.PROVIDER_URL, "http://127.0.0.1:8080/applet/ejb");
            try {
                ctx = new InitialContext(props);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
            button.addActionListener(new ActionListener() {
    
                public void actionPerformed(ActionEvent e) {
    
                    try {
                        final Object ref = ctx.lookup("CalculatorImplRemote");
                        Calculator calc = (Calculator) PortableRemoteObject.narrow(
                                ref, Calculator.class);
                        String text1 = field1.getText();
                        String text2 = field2.getText();
                        int num1 = Integer.parseInt(text1);
                        int num2 = Integer.parseInt(text2);
                        double result = calc.add(num1, num2);
                        label3.setText("RESULT=" + result);
                    } catch (NamingException ex) {
                        throw new RuntimeException(ex);
                    }
    
                }
            });
    
        }
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.applet;
    
    import javax.ejb.Stateless;
    
    @Stateless
    public class CalculatorImpl implements Calculator {
    
        public double add(double x, double y) {
            return x + y;
        }
    
    }
    /**
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz;
    
    import org.junit.Assert;
    import org.junit.Test;
    import org.superbiz.applet.Calculator;
    
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.rmi.PortableRemoteObject;
    import java.util.Properties;
    
    
    public class JNDILookupTest {
    
        @Test
        public void test() {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put(Context.PROVIDER_URL, "http://127.0.0.1:8080/openejb/ejb");
            try {
                Context ctx = new InitialContext(props);
                System.out.println("Found context " + ctx);
                final Object ref = ctx.lookup("CalculatorImplRemote");
                Calculator calc = (Calculator) PortableRemoteObject.narrow(ref, Calculator.class);
                double result = calc.add(10, 30);
                Assert.assertEquals(40, result, 0.5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
