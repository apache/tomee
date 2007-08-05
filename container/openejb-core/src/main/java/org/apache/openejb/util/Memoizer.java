/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Memoizer<K, V> implements Computable<K, V> {
	private final ConcurrentMap<K, Future<V>> cache = new ConcurrentHashMap<K, Future<V>>();

	private final Computable<K, V> c;

	public Memoizer(Computable<K, V> c) {
		this.c = c;
	}

	public V compute(final K key) throws InterruptedException {
		while (true) {
			Future<V> future = cache.get(key);
			if (future == null) {

				Callable<V> eval = new Callable<V>() {
					public V call() throws Exception {
						return c.compute(key);
					}
				};
				FutureTask<V> futureTask = new FutureTask<V>(eval);
				future = cache.putIfAbsent(key, futureTask);
				if (future == null) {
					future = futureTask;
					futureTask.run();
				}
			}
			try {
				return future.get();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
}
