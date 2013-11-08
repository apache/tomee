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
import java.util.ArrayList;
import java.util.Random;

import org.apache.openjpa.lib.log.Log;


/**
 * TradeConfig is a JavaBean holding all configuration and runtime parameters for the Trade application
 * TradeConfig sets runtime parameters such as the RunTimeMode (EJB3, DIRECT, SESSION3, JDBC, JPA)
 *
 */

public class TradeConfig {

    // added by OpenJPA
    public static Log log = null;
    
    /* Trade Runtime Configuration Parameters */
    public static final int UNKNOWN = -1;

    /* Trade Runtime Mode parameters */
    public static String[] runTimeModeNames = {"Full EJB3", "Direct (JDBC)", "Session (EJB3) To Direct",
                                                "Web JDBC", "Web JPA"};
    public static final int EJB3 = 0;
    public static final int DIRECT = 1;
    public static final int SESSION3 = 2;
    public static final int JDBC = 3;
    public static final int JPA = 4;
    public static int runTimeMode = JPA;
	
    /* Trade JPA Layer parameters */
	public static String[] jpaLayerNames = {"OpenJPA", "Hibernate"};
	public static final int OPENJPA = 0;
	public static final int HIBERNATE = 1;
	public static int jpaLayer = OPENJPA;

	public static String[] orderProcessingModeNames =
		{ "Synchronous", "Asynchronous_2-Phase" };
	public static final int SYNCH = 0;
	public static final int ASYNCH_2PHASE = 1;
	public static int orderProcessingMode = SYNCH;

	public static String[] accessModeNames = { "Standard", "WebServices" };
	public static final int STANDARD = 0;
	public static final int WEBSERVICES = 1;
	private static int accessMode = STANDARD;

	/* Trade Scenario Workload parameters */
	public static String[] workloadMixNames = { "Standard", "High-Volume", };
	public final static int SCENARIOMIX_STANDARD = 0;
	public final static int SCENARIOMIX_HIGHVOLUME = 1;
	public static int workloadMix = SCENARIOMIX_STANDARD;

	/* Trade Web Interface parameters */
	public static String[] webInterfaceNames = { "JSP", "JSP-Images" };
	public static final int JSP = 0;
	public static final int JSP_Images = 1;
	public static int webInterface = JSP;

	/* Trade Caching Type parameters */
	public static String[] cachingTypeNames = { "DistributedMap", "Command Caching", "No Caching" };
	public static final int DISTRIBUTEDMAP = 0;
	public static final int COMMAND_CACHING = 1;
	public static final int NO_CACHING = 2;
	public static int cachingType = NO_CACHING;
	
	/* Trade Database Scaling parameters*/
	private static int MAX_USERS = 500;
	private static int MAX_QUOTES = 1000;

	/* Trade Database specific paramters */
	public static String JDBC_UID = null;
	public static String JDBC_PWD = null;
	public static String DS_NAME = "java:comp/env/jdbc/TradeDataSource";

	/*Trade SOAP specific parameters */
	private static String SoapURL =
		"http://localhost:8080/daytrader/services/TradeWSServices";

	/*Trade XA Datasource specific parameters */
	public static boolean JDBCDriverNeedsGlobalTransaction = false;

	/* Trade Config Miscellaneous itmes */
	public static String DATASOURCE = "java:comp/env/jdbc/TradeDataSource";
	public static int KEYBLOCKSIZE = 1000;
	public static int QUOTES_PER_PAGE = 10;
	public static boolean RND_USER = true;
	//public static int		RND_SEED = 0;
	private static int MAX_HOLDINGS = 10;
	private static int count = 0;
	private static Object userID_count_semaphore = new Object();
	private static int userID_count = 0;
	private static String hostName = null;
	private static Random r0 = new Random(System.currentTimeMillis());
	//private static Random r1 = new Random(RND_SEED);
	private static Random randomNumberGenerator = r0;
	public static final String newUserPrefix = "ru:";
	public static final int verifyPercent = 5;
	private static boolean trace = false;
	private static boolean actionTrace = false;
	private static boolean updateQuotePrices = true;
	private static int primIterations = 1;
	private static boolean longRun = true;
	private static boolean publishQuotePriceChange = false;
	
	/**
	 *   -1 means every operation
	 *    0 means never perform a market summary
	 *  > 0 means number of seconds between summaries.  These will be
	 *      synchronized so only one transaction in this period will create a summary and 
	 *      will cache its results.
	 */
	private static int  marketSummaryInterval = 20;

	/*
	 * Penny stocks is a problem where the random price change factor gets a stock
	 * down to $.01.  In this case trade jumpstarts the price back to $6.00 to
	 * keep the math interesting.
	 */
	public static BigDecimal PENNY_STOCK_PRICE;
	public static BigDecimal PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
	static {
		PENNY_STOCK_PRICE = new BigDecimal(0.01);
		PENNY_STOCK_PRICE =
			PENNY_STOCK_PRICE.setScale(2, BigDecimal.ROUND_HALF_UP);
		PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER = new BigDecimal(600.0);
		PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER.setScale(
			2,
			BigDecimal.ROUND_HALF_UP);
	}

	/* CJB (DAYTRADER-25) - Also need to impose a ceiling on the quote price to ensure
	 * prevent account and holding balances from exceeding the databases decimal precision.
	 * At some point, this maximum value can be used to trigger a stock split.
	 */

	public static BigDecimal MAXIMUM_STOCK_PRICE;
	public static BigDecimal MAXIMUM_STOCK_SPLIT_MULTIPLIER;
	static {
		MAXIMUM_STOCK_PRICE = new BigDecimal(400);
		MAXIMUM_STOCK_PRICE.setScale(2, BigDecimal.ROUND_HALF_UP);
		MAXIMUM_STOCK_SPLIT_MULTIPLIER = new BigDecimal(0.5);
		MAXIMUM_STOCK_SPLIT_MULTIPLIER.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	/* Trade Scenario actions mixes. Each of the array rows represents a specific Trade Scenario Mix. 
	   The columns give the percentages for each action in the column header. Note: "login" is always 0. 
	   logout represents both login and logout (because each logout operation will cause a new login when
	   the user context attempts the next action.
	 */
	/* Trade Scenario Workload parameters */
	public final static int HOME_OP = 0;
	public final static int QUOTE_OP = 1;
	public final static int LOGIN_OP = 2;
	public final static int LOGOUT_OP = 3;
	public final static int REGISTER_OP = 4;
	public final static int ACCOUNT_OP = 5;
	public final static int PORTFOLIO_OP = 6;
	public final static int BUY_OP = 7;
	public final static int SELL_OP = 8;
	public final static int UPDATEACCOUNT_OP = 9;

	private static int scenarioMixes[][] = {
		//	h	q	l	o	r	a	p	b	s	u
		{ 20, 40, 0, 4, 2, 10, 12, 4, 4, 4 }, //STANDARD
		{
			20, 40, 0, 4, 2, 7, 7, 7, 7, 6 }, //High Volume
	};
	private static char actions[] =
		{ 'h', 'q', 'l', 'o', 'r', 'a', 'p', 'b', 's', 'u' };
	private static int sellDeficit = 0;
	//Tracks the number of buys over sell when a users portfolio is empty
	// Used to maintain the correct ratio of buys/sells

	/* JSP pages for all Trade Actions */

	public final static int WELCOME_PAGE = 0;
	public final static int REGISTER_PAGE = 1;
	public final static int PORTFOLIO_PAGE = 2;
	public final static int QUOTE_PAGE = 3;
	public final static int HOME_PAGE = 4;
	public final static int ACCOUNT_PAGE = 5;
	public final static int ORDER_PAGE = 6;
	public final static int CONFIG_PAGE = 7;
	public final static int STATS_PAGE = 8;

	//FUTURE Add XML/XSL View
	public static String webUI[][] =
		{
			{
				"/welcome.jsp",
				"/register.jsp",
				"/portfolio.jsp",
				"/quote.jsp",
				"/tradehome.jsp",
				"/account.jsp",
				"/order.jsp",
				"/config.jsp",
				"/runStats.jsp" },
		//JSP Interface
		{
			"/welcomeImg.jsp",
				"/registerImg.jsp",
				"/portfolioImg.jsp",
				"/quoteImg.jsp",
				"/tradehomeImg.jsp",
				"/accountImg.jsp",
				"/orderImg.jsp",
				"/config.jsp",
				"/runStats.jsp" },
		//JSP Interface	
	};

	// These are the property settings the VAJ access beans look for.	
	private static final String NAMESERVICE_TYPE_PROPERTY =
		"java.naming.factory.initial";
	private static final String NAMESERVICE_PROVIDER_URL_PROPERTY =
		"java.naming.provider.url";

	// FUTURE:
	// If a "trade2.properties" property file is supplied, reset the default values 
	// to match those specified in the file. This provides a persistent runtime 
	// property mechanism during server startup

	/**
	 * Return the hostname for this system
	 * Creation date: (2/16/2000 9:02:25 PM)
	 */

	private static String getHostname() {
		try {
			if (hostName == null) {
				hostName = java.net.InetAddress.getLocalHost().getHostName();
				//Strip of fully qualifed domain if necessary
				try {
					hostName = hostName.substring(0, hostName.indexOf('.'));
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			log.error(
				"Exception getting local host name using 'localhost' - ",
				e);
			hostName = "localhost";
		}
		return hostName;
	}

	/**
	 * Return a Trade UI Web page based on the current configuration
	 * This may return a JSP page or a Servlet page 
	 * Creation date: (3/14/2000 9:08:34 PM)
	 */

	public static String getPage(int pageNumber) {
		return webUI[webInterface][pageNumber];
	}

	/**
	 * Return the list of run time mode names
	 * Creation date: (3/8/2000 5:58:34 PM)
	 * @return java.lang.String[]
	 */
	public static java.lang.String[] getRunTimeModeNames() {
		return runTimeModeNames;
	}

	private static int scenarioCount = 0;

	/**
	 * Return a Trade Scenario Operation based on the setting of the current mix (TradeScenarioMix)
	 * Creation date: (2/10/2000 9:08:34 PM)
	 */

	public static char getScenarioAction(boolean newUser) {
		int r = rndInt(100); //0 to 99 = 100
		int i = 0;
		int sum = scenarioMixes[workloadMix][i];
		while (sum <= r) {
			i++;
			sum += scenarioMixes[workloadMix][i];
		}

		incrementScenarioCount();

		/* In TradeScenarioServlet, if a sell action is selected, but the users portfolio is empty,
		 * a buy is executed instead and sellDefecit is incremented. This allows the number of buy/sell
		 * operations to stay in sync w/ the given Trade mix.
		 */

		if ((!newUser) && (actions[i] == 'b')) {
			synchronized (TradeConfig.class) {
				if (sellDeficit > 0) {
					sellDeficit--;
					return 's';
					//Special case for TradeScenarioServlet to note this is a buy switched to a sell to fix sellDeficit
				}
			}
		}

		return actions[i];
	}

	public static String getUserID() {
		String userID;
		if (RND_USER) {
			userID = rndUserID();
		} else {
			userID = nextUserID();
		}
		return userID;
	}
	private static final BigDecimal orderFee = new BigDecimal("24.95");
	private static final BigDecimal cashFee = new BigDecimal("0.0");
	public static BigDecimal getOrderFee(String orderType) {
		if ((orderType.compareToIgnoreCase("BUY") == 0)
			|| (orderType.compareToIgnoreCase("SELL") == 0))
			return orderFee;

		return cashFee;

	}

	/**
	 * Increment the sell deficit counter
	 * Creation date: (6/21/2000 11:33:45 AM)
	 */
	public synchronized static void incrementSellDeficit() {
		sellDeficit++;
	}

	public static String nextUserID() {
		String userID;
		synchronized (userID_count_semaphore) {
			userID = "uid:" + userID_count;
			userID_count++;
			if (userID_count % MAX_USERS == 0) {
				userID_count = 0;
			}
		}
		return userID;
	}
	public static double random() {
		return randomNumberGenerator.nextDouble();
	}
	public static String rndAddress() {
		return rndInt(1000) + " Oak St.";
	}
	public static String rndBalance() {
		//Give all new users a cool mill in which to trade
		return "1000000";
	}
	public static String rndCreditCard() {
		return rndInt(100)
			+ "-"
			+ rndInt(1000)
			+ "-"
			+ rndInt(1000)
			+ "-"
			+ rndInt(1000);
	}
	public static String rndEmail(String userID) {
		return userID + "@" + rndInt(100) + ".com";
	}
	public static String rndFullName() {
		return "first:" + rndInt(1000) + " last:" + rndInt(5000);
	}
	public static int rndInt(int i) {
		return (new Float(random() * i)).intValue();
	}
	public static float rndFloat(int i) {
		return (new Float(random() * i)).floatValue();
	}
	public static BigDecimal rndBigDecimal(float f) {
		return (new BigDecimal(random() * f)).setScale(
			2,
			BigDecimal.ROUND_HALF_UP);
	}

	public static boolean rndBoolean() {
		return randomNumberGenerator.nextBoolean();
	}

	/**
	 * Returns a new Trade user
	 * Creation date: (2/16/2000 8:50:35 PM)
	 */
	public synchronized static String rndNewUserID() {

		return newUserPrefix
			+ getHostname()
			+ System.currentTimeMillis()
			+ count++;
	}

	public static float rndPrice() {
		return ((new Integer(rndInt(200))).floatValue()) + 1.0f;
	}
	private final static BigDecimal ONE = new BigDecimal(1.0);
	public static BigDecimal getRandomPriceChangeFactor() {
		// CJB (DAYTRADER-25) - Vary change factor between 1.2 and 0.8
		double percentGain = rndFloat(1) * 0.2;
		if (random() < .5)
			percentGain *= -1;
		percentGain += 1;

		// change factor is between +/- 20%
		BigDecimal percentGainBD =
			(new BigDecimal(percentGain)).setScale(2, BigDecimal.ROUND_HALF_UP);
		if (percentGainBD.doubleValue() <= 0.0)
			percentGainBD = ONE;

		return percentGainBD;
	}

	public static float rndQuantity() {
		return ((new Integer(rndInt(200))).floatValue()) + 1.0f;
	}

	public static String rndSymbol() {
		return "s:" + rndInt(MAX_QUOTES - 1);
	}
	public static String rndSymbols() {

		String symbols = "";
		int num_symbols = rndInt(QUOTES_PER_PAGE);

		for (int i = 0; i <= num_symbols; i++) {
			symbols += "s:" + rndInt(MAX_QUOTES - 1);
			if (i < num_symbols)
				symbols += ",";
		}
		return symbols;
	}

	public static String rndUserID() {
		String nextUser = getNextUserIDFromDeck();
		if (log.isTraceEnabled())
			log.trace("TradeConfig:rndUserID -- new trader = " + nextUser);

		return nextUser;
	}

	private static synchronized String getNextUserIDFromDeck() {
		int numUsers = getMAX_USERS();
		if (deck == null) {
			deck = new ArrayList(numUsers);
			for (int i = 0; i < numUsers; i++)
				deck.add(i, new Integer(i));
			java.util.Collections.shuffle(deck, r0);
		}
		if (card >= numUsers)
			card = 0;
		return "uid:" + deck.get(card++);

	}

	//Trade implements a card deck approach to selecting 
	// users for trading with tradescenarioservlet
	private static ArrayList deck = null;
	private static int card = 0;

	/**
	 * Set the list of run time mode names
	 * Creation date: (3/8/2000 5:58:34 PM)
	 * @param newRunTimeModeNames java.lang.String[]
	 */
	public static void setRunTimeModeNames(
		java.lang.String[] newRunTimeModeNames) {
		runTimeModeNames = newRunTimeModeNames;
	}
	/**
	 * This is a convenience method for servlets to set Trade configuration parameters
	 * from servlet initialization parameters. The servlet provides the init param and its
	 * value as strings. This method then parses the parameter, converts the value to the
	 * correct type and sets the corresponding TradeConfig parameter to the converted value
	 * 
	 */
	public static void setConfigParam(String parm, String value) {
	    if (log.isTraceEnabled())
	        log.trace("TradeConfig setting parameter: " + parm + "=" + value);
		// Compare the parm value to valid TradeConfig parameters that can be set
		// by servlet initialization

		// First check the proposed new parm and value - if empty or null ignore it
		if (parm == null)
			return;
		parm = parm.trim();
		if (parm.length() <= 0)
			return;
		if (value == null)
			return;
		value = value.trim();

		if (parm.equalsIgnoreCase("runTimeMode")) {
			try {
				for (int i = 0; i < runTimeModeNames.length; i++) {
					if (value.equalsIgnoreCase(runTimeModeNames[i])) {
						setRunTimeMode(i);
						break;
					}
				}
			} catch (Exception e) {
				//>>rjm
				log.error(
					"TradeConfig.setConfigParm(..): minor exception caught"
						+ "trying to set runtimemode to "
						+ value
						+ "reverting to current value: "
						+ runTimeModeNames[getRunTimeMode()],
					e);
			} // If the value is bad, simply revert to current
		} else if (parm.equalsIgnoreCase("orderProcessingMode")) {
			try {
				for (int i = 0; i < orderProcessingModeNames.length; i++) {
					if (value.equalsIgnoreCase(orderProcessingModeNames[i])) {
						orderProcessingMode = i;
						break;
					}
				}
			} catch (Exception e) {
				log.error(
					"TradeConfig.setConfigParm(..): minor exception caught"
						+ "trying to set orderProcessingMode to "
						+ value
						+ "reverting to current value: "
						+ orderProcessingModeNames[orderProcessingMode],
					e);
			} // If the value is bad, simply revert to current
		} else if (parm.equalsIgnoreCase("accessMode")) {		
			try {
				for (int i = 0; i < accessModeNames.length; i++) {
					if (value.equalsIgnoreCase(accessModeNames[i])) {
						accessMode = i;
						break;
					}
				}
			}
			catch (Exception e) {
				log.error(
					"TradeConfig.setConfigParm(..): minor exception caught"
						+ "trying to set accessMode to "
						+ value
						+ "reverting to current value: "
						+ accessModeNames[accessMode],
					e);
			}
		} else if (parm.equalsIgnoreCase("webServicesEndpoint")) {
			try {
				setSoapURL(value);
			} catch (Exception e) {
				log.error(
					"TradeConfig.setConfigParm(..): minor exception caught"
						+ "Setting web services endpoint",
					e);
			} //On error, revert to saved		
		} else if (parm.equalsIgnoreCase("workloadMix")) {
			try {
				for (int i = 0; i < workloadMixNames.length; i++) {
					if (value.equalsIgnoreCase(workloadMixNames[i])) {
						workloadMix = i;
						break;
					}
				}
			} catch (Exception e) {
				log.error(
					"TradeConfig.setConfigParm(..): minor exception caught"
						+ "trying to set workloadMix to "
						+ value
						+ "reverting to current value: "
						+ workloadMixNames[workloadMix],
					e);
			} // If the value is bad, simply revert to current		
		} else if (parm.equalsIgnoreCase("WebInterface")) {
			try {
				for (int i = 0; i < webInterfaceNames.length; i++) {
					if (value.equalsIgnoreCase(webInterfaceNames[i])) {
						webInterface = i;
						break;
					}
				}
			} catch (Exception e) {
				log.error(
					"TradeConfig.setConfigParm(..): minor exception caught"
						+ "trying to set WebInterface to "
						+ value
						+ "reverting to current value: "
						+ webInterfaceNames[webInterface],
					e);

			} // If the value is bad, simply revert to current
		} else if (parm.equalsIgnoreCase("CachingType")) {
			try {
				for (int i = 0; i < cachingTypeNames.length; i++) {
					if (value.equalsIgnoreCase(cachingTypeNames[i])) {
						cachingType = i;
						break;
					}
				}
			} catch (Exception e) {
				log.error(
					"TradeConfig.setConfigParm(..): minor exception caught"
						+ "trying to set CachingType to "
						+ value
						+ "reverting to current value: "
						+ cachingTypeNames[cachingType],
					e);
			} // If the value is bad, simply revert to current
		} else if (parm.equalsIgnoreCase("maxUsers")) {
			try {
				MAX_USERS = Integer.parseInt(value);
			} catch (Exception e) {
				log.error(
					"TradeConfig.setConfigParm(..): minor exception caught"
						+ "Setting maxusers, error parsing string to int:"
						+ value
						+ "revering to current value: "
						+ MAX_USERS,
					e);
			} //On error, revert to saved		
		} else if (parm.equalsIgnoreCase("maxQuotes")) {
			try {
				MAX_QUOTES = Integer.parseInt(value);
			} catch (Exception e) {
				//>>rjm
				log.error(
					"TradeConfig.setConfigParm(...) minor exception caught"
						+ "Setting max_quotes, error parsing string to int "
						+ value
						+ "reverting to current value: "
						+ MAX_QUOTES,
					e);
				//<<rjm
			} //On error, revert to saved		
		} else if (parm.equalsIgnoreCase("primIterations")) {
			try {
				primIterations = Integer.parseInt(value);
			} catch (Exception e) {
				log.error(
					"TradeConfig.setConfigParm(..): minor exception caught"
						+ "Setting primIterations, error parsing string to int:"
						+ value
						+ "revering to current value: "
						+ primIterations,
					e);
			} //On error, revert to saved
		}		
	}

	/**
	 * Gets the orderProcessingModeNames
	 * @return Returns a String[]
	 */
	public static String[] getOrderProcessingModeNames() {
		return orderProcessingModeNames;
	}

	/**
	 * Gets the workloadMixNames
	 * @return Returns a String[]
	 */
	public static String[] getWorkloadMixNames() {
		return workloadMixNames;
	}

	/**
	 * Gets the webInterfaceNames
	 * @return Returns a String[]
	 */
	public static String[] getWebInterfaceNames() {
		return webInterfaceNames;
	}

	/**
	 * Gets the webInterfaceNames
	 * @return Returns a String[]
	 */
	public static String[] getCachingTypeNames() {
		return cachingTypeNames;
	}

	/**
	 * Gets the scenarioMixes
	 * @return Returns a int[][]
	 */
	public static int[][] getScenarioMixes() {
		return scenarioMixes;
	}

	/**
	 * Gets the trace
	 * @return Returns a boolean
	 */
	public static boolean getTrace() {
		return trace;
	}
	/**
	 * Sets the trace
	 * @param trace The trace to set
	 */
	public static void setTrace(boolean traceValue) {
		trace = traceValue;
	}

	/**
	 * Gets the mAX_USERS.
	 * @return Returns a int
	 */
	public static int getMAX_USERS() {
		return MAX_USERS;
	}

	/**
	 * Sets the mAX_USERS.
	 * @param mAX_USERS The mAX_USERS to set
	 */
	public static void setMAX_USERS(int mAX_USERS) {
		MAX_USERS = mAX_USERS;
		deck = null; // reset the card deck for selecting users
	}

	/**
	 * Gets the mAX_QUOTES.
	 * @return Returns a int
	 */
	public static int getMAX_QUOTES() {
		return MAX_QUOTES;
	}

	/**
	 * Sets the mAX_QUOTES.
	 * @param mAX_QUOTES The mAX_QUOTES to set
	 */
	public static void setMAX_QUOTES(int mAX_QUOTES) {
		MAX_QUOTES = mAX_QUOTES;
	}

	/**
	 * Gets the mAX_HOLDINGS.
	 * @return Returns a int
	 */
	public static int getMAX_HOLDINGS() {
		return MAX_HOLDINGS;
	}

	/**
	 * Sets the mAX_HOLDINGS.
	 * @param mAX_HOLDINGS The mAX_HOLDINGS to set
	 */
	public static void setMAX_HOLDINGS(int mAX_HOLDINGS) {
		MAX_HOLDINGS = mAX_HOLDINGS;
	}

	/**
	 * Gets the actionTrace.
	 * @return Returns a boolean
	 */
	public static boolean getActionTrace() {
		return actionTrace;
	}

	/**
	 * Sets the actionTrace.
	 * @param actionTrace The actionTrace to set
	 */
	public static void setActionTrace(boolean actionTrace) {
		TradeConfig.actionTrace = actionTrace;
	}

	/**
	 * Gets the scenarioCount.
	 * @return Returns a int
	 */
	public static int getScenarioCount() {
		return scenarioCount;
	}

	/**
	 * Sets the scenarioCount.
	 * @param scenarioCount The scenarioCount to set
	 */
	public static void setScenarioCount(int scenarioCount) {
		TradeConfig.scenarioCount = scenarioCount;
	}

	public static synchronized void incrementScenarioCount() {
		scenarioCount++;
	}

	/**
	 * Gets the jdbc driver needs global transaction
	 * Some XA Drivers require a global transaction to be started
	 * for all SQL calls.  To work around this, set this to true
	 * to cause the direct mode to start a user transaction.
	 * @return Returns a boolean
	 */
	public static boolean getJDBCDriverNeedsGlobalTransaction() {
		return JDBCDriverNeedsGlobalTransaction;
	}

	/**
	 * Sets the jdbc driver needs global transaction
         * @param JDBCDriverNeedsGlobalTransactionVal the value
	 */
	public static void setJDBCDriverNeedsGlobalTransaction(boolean JDBCDriverNeedsGlobalTransactionVal) {
		JDBCDriverNeedsGlobalTransaction = JDBCDriverNeedsGlobalTransactionVal;
	}

	/**
	 * Gets the updateQuotePrices.
	 * @return Returns a boolean
	 */
	public static boolean getUpdateQuotePrices() {
		return updateQuotePrices;
	}

	/**
	 * Sets the updateQuotePrices.
	 * @param updateQuotePrices The updateQuotePrices to set
	 */
	public static void setUpdateQuotePrices(boolean updateQuotePrices) {
		TradeConfig.updateQuotePrices = updateQuotePrices;
	}
	
	public static String getSoapURL() {
		return SoapURL;
	}
	
	public static void setSoapURL(String value) {
		SoapURL = value;
//		TradeWebSoapProxy.updateServicePort();
	}
	
	public static int getAccessMode() {
		return accessMode;
	}
	
	public static void setAccessMode(int value) {
		accessMode = value;
//		TradeWebSoapProxy.updateServicePort();
	}

    public static int getRunTimeMode() {
        return runTimeMode;
    }
    
    public static void setRunTimeMode(int value) {
        runTimeMode = value;
    }

	public static int getPrimIterations() {
		return primIterations;
	}
	
	public static void setPrimIterations(int iter) {
		primIterations = iter;
	}	

    public static boolean getLongRun() {
        return longRun;
    }

    public static void setLongRun(boolean longRun) {
        TradeConfig.longRun = longRun;
    }

    public static void setPublishQuotePriceChange(boolean publishQuotePriceChange) {
        TradeConfig.publishQuotePriceChange = publishQuotePriceChange;
    }
    
    public static boolean getPublishQuotePriceChange() {
        return publishQuotePriceChange;
    }

    public static void setMarketSummaryInterval(int seconds) {
        TradeConfig.marketSummaryInterval = seconds;
    }
    
    public static  int getMarketSummaryInterval() {
        return TradeConfig.marketSummaryInterval;
    }
    
    /**
	 * Return the list of JPA Layer names
	 * Creation date: (01/10/2009)
	 * @return java.lang.String[]
	 */
	public static java.lang.String[] getJPALayerNames() {
		return jpaLayerNames;
    }

	// added by OpenJPA
	public static void setLog(Log log) {
	    TradeConfig.log = log;
	}
}
