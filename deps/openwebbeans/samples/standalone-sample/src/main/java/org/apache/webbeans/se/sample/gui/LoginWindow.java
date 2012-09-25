/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.se.sample.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.webbeans.se.sample.Boot;
import org.apache.webbeans.se.sample.LoggerFactory;
import org.apache.webbeans.se.sample.Login;
import org.apache.webbeans.se.sample.bindings.FileLoginBinding;
import org.apache.webbeans.se.sample.bindings.JavaLogger;

@Named
public class LoginWindow extends JPanel
{
    private static final long serialVersionUID = 3330746610475053600L;

    @Inject @FileLoginBinding Login loginBean; 
    
    private JLabel lblUserName;
    
    private JLabel lblPassword;
    
    private JTextField txtUserName;
    
    private JPasswordField txtPassword;
    
    private JButton btnLogin;
    
    private JButton btnCancel;
    
    private Logger logger = null;
    
    @Inject
    public LoginWindow(@JavaLogger LoggerFactory loggerFactory)
    {
        logger = loggerFactory.getLogger(LoginWindow.class, Logger.class);
        
        setBorder(BorderFactory.createTitledBorder("Login Information"));
        initialize();
    }
    
    public void initialize()
    {
        GridLayout gridLayout = new GridLayout(3,2,3,5);
        setLayout(gridLayout);
        
        lblUserName = new JLabel("User Name : ");
        add(lblUserName);
        
        txtUserName = new JTextField(10);
        add(txtUserName);
        
        lblPassword = new JLabel("Password : ");
        add(lblPassword);
        
        txtPassword =  new JPasswordField(10);
        add(txtPassword);
        
        btnLogin = new JButton("Login");
        add(btnLogin);
        
        btnLogin.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e)
            {
                LoginWindow.this.logger.info("Starting to login with user name : " + txtUserName.getText().trim());
                
                char[] passwd = txtPassword.getPassword();
                boolean result = LoginWindow.this.loginBean.login(txtUserName.getText().trim(), passwd);
                Arrays.fill(passwd, '0');
                
                if(!result)
                {
                    JOptionPane.showMessageDialog(null, "Wrong password or user name, try again with 'gurkan:erdogdu','mark:struberg','david.blevins'");
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "This demo shows simple usage of OpenWebBeans in Java Swing client. OpenWebBeans, 2009");
                }
            }
            
        });
        
        btnCancel = new JButton("Cancel");
        
        btnCancel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Boot.getFrame().dispose();
            }
            
        });
        
        add(btnCancel);
    }
}
