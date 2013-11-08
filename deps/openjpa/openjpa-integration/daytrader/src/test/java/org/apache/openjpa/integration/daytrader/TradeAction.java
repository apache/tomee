/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openjpa.integration.daytrader;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.math.BigDecimal;

import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.lib.log.Log;

/**
 * OpenJPA created TradeAction, which was adapted from TradeServletAction.
 * TradeServletAction provides servlet specific client side access to each of
 * the Trade brokerage user operations. These include login, logout, buy, sell,
 * getQuote, etc. TradeServletAction manages a web interface to Trade handling
 * HttpRequests/HttpResponse objects and forwarding results to the appropriate
 * JSP page for the web interface. TradeServletAction invokes
 * {@link TradeAction} methods to actually perform each trading operation.
 * 
 */
public class TradeAction extends TradeJPADirect {

    public TradeAction(Log log, EntityManagerFactory emf, boolean poolEm) {
        super(log, emf, poolEm);
    }

    /**
     * Display User Profile information such as address, email, etc. for the
     * given Trader Dispatch to the Trade Account JSP for display
     * 
     */
    void doAccount(StringBuilder sb, String userID, String results)
            throws RuntimeException, java.io.IOException {
        setAttribute(sb, "Page", "Account");
        try {
            AccountDataBean accountData = getAccountData(userID);
            AccountProfileDataBean accountProfileData = getAccountProfileData(userID);
            ArrayList orderDataBeans = (TradeConfig.getLongRun() ?
                new ArrayList() : (ArrayList)getOrders(userID));
            setAttribute(sb, "accountData", accountData);
            setAttribute(sb, "accountProfileData", accountProfileData);
            setAttribute(sb, "orderDataBeans", orderDataBeans);
            setAttribute(sb, "results", results);
        } catch (java.lang.IllegalArgumentException e) {
            // this is a user error so I will
            // forward them to another page rather than throw a 500
            setAttribute(sb, "results", results + "could not find account for userID = " + userID);
            // redirect to home page
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            setAttribute(sb, "Exception", e);
        } catch (Exception e) {
            // log the exception with error page
            sb.append("TradeServletAction.doAccount(...)" + " exception user =" + userID);
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.doAccount(...)" + " exception user =" + userID, e);
        }
    }

    /**
     * Update User Profile information such as address, email, etc. for the
     * given Trader Dispatch to the Trade Account JSP for display If any in put
     * is incorrect revert back to the account page w/ an appropriate message
     * 
     */
    void doAccountUpdate(StringBuilder sb, String userID, String password,
            String cpassword, String fullName, String address,
            String creditcard, String email)
            throws RuntimeException, java.io.IOException {
        String results = "";
        setAttribute(sb, "Page", "Account Update");
        // First verify input data
        boolean doUpdate = true;
        if (password.equals(cpassword) == false) {
            results = "Update profile error: passwords do not match";
            doUpdate = false;
        } else if (password.length() <= 0 || fullName.length() <= 0
                || address.length() <= 0 || creditcard.length() <= 0
                || email.length() <= 0) {
            results = "Update profile error: please fill in all profile information fields";
            doUpdate = false;
        }
        AccountProfileDataBean accountProfileData = new AccountProfileDataBean(
                userID, password, fullName, address, email, creditcard);
        try {
            if (doUpdate) {
                accountProfileData = updateAccountProfile(accountProfileData);
                results = "Account profile update successful";
            }
        } catch (java.lang.IllegalArgumentException e) {
            // this is a user error so I will
            // forward them to another page rather than throw a 500
            setAttribute(sb, "results",
                results + "invalid argument, check userID is correct, and the database is populated" + userID);
            setAttribute(sb, "Exception", e);
        } catch (Exception e) {
            // log the exception with error page
            setAttribute(sb, "results", "TradeServletAction.doAccountUpdate(...)" + " exception user =" + userID);
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.doAccountUpdate(...)" + " exception user =" + userID, e);
        } finally {
            sb.append(results);
        }
        doAccount(sb, userID, results);
    }

    /**
     * Buy a new holding of shares for the given trader Dispatch to the Trade
     * Portfolio JSP for display
     * 
     */
    void doBuy(StringBuilder sb, String userID, String symbol,
            String quantity) throws RuntimeException, IOException {
        String results = "";
        setAttribute(sb, "Page", "Buy");
        try {
            OrderDataBean orderData = buy(userID, symbol, new Double(
                    quantity).doubleValue(), TradeConfig.orderProcessingMode);
            setAttribute(sb, "orderData", orderData);
            setAttribute(sb, "results", results);
        } catch (java.lang.IllegalArgumentException e) {
            // this is a user error so I will
            // forward them to another page rather than throw a 500
            setAttribute(sb, "results", results + "illegal argument. userID=" + userID + ", symbol=" + symbol);
            setAttribute(sb, "Exception", e);
            // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.HOME_PAGE));
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
        } catch (Exception e) {
            // log the exception with error page
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.buy(...)"
                    + " exception buying stock " + symbol + " for user "
                    + userID, e);
        }
        // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.ORDER_PAGE));
    }

    /**
     * Create the Trade Home page with personalized information such as the
     * traders account balance Dispatch to the Trade Home JSP for display
     * 
     */
    void doHome(StringBuilder sb, String userID, String results)
            throws RuntimeException, java.io.IOException {
        BigDecimal balance;
        String result = "";
        setAttribute(sb, "Page", "Home");
        try {
            AccountDataBean accountData = getAccountData(userID);
            Collection holdingDataBeans = getHoldings(userID);

            // Edge Caching:
            // Getting the MarketSummary has been moved to the JSP
            // MarketSummary.jsp. This makes the MarketSummary a
            // standalone "fragment", and thus is a candidate for
            // Edge caching.
            MarketSummaryDataBean marketSummaryData = getMarketSummary();

            setAttribute(sb, "accountData", accountData);
            setAttribute(sb, "holdingDataBeans", holdingDataBeans);
            // See Edge Caching above
            setAttribute(sb, "marketSummaryData", marketSummaryData);
            setAttribute(sb, "results", results);
        } catch (java.lang.IllegalArgumentException e) {
            // this is a user error so I will
            // forward them to another page rather than throw a 500
            setAttribute(sb, "results", results + "check userID = " + userID
                    + " and that the database is populated");
            // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.HOME_PAGE));
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            setAttribute(sb, "Exception", e);
        } catch (RuntimeException e) {
            // this is a user error so I will
            // forward them to another page rather than throw a 500
            setAttribute(sb, "results", results + " Could not find account for + " + userID);
            // requestDispatch(ctx, req, resp, TradeConfig.getPage(TradeConfig.HOME_PAGE));
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            setAttribute(sb, "Exception", e);
        } catch (Exception e) {
            // log the exception with error page
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.doHome(...)" + " exception user =" + userID, e);
        }
        // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.HOME_PAGE));
    }

    /**
     * Login a Trade User. Dispatch to the Trade Home JSP for display
     * 
     */
    boolean doLogin(StringBuilder sb, String userID, String passwd)
            throws RuntimeException, java.io.IOException {
        String results = "";
        setAttribute(sb, "Page", "Login");
        try {
            // Got a valid userID and passwd, attempt login
            AccountDataBean accountData = login(userID, passwd);
            if (accountData != null) {
                // HttpSession session = req.getSession(true);
                setAttribute(sb, "uidBean", userID);
                setAttribute(sb, "sessionCreationDate", new java.util.Date());
                results = "Ready to Trade";
                doHome(sb, userID, results);
                return true;
            } else {
                setAttribute(sb, "results", results + " Could not find account for + " + userID);
                // log the exception with an error level of 3 which means,
                // handled exception but would invalidate a automation run
            }
        } catch (java.lang.IllegalArgumentException e) {
            // this is a user error so I will
            // forward them to another page rather than throw a 500
            setAttribute(sb, "results", results + "illegal argument.  userID=" + userID + ", passwd=" + passwd);
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            setAttribute(sb, "Exception", e);
        } catch (Exception e) {
            // log the exception with error page
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.doLogin(...)"
                    + "Exception logging in user " + userID + "with password"
                    + passwd, e);
        }
        // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.WELCOME_PAGE));
        return false;
    }

    /**
     * Logout a Trade User Dispatch to the Trade Welcome JSP for display
     * 
     */
    void doLogout(StringBuilder sb, String userID) throws RuntimeException,
            IOException {
        String results = "";
        setAttribute(sb, "Page", "Logout");
        try {
            logout(userID);
        } catch (java.lang.IllegalArgumentException e) {
            // this is a user error so I will
            // forward them to another page, at the end of the page.
            setAttribute(sb, "results", results + "illegal argument:" + e.getMessage());
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
            setAttribute(sb, "Exception", e);
        } catch (Exception e) {
            // log the exception and foward to a error page
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.doLogout(...)"
                    + "exception logging out user " + userID, e);
        }
        // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.WELCOME_PAGE));
    }

    /**
     * Retrieve the current portfolio of stock holdings for the given trader
     * Dispatch to the Trade Portfolio JSP for display
     * 
     */
    void doPortfolio(StringBuilder sb, String userID, String results)
            throws RuntimeException, IOException {
        setAttribute(sb, "Page", "Portfolio");
        try {
            // Get the holdiings for this user
            Collection quoteDataBeans = new ArrayList();
            Collection holdingDataBeans = getHoldings(userID);
            // Walk through the collection of user
            // holdings and creating a list of quotes
            if (holdingDataBeans.size() > 0) {
                Iterator it = holdingDataBeans.iterator();
                while (it.hasNext()) {
                    HoldingDataBean holdingData = (HoldingDataBean) it.next();
                    QuoteDataBean quoteData = getQuote(holdingData.getQuoteID());
                    quoteDataBeans.add(quoteData);
                }
            } else {
                results = results + ".  Your portfolio is empty.";
            }
            setAttribute(sb, "results", results);
            setAttribute(sb, "holdingDataBeans", holdingDataBeans);
            setAttribute(sb, "quoteDataBeans", quoteDataBeans);
            // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.PORTFOLIO_PAGE));
        } catch (java.lang.IllegalArgumentException e) {
            // this is a user error so I will
            // forward them to another page rather than throw a 500
            setAttribute(sb, "results", results + "illegal argument for userID=" + userID);
            setAttribute(sb, "Exception", e);
            // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.PORTFOLIO_PAGE));
            // log the exception with an error level of 3 which means, handled
            // exception but would invalidate a automation run
        } catch (Exception e) {
            // log the exception with error page
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.doPortfolio(...)"
                    + " exception user =" + userID, e);
        }
    }

    /**
     * Retrieve the current Quote for the given stock symbol Dispatch to the
     * Trade Quote JSP for display
     * 
     */
    void doQuotes(StringBuilder sb, String userID, String symbols)
            throws RuntimeException, IOException {
        String results = "";
        setAttribute(sb, "Page", "Quotes");
        // Edge Caching:
        // Getting Quotes has been moved to the JSP
        // Quote.jsp. This makes each Quote a
        // standalone "fragment", and thus is a candidate for
        // Edge caching.
        //			
        // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.QUOTE_PAGE));
        try {
            Collection<OrderDataBean> closedOrders = getClosedOrders(userID);
            // quote.jsp displays closed orders
            if (closedOrders.size() > 0) {
                setAttribute(sb, "closedOrders", closedOrders);
            } else {
                results = results + ".  You have no closed orders.";
            }
            // quote.jsp displays quotes for the given symbol(s)
            setAttribute(sb, "symbols", symbols);
            QuoteDataBean quote = getQuote(symbols);
            setAttribute(sb, "quote", quote);
        } catch (Exception e) {
            // log the exception with error page
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.doQuotes(...)"
                    + " exception user =" + userID + ", symbols=" + symbols, e);
        }
    }

    /**
     * Register a new trader given the provided user Profile information such as
     * address, email, etc. Dispatch to the Trade Home JSP for display
     * 
     */
    void doRegister(StringBuilder sb, String userID, String passwd,
            String cpasswd, String fullname, String ccn,
            String openBalanceString, String email, String address)
            throws RuntimeException, IOException {
        String results = "";
        setAttribute(sb, "Page", "Register");
        try {
            // Validate user passwords match and are at least 1 char in length
            if ((passwd.equals(cpasswd)) && (passwd.length() >= 1)) {
                AccountDataBean accountData = register(userID, passwd,
                        fullname, address, email, ccn, new BigDecimal(
                                openBalanceString));
                if (accountData == null) {
                    results = "Registration operation failed;";
                    setAttribute(sb, "results", results);
                    // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.REGISTER_PAGE));
                } else {
                    doLogin(sb, userID, passwd);
                    results = "Registration operation succeeded;  Account "
                            + accountData.getAccountID() + " has been created.";
                    setAttribute(sb, "results", results);
                }
            } else {
                // Password validation failed
                results = "Registration operation failed, your passwords did not match";
                System.out.println(results);
                setAttribute(sb, "results", results);
                // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.REGISTER_PAGE));
            }

        } catch (Exception e) {
            // log the exception with error page
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.doRegister(...)"
                    + " exception user =" + userID, e);
        }
    }

    /**
     * Sell a current holding of stock shares for the given trader. Dispatch to
     * the Trade Portfolio JSP for display
     * 
     */
    void doSell(StringBuilder sb, String userID, Integer holdingID)
            throws RuntimeException, IOException {
        String results = "";
        setAttribute(sb, "Page", "Sell");
        try {
            OrderDataBean orderData = sell(userID, holdingID, TradeConfig.orderProcessingMode);

            setAttribute(sb, "orderData", orderData);
            setAttribute(sb, "results", results);
        } catch (java.lang.IllegalArgumentException e) { // this is a user
                                                            // error so I will
            // just log the exception and then later on I will redisplay the
            // portfolio page
            // because this is just a user exception
            setAttribute(sb, "Exception", e);
        } catch (Exception e) {
            // log the exception with error page
            setAttribute(sb, "Exception", e);
            throw new RuntimeException("TradeServletAction.doSell(...)"
                    + " exception selling holding " + holdingID + " for user ="
                    + userID, e);
        }
        // requestDispatch(ctx, req, resp, userID, TradeConfig.getPage(TradeConfig.ORDER_PAGE));
    }

    void doWelcome(StringBuilder sb, String status) throws RuntimeException,
            IOException {
        setAttribute(sb, "Page", "Welcome");
        setAttribute(sb, "results", status);
        // requestDispatch(ctx, req, resp, null, TradeConfig.getPage(TradeConfig.WELCOME_PAGE));
    }

    /*
    private void requestDispatch(ServletContext ctx, HttpServletRequest req,
            HttpServletResponse resp, String userID, String page)
            throws RuntimeException, IOException {

        ctx.getRequestDispatcher(page).include(req, resp);
    }
    */

    /*
    private void sendRedirect(HttpServletResponse resp, String page)
            throws RuntimeException, IOException {
        resp.sendRedirect(resp.encodeRedirectURL(page));
    }
    */
    
    private void setAttribute(StringBuilder sb, String attribute, Object value) {
        if (log != null && log.isTraceEnabled()) {
            sb.append(attribute);
            sb.append(" = ");
            sb.append(value);
            sb.append(System.getProperty("line.separator"));
        }
    }
}