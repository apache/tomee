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
import org.apache.openjpa.trader.client.event.ServiceEventHandler.UpdateStockHandler;
import org.apache.openjpa.trader.client.ui.GridCellRenderer;
import org.apache.openjpa.trader.client.ui.ScrollableTable;
import org.apache.openjpa.trader.domain.Stock;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays the current Stock prices and updates periodically.
 * 
 * 
 * @author Pinaki Poddar
 * 
 */
public class MarketDataPanel extends ScrollableTable<Stock> implements UpdateStockHandler {
    private final OpenTrader session;
    private Timer refreshTimer;
    private static int refreshInterval = 60*1000;
    
    public MarketDataPanel(final OpenTrader session, final int w, final int h) {
        super("Market Prices (Updated every " + refreshInterval/1000 + "s)", w, h, true);
        this.session = session;
        
        session.registerHandler(ServiceEvent.StockUpdated.TYPE, this);
        
        setColumnHeader(0, "Symbol", "25%");
        setColumnHeader(1, "Price",  "25%");
        setColumnHeader(2, "Change", "50%");
        
        // Stock Symbol
        setRenderer(0, new GridCellRenderer<Stock>() {
            public Widget render(Stock stock) {
                return new Label(stock.getSymbol());
            }
        });
        
        // Current Market Price
        setRenderer(1, new GridCellRenderer<Stock>() {
            public Widget render(Stock stock) {
                return FormatUtil.formatPrice(stock.getMarketPrice());
            }
        });
        
        // Percent Change since last update
        setRenderer(2, new GridCellRenderer<Stock>() {
            public Widget render(Stock stock) {
                return FormatUtil.formatChange(stock.getMarketPrice(), stock.getLastPrice(), true);
            }
        });
    }
    
    /**
     * Sets the interval to refresh the stock data from the server.
     * 
     * @param interval period in milliseconds.
     */
    public void setRefreshInterval(int interval) {
        refreshInterval = interval;
        setCaption("Market Prices (Updated every " + refreshInterval/1000 + "s)");
        if (refreshTimer != null)
            refreshTimer.scheduleRepeating(refreshInterval);
    }
    
    /**
     * Gets the interval (in milliseconds) to refresh  the stock data from the server.
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Starts a periodic update of the stocks from the server.
     */
    public void startStockWatcher() {
        if (refreshTimer != null)
            return;
        // Setup timer to refresh list automatically.
        refreshTimer = new Timer() {
            @Override
            public void run() {
                session.getService().getStocks(new UpdateStocks());
            }
        };
        refreshTimer.run();
        refreshTimer.scheduleRepeating(refreshInterval);
    }
    
    /**
     * Starts periodic update of the stocks from the server.
     */
    public void stopStockWatcher() {
        if (refreshTimer == null)
            return;
        refreshTimer.cancel();
        refreshTimer = null;
    }
    
    /**
     * ---------------------------------------------------------------------------------
     * Service Event Response Management
     * ---------------------------------------------------------------------------------
     */
    
    /**
     * Updates the stock data.
     */
    @Override
    public void onStockUpdated(ServiceEvent.StockUpdated event) {
        update(event.getPayload(), null);
    }


    /**
     * ---------------------------------------------------------------------------------
     * Asynchronous RPC service callbacks
     * ---------------------------------------------------------------------------------
     */

    /**
     * Periodically update the stocks and notifies the listeners via the 
     * {@link OpenTrader#fireEvent(com.google.gwt.event.shared.GwtEvent) mediator}.
     * In this case, one of the listeners is this widget itself. Still the
     * {@link ServiceEvent.StockUpdated service event} is propagated via the
     * mediator (so that others can listen as well).
     * 
     */
    public class UpdateStocks implements AsyncCallback<List<Stock>> {
        public void onFailure(Throwable caught) {
            session.handleError(caught);
        }

        public void onSuccess(List<Stock> result) {
            int n = result.size();
            for (int i = 0; i < n; i++) {
                session.fireEvent(new ServiceEvent.StockUpdated(result.get(i)));
            }
        }
    }
}
