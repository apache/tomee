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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Fun stuff - a progress monitor.
 * CSS Styles
 * <LI>progressMonitor
 * <LI>progressMonitor-caption
 * @author Pinaki Poddar
 *
 */
public class ProgressMonitor extends Timer {
    static final String OPACITY = "opacity";
    private final String highOpacity = "1.0";
    private final String lowOpacity  = "0.2";
    private volatile int current = 0;
    private final FlexTable bar;
    private final HTML header;
    private final int N = 10;
    private final  PopupPanel popup;
    private final  static ProgressMonitor _instance = new ProgressMonitor();
    
    private ProgressMonitor() {
        popup = new PopupPanel();
        popup.addStyleName("progressMonitor");
        
        
        header = new HTML();
        bar = new FlexTable();
        
        bar.setCellSpacing(0);
        bar.setCellPadding(0);
        bar.setWidget(0, 0, header);
        header.addStyleName("progressMonitor-caption");
        for (int i = 0; i < N; i++) {
            Label box = new Label();
            box.setSize("30px", "20px");
            DOM.setStyleAttribute(box.getElement(), "backgroundColor", "black");
            DOM.setStyleAttribute(box.getElement(), OPACITY, lowOpacity);
            bar.setWidget(1, i, box);
        }
        bar.getFlexCellFormatter().setColSpan(0, 0, N);
        
        popup.add(bar);
    }

    public static void showProgress(String caption) {
        _instance.header.setText(caption);
        _instance.popup.center();
        _instance.scheduleRepeating(10);
        _instance.run();
    }
    
    public static void stop() {
        _instance.cancel();
        _instance.popup.hide();
    }
    
    @Override
    public void run() {
        Element elem = bar.getWidget(1, current).getElement();
        DOM.setStyleAttribute(elem, OPACITY, lowOpacity);
        current++;
        current = current%N;
        elem = bar.getWidget(1, current).getElement();
        DOM.setStyleAttribute(elem, OPACITY, highOpacity);
    }
}
