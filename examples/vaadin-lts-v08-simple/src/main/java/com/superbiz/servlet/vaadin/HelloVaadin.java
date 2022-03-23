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
package com.superbiz.servlet.vaadin;

import jakarta.servlet.annotation.WebServlet;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


public class HelloVaadin {

    public static class MyUI extends UI {
        @Override
        protected void init(VaadinRequest request) {

            //create the components you want to use
            // and set the main component with setContent(..)
            final Layout layout = new VerticalLayout();
            layout
                .addComponent(new Button("click me",
                                         event -> layout.addComponents(new Label("clicked again"))
                ));

            //set the main Component
            setContent(layout);
        }

        @WebServlet("/*")
        @VaadinServletConfiguration(productionMode = false, ui = MyUI.class)
        public static class MyProjectServlet extends VaadinServlet { }
    }
}
