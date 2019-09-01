/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.applet;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

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
                    Calculator calc = (Calculator) ref;
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
