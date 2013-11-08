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
import org.apache.openjpa.trader.client.ui.ErrorDialog;
import org.apache.openjpa.trader.client.ui.ScrollableTable;
import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Trade;
import org.apache.openjpa.trader.domain.Trader;
import org.apache.openjpa.trader.service.TradingService;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The GWT module for OpenTrader.
 * 
 * <br><b>Initialization</b>:
 * This module entry point acts as the Application Controller.
 * It initializes the widget components and lays them out. As it is often the case, some of 
 * the widgets require initialization data. 
 * Hence, this entry point establishes connection to remote {@link TradingService} via 
 * {@link TradingServiceAdapterAsync asynchronous}, client-side, proxy stub. The GWT
 * framework provides that stub when supplied with the original interface class.
 * <br>
 * <b>Operation</b>:
 * Once initialized, the widgets operate relatively independently throughout the rest of the 
 * operations. Each widget provides a view and invokes server functions (asynchronously via
 * RPC) to update the view. 
 * <br>
 * <b>Event Management</b>:
 * The widgets communicate to other widgets via this module entry point i.e.
 * if the server callback results from a view action requires to update another view, then the request 
 * is relayed through this entry point instead of one view calling the other directly.
 * The GWT framework does provide the core infrastructure for
 * DOM and user event propagation. This entry point reuses the same infrastructure with
 * the application-defined specialized {@link ServiceEvent <em>service events</em>} to represent 
 * the <em>business</em> functions such as a new Ask or Bid request is placed, or a Trade
 * committed etc.  
 * <br>
 * <b>Session State Management</b>: One of the core advantages of GWT framework (apart from ease
 * of development and out-of-the-box cross-browser compatibility), is that GWT allows the client to
 * maintain its own state, thereby opening up the possibility of state-less (or at least less
 * stateful and hence more scalable) servers. This client maintains its session state in the
 * {@link ScrollableTable#getModel() data models} of the core widgets. The only <em>common,
 * shared</em> state held by this Application Controller is the logged-in {@link Trader}.   
 * 
 * 
 * @author Pinaki Poddar
 *
 */
public class OpenTrader implements EntryPoint, UncaughtExceptionHandler {
    // Event management
    private HandlerManager  eventBus;
    
    // The main widget components
    private MarketDataPanel stockPanel;               // the market prices 
    private TradeOrderWindow orderPanel;              // Creates a Ask/Bid request
    private TradingWindow   tradePanel;               // Issues a trade order
    private ScrollableTable<Trade> soldTradePanel;    // displays committed trades
    private ScrollableTable<Trade> boughtTradePanel;  // displays committed trades
    private ServerLogPanel serverPanel;               // displays server logs
    
    
    // Server-State variables as Session Identifier
    private Trader trader;
    private String _serviceURI;

    // The handle to the remote service.
    private TradingServiceAdapterAsync tradingService;

    // Resource bundle for images etc.
    public static final OpenTradeImageBundle bundle = GWT.create(OpenTradeImageBundle.class);
    
    /**
     * ------------------------------------------------------------------------
     * The entry point for GWT module.
     * ------------------------------------------------------------------------
     */
    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(this);
        eventBus = new HandlerManager(this);
        new LoginDialog(this).center();
    }
       
    /**
     * Gets the handle to the remote service. The service handle is the asynchronous interface
     * but it is created by the GWT framework from the synchronous interface class literal. 
     */
    public TradingServiceAdapterAsync getService() {
        if (tradingService == null) {
            tradingService = GWT.create(TradingServiceAdapter.class);
        }
        return tradingService;
    }
    
    /**
     * Gets the name of the trader as the name of this session.
     */
    public String getName() {
        return trader == null ? "" : trader.getName();
    }
    
    /**
     * Gets the trader who is running this session.
     */
    public Trader getTrader() {
        return trader;
    }
    
    /**
     * Gets all the traded stocks traded by the service.
     * The stocks are maintained by the {@link ScrollableTable#getModel() data model} of
     * the Market Data Panel widget.
     */
    public List<Stock> getTradedStocks() {
        return stockPanel.getModel();
    } 
    
    String getServiceURI() {
    	return _serviceURI;
    }
    
    /**
     * Builds up the widgets once the login is complete i.e. the server has supplied
     * the initialization data.
     * 
     */
    void init(Trader trader, String uri, List<Stock> stocks) {
        this.trader = trader;
        _serviceURI = uri;
        
        Window.setTitle(trader.getName());

        int W = Window.getClientWidth();
        int H = 900;//Window.getClientHeight();
        
                                       int headerHeight = 05*H/100;
        int westWidth   = 25*W/100;    int westHeight   = 80*H/100;
        int centerWidth = 60*W/100;    int centerHeight = 80*H/100;
                                       int footerHeight = 02*H/100;
        Unit unit = Unit.PX;

        stockPanel       = new MarketDataPanel(this, westWidth-10, 40*westHeight/100);
        int N = stocks.size();
        for (int i = 0; i < N; i++) {
            stockPanel.insert(stocks.get(i));
        }
        
        soldTradePanel   = new TradeHistoryPanel(this, Ask.class, westWidth-10, 20*westHeight/100);
        boughtTradePanel = new TradeHistoryPanel(this, Bid.class, westWidth-10, 20*westHeight/100);
        serverPanel      = new ServerLogPanel(this,  centerWidth, 40*centerHeight/100);
        tradePanel       = new TradingWindow(this,   centerWidth, 40*centerHeight/100);
        orderPanel       = new TradeOrderWindow(this,centerWidth, 10*centerHeight/100);

        
        FlowPanel west = new FlowPanel();
        west.setSize((westWidth-10)+"px", westHeight+"px");
        west.add(decorate(stockPanel));
        west.add(new HTML("<p>"));
        west.add(decorate(soldTradePanel));
        west.add(new HTML("<p>"));
        west.add(decorate(boughtTradePanel));
        
        FlowPanel center = new FlowPanel();
        center.setSize(centerWidth+"px", centerHeight+"px");
        center.add(decorate(orderPanel));
        center.add(new HTML("<p>"));
        center.add(decorate(tradePanel));
        center.add(new HTML("<p>"));
        center.add(decorate(serverPanel));
        
        DockLayoutPanel main = new DockLayoutPanel(unit);
        
        main.addNorth(createHeader(), headerHeight);
        main.addSouth(createFooter(), footerHeight);
        main.addWest(west, westWidth);
        main.add(center);

        RootLayoutPanel.get().add(main);
        main.animate(500);
        setUpHelp();
        stockPanel.startStockWatcher();
        tradePanel.startTradableRefresher();
    }
    
    /**
     * Decorates an widget by wrapping in a rounded panel (that seems to be cool thing nowadays).
     */
    Widget decorate(Widget w) {
        RoundedPanel rp = new RoundedPanel(w,RoundedPanel.ALL, 2);
        rp.setBorderColor("#005B9A");
        return rp;
    }
    
    /**
     * Sets up a help page for each of the main widgets.
     * 
     * @see ScrollableTable#addHelp(String)
     */
    void setUpHelp() {
        stockPanel.addHelp("help/MarketData.html");
        tradePanel.addHelp("help/Trade.html");
        orderPanel.addHelp("help/TradeOrder.html");
        soldTradePanel.addHelp("help/CommittedTrade.html");
        boughtTradePanel.addHelp("help/CommittedTrade.html");
        serverPanel.addHelp("help/Logging.html");
    }
    
    /**
     * Creates a header panel. Uses the image resources for a logo and a banner text.
     */
    Widget createHeader() {
        HorizontalPanel panel = new HorizontalPanel();
        Image logo = new Image(bundle.logo());
        HTML banner = new HTML("OpenTrader");
        banner.setStylePrimaryName("header");
        panel.add(logo);
        panel.add(banner);
        return panel;
    }
    
    /**
     * Creates a footer panel. 
     */
    Widget createFooter() {
        HorizontalPanel panel = new HorizontalPanel();
        Label footer = new HTML("Built with OpenJPA/Slice and GWT");
        footer.setStylePrimaryName("footer");
        panel.add(footer);
        return panel;
    }
    
    /**
     * ---------------------------------------------------------------------------------
     * Error Handling
     * ---------------------------------------------------------------------------------
     */
    /**
     * Catches any uncaught exception and pops up a {@link ErrorDialog error dialog}.
     */
    public void onUncaughtException(Throwable t) {
        handleError(t);
    }
    
    /**
     * Pops up a modal {@link ErrorDialog error dialog} with the given error.
     */
    void handleError(Throwable t) {
        t.printStackTrace();
        ErrorDialog.showError(t);
    }
    
    /**
     * ---------------------------------------------------------------------------------
     * Service Event Handling
     * ---------------------------------------------------------------------------------
     */
    
    /**
     * Registers a event with its handler. This mediator pattern facilitates communication
     * between the component widgets without them being aware of each other.
     * <br>
     * GWT framework supports this patter out-of-the-box. This application reuses the
     * framework for a set of {@link ServiceEvent service events}. 
     */
    public <H extends EventHandler> void registerHandler(Type<H> eventType, H handler) {
        eventBus.addHandler(eventType, handler);
    }
    
    /**
     * Fires a event to the registered handlers.
     * 
     * @param event can be any GwtEvent but used here for specialized {@link ServiceEvent service events}. 
     */
    public void fireEvent(GwtEvent<? extends EventHandler> event) {
        eventBus.fireEvent(event);
    }
    
}
