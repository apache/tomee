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

import org.apache.openjpa.trader.client.event.ServiceEvent;
import org.apache.openjpa.trader.client.event.ServiceEventHandler;
import org.apache.openjpa.trader.client.ui.GridCellRenderer;
import org.apache.openjpa.trader.client.ui.ScrollableTable;
import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.domain.Trade;
import org.apache.openjpa.trader.domain.Trader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * One of the core component widgets to display the committed trades.
 * The panel is configured for either {@link Ask} or {@link Bid} and
 * accordingly it adjusts some of its labels or contents.
 * 
 * @author Pinaki Poddar
 *
 */
public class TradeHistoryPanel extends ScrollableTable<Trade> 
    implements ServiceEventHandler.AddTradeHandler {
    
    private final OpenTrader session;
    private final Class<? extends Tradable> _type;
    
    public TradeHistoryPanel(final OpenTrader session, Class<? extends Tradable> type, 
            final int w, final int h) {
        super("Stocks " + (type == Ask.class ? "sold" : "bought") + " by " + session.getName(), w,h, true);
        this.session = session;
        this._type = type;
        
        session.registerHandler(ServiceEvent.TradeCommitted.TYPE, this);
        
        setColumnHeader(0, "Stock",  "20%");
        setColumnHeader(1, "Price",  "20%");
        setColumnHeader(2, "Volume", "20%");
        setColumnHeader(3, type == Ask.class ? "Buyer" : "Seller",  "40%");
        
        // Stock symbol
        setRenderer(0, new GridCellRenderer<Trade>() {
            public Widget render(Trade model) {
                return new Label(model.getStock().getSymbol());
           }
        });
        
        // Price of the trade
        setRenderer(1, new GridCellRenderer<Trade>() {
            public Widget render(Trade t) {
                return FormatUtil.formatPrice(t.getPrice());
           }
        });
        
        // Volume of the trade
        setRenderer(2, new GridCellRenderer<Trade>() {
            public Widget render(Trade t) {
                return FormatUtil.formatVolume(t.getVolume());
           }
        });
        
        // Counter Party
        setRenderer(3, new GridCellRenderer<Trade>() {
            public Widget render(Trade t) {
                Trader cpty = session.getTrader().equals(t.getBuyer()) ? t.getSeller() : t.getBuyer(); 
                return new Label(cpty.getName());
           }
        });
    }

    /**
     * ---------------------------------------------------------------------------------
     * Service Event Response Management
     * ---------------------------------------------------------------------------------
     */
    
    /**
     * On receipt of the event determines if it is relevant for this instance.
     * Because an instance display either the Trades sold or bought.
     * If relevant then updates the display. 
     */
    public void onTradeCommitted(ServiceEvent.TradeCommitted event) {
        Trader trader = session.getTrader();
        Trade trade = event.getPayload();
        if ((trader.equals(trade.getSeller()) && _type == Ask.class) 
         || (trader.equals(trade.getBuyer())  && _type == Bid.class)) {   
            insert(trade);
        }
    }
}
