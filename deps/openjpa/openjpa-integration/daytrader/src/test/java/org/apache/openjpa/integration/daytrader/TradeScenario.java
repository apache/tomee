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

import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;

/**
 * TradeScenarioServlet emulates a population of web users by generating a specific Trade operation 
 * for a randomly chosen user on each access to the URL. Test this servlet by clicking Trade Scenario 
 * and hit "Reload" on your browser to step through a Trade Scenario. To benchmark using this URL aim 
 * your favorite web load generator (such as AKStress) at the Trade Scenario URL and fire away.
 */
public class TradeScenario {

    private TradeAction tAction = null;

    public TradeScenario(TradeAction tAction) {
        this.tAction = tAction;
    }
    
    /**
     * Perform the following 15 tasks for the given userID:
     *     login, home, account, update, home, portfolio, sell, buy, home, portfolio, sell, buy, home, account, logout
     *     
     * @param userID
     * @return
     */
    public boolean performUserTasks(String userID) {
        StringBuilder sb = new StringBuilder(16384);
        boolean brc = false;

        if (TradeConfig.log.isTraceEnabled()) {
            TradeConfig.log.trace("TradeScenario.performUserTasks(" + userID + ")");
        }
        
        try {
            // login
            log(sb, performTask("l", userID));
            // home page
            log(sb, performTask("h", userID));
            // account info and orders
            log(sb, performTask("a", userID));
            // update account info
            log(sb, performTask("u", userID));
            // home page
            log(sb, performTask("h", userID));
            // portfolio holdings
            log(sb, performTask("p", userID));
            // sell
            log(sb, performTask("s", userID));
            // buy
            log(sb, performTask("b", userID));
            // home page
            log(sb, performTask("h", userID));
            // portfolio holdings
            log(sb, performTask("p", userID));
            // sell
            log(sb, performTask("s", userID));
            // buy
            log(sb, performTask("b", userID));
            // home page
            log(sb, performTask("h", userID));
            // account info and orders
            log(sb, performTask("a", userID));
            // logout
            log(sb, performTask("o", userID));
            brc = true;
            if (TradeConfig.log.isTraceEnabled()) {
                TradeConfig.log.trace(sb.toString());
            }
        } catch (Exception e) {
            TradeConfig.log.error("TradeScenario.performUserTasks(" + userID + ") failed", e);
        }            
        return brc;
    }
    
   /** 
	* Main service method for TradeScenarioServlet
	*
	* @param request Object that encapsulates the request to the servlet
	* @param response Object that encapsulates the response from the servlet
	*/    
	public String performTask(String scenarioAction, String userID) throws IOException {
        StringBuilder sb = new StringBuilder(256);
	    String results = "";
		// Scenario generator for Trade2
		char action = ' ';

		// String to create full dispatch path to TradeAppServlet w/ request Parameters
		String dispPath = null; // Dispatch Path to TradeAppServlet

		if ((scenarioAction != null) && (scenarioAction.length() >= 1))
		{
			action = scenarioAction.charAt(0);
			if (action == 'n')
			{ //null;
				try
				{
					log(sb, "TradeScenario.performTask() scenarioAction=" + scenarioAction + ", userID=" + userID); 
				}
				catch (Exception e)
				{
					log(sb, "ERROR - TradeScenario.performTask() - Exception ", e);
				} finally {
                    return sb.toString();
				}
			} //end of action=='n'
		}

		if (userID == null || userID.trim().length() == 0) {
	        // These operations require the user to be logged in. Verify the user and if not logged in
	        // change the operation to a login
			userID = null;
			action = 'l';
            TradeConfig.incrementScenarioCount();
		} else if (action == ' ') {
			//action is not specified perform a random operation according to current mix
			// Tell getScenarioAction if we are an original user or a registered user 
			// -- sellDeficits should only be compensated for with original users.
			action = TradeConfig.getScenarioAction(
				userID.startsWith(TradeConfig.newUserPrefix));
		}
		
		switch (action) {
				case 'q' : //quote
				    tAction.doQuotes(sb, userID, TradeConfig.rndSymbols());
					break;
				case 'a' : //account
					tAction.doAccount(sb, userID, results);
					break;
				case 'u' : //update account profile
					String fullName = "rnd" + System.currentTimeMillis();
					String address = "rndAddress";
					String   password = "xxx";
					String email = "rndEmail";
					String creditcard = "rndCC";
					tAction.doAccountUpdate(sb, userID, password, password, fullName, address, creditcard, email);
					break;
				case 'h' : //home
				    tAction.doHome(sb, userID, results);
					break;
				case 'l' : //login
					userID = TradeConfig.getUserID();
					String password2 = "xxx";
					boolean brc = tAction.doLogin(sb, userID, password2);
					// login is successful if the userID is written to the HTTP session
					if (!brc) {
						log(sb, "TradeScenario login failed. Reset DB between runs.");
					} 
					break;
				case 'o' : //logout
				    tAction.doLogout(sb, userID);
					break;
				case 'p' : //portfolio
				    tAction.doPortfolio(sb, userID, results);
					break;
				case 'r' : //register
					//Logout the current user to become a new user
					// see note in TradeServletAction
                    tAction.doLogout(sb, userID);

					userID = TradeConfig.rndNewUserID();
					String passwd = "yyy";
					fullName = TradeConfig.rndFullName();
					creditcard = TradeConfig.rndCreditCard();
					String money = TradeConfig.rndBalance();
					email = TradeConfig.rndEmail(userID);
					String smail = TradeConfig.rndAddress();
					tAction.doRegister(sb, userID, passwd, passwd, fullName, creditcard, money, email, smail);
					break;
				case 's' : //sell
				    Collection<HoldingDataBean> holdings = tAction.getHoldings(userID);
					int numHoldings = holdings.size();
					if (numHoldings > 0)
					{
						//sell first available security out of holding 						
						Iterator it = holdings.iterator();
						boolean foundHoldingToSell = false;
						while (it.hasNext()) 
						{
							HoldingDataBean holdingData = (HoldingDataBean) it.next();
							if ( !(holdingData.getPurchaseDate().equals(new java.util.Date(0)))  )
							{
								Integer holdingID = holdingData.getHoldingID();
								tAction.doSell(sb, userID, holdingID);
								foundHoldingToSell = true;
								break;	
							}
						}
						if (foundHoldingToSell) break;
						tAction.log.warn("TradeScenario: No holdings sold for userID=" + userID +
						    ", holdings=" + numHoldings);
					} else {
                        tAction.log.warn("TradeScenario: No holdings to sell for userID=" + userID);
					}
					// At this point: A TradeScenario Sell was requested with No Stocks in Portfolio
					// This can happen when a new registered user happens to request a sell before a buy
					// In this case, fall through and perform a buy instead
                    tAction.log.warn("TradeScenario: No holdings sold - switching to buy -- userID=" + userID);

					/* Trade 2.037: Added sell_deficit counter to maintain correct buy/sell mix.
					 * When a users portfolio is reduced to 0 holdings, a buy is requested instead of a sell.
					 * This throws off the buy/sell mix by 1. This results in unwanted holding table growth
					 * To fix we increment a sell deficit counter to maintain the correct ratio in getScenarioAction
					 * The 'z' action from getScenario denotes that this is a sell action that was switched from a buy
					 * to reduce a sellDeficit
					 */
					if (userID.startsWith(TradeConfig.newUserPrefix) == false)
					{
						TradeConfig.incrementSellDeficit();
					}
				case 'b' : //buy
					String symbol = TradeConfig.rndSymbol();
					String amount = TradeConfig.rndQuantity() + "";
					tAction.doQuotes(sb, userID, symbol);
					tAction.doBuy(sb, userID, symbol, amount);
					break;
			} //end of switch statement
		log(sb, "Results", results);
		return sb.toString();
	}

    private void log(StringBuilder sb, String msg) {
        if (tAction.log != null && tAction.log.isTraceEnabled()) {
            sb.append(msg);
            sb.append(System.getProperty("line.separator"));
        }
    }

    private void log(StringBuilder sb, String msg, Object obj) {
        if (tAction.log != null && tAction.log.isTraceEnabled()) {
            sb.append(msg);
            sb.append(" = ");
            sb.append(obj);
            sb.append(System.getProperty("line.separator"));
        }
    }
}
