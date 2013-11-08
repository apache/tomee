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
import org.apache.openjpa.trader.client.event.ServiceEventHandler;
import org.apache.openjpa.trader.client.ui.GridCellRenderer;
import org.apache.openjpa.trader.client.ui.ScrollableTable;
import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.Match;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Tradable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Trading Window allows the user to buy/sell a {@link Tradable tradable} or withdraw it.
 * <br>
 * This widget demonstrates combination of both read-only and updatable visual elements 
 * as well as active widgets such as a button.
 * <br>
 * Both the user actions (such as when a tradable is withdrawn) or other events such
 * as a Stock price change changes the expected gain/loss of a tradable,  
 * <br>
 * The complexity arises from the fact that a displayed tradable may have been consumed
 * by a matching tradable in another session. A tradable undergoes a state transition when
 * it is traded. Thus the displayed tradable can be an inconsistent state than its original
 * state in the server. Though all the displayed tradables are periodically refreshed, the
 * latency still exists.  
 *   
 *   
 * @author Pinaki Poddar
 *
 */
public class TradingWindow extends ScrollableTable<Tradable> 
       implements ServiceEventHandler.AddTradableHandler,
                  ServiceEventHandler.RemoveTradableHandler, 
                  ServiceEventHandler.UpdateStockHandler {
    private final OpenTrader session;
    private Timer refreshTimer;
    private int refreshInterval = 15*1000;

    public TradingWindow(final OpenTrader session, final int w, final int h) {
        super("Trading Window for " + session.getTrader().getName(), w, h, true);
        this.session = session;

        session.registerHandler(ServiceEvent.TradableAdded.TYPE, this);
        session.registerHandler(ServiceEvent.TradableRemoved.TYPE, this);
        session.registerHandler(ServiceEvent.StockUpdated.TYPE, this);

        setColumnHeader(0, "Stock", "15%");
        setColumnHeader(1, "Market", "15%");
        setColumnHeader(2, "Price", "15%");
        setColumnHeader(3, "Volume", "15%");
        setColumnHeader(4, "Gain/Loss", "15%");
        setColumnHeader(5, "Buy/Sell", "15%");
        setColumnHeader(6, "Withdraw", "15%");

        setRenderer(0, new GridCellRenderer<Tradable>() {
            public Widget render(Tradable model) {
                return new Label(model.getStock().getSymbol());
            }
        });

        // Market Price as changing Label
        setRenderer(1, new GridCellRenderer<Tradable>() {
            public Widget render(Tradable model) {
                return FormatUtil.formatPrice(model.getStock().getMarketPrice());
            }
        });

        // Ask/Bid Price as Label
        setRenderer(2, new GridCellRenderer<Tradable>() {
            public Widget render(Tradable model) {
                return FormatUtil.formatPrice(model.getPrice());
            }
        });

        // Ask/Bid Volume as Label
        setRenderer(3, new GridCellRenderer<Tradable>() {
            public Widget render(Tradable model) {
                return FormatUtil.formatVolume(model.getVolume());
            }
        });

        // Gain or loss
        setRenderer(4, new GridCellRenderer<Tradable>() {
            public Widget render(Tradable t) {
                return FormatUtil.formatChange(t.getGain());
            }
        });

        // Buy/Sell Button
        setRenderer(5, new GridCellRenderer<Tradable>() {
            public Widget render(Tradable t) {
                String action = t instanceof Ask ? "Sell" : "Buy";
                Button button = new Button(action);
                button.addClickHandler(new MatchCallback(t));
                return button;
            }
        });

        // Withdraw button
        setRenderer(6, new GridCellRenderer<Tradable>() {
            public Widget render(Tradable t) {
                Button button = new Button("Withdraw");
                button.addClickHandler(new WithdrawCallback(t));
                return button;
            }
        });
    }

    /**
     * Starts to run a period update of the tradables from the server.
     */
    public void startTradableRefresher() {
        if (refreshTimer != null)
            return;
        // Setup timer to refresh list automatically.
        refreshTimer = new Timer() {
            @Override
            public void run() {
                int n = getRowCount();
                for (int i = 0; i < n; i++) {
                    Tradable t = get(i);
                    if (t != null) {
                        session.getService().refresh(t, new RefreshTradableCallback());
                    }
                }
            }
        };
        refreshTimer.run();
        refreshTimer.scheduleRepeating(refreshInterval);
    }

    public void stopTradableRefresher() {
        if (refreshTimer == null)
            return;
        refreshTimer.cancel();
        refreshTimer = null;
    }


    @Override
    public void onTradableAdded(ServiceEvent.TradableAdded event) {
        insert(event.getPayload());
    }

    @Override
    public void onTradableRemoved(ServiceEvent.TradableRemoved event) {
        remove(event.getPayload());
    }

    @Override
    public void onStockUpdated(ServiceEvent.StockUpdated event) {
        int n = getRowCount();
        Stock updatedStock = event.getPayload();
        for (int i = 0; i < n; i++) {
            Tradable t = get(i);
            if (updatedStock.equals(t.getStock())) {
                t.updateStock(updatedStock);
                update(t, new Integer[]{1,4});
            }
        }
    }

    class WithdrawCallback implements AsyncCallback<Tradable>, ClickHandler {
        private final Tradable tradable;

        WithdrawCallback(Tradable source) {
            tradable = source;
        }

        public void onFailure(Throwable caught) {
            session.handleError(caught);
        }

        public void onSuccess(Tradable result) {
            session.fireEvent(new ServiceEvent.TradableRemoved(result));
        }

        @Override
        public void onClick(ClickEvent event) {
            session.getService().withdraw(tradable, this);
        }
    }

    public class MatchCallback implements AsyncCallback<List<Match>>, ClickHandler {
        private final Tradable tradable;

        public MatchCallback(Tradable tradable) {
            super();
            this.tradable = tradable;
        }

        public void onFailure(Throwable caught) {
            session.handleError(caught);
        }

        public void onSuccess(List<Match> result) {
            new MatchWindow(session, tradable, result).center();
        }

        @Override
        public void onClick(ClickEvent event) {
            if (tradable instanceof Ask) {
                session.getService().matchAsk((Ask) tradable, this);
            } else {
                session.getService().matchBid((Bid) tradable, this);
            }
        }
    }

    public class RefreshTradableCallback implements AsyncCallback<Tradable> {

        @Override
        public void onSuccess(Tradable result) {
            if (result.isTraded()) {
                remove(result);
                session.fireEvent(new ServiceEvent.TradeCommitted(result.getTrade()));
            }
        }
        
        @Override
        public void onFailure(Throwable t) {
            session.handleError(t);
        }
    }

}
