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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * An anchor that pops up a min-browser with some hopefully helpful text.
 *  
 * @author Pinaki Poddar
 *
 */
public class HelpLink extends Anchor implements ClickHandler {
    private final String url;
    private static HelpWindow window; 
   
    public HelpLink(String url) {
        super("Help", url, "Help");
        this.url = url;
        addClickHandler(this);
        if (window == null) {
            window = new HelpWindow();
        }
        addStyleName("help");
    }
    
    @Override
    public void onClick(ClickEvent event) {
        window.showHelp(url);
        event.preventDefault();
    }
    
    
    public class HelpWindow extends PopupPanel implements PopupPanel.PositionCallback {
        private final Frame frame;
        
        
        public HelpWindow() {
            super(true);
            setAnimationEnabled(true);
            setAutoHideEnabled(true);
            setModal(false);
            
            VerticalPanel panel = new VerticalPanel();
            
            frame = new Frame();
            frame.setPixelSize(400, 300);
            Button close = new Button("close");
            close.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    hide();
                }
            });
            panel.add(frame);
            panel.add(close);
            setWidget(panel);
            setVisible(true);
        }
        
        public void showHelp(String url) {
            frame.setUrl(url);
            super.setPopupPositionAndShow(this);
        }
        
        public void setPosition(int offsetWidth, int offsetHeight) {
               int left = Window.getClientWidth() - getWidget().getOffsetWidth() - 100;
               int top  = 40;
               setPopupPosition(left, top);
        }
    }
}
