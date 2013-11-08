/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.trader.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A non-modal, pop-up message box.
 * <br>
 * CSS Styles:
 * <LI>messageBox 
 * <LI>messageBox-content
 * <LI>messageBox-caption 
 * <LI>
 * The 
 * @author Pinaki Poddar
 *
 */
public class MessageBox extends PopupPanel {
    private static MessageBox _popup = new MessageBox();
    
    private final HTML header;
    private final Label message;
    private MessageBox() {
        super(false, true);
        setAnimationEnabled(true);
        
        DockPanel panel = new DockPanel();
        panel.setStyleName("messageBox");
        panel.setHorizontalAlignment(DockPanel.ALIGN_CENTER);

        header = new HTML();
        header.addStyleName("messageBox-caption");
        
        Button close = new Button("OK");
        close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        close.setEnabled(true);
        close.setFocus(true);
        
        message  = new Label();
        message.addStyleName("messageBox-content");
        
        panel.add(header, DockPanel.NORTH);
        panel.add(close, DockPanel.SOUTH);
        panel.add(message, DockPanel.CENTER);
        
        setWidget(panel);
    }
    
    public static void alert(String message) {
        alert("Alert", message);
    }
    
    public static void alert(String header, String message) {
        _popup.header.setText(message);
        _popup.message.setText(message);
         _popup.center();
    }
}
