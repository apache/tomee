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
package org.apache.openjpa.kernel;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.lib.util.concurrent.SizedConcurrentHashMap;

/**
 * Records query execution statistics.
 * 
 * Statistics can be reset.
 * 
 * Gathers both accumulated statistics since start as well as statistics since
 * last reset.
 *  
 * @since 1.3.0
 * 
 * @author Pinaki Poddar
 * 
 */
public interface QueryStatistics<T> extends Serializable {
    
    /**
     *  Gets all the identifier keys for the cached queries.
     */
    public Set<T> keys();
	
	/**
	 * Record that the given query has been executed. 
	 */
	void recordExecution(T query);

    /**
     * Record that the given query has been evicted. 
     */
    void recordEviction(T query);

	/**
	 * Gets number of total query execution since last reset.
	 */
	public long getExecutionCount();

	/**
	 * Gets number of total query execution since start.
	 */
	public long getTotalExecutionCount();

	/**
	 * Gets number of executions for the given query since last reset.
	 */
	public long getExecutionCount(T query);

	/**
	 * Gets number of executions for the given query since start.
	 */
	public long getTotalExecutionCount(T query);

	/**
     * Gets number of total query execution that are cached since last reset.
	 */
	public long getHitCount();

	/**
	 * Gets number of total query execution that are cached since start.
	 */
	public long getTotalHitCount();

	/**
	 * Gets number of executions for the given query that are cached since 
	 * last reset.
	 */
	public long getHitCount(T query);

	/**
	 * Gets number of executions for the given query that are cached since 
	 * start.
	 */
	public long getTotalHitCount(T query);

	 /**
     * Gets number of total query evictions since last reset.
     */
    public long getEvictionCount();
        
    /**
     * Gets number of total query evictions since start.
     */
    public long getTotalEvictionCount();

	/**
	 * Gets the time of last reset.
	 */
	public Date since();

	/**
	 * Gets the time of start.
	 */
	public Date start();

	/**
	 * Clears all  statistics accumulated since last reset.
	 */
	public void reset();
	
	/**
	 * Clears all statistics accumulated since start.
	 */
	public void clear();
	
	/**
	 * Dumps on the given output stream.
	 */
	public void dump(PrintStream out);
	
	/**
	 * A default implementation.
	 * 
	 * Maintains statistics for only a fixed number of queries.
	 * Statistical counts are approximate and not exact (to keep thread synchorization overhead low).
	 * 
	 */
	public static class Default<T> implements QueryStatistics<T> {
	    private static final int FIXED_SIZE = 1000;
	    private static final float LOAD_FACTOR = 0.75f;
	    private static final int CONCURRENCY = 16;
	    
		private static final int ARRAY_SIZE = 3;
        private static final int READ  = 0;
        private static final int HIT   = 1;
        private static final int EVICT = 2;
        
		private long[] astat = new long[ARRAY_SIZE];
		private long[] stat  = new long[ARRAY_SIZE];
		private Map<T, long[]> stats  = new SizedConcurrentHashMap(FIXED_SIZE, LOAD_FACTOR, CONCURRENCY);
		private Map<T, long[]> astats = new SizedConcurrentHashMap(FIXED_SIZE, LOAD_FACTOR, CONCURRENCY);
		private Date start = new Date();
		private Date since = start;
		
		public Set<T> keys() {
		    return stats.keySet();
		}

		public long getExecutionCount() {
			return stat[READ];
		}

		public long getTotalExecutionCount() {
			return astat[READ];
		}

		public long getExecutionCount(T query) {
			return getCount(stats, query, READ);
		}

		public long getTotalExecutionCount(T query) {
			return getCount(astats, query, READ);
		}

		public long getHitCount() {
			return stat[HIT];
		}

		public long getTotalHitCount() {
			return astat[HIT];
		}

		public long getHitCount(T query) {
			return getCount(stats, query, HIT);
		}

		public long getTotalHitCount(T query) {
			return getCount(astats, query, HIT);
		}

		private long getCount(Map<T, long[]> target, T query, int i) {
			long[] row = target.get(query);
			return (row == null) ? 0 : row[i];
		}

		public Date since() {
			return since;
		}

		public Date start() {
			return start;
		}

		public synchronized void reset() {
			stat = new long[ARRAY_SIZE];
			stats.clear();
			since = new Date();
		}
		
	    public synchronized void clear() {
	       astat = new long[ARRAY_SIZE];
	       stat  = new long[ARRAY_SIZE];
	       stats = new SizedConcurrentHashMap(FIXED_SIZE, LOAD_FACTOR, CONCURRENCY);
	       astats = new SizedConcurrentHashMap(FIXED_SIZE, LOAD_FACTOR, CONCURRENCY);
	       start  = new Date();
	       since  = start;
	    }


		private void addSample(T query, int index) {
			stat[index]++;
			astat[index]++;
			addSample(stats, query, index);
			addSample(astats, query, index);
		}
		
		private void addSample(Map<T, long[]> target, T query, int i) {
			long[] row = target.get(query);
			if (row == null) {
				row = new long[ARRAY_SIZE];
			}
			row[i]++;
			target.put(query, row);
		}
		
		public void recordExecution(T query) {
		    if (query == null)
		        return;
		    boolean cached = astats.containsKey(query);
			addSample(query, READ);
			if (cached)
				addSample(query, HIT);
		}
		
        public void recordEviction(T query) {
            if (query == null) {
                return;
            }
            addSample(query, EVICT);
        }

		public void dump(PrintStream out) {
            String header = "Query Statistics starting from " + start;
			out.print(header);
			if (since == start) {
				out.println();
                out.println("Total Query Execution: " + toString(astat)); 
				out.println("\tTotal \t\tQuery");
			} else {
				out.println(" last reset on " + since);
                out.println("Total Query Execution since start " + 
                        toString(astat)  + " since reset " + toString(stat));
                out.println("\tSince Start \tSince Reset \t\tQuery");
			}
			int i = 0;
			for (T key : stats.keySet()) {
				i++;
				long[] arow = astats.get(key);
				if (since == start) {
                    out.println(i + ". \t" + toString(arow) + " \t" + key);
				} else {
					long[] row  = stats.get(key);
                    out.println(i + ". \t" + toString(arow) + " \t"  + toString(row) + " \t\t" + key);
				}
			}
		}
		
		long pct(long per, long cent) {
			if (cent <= 0)
				return 0;
			return (100*per)/cent;
		}
		
		String toString(long[] row) {
            return row[READ] + ":" + row[HIT] + "(" + pct(row[HIT], row[READ]) + "%)";
		}

        public long getEvictionCount() {
            return stat[EVICT];
        }

        public long getTotalEvictionCount() {
            return astat[EVICT];
        }
	}
	
	/**
	 * A do-nothing implementation.
	 * 
	 * @author Pinaki Poddar
	 *
	 * @param <T>
	 */
	public static class None<T> implements QueryStatistics<T> {
        private Date start = new Date();
        private Date since = start;

        public void clear() {
        }

        public void dump(PrintStream out) {
        }

        public long getExecutionCount() {
            return 0;
        }

        public long getExecutionCount(T query) {
            return 0;
        }

        public long getHitCount() {
            return 0;
        }

        public long getHitCount(T query) {
            return 0;
        }

        public long getTotalExecutionCount() {
            return 0;
        }

        public long getTotalExecutionCount(T query) {
            return 0;
        }

        public long getTotalHitCount() {
            return 0;
        }

        public long getTotalHitCount(T query) {
            return 0;
        }

        public long getEvictionCount() {
            return 0;
        }

        public long getTotalEvictionCount() {
            return 0;
        }

        public Set<T> keys() {
            return Collections.emptySet();
        }

        public void recordExecution(T query) {
        }

        public void reset() {
            start  = new Date();
            since  = start;
        }

        public Date since() {
            return since;
        }

        public Date start() {
            return start;
        }

        public void recordEviction(T query) {
        }
	}
}

