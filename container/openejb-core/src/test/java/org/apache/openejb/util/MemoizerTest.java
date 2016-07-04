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
package org.apache.openejb.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MemoizerTest {
    @Test
    public void itLeaksByDesignWithWrongEqualsHashCodeAsKey() throws InterruptedException {
        final Memoizer<Object[], String> objectArrayCache = new Memoizer<>(new Computable<Object[], String>() {
            @Override
            public String compute(final Object[] key) throws InterruptedException {
                return key[0].toString();
            }
        });
        objectArrayCache.compute(new Object[]{"1"});
        assertEquals(1, objectArrayCache.getCache().size());

        // this is a leak but the way it works, NEVER use an array as key without wrapping it worse case in a list!
        objectArrayCache.compute(new Object[]{"1"});
        assertEquals(2, objectArrayCache.getCache().size());
    }

    @Test
    public void noLeakWithLoggerKey() throws InterruptedException {
        final Memoizer<Logger.LoggerKey, String> objectArrayCache = new Memoizer<>(new Computable<Logger.LoggerKey, String>() {
            @Override
            public String compute(final Logger.LoggerKey key) throws InterruptedException {
                return key.baseName;
            }
        });
        objectArrayCache.compute(new Logger.LoggerKey(LogCategory.OPENEJB.createChild("test"), "MemoizerTest"));
        assertEquals(1, objectArrayCache.getCache().size());

        // this is a leak but the way it works
        objectArrayCache.compute(new Logger.LoggerKey(LogCategory.OPENEJB.createChild("test"), "MemoizerTest") /*new instance*/);
        assertEquals(1, objectArrayCache.getCache().size());
    }
}
