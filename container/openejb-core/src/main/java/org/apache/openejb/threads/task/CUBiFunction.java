/*
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
package org.apache.openejb.threads.task;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.threads.impl.ContextServiceImpl;

import java.util.function.BiFunction;

public class CUBiFunction<T, U, R> extends CUTask<R> implements BiFunction<T, U, R> {
    private final BiFunction<T, U, R> delegate;

    public CUBiFunction(BiFunction<T, U, R> task, ContextServiceImpl contextService) {
        super(task, contextService);

        this.delegate = task;
    }

    @Override
    public R apply(T t, U u) {
        try {
            return invoke(() -> delegate.apply(t, u));
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }
}
