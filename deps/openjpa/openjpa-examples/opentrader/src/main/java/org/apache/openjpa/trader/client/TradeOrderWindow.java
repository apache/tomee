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
import org.apache.openjpa.trader.client.ui.HelpLink;
import org.apache.openjpa.trader.client.ui.MessageBox;
import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.Stock;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * This Widget allows the user to enter the details of a trade order (an Ask or
 * Bid) and call the {@link TradingServiceAdapterAsync Trading Service} via asynchronous RPC 
 * callback to record the order.
 * <br>
 * The widget demonstrates the aspect where a displayed element can change either
 * because other elements within the same widget are changing or because some external
 * state is changing. For example, the gain/loss of a requested buy/sell offer can
 * change as the user enters a different price. It can also change if the market
 * price of the stock has changed externally. The former changes are handled by adding
 * event handlers to the widget elements (such as onKeyUp in a text box), the later
 * changes are notified by this widget registering to the {@link OpenTrader main application}.   
 * 
 * 
 * @author Pinaki Poddar
 * 
 */
class TradeOrderWindow extends FlexTable implements UpdateStockHandler {
    private final OpenTrader session;
    final ListBox symbols     = new ListBox(false);
    final Button ask          = new Button("Ask");
    final Button bid          = new Button("Bid");
    final TextBox marketPrice = new TextBox();
    final TextBox userPrice   = new TextBox();
    final TextBox userVolume  = new TextBox();
    final TextBox margin      = new TextBox();
    final TextBox gain        = new TextBox();

    public TradeOrderWindow(final OpenTrader session, int w, int h) {
        super();
        this.session = session;

        setPixelSize(w, h);
        setStyleName("TradeOrderWindow");

        session.registerHandler(ServiceEvent.StockUpdated.TYPE, this);
        
        marketPrice.setReadOnly(true);
        margin.setReadOnly(true);
        gain.setReadOnly(true);
        userPrice.setMaxLength(10);
        userVolume.setMaxLength(10);

        setCellPadding(-2);
        setCellSpacing(-1);
        setHTML(0, 0, "Stock");
        setHTML(0, 1, "Market");
        setHTML(0, 2, session.getName());
        setHTML(0, 3, "Margin");
        setHTML(0, 4, "Volume");
        setHTML(0, 5, "Gain/Loss");
        for (int i = 0; i < 5; i++) {
            getCellFormatter().addStyleName(0, i, "TradingWindow-Label");
        }

        setWidget(1, 0, symbols);
        setWidget(1, 1, marketPrice);
        setWidget(1, 2, userPrice);
        setWidget(1, 3, margin);
        setWidget(1, 4, userVolume);
        setWidget(1, 5, gain);

        setWidget(2, 2, ask);
        setWidget(2, 3, bid);

        DOM.setStyleAttribute(getRowFormatter().getElement(0), "height", "4px");
        userPrice.setFocus(true);
        userPrice.setTabIndex(1);
        userPrice.setTabIndex(2);
        ask.setTabIndex(3);
        bid.setTabIndex(4);

        userPrice.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                if (userPrice.getText().trim().length() == 0)
                    return;
                double price = 0.0;
                try {
                    price = Double.parseDouble(userPrice.getText());
                } catch (NumberFormatException e) {
                    MessageBox.alert(userPrice.getText() + " must be a number");
                    return;
                }
                double diff = calculateDiff(price, getSelectedStock().getMarketPrice());
                margin.setText(FormatUtil.priceFormat.format(diff));
                gain.setText(FormatUtil.changeFormat.format(diff * Integer.parseInt(userVolume.getText())));
            }
        });
        userVolume.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                if (userVolume.getText().trim().length() == 0)
                    return;
                int volume = 0;
                try {
                    volume = Integer.parseInt(userVolume.getText());
                } catch (NumberFormatException e) {
                    MessageBox.alert(userVolume.getText() + " must be a positive integer");
                    return;
                }
                if (volume <= 0) {
                    MessageBox.alert(userVolume.getText() + " must be a positive integer");
                    return;
                }
                double diff = Double.parseDouble(margin.getText());
                gain.setText(FormatUtil.changeFormat.format(diff * volume));
            }
        });
        
        List<Stock> stocks = session.getTradedStocks();
        int n = stocks.size();
        for (int i = 0; i < n; i++) {
            symbols.addItem(stocks.get(i).getSymbol());
        }
        symbols.setSelectedIndex(0);
        initialize(stocks.get(0), false);

        symbols.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                Stock stock = getSelectedStock();
                initialize(stock, false);
            }
        });

        ask.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent ce) {
                if (!validateData())
                    return;
                session.getService().ask(session.getTrader(), 
                        getSelectedStock(), 
                        Integer.parseInt(userVolume.getText()),
                        Double.parseDouble(userPrice.getText()), 
                        new AskCallback());
            }
        });

        bid.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent ce) {
                if (!validateData())
                    return;
                session.getService().bid(session.getTrader(), 
                        getSelectedStock(), 
                        Integer.parseInt(userVolume.getText()),
                        Double.parseDouble(userPrice.getText()), 
                        new BidCallback());
            }
        });

    }

    /**
     * Sets the content of the widgets based on the given stock. The widget
     * content depends on the current stock price as well as user entered
     * values. 
     * 
     * @param stock
     * @param retainUserValue
     */
    void initialize(Stock stock, boolean retainUserValue) {
        marketPrice.setText(FormatUtil.priceFormat.format(stock.getMarketPrice()));
        if (retainUserValue && userPrice.getText().length() > 0) {
            double diff = calculateDiff(Double.parseDouble(userPrice.getText()), stock.getMarketPrice());
            margin.setText(FormatUtil.priceFormat.format(diff));
            gain.setText(FormatUtil.priceFormat.format(diff * Integer.parseInt(userVolume.getText())));
        } else {
            userPrice.setText(FormatUtil.priceFormat.format(stock.getMarketPrice()));
            userVolume.setText(FormatUtil.volumeFormat.format(100));
            margin.setText(FormatUtil.priceFormat.format(0));
            gain.setText(FormatUtil.priceFormat.format(0));
        }
    }
    
    public void addHelp(final String url) {
        HelpLink help = new HelpLink(url);
        setWidget(0, 6, help);
        getCellFormatter().setAlignment(0, 6, 
                HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    Stock getSelectedStock() {
        return session.getTradedStocks().get(symbols.getSelectedIndex());
    }

    boolean validateData() {
        try {
            if (Double.parseDouble(userPrice.getText()) <= 0) {
                MessageBox.alert("Price [" + userPrice.getText() + "] must be positive number");
                return false;
            }
        } catch (NumberFormatException e) {
            MessageBox.alert("Price [" + userPrice.getText() + "] must be a positive number");
            return false;
        }
        try {
            if (Integer.parseInt(userVolume.getText()) <= 0) {
                MessageBox.alert("Volume [" + userVolume.getText() + "] must be a positive integer");
                return false;
            }
        } catch (NumberFormatException e) {
            MessageBox.alert("Volume [" + userVolume.getText() + "] must be a positive integer");
            return false;
        }
        return true;
    }

    double calculateDiff(double p1, double p2) {
        return truncate(Math.abs(p1-p2));
    }
    
    private static double truncate (double x){
        double fract;
        double whole;
        if ( x > 0 ){
          whole = Math.floor(x);
          fract = Math.floor( (x - whole) * 100) / 100;
        } else {
          whole = Math.ceil(x);
          fract = Math.ceil( (x - whole) * 100) / 100;
        }
        return whole + fract;
      }
    
    
    /**
     * ---------------------------------------------------------------------------------
     * Service Event Response Management
     * ---------------------------------------------------------------------------------
     */
    public void onStockUpdated(ServiceEvent.StockUpdated event) {
        Stock updated = event.getPayload();
        Stock current = getSelectedStock();
        if (updated.equals(current)) {
            initialize(updated, true);
        }
    }
    
    /**
     * ---------------------------------------------------------------------------------
     * Asynchronous RPC service callbacks
     * ---------------------------------------------------------------------------------
     */
    
    /**
     * Updates display once the offer to sell has been successfully placed.
     * 
     */
    class AskCallback implements AsyncCallback<Ask> {
        public void onSuccess(Ask result) {
            session.fireEvent(new ServiceEvent.TradableAdded(result));
        }
        
        public void onFailure(Throwable caught) {
            session.handleError(caught);
        }
    }

    /**
     * Updates display once the offer to buy has been successfully placed.
     * 
     */
    class BidCallback implements AsyncCallback<Bid> {
       public void onSuccess(Bid result) {
            session.fireEvent(new ServiceEvent.TradableAdded(result));
       }
       
       public void onFailure(Throwable caught) {
           session.handleError(caught);
       }

    }
}
