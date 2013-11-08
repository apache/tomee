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
package org.apache.openjpa.slice.jdbc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.kernel.JDBCStoreQuery;
import org.apache.openjpa.kernel.ExpressionStoreQuery;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OrderingMergedResultObjectProvider;
import org.apache.openjpa.kernel.QueryContext;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.lib.rop.MergedResultObjectProvider;
import org.apache.openjpa.lib.rop.RangeResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.slice.DistributedConfiguration;
import org.apache.openjpa.slice.SliceThread;
import org.apache.openjpa.util.StoreException;

/**
 * A query for distributed databases.
 * 
 * @author Pinaki Poddar
 * 
 */
@SuppressWarnings("serial")
class DistributedStoreQuery extends JDBCStoreQuery {
	private List<StoreQuery> _queries = new ArrayList<StoreQuery>();
	private ExpressionParser _parser;

	public DistributedStoreQuery(JDBCStore store, ExpressionParser parser) {
		super(store, parser);
		_parser = parser;
	}

	void add(StoreQuery q) {
		_queries.add(q);
	}

	public DistributedJDBCStoreManager getDistributedStore() {
		return (DistributedJDBCStoreManager) getStore();
	}

	public Executor newDataStoreExecutor(ClassMetaData meta, boolean subs) {
		boolean parallel = !getContext().getStoreContext().getBroker()
			.getMultithreaded();
        ParallelExecutor ex = new ParallelExecutor(this, meta, subs, _parser,
			ctx.getCompilation(), parallel);
		for (StoreQuery q : _queries) {
			ex.addExecutor(q.newDataStoreExecutor(meta, subs));
		}
		return ex;
	}

	public void setContext(QueryContext ctx) {
		super.setContext(ctx);
		for (StoreQuery q : _queries)
			q.setContext(ctx);
	}

	/**
	 * Executes queries on multiple databases.
	 * 
	 * @author Pinaki Poddar
	 * 
	 */
	public static class ParallelExecutor extends
			ExpressionStoreQuery.DataStoreExecutor {
		private List<Executor> executors = new ArrayList<Executor>();
		private DistributedStoreQuery owner = null;

        public ParallelExecutor(DistributedStoreQuery dsq, ClassMetaData meta,
                boolean subclasses, ExpressionParser parser, Object parsed, 
				boolean parallel) {
			super(dsq, meta, subclasses, parser, parsed);
			owner = dsq;
		}

		public void addExecutor(Executor ex) {
			executors.add(ex);
		}

		/**
         * Each child query must be executed with slice context and not the
		 * given query context.
		 */
		public ResultObjectProvider executeQuery(StoreQuery q,
				final Object[] params, final Range range) {
			List<Future<ResultObjectProvider>> futures = new ArrayList<Future<ResultObjectProvider>>();
            final List<Executor> usedExecutors = new ArrayList<Executor>();
			final List<ResultObjectProvider> rops = new ArrayList<ResultObjectProvider>();
			List<SliceStoreManager> targets = findTargets();
			QueryContext ctx = q.getContext();
			boolean isReplicated = containsReplicated(ctx);
            ExecutorService threadPool = SliceThread.getPool();
           
			for (int i = 0; i < owner._queries.size(); i++) {
                // if replicated, then execute only on single slice
				if (isReplicated && !usedExecutors.isEmpty()) {
					break;
				}
                StoreManager sm = owner.getDistributedStore().getSlice(i);
				if (!targets.contains(sm))
					continue;
                QueryExecutor call = new QueryExecutor();
                call.executor = executors.get(i);
                call.query = owner._queries.get(i);
                call.params = params;
                call.range = range;
				usedExecutors.add(call.executor);
                futures.add(threadPool.submit(call));
			}
			for (Future<ResultObjectProvider> future : futures) {
				try {
					rops.add(future.get());
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (ExecutionException e) {
					throw new StoreException(e.getCause());
				}
			}
			
			ResultObjectProvider[] tmp = rops.toArray(new ResultObjectProvider[rops.size()]);
			ResultObjectProvider result = null;
			boolean[] ascending = getAscending(q);
			boolean isAscending = ascending.length > 0;
			boolean isAggregate = ctx.isAggregate();
			boolean hasRange = ctx.getEndRange() != Long.MAX_VALUE;
			if (isAggregate) {
				result = new UniqueResultObjectProvider(tmp, q,	getQueryExpressions());
			} else if (isAscending) {
                result = new OrderingMergedResultObjectProvider(tmp, ascending,
                    usedExecutors.toArray(new Executor[usedExecutors.size()]),
					q, params);
			} else {
				result = new MergedResultObjectProvider(tmp);
			}
			if (hasRange) {
                result = new RangeResultObjectProvider(result, ctx.getStartRange(), ctx.getEndRange());
			}
			return result;
		}

		/**
         * Scans metadata to find out if a replicated class is the candidate.
		 */
		boolean containsReplicated(QueryContext query) {
			Class<?> candidate = query.getCandidateType();
			DistributedConfiguration conf = (DistributedConfiguration)query.getStoreContext()
			    .getConfiguration();
			if (candidate != null) {
			    return conf.isReplicated(candidate);
			}
			ClassMetaData[] metas = query.getAccessPathMetaDatas();
			if (metas == null || metas.length < 1)
				return false;
			for (ClassMetaData meta : metas)
				if (conf.isReplicated(meta.getDescribedType()))
					return true;
			return false;
		}

		public Number executeDelete(StoreQuery q, Object[] params) {
			List<Future<Number>> futures = new ArrayList<Future<Number>>();
			int result = 0;
            ExecutorService threadPool = SliceThread.getPool();
			List<SliceStoreManager> targets = findTargets();
			for (int i = 0; i < owner._queries.size(); i++) {
                StoreManager sm = owner.getDistributedStore().getSlice(i);
				if (!targets.contains(sm))
					continue;
				
				DeleteExecutor call = new DeleteExecutor();
				call.executor = executors.get(i);
				call.query = owner._queries.get(i);
				call.params = params;
				futures.add(threadPool.submit(call));
			}
			for (Future<Number> future : futures) {
				try {
					Number n = future.get();
					if (n != null)
						result += n.intValue();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (ExecutionException e) {
					throw new StoreException(e.getCause());
				}
			}
			return result;
		}

		public Number executeUpdate(StoreQuery q, Object[] params) {
			Iterator<StoreQuery> qs = owner._queries.iterator();
			List<Future<Number>> futures = null;
			int result = 0;
            ExecutorService threadPool = SliceThread.getPool();
			for (Executor ex : executors) {
				if (futures == null)
                    futures = new ArrayList<Future<Number>>();
				UpdateExecutor call = new UpdateExecutor();
				call.executor = ex;
				call.query = qs.next();
				call.params = params;
				futures.add(threadPool.submit(call));
			}
			for (Future<Number> future : futures) {
				try {
					Number n = future.get();
                    result += (n == null) ? 0 : n.intValue();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (ExecutionException e) {
					throw new StoreException(e.getCause());
				}
			}
			return result;
		}

		List<SliceStoreManager> findTargets() {
  		    FetchConfiguration fetch = owner.getContext().getFetchConfiguration();
			return owner.getDistributedStore().getTargets(fetch);
		}
		
	}

	static class QueryExecutor implements Callable<ResultObjectProvider> {
		StoreQuery query;
		Executor executor;
		Object[] params;
		Range range;

		public ResultObjectProvider call() throws Exception {
			return executor.executeQuery(query, params, range);
		}
	}

	static class DeleteExecutor implements Callable<Number> {
		StoreQuery query;
		Executor executor;
		Object[] params;

		public Number call() throws Exception {
			return executor.executeDelete(query, params);
		}
	}

	static class UpdateExecutor implements Callable<Number> {
		StoreQuery query;
		Executor executor;
		Object[] params;

		public Number call() throws Exception {
		    return executor.executeUpdate(query, params);
		}
	}
}
