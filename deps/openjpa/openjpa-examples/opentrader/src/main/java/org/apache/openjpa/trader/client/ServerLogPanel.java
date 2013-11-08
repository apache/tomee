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

import org.apache.openjpa.trader.client.event.ServiceEvent;
import org.apache.openjpa.trader.client.event.ServiceEventHandler.AddTradableHandler;
import org.apache.openjpa.trader.client.event.ServiceEventHandler.AddTradeHandler;
import org.apache.openjpa.trader.client.event.ServiceEventHandler.RemoveTradableHandler;
import org.apache.openjpa.trader.client.event.ServiceEventHandler.UpdateStockHandler;
import org.apache.openjpa.trader.client.ui.GridCellRenderer;
import org.apache.openjpa.trader.client.ui.ScrollableTable;
import org.apache.openjpa.trader.domain.LogStatement;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * One of the component widgets to display the server logs.
 * The log messages are parsed to determine whether they are SQL statements and further
 * categorized as SELECT, INSERT, UPDATE or DELETE to add a decorative CSS style.
 * <br>
 * CSS styles are
 * <LI>sql-insert
 * <LI>sql-update
 * <LI>sql-select
 * <LI>sql-delete
 * 
 * @author Pinaki Poddar
 *
 */
public class ServerLogPanel extends ScrollableTable<LogStatement> 
    implements AddTradableHandler, RemoveTradableHandler, 
    AddTradeHandler, UpdateStockHandler {
    final OpenTrader session;
    public static final String[] MARKERS_AND_STYLES = {"SELECT", "INSERT", "UPDATE", "DELETE"}; 
    
    
    public ServerLogPanel(final OpenTrader session, final int w, final int h) {
        super("Server Log (" + session.getServiceURI() + ")", w,h, false);
        this.session = session;
        
        session.registerHandler(ServiceEvent.TradableAdded.TYPE, this);
        session.registerHandler(ServiceEvent.TradableRemoved.TYPE, this);
        session.registerHandler(ServiceEvent.TradeCommitted.TYPE, this);
        session.registerHandler(ServiceEvent.StockUpdated.TYPE, this);

        setColumnHeader(0, "Context", "10%");
        setColumnHeader(1, "Message", "90%");
        
        setRenderer(0, new GridCellRenderer<LogStatement>() {
            public Widget render(LogStatement log) {
                return new Label(log.getContext());
           }
        });
        setRenderer(1, new GridCellRenderer<LogStatement>() {
            public Widget render(LogStatement log) {
                return decorate(log.getMessage(), MARKERS_AND_STYLES);
           }
        });
    }
    
    HTML decorate(String s, String[] markersAndStyles) {
            HTML html = new HTML(s);
            String style = getStyle(s, MARKERS_AND_STYLES);
            if (style != null)
                html.addStyleName(style);
            return html;
    }
    
    static String getStyle(String s, String[] markersAndStyles) {
        String style = null;
        for (int i = 0; i < markersAndStyles.length; i++) {
            String marker = markersAndStyles[i];
            int n = marker.length();
            if (s.length() < n) {
                continue;
            }
            String preamble = s.substring(0,n);
            if (preamble.equalsIgnoreCase(marker)) {
                style = "sql-"+marker.toLowerCase();
                return style;
            }
        }
        return null;
    }
    
    private void log() {
        session.getService().getLog(new LoggingCallback());
    }

    /**
     * ---------------------------------------------------------------------------------
     * Service Event Response Management
     * 
     * This widget receives all service event update and logs the corresponding server
     * logs.
     * ---------------------------------------------------------------------------------
     */
    public void onTradableAdded(ServiceEvent.TradableAdded event) {
        log();
    }

    public void onTradableRemoved(ServiceEvent.TradableRemoved event) {
        log();
    }

    public void onTradeCommitted(ServiceEvent.TradeCommitted event) {
        log();
    }

    public void onStockUpdated(ServiceEvent.StockUpdated event) {
        log();
    }
    
    /**
     * ---------------------------------------------------------------------------------
     * Asynchronous RPC service callbacks
     * ---------------------------------------------------------------------------------
     */
    
    /**
     * Unlike other callbacks, this callback on completion does not broadcast the log messages.
     * Instead it simply inserts the message in its own tabular display.  
     */
    public class LoggingCallback implements AsyncCallback<List<LogStatement>> {
        public void onFailure(Throwable caught) {
            session.handleError(caught);
        }

        public void onSuccess(List<LogStatement> messages) {
            if (messages == null)
                return;
            int N = messages.size();
            for (int i = 0; i < N; i++) {
               insert(messages.get(i));
            }
        }
    }
}
