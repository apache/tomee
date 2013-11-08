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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.apache.openjpa.lib.log.Log;

// import org.apache.geronimo.samples.daytrader.beans.AccountDataBean;
// import org.apache.geronimo.samples.daytrader.beans.AccountProfileDataBean;
// import org.apache.geronimo.samples.daytrader.beans.HoldingDataBean;
// import org.apache.geronimo.samples.daytrader.beans.OrderDataBean;
// import org.apache.geronimo.samples.daytrader.beans.QuoteDataBean;
// import org.apache.geronimo.samples.daytrader.core.FinancialUtils;
// import org.apache.geronimo.samples.daytrader.core.MarketSummaryDataBean;
// import org.apache.geronimo.samples.daytrader.core.RunStatsDataBean;
// import org.apache.geronimo.samples.daytrader.core.TradeDBServices;
// import org.apache.geronimo.samples.daytrader.core.TradeServices;
// import org.apache.geronimo.samples.daytrader.util.Log;
// import org.apache.geronimo.samples.daytrader.util.TradeConfig;

/**
 * TradeJPADirect uses JPA to implement the business methods of the Trade online
 * broker application. These business methods represent the features and
 * operations that can be performed by customers of the brokerage such as login,
 * logout, get a stock quote, buy or sell a stock, etc. and are specified in the
 * {@link org.apache.geronimo.samples.daytrader.TradeServices} interface
 * 
 * Note: In order for this class to be thread-safe, a new TradeJPA must be
 * created for each call to a method from the TradeInterface interface.
 * Otherwise, pooled connections may not be released.
 * 
 * @see org.apache.geronimo.samples.daytrader.TradeServices
 * 
 */

// public class TradeJPADirect implements TradeServices, TradeDBServices {
public class TradeJPADirect {

    @PersistenceUnit(unitName="daytrader")
    private static EntityManagerFactory emf;

    private static BigDecimal ZERO = new BigDecimal(0.0);

    private static boolean initialized = false;

    protected static Log log = null;
    boolean _poolEm = true;
    
    EntityManager _em;
    
    public EntityManager getEm(EntityManagerFactory emf) {
        if (_poolEm) {
            if (_em == null)
                _em = emf.createEntityManager();
            return _em;
        }
        return emf.createEntityManager();
    }
    
    public void putEm(EntityManager em){
        if(_poolEm)
            em.clear();
        else{
            if(em.isOpen())
                em.close();
        }
    }
    

    // constructor for OpenJPA junit tests
    public TradeJPADirect(Log log, EntityManagerFactory emf, boolean poolEm) {
        this.log = log;
        this.emf = emf;
        _poolEm = poolEm;
        if (initialized == false)
            init();
    }
    
    public static synchronized void init() {
        if (initialized)
            return;

        TradeConfig.setPublishQuotePriceChange(false);
        initialized = true;
    }

    public static void destroy() {
        try {
            if (!initialized)
                return;
            log.trace("TradeJPADirect:destroy");
        }
        catch (Exception e) {
            log.error("TradeJPADirect:destroy", e);
        }

    }

    public MarketSummaryDataBean getMarketSummary() {
        MarketSummaryDataBean marketSummaryData;

        /*
         * Creating entiManager
         */
        EntityManager entityManager = getEm(emf);

        try {
            if (log.isTraceEnabled())
                log.trace("TradeJPADirect:getMarketSummary -- getting market summary");

            // Find Trade Stock Index Quotes (Top 100 quotes)
            // ordered by their change in value
            Collection<QuoteDataBean> quotes;

            Query query = entityManager.createNamedQuery("quoteejb.quotesByChange");
            quotes = query.getResultList();

            QuoteDataBean[] quoteArray = (QuoteDataBean[]) quotes.toArray(new QuoteDataBean[quotes.size()]);
            ArrayList<QuoteDataBean> topGainers = new ArrayList<QuoteDataBean>(
                                                                              5);
            ArrayList<QuoteDataBean> topLosers = new ArrayList<QuoteDataBean>(5);
            // BigDecimal TSIA = FinancialUtils.ZERO;
            BigDecimal TSIA = (new BigDecimal(0.00)).setScale(2);
            // BigDecimal openTSIA = FinancialUtils.ZERO;
            BigDecimal openTSIA = (new BigDecimal(0.00)).setScale(2);
            double totalVolume = 0.0;

            if (quoteArray.length > 5) {
                for (int i = 0; i < 5; i++)
                    topGainers.add(quoteArray[i]);
                for (int i = quoteArray.length - 1; i >= quoteArray.length - 5; i--)
                    topLosers.add(quoteArray[i]);

                for (QuoteDataBean quote : quoteArray) {
                    BigDecimal price = quote.getPrice();
                    BigDecimal open = quote.getOpen();
                    double volume = quote.getVolume();
                    TSIA = TSIA.add(price);
                    openTSIA = openTSIA.add(open);
                    totalVolume += volume;
                }
                TSIA = TSIA.divide(new BigDecimal(quoteArray.length),
                    // FinancialUtils.ROUND);
                    BigDecimal.ROUND_HALF_UP);
                openTSIA = openTSIA.divide(new BigDecimal(quoteArray.length),
                    // FinancialUtils.ROUND);
                    BigDecimal.ROUND_HALF_UP);
            }

            marketSummaryData = new MarketSummaryDataBean(TSIA, openTSIA,
                                                          totalVolume, topGainers, topLosers);
        }
        catch (Exception e) {
            log.error("TradeJPADirect:getMarketSummary", e);
            throw new RuntimeException("TradeJPADirect:getMarketSummary -- error ", e);
        } finally {
            putEm(entityManager);
        }

        return marketSummaryData;
    }

    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) {
        OrderDataBean order = null;
        BigDecimal total;
        /*
         * creating entitymanager
         */
        EntityManager entityManager = getEm(emf);

        try {
            if (log.isTraceEnabled())
                log.trace("TradeJPADirect:buy - userID=" + userID + " symbol=" + symbol + " quantity=" + quantity);

            entityManager.getTransaction().begin();

            AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
            AccountDataBean account = profile.getAccount();
            QuoteDataBean quote = entityManager.find(QuoteDataBean.class, symbol);
            HoldingDataBean holding = null; // The holding will be created by this buy order
            order = createOrder(account, quote, holding, "buy", quantity, entityManager);
            // order = createOrder(account, quote, holding, "buy", quantity);
            // UPDATE - account should be credited during completeOrder
            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal balance = account.getBalance();
            total = (new BigDecimal(quantity).multiply(price)).add(orderFee);
            account.setBalance(balance.subtract(total));

            // commit the transaction before calling completeOrder
            entityManager.getTransaction().commit();

            if (orderProcessingMode == TradeConfig.SYNCH)
                completeOrder(order.getOrderID(), false);
            else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE)
                queueOrder(order.getOrderID(), true);
        } catch (Exception e) {
            log.error("TradeJPADirect:buy(" + userID + "," + symbol + "," + quantity + ") --> failed", e);
            /* On exception - cancel the order */
            // TODO figure out how to do this with JPA
            if (order != null)
                order.cancel();
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            // throw new EJBException(e);
            throw new RuntimeException(e);
        } finally {
            if (entityManager != null) {
                putEm(entityManager);
                entityManager = null;
            }
        }
        // after the purchase or sell of a stock, update the stocks volume and
        // price
        updateQuotePriceVolume(symbol, TradeConfig.getRandomPriceChangeFactor(), quantity);
        return order;
    }

    public OrderDataBean sell(String userID, Integer holdingID,
                              int orderProcessingMode) {
        EntityManager entityManager = getEm(emf);

        OrderDataBean order = null;
        BigDecimal total;
        try {
            entityManager.getTransaction().begin();
            if (log.isTraceEnabled())
                log.trace("TradeJPADirect:sell - userID=" + userID + " holding=" + holdingID);

            AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);

            AccountDataBean account = profile.getAccount();
            HoldingDataBean holding = entityManager.find(HoldingDataBean.class, holdingID);

            if (holding == null) {
                log.error("TradeJPADirect:sell User " + userID
                          + " attempted to sell holding " + holdingID
                          + " which has already been sold");

                OrderDataBean orderData = new OrderDataBean();
                orderData.setOrderStatus("cancelled");

                entityManager.persist(orderData);
                entityManager.getTransaction().commit();
                return orderData;
            }

            QuoteDataBean quote = holding.getQuote();
            double quantity = holding.getQuantity();

            order = createOrder(account, quote, holding, "sell", quantity,
                                entityManager);
            // UPDATE the holding purchase data to signify this holding is
            // "inflight" to be sold
            // -- could add a new holdingStatus attribute to holdingEJB
            holding.setPurchaseDate(new java.sql.Timestamp(0));

            // UPDATE - account should be credited during completeOrder
            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal balance = account.getBalance();
            total = (new BigDecimal(quantity).multiply(price)).subtract(orderFee);

            account.setBalance(balance.add(total));

            // commit the transaction before calling completeOrder
            entityManager.getTransaction().commit();

            if (orderProcessingMode == TradeConfig.SYNCH)
                completeOrder(order.getOrderID(), false);
            else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE)
                queueOrder(order.getOrderID(), true);

        }
        catch (Exception e) {
            log.error("TradeJPADirect:sell(" + userID + "," + holdingID + ") --> failed", e);
            // TODO figure out JPA cancel
            if (order != null)
                order.cancel();
            entityManager.getTransaction().rollback();
            throw new RuntimeException("TradeJPADirect:sell(" + userID + "," + holdingID + ")", e);
        } finally {
            if (entityManager != null) {
                putEm(entityManager);
            }
        }

        if (!(order.getOrderStatus().equalsIgnoreCase("cancelled")))
            //after the purchase or sell of a stock, update the stocks volume and price
            updateQuotePriceVolume(order.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), order.getQuantity());

        return order;
    }

    public void queueOrder(Integer orderID, boolean twoPhase) {
        log.error("TradeJPADirect:queueOrder() not implemented for this runtime mode");
        throw new UnsupportedOperationException("TradeJPADirect:queueOrder() not implemented for this runtime mode");
    }

    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase)
    throws Exception {
        EntityManager entityManager = getEm(emf);
        OrderDataBean order = null;

        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:completeOrder - orderId=" + orderID + " twoPhase=" + twoPhase);

        try {
            order = entityManager.find(OrderDataBean.class, orderID);
            order.getQuote();

            if (order == null) {
                log.error("TradeJPADirect:completeOrder -- Unable to find orderID=" + orderID);
                return null;
            }

            if (order.isCompleted()) {
                throw new RuntimeException("Error: attempt to complete Order that is already completed\n" + order);
            }

            AccountDataBean account = order.getAccount();
            QuoteDataBean quote = order.getQuote();
            HoldingDataBean holding = order.getHolding();
            BigDecimal price = order.getPrice();
            double quantity = order.getQuantity();

            String userID = account.getProfile().getUserID();

            if (log.isTraceEnabled())
                log.trace("TradeJPADirect:completeOrder--> Completing Order "
                          + order.getOrderID() + "\n\t Order info: " + order
                          + "\n\t Account info: " + account + "\n\t Quote info: "
                          + quote + "\n\t Holding info: " + holding);

            HoldingDataBean newHolding = null;
            if (order.isBuy()) {
                /*
                 * Complete a Buy operation - create a new Holding for the Account -
                 * deduct the Order cost from the Account balance
                 */
                newHolding = createHolding(account, quote, quantity, price, entityManager);
            }

            entityManager.getTransaction().begin();

            if (newHolding != null) {
                order.setHolding(newHolding);
            }

            if (order.isSell()) {
                /*
                 * Complete a Sell operation - remove the Holding from the Account -
                 * deposit the Order proceeds to the Account balance
                 */
                if (holding == null) {
                    log.error("TradeJPADirect:completeOrder -- Unable to sell order " + order.getOrderID()
                            + ", holding already sold (null)");
                    order.cancel();
                    entityManager.getTransaction().commit();
                    return order;
                }
                else {
                    entityManager.remove(holding);
                    order.setHolding(null);
                }
            }

            order.setOrderStatus("closed");
            order.setCompletionDate(new java.sql.Timestamp(System.currentTimeMillis()));
            entityManager.persist(order);
            entityManager.getTransaction().commit();
            
            if (log.isTraceEnabled())
                log.trace("TradeJPADirect:completeOrder--> Completed Order "
                          + order.getOrderID() + "\n\t Order info: " + order
                          + "\n\t Account info: " + account + "\n\t Quote info: "
                          + quote + "\n\t Holding info: " + newHolding);

        } catch (Exception e) {
            log.error("TradeJPADirect:completeOrder() failed for orderID=" + orderID, e);
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
        } finally {
            if (entityManager != null) {
                putEm(entityManager);
            }
        }

        return order;
    }

    public void cancelOrder(Integer orderID, boolean twoPhase) {
        EntityManager entityManager = getEm(emf);

        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:cancelOrder - orderId=" + orderID + " twoPhase=" + twoPhase);

        OrderDataBean order = entityManager.find(OrderDataBean.class, orderID);
        /*
         * managed transaction
         */
        try {
            entityManager.getTransaction().begin();
            order.cancel();
            entityManager.getTransaction().commit();
        }
        catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            putEm(entityManager);
        }
    }

    public void orderCompleted(String userID, Integer orderID) {
        if (log.isTraceEnabled())
            log.trace("TradeAction:orderCompleted - userID=" + userID + " orderID=" + orderID);
    }

    public Collection<OrderDataBean> getOrders(String userID) {
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:getOrders - userID=" + userID);
        EntityManager entityManager = getEm(emf);
        AccountProfileDataBean profile = entityManager.find(
                                                           AccountProfileDataBean.class, userID);
        AccountDataBean account = profile.getAccount();
        putEm(entityManager);
        return account.getOrders();
    }

    public Collection<OrderDataBean> getClosedOrders(String userID) {

        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:getClosedOrders - userID=" + userID);
        EntityManager entityManager = getEm(emf);

        try {

            // Get the primary keys for all the closed Orders for this
            // account.
            /*
             * managed transaction
             */
            entityManager.getTransaction().begin();
            Query query = entityManager
                          .createNamedQuery("orderejb.closedOrders");
            query.setParameter("userID", userID);

            entityManager.getTransaction().commit();
            Collection results = query.getResultList();
            Iterator itr = results.iterator();
            // entityManager.joinTransaction();
            // Spin through the orders to populate the lazy quote fields
            while (itr.hasNext()) {
                OrderDataBean thisOrder = (OrderDataBean) itr.next();
                thisOrder.getQuote();
            }

            if (TradeConfig.jpaLayer == TradeConfig.OPENJPA) {
                Query updateStatus = entityManager
                                     .createNamedQuery("orderejb.completeClosedOrders");
                /*
                 * managed transaction
                 */
                try {
                    entityManager.getTransaction().begin();
                    updateStatus.setParameter("userID", userID);

                    updateStatus.executeUpdate();
                    entityManager.getTransaction().commit();
                }
                catch (Exception e) {
                    entityManager.getTransaction().rollback();
                    putEm(entityManager);
                }
            }
            else if (TradeConfig.jpaLayer == TradeConfig.HIBERNATE) {
                /*
                 * Add logic to do update orders operation, because JBoss5'
                 * Hibernate 3.3.1GA DB2Dialect and MySQL5Dialect do not work
                 * with annotated query "orderejb.completeClosedOrders" defined
                 * in OrderDatabean
                 */
                Query findaccountid = entityManager.createNativeQuery(
                                            "select "
                                            + "a.ACCOUNTID, "
                                            + "a.LOGINCOUNT, "
                                            + "a.LOGOUTCOUNT, "
                                            + "a.LASTLOGIN, "
                                            + "a.CREATIONDATE, "
                                            + "a.BALANCE, "
                                            + "a.OPENBALANCE, "
                                            + "a.PROFILE_USERID "
                                            + "from accountejb a where a.profile_userid = ?",
                                            org.apache.openjpa.integration.daytrader.AccountDataBean.class);
                findaccountid.setParameter(1, userID);
                AccountDataBean account = (AccountDataBean) findaccountid.getSingleResult();
                Integer accountid = account.getAccountID();
                Query updateStatus = entityManager.createNativeQuery(
                        "UPDATE orderejb o SET o.orderStatus = 'completed' WHERE "
                        + "o.orderStatus = 'closed' AND o.ACCOUNT_ACCOUNTID  = ?");
                updateStatus.setParameter(1, accountid.intValue());
                updateStatus.executeUpdate();
            }
            if (entityManager != null) {
                putEm(entityManager);
            }
            return results;
        } catch (Exception e) {
            log.error("TradeJPADirect.getClosedOrders", e);
            putEm(entityManager);
            throw new RuntimeException("TradeJPADirect.getClosedOrders - error", e);

        } finally {
            if (entityManager != null) {
                putEm(entityManager);
            }
        }
    }

    public QuoteDataBean createQuote(String symbol, String companyName,
                                     BigDecimal price) {
        EntityManager entityManager = getEm(emf);
        QuoteDataBean quote = null;
        try {
            quote = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
            /*
             * managed transaction
             */
            entityManager.getTransaction().begin();
            entityManager.persist(quote);
            entityManager.getTransaction().commit();

            if (log.isTraceEnabled())
                log.trace("TradeJPADirect:createQuote-->" + quote);
            return quote;
        } catch (Exception e) {
            log.error("TradeJPADirect:createQuote -- exception creating Quote=" + quote, e);
            if (entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (entityManager != null) {
                putEm(entityManager);
            }
        }

    }

    public QuoteDataBean getQuote(String symbol) {
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:getQuote - symbol=" + symbol);
        EntityManager entityManager = getEm(emf);

        QuoteDataBean qdb = entityManager.find(QuoteDataBean.class, symbol);

        if (entityManager != null) {
            putEm(entityManager);
        }
        return qdb;
    }

    public Collection<QuoteDataBean> getAllQuotes() {
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:getAllQuotes");
        EntityManager entityManager = getEm(emf);

        Query query = entityManager.createNamedQuery("quoteejb.allQuotes");

        if (entityManager != null) {
            putEm(entityManager);

        }
        return query.getResultList();
    }

    public QuoteDataBean updateQuotePriceVolume(String symbol,
                                                BigDecimal changeFactor, double sharesTraded) {
        if (!TradeConfig.getUpdateQuotePrices())
            return new QuoteDataBean();

        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:updateQuote - symbol=" + symbol + "changeFactor=" + changeFactor);

        /*
         * Add logic to determine JPA layer, because JBoss5' Hibernate 3.3.1GA
         * DB2Dialect and MySQL5Dialect do not work with annotated query
         * "quoteejb.quoteForUpdate" defined in QuoteDatabean
         */
        EntityManager entityManager = getEm(emf);
        QuoteDataBean quote = null;
        if (TradeConfig.jpaLayer == TradeConfig.HIBERNATE) {
            quote = entityManager.find(QuoteDataBean.class, symbol);
        } else if (TradeConfig.jpaLayer == TradeConfig.OPENJPA) {
  
            Query q = entityManager.createNamedQuery("quoteejb.quoteForUpdate");
            q.setParameter(1, symbol);
  
            quote = (QuoteDataBean) q.getSingleResult();
        }

        BigDecimal oldPrice = quote.getPrice();

        if (quote.getPrice().equals(TradeConfig.PENNY_STOCK_PRICE)) {
            changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
        }

        BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, BigDecimal.ROUND_HALF_UP);

        /*
         * managed transaction
         */

        try {

            quote.setPrice(newPrice);
            quote.setVolume(quote.getVolume() + sharesTraded);
            quote.setChange((newPrice.subtract(quote.getOpen()).doubleValue()));

            entityManager.getTransaction().begin();
            entityManager.merge(quote);
            entityManager.getTransaction().commit();
        }
        catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            if (entityManager != null) {
                putEm(entityManager);
            }
        }

        this.publishQuotePriceChange(quote, oldPrice, changeFactor, sharesTraded);

        return quote;
    }

    public Collection<HoldingDataBean> getHoldings(String userID) {
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:getHoldings - userID=" + userID);
        EntityManager entityManager = getEm(emf);
        /*
         * managed transaction
         */
        entityManager.getTransaction().begin();

        Query query = entityManager.createNamedQuery("holdingejb.holdingsByUserID");
        query.setParameter("userID", userID);

        entityManager.getTransaction().commit();
        Collection<HoldingDataBean> holdings = query.getResultList();
        /*
         * Inflate the lazy data memebers
         */
        Iterator itr = holdings.iterator();
        while (itr.hasNext()) {
            ((HoldingDataBean) itr.next()).getQuote();
        }

        putEm(entityManager);
        return holdings;
    }

    public HoldingDataBean getHolding(Integer holdingID) {
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:getHolding - holdingID=" + holdingID);
        HoldingDataBean holding;
        EntityManager entityManager = getEm(emf);
        holding = entityManager.find(HoldingDataBean.class, holdingID);
        putEm(entityManager);
        return holding;
    }

    public AccountDataBean getAccountData(String userID) {
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:getAccountData - userID=" + userID);

        EntityManager entityManager = getEm(emf);

        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        /*
         * Inflate the lazy data memebers
         */
        AccountDataBean account = profile.getAccount();
        account.getProfile();

        // Added to populate transient field for account
        account.setProfileID(profile.getUserID());
        putEm(entityManager);

        return account;
    }

    public AccountProfileDataBean getAccountProfileData(String userID) {
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:getProfileData - userID=" + userID);
        EntityManager entityManager = getEm(emf);

        AccountProfileDataBean apb = entityManager.find(AccountProfileDataBean.class, userID);
        putEm(entityManager);
        return apb;
    }

    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) {

        EntityManager entityManager = getEm(emf);

        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:updateAccountProfileData - profileData=" + profileData);
        /*
         * // Retrieve the previous account profile in order to get account
         * data... hook it into new object AccountProfileDataBean temp =
         * entityManager.find(AccountProfileDataBean.class,
         * profileData.getUserID()); // In order for the object to merge
         * correctly, the account has to be hooked into the temp object... // -
         * may need to reverse this and obtain the full object first
         * 
         * profileData.setAccount(temp.getAccount());
         * 
         * //TODO this might not be correct temp =
         * entityManager.merge(profileData); //System.out.println(temp);
         */

        AccountProfileDataBean temp = entityManager.find(AccountProfileDataBean.class, profileData.getUserID());
        temp.setAddress(profileData.getAddress());
        temp.setPassword(profileData.getPassword());
        temp.setFullName(profileData.getFullName());
        temp.setCreditCard(profileData.getCreditCard());
        temp.setEmail(profileData.getEmail());
        /*
         * Managed Transaction
         */
        try {

            entityManager.getTransaction().begin();
            entityManager.merge(temp);
            entityManager.getTransaction().commit();
        }
        catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            putEm(entityManager);
        }

        return temp;
    }

    public AccountDataBean login(String userID, String password)
    throws Exception {

        EntityManager entityManager = getEm(emf);

        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);

        if (profile == null) {
            throw new RuntimeException("No such user: " + userID);
        }
        /*
         * Managed Transaction
         */
        entityManager.getTransaction().begin();
        entityManager.merge(profile);

        AccountDataBean account = profile.getAccount();

        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:login - userID=" + userID + " password=" + password);

        account.login(password);
        entityManager.getTransaction().commit();
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:login(" + userID + "," + password + ") success" + account);
        putEm(entityManager);
        return account;
    }

    public void logout(String userID) {
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:logout - userID=" + userID);
        EntityManager entityManager = getEm(emf);

        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        AccountDataBean account = profile.getAccount();

        /*
         * Managed Transaction
         */
        try {
            entityManager.getTransaction().begin();
            account.logout();
            entityManager.getTransaction().commit();
        }
        catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            putEm(entityManager);
        }

        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:logout(" + userID + ") success");
    }

    public AccountDataBean register(String userID, String password, String fullname, 
                                    String address, String email, String creditcard,
                                    BigDecimal openBalance) {
        AccountDataBean account = null;
        AccountProfileDataBean profile = null;
        EntityManager entityManager = getEm(emf);

        if (log.isTraceEnabled()) {
            //Log.trace("TradeJPADirect:register", userID, password, fullname, address, email, creditcard, openBalance);
            log.trace("TradeJPADirect:register - userID=" + userID);
        }
        
        // Check to see if a profile with the desired userID already exists
        profile = entityManager.find(AccountProfileDataBean.class, userID);
        if (profile != null) {
            log.error("Failed to register new Account - AccountProfile with userID(" + userID + ") already exists");
            return null;
        }
        else {
        	profile = new AccountProfileDataBean(userID, password, fullname,
                                                 address, email, creditcard);
            account = new AccountDataBean(0, 0, null, new Timestamp(System.currentTimeMillis()),
                    openBalance, openBalance, userID);
            profile.setAccount(account);
            account.setProfile(profile);
            /*
             * managed Transaction
             */
            try {
                entityManager.getTransaction().begin();
                entityManager.persist(profile);
                entityManager.persist(account);
                entityManager.getTransaction().commit();
            }
            catch (Exception e) {
                log.error("Failed to create account and profile for userId=" + userID, e);
                entityManager.getTransaction().rollback();
            } finally {
                putEm(entityManager);
            }

        }

        return account;
    }

    // @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    //public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
    public void resetTrade(boolean deleteAll) throws Exception {
        // return(new TradeJDBCDirect(false)).resetTrade(deleteAll);
        new RuntimeException("TradeJPADirect:resetTrade - not implemented in JPA mode");
    }

    /*
     * NO LONGER USE
     */

    private void publishQuotePriceChange(QuoteDataBean quote,
                                         BigDecimal oldPrice, BigDecimal changeFactor, double sharesTraded) {
        if (!TradeConfig.getPublishQuotePriceChange())
            return;
        log.error("TradeJPADirect:publishQuotePriceChange - is not implemented for this runtime mode");
        throw new UnsupportedOperationException(
            "TradeJPADirect:publishQuotePriceChange - is not implemented for this runtime mode");
    }

    /*
     * new Method() that takes EntityManager as a parameter
     */
    private OrderDataBean createOrder(AccountDataBean account,
                                      QuoteDataBean quote, HoldingDataBean holding, String orderType,
                                      double quantity, EntityManager entityManager) {
        OrderDataBean order;
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:createOrder(orderID=" + " account="
                      + ((account == null) ? null : account.getAccountID())
                      + " quote=" + ((quote == null) ? null : quote.getSymbol())
                      + " orderType=" + orderType + " quantity=" + quantity);
        try {
            order = new OrderDataBean(orderType, 
                                      "open", 
                                      new Timestamp(System.currentTimeMillis()), 
                                      null, 
                                      quantity, 
                                      // quote.getPrice().setScale(FinancialUtils.SCALE, FinancialUtils.ROUND),
                                      quote.getPrice().setScale(2, BigDecimal.ROUND_HALF_UP),
                                      TradeConfig.getOrderFee(orderType), 
                                      account, 
                                      quote, 
                                      holding);
                entityManager.persist(order);
        }
        catch (Exception e) {
            log.error("TradeJPADirect:createOrder -- failed to create Order", e);
            throw new RuntimeException("TradeJPADirect:createOrder -- failed to create Order", e);
        }
        return order;
    }

    private HoldingDataBean createHolding(AccountDataBean account,
                                          QuoteDataBean quote, double quantity, BigDecimal purchasePrice,
                                          EntityManager entityManager) throws Exception {
        HoldingDataBean newHolding = new HoldingDataBean(quantity,
                                                         purchasePrice, new Timestamp(System.currentTimeMillis()),
                                                         account, quote);
        try {
            /*
             * manage transactions
             */
            entityManager.getTransaction().begin();
            entityManager.persist(newHolding);
            entityManager.getTransaction().commit();
            if (log.isTraceEnabled())
                log.trace("TradeJPADirect:createHolding(account=" + ((account == null) ? null : account.getAccountID())
                          + " quote=" + ((quote == null) ? null : quote.getSymbol())
                          + " quantity=" + quantity + " purchasePrice=" + purchasePrice + ")");
        }
        catch (Exception e) {
            log.error("createHolding() failed for newHolding=" + newHolding, e);
            entityManager.getTransaction().rollback();
        }
        return newHolding;
    }

    public double investmentReturn(double investment, double NetValue)
    throws Exception {
        if (log.isTraceEnabled())
            log.trace("TradeJPADirect:investmentReturn");

        double diff = NetValue - investment;
        double ir = diff / investment;
        return ir;
    }

    public QuoteDataBean pingTwoPhase(String symbol) throws Exception {
        log.error("TradeJPADirect:pingTwoPhase - is not implemented for this runtime mode");
        throw new UnsupportedOperationException(
                "TradeJPADirect:pingTwoPhase - is not implemented for this runtime mode");
    }

    class quotePriceComparator implements java.util.Comparator {
        public int compare(Object quote1, Object quote2) {
            double change1 = ((QuoteDataBean) quote1).getChange();
            double change2 = ((QuoteDataBean) quote2).getChange();
            return new Double(change2).compareTo(change1);
        }
    }

    /**
     * TradeBuildDB needs this abstracted method
     */
    public String checkDBProductName() throws Exception {
        log.error("TradeJPADirect:checkDBProductName - is not implemented in JPA mode");
        // return(new TradeJDBCDirect(false)).checkDBProductName();
        new RuntimeException("TradeJPADirect:checkDBProductName - is not implemented in JPA mode");
        return null;
    }

    /**
     * TradeBuildDB needs this abstracted method
     */
    public boolean recreateDBTables(Object[] sqlBuffer, java.io.PrintWriter out)
    throws Exception {
        log.error("TradeJPADirect:recreateDBTables - is not implemented in JPA mode");
        //return(new TradeJDBCDirect(false)).recreateDBTables(sqlBuffer, out);
        new RuntimeException("TradeJPADirect:recreateDBTables - is not implemented in JPA mode");
        return false;
    }

    /**
     * Get mode - returns the persistence mode (TradeConfig.JPA)
     * 
     * @return int mode
     */
    public int getMode() {
        return TradeConfig.JPA;
    }

}
