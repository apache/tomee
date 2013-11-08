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
package org.apache.openjpa.trader.service.slice;

import java.util.List;
import java.util.Map;

import org.apache.openjpa.slice.QueryTargetPolicy;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.service.TradingService;

/**
 * An example of a {@link QueryTargetPolicy query target policy} that directs the query based
 * on its parameters.
 * 
 * @author Pinaki Poddar
 *
 */
public class SectorBasedQueryTargetPolicy implements QueryTargetPolicy {

	@Override
	public String[] getTargets(String query, Map<Object, Object> params,
			String language, List<String> slices, Object context) {        
		Stock stock = null;
		if (TradingService.MATCH_ASK.equals(query)) {
			stock = ((Tradable)params.get("ask")).getStock();
		} else if (TradingService.MATCH_BID.equals(query)) {
			stock = ((Tradable)params.get("bid")).getStock();
		}
        return stock != null ? new String[]{slices.get(stock.getSector().ordinal())} : null;
	}

}
