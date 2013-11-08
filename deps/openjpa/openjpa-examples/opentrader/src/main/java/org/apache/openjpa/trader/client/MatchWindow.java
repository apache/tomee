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
import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Match;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.domain.Trade;
import org.apache.openjpa.trader.domain.Trader;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * A popup presents a list of matching Tradables and lets the user select one of them
 * to commit a trade.
 * 
 * @author Pinaki Poddar
 *
 */
public class MatchWindow extends PopupPanel {
    private final OpenTrader session;
    
    public MatchWindow(final OpenTrader session, final Tradable tradable, final List<Match> matches) {
        super(false, true);
        this.session = session;
        setAnimationEnabled(true);
        
        DockPanel panel = new DockPanel();
        panel.setHorizontalAlignment(DockPanel.ALIGN_CENTER);

        final HTML header = new HTML();
        final boolean ask = (tradable instanceof Ask);
        String txt = (matches.isEmpty() ? "No" : ""+matches.size()) + " matching "+ (ask ? "Bid" : "Ask") + " for " 
                   + toString(tradable) + "<br>";
        header.setHTML(txt);
        header.addStyleName("table-caption");
        
        Button close = new Button(matches.isEmpty() ? "OK" : "Cancel");
        close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        FlexTable body = new FlexTable();
        final RadioButton[] buttons = new RadioButton[matches.size()];
        if (!matches.isEmpty()) {
            for (int i = 0;  i < matches.size(); i++) {
                Match match = matches.get(i);
                Tradable t2 = ask ? match.getBid() : match.getAsk();
                Trader cpty = ask ? match.getBid().getBuyer() : match.getAsk().getSeller();
                buttons[i] = new RadioButton("matches");
                buttons[i].setValue(i == 0);
                body.setWidget(i, 0, buttons[i]);
                body.setWidget(i, 1, FormatUtil.formatPrice(t2.getPrice()));
                body.setWidget(i, 2, FormatUtil.formatVolume(t2.getVolume()));
                body.setText(i, 3, " by " + cpty.getName());
            }
            
            Button act = new Button(ask ? "Sell" : "Buy");
            act.setFocus(true);
            body.setWidget(matches.size()+1, 1, act);
            act.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    for (int i = 0; i < buttons.length; i++) {
                        if (buttons[i].getValue()) {
                            Match match = matches.get(i);
                            Tradable t = ask ? match.getAsk() : match.getBid();
                            session.getService().trade(match, new TradeCallback(t));
                            hide(true);
                        }
                    }
                }
            });
            body.setWidget(matches.size()+1, 2, close);
        } else {
            body.setWidget(0,0, new HTML("<p>Open a new browser page and login with a different Trader name<br>"
                    + "to create a matching " + (ask ? "Bid" : "Ask") + "<p>"));
            
            close.setFocus(true);
            body.setWidget(1, 0, close);
            body.getFlexCellFormatter().setAlignment(1,0, 
                    HasHorizontalAlignment.ALIGN_CENTER, 
                    HasVerticalAlignment.ALIGN_MIDDLE);
        }
        
        
        panel.add(header, DockPanel.NORTH);
        panel.add(body, DockPanel.CENTER);
        setWidget(panel);
    }
    
    String toString(Tradable t) {
        return "" + t.getVolume() + " of " + t.getStock().getSymbol() + " at price " 
        + FormatUtil.priceFormat.format(t.getPrice());
    }
    
    /**
     * ---------------------------------------------------------------------------------
     * Asynchronous RPC service callbacks
     * ---------------------------------------------------------------------------------
     */
    
    /**
     * Commits a Trade and notifies the listeners to {@link ServiceEvent.TradableRemoved
     * remove} the {@link Tradable tradable} and newly {@link ServiceEvent.TradeCommitted committed}
     * {@link Trade trade}.
     * <br>
     * This is an example of a callback that has a input state (the tradable entity). On completion
     * of the asynchronous RPC, this function will notify the listeners with the newly commited trade
     * which is the result of the callback invocation, as well as the input tradable entity.
     */
    public class TradeCallback implements AsyncCallback<Trade> {
        private final Tradable tradable;
        
        public TradeCallback(Tradable m) {
            tradable = m;
        }
        public void onFailure(Throwable caught) {
            session.handleError(caught);
        }

        public void onSuccess(Trade trade) {
            session.fireEvent(new ServiceEvent.TradableRemoved(tradable));                         
            session.fireEvent(new ServiceEvent.TradeCommitted(trade));
        }
    }
}
