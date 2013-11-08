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
import com.google.gwt.user.client.ui.Widget;

/**
 * Fun stuff to fade-in, fade-out a changing label.
 * 
 * @author Pinaki Poddar
 *
 */
public class FadeEffect extends Timer {
    static final String OPACITY = "opacity";
    final Element elem;
    final Widget html;
    double opacity = 0.0;
    double delta = 0.01;
    int sign = -1;

    FadeEffect(Widget html, boolean appear) {
        this.html = html;
        elem = html.getElement();
        sign = appear ? 1 : -1;
        opacity = appear ? 0.0 : 1.0;
    }

    @Override
    public void run() {
        DOM.setStyleAttribute(elem, OPACITY, "" + opacity);
        opacity = opacity + sign * delta;
        if (ended()) {
            cancel();
        }
    }
    
    boolean ended() {
        if (sign == -1) {
            return opacity <= 0.0;
        } else {
            return opacity >= 1.0;
        }
    }
}
