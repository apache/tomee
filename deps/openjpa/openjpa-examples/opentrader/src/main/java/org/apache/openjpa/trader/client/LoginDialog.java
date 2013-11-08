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
package org.apache.openjpa.trader.client;

import java.util.List;

import org.apache.openjpa.trader.client.ui.MessageBox;
import org.apache.openjpa.trader.client.ui.ProgressMonitor;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Trader;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A dialog box for login a Trader. Once the trader's name is entered, this widget calls the server 
 * to start a session for the trader, gets all the tradable Stocks and passes that initialization data
 * to the {@link OpenTrader main application} to {@link OpenTrader#init(Trader, List) initialize}.
 * 
 * <br>
 * CSS styles used
 * <LI> login : for the main dialog box
 * <LI> login-caption: for the caption
 * 
 * 
 * @author Pinaki Poddar
 *
 */
public class LoginDialog extends PopupPanel {
    private Trader trader;
    private String serverURI;
    private final OpenTrader session;
    
    public LoginDialog(final OpenTrader session) {
        super(false, true);

        setAnimationEnabled(true);
        this.session = session;
        
        final FlexTable table = new FlexTable();
        final HTML header = new HTML("&nbsp;&nbsp;Welcome to OpenTrader&nbsp;&nbsp;");
        final Label label  = new Label("Please enter name:");
        DOM.setStyleAttribute(label.getElement(), "textAlign", "right");
        final TextBox traderName = new TextBox();
        traderName.setText("OpenTrader");
        final Button enter = new Button("Enter");

        addStyleName("login");
        table.addStyleName("login");
        label.addStyleName("login");
        header.addStyleName("login-caption");
                
        table.setWidget(0, 0, header);
        table.setWidget(2, 0, label);
        table.setWidget(2, 1, traderName);
        table.setWidget(4, 1, enter);
        table.getFlexCellFormatter().setColSpan(0, 0, 2);

        enter.setEnabled(true);
        traderName.setFocus(true);
        
        enter.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (traderName.getText().trim().length() == 0) {
                    MessageBox.alert("Trader's name must not be empty.");
                    return;
                }
                hide();
                ProgressMonitor.showProgress("Connecting to OpenTrader Server...");
                session.getService().login(traderName.getText(), new LoginCallback());
            }
        });
        setWidget(table);
    }
    
    /**
     * ---------------------------------------------------------------------------------
     * Asynchronous RPC service callbacks
     * ---------------------------------------------------------------------------------
     */

    /**
     * Logs in a [@link Trader} and then invokes another RPC service to get the list of tradable stocks.
     * This pattern of calling one RPC from another addresses sequential execution of asynchronous
     * callbacks. This pattern is necessary when the second RPC depends in some way to the result of
     * the first RPC. 
     * 
     */
    class LoginCallback implements AsyncCallback<Trader> {
        public void onFailure(Throwable caught) {
            ProgressMonitor.stop();
            session.handleError(caught);
        }
        
        public void onSuccess(Trader result) {
            trader = result;
            session.getService().getServiceURI(new GetServerURI());
        }
    }
    
    /**
     * Initializes the server URI.
     *
     */
    public class GetServerURI implements AsyncCallback<String> {
        public void onFailure(Throwable caught) {
            session.handleError(caught);
        }

        public void onSuccess(String uri) {
            serverURI = uri;
            session.getService().getStocks(new InitializeStocks());
        }
    }

    
    /**
     * Initializes the tradable stocks followed by the main application.
     *
     */
    public class InitializeStocks implements AsyncCallback<List<Stock>> {
        public void onFailure(Throwable caught) {
            session.handleError(caught);
        }

        public void onSuccess(List<Stock> stocks) {
            ProgressMonitor.stop();
            session.init(trader, serverURI, stocks);
        }
    }
}
