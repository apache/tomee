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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import junit.framework.TestCase;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @version $Rev$ $Date$
 */
public class DurationTest extends TestCase {

    public void test() throws Exception {

        assertEquals(new Duration(1000, MILLISECONDS), new Duration("1000ms"));
        assertEquals(new Duration(1000, MILLISECONDS), new Duration("1000 ms"));
        assertEquals(new Duration(1000, MILLISECONDS), new Duration("1000  ms"));

        assertEquals(new Duration(60, SECONDS), new Duration("1m"));
        assertEquals(new Duration(3600, SECONDS), new Duration("1h"));
        assertEquals(new Duration(86400, SECONDS), new Duration("1d"));

        assertEquals(new Duration(1000, MICROSECONDS), new Duration("1000 microseconds"));
        assertEquals(new Duration(1000, NANOSECONDS), new Duration("1000 nanoseconds"));

        assertEquals(new Duration(1, null), new Duration("1"));
        assertEquals(new Duration(234, null), new Duration("234"));
        assertEquals(new Duration(123, null), new Duration("123"));
        assertEquals(new Duration(-1, null), new Duration("-1"));
    }

    public void testUnitConversion() throws Exception {
        assertEquals(2 * 1000, MILLISECONDS.convert(2, SECONDS));
        assertEquals(2 * 1000 * 1000, MICROSECONDS.convert(2, SECONDS));
        assertEquals(2 * 1000 * 1000 * 1000, NANOSECONDS.convert(2, SECONDS));

        assertEquals(2, SECONDS.convert(2 * 1000 * 1000 * 1000, NANOSECONDS));
        assertEquals(2, SECONDS.convert(2 * 1000 * 1000, MICROSECONDS));
        assertEquals(2, SECONDS.convert(2 * 1000, MILLISECONDS));

        // The verbose way of doing the above
        assertEquals(2 * 1000, new Duration(2, SECONDS).getTime(MILLISECONDS));
        assertEquals(2 * 1000 * 1000, new Duration(2, SECONDS).getTime(MICROSECONDS));
        assertEquals(2 * 1000 * 1000 * 1000, new Duration(2, SECONDS).getTime(NANOSECONDS));

        assertEquals(2, new Duration(2 * 1000 * 1000 * 1000, NANOSECONDS).getTime(SECONDS));
        assertEquals(2, new Duration(2 * 1000 * 1000, MICROSECONDS).getTime(SECONDS));
        assertEquals(2, new Duration(2 * 1000, MILLISECONDS).getTime(SECONDS));
    }

    public void testMultiple() throws Exception {

        assertEquals(new Duration(2300, MILLISECONDS), Duration.parse("2 seconds and 300 milliseconds"));
        assertEquals(new Duration(2300, MILLISECONDS), Duration.parse("2 seconds, 300 milliseconds"));
        assertEquals(new Duration(2300, MILLISECONDS), Duration.parse("2 seconds,300 milliseconds"));

        assertEquals(new Duration(125, SECONDS), Duration.parse("2 minutes and 5 seconds"));

    }
}
