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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * A singleton Error Dialog to show the main error message and optional stack traces.
 * <br>
 * CSS Style names
 * <LI>errorDialog-caption:
 * <LI>errorDialog-message:
 * 
 * @author Pinaki Poddar
 *
 */
public class ErrorDialog extends PopupPanel {
    private FlexTable table;
    private HTML header;
    private Button close;
    private Tree tree;
    private static ErrorDialog _instance = new ErrorDialog();
    private static final String STYLE_CAPTION = "errorDialog-caption";
    private static final String STYLE_MESSAGE = "errorDialog-message";
    
    private ErrorDialog() {
        super(false, true);
        setAnimationEnabled(true);
        setGlassEnabled(true);
        setVisible(false);
        
        header = new HTML();
        header.addStyleName(STYLE_CAPTION);
        
        close = new Button("x");
        close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        tree = new Tree();
        ScrollPanel scroll = new ScrollPanel();
        scroll.setSize("600px", "200px");
        scroll.add(tree);
        DOM.setStyleAttribute(scroll.getElement(), "border", "1px");
        
        table = new FlexTable();
        table.setWidget(0, 0, header);
        table.setWidget(0, 1, close);
        table.setWidget(1, 0, scroll);
        table.getFlexCellFormatter().setColSpan(1, 0, 2);
        table.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
        setWidget(table);
   }
    
    public static void showError(Throwable t) {
        _instance.populate(t);
        _instance.center();
    }
    
    private void populate(Throwable t) {
        header.setHTML(t.getClass().getName());
        tree.clear();
        tree = addStackTrace(t);
    }
    
    private Tree addStackTrace(Throwable t) {
        TreeItem root = new TreeItem(t.getClass().getName());
        root.addItem(createMessageLabel(t));
        StackTraceElement[] traces = t.getStackTrace();
        for (int i = 0; i < traces.length; i++) {
            root.addItem(createStackTrace(traces[i]));
        }
        tree.addItem(root);
        Throwable cause = t.getCause();
        if (cause == null || cause == t) {
            return tree;
        }
        return addStackTrace(cause);
    }
    
    Label createMessageLabel(Throwable t) {
        HTML label = new HTML(t.getMessage());
        label.addStyleName(STYLE_MESSAGE);
        return label;
    }
    
    Label createStackTrace(StackTraceElement trace) {
        HTML label = new HTML(trace.toString());
        label.addStyleName(STYLE_MESSAGE);
        return label;
    }
}
