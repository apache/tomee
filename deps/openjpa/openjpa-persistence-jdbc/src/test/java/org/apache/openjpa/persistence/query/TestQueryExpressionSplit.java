/*
 * TestQueryExpressionSplit.java
 *
 * Created on October 18, 2006, 1:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
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
package org.apache.openjpa.persistence.query;

import java.util.Arrays;
import java.util.List;




import org.apache.openjpa.kernel.Filters;

public class TestQueryExpressionSplit extends BaseQueryTest {

    /**
     * Creates a new instance of TestQueryExpressionSplit
     */

    public TestQueryExpressionSplit(String test) {
        super(test);
    }

    public void testSimple() {
        assertEquals(new String[]{ "foo() bar(boo)" },
            Filters.splitExpressions("foo() bar(boo)", ',', 3));
        assertEquals(new String[]{ "foo() bar(boo)", "biz()", "baz(boo)" },
            Filters.splitExpressions("foo() bar(boo), biz(), baz(boo)",
                ',', 3));
    }

    public void testCommaInString() {
        assertEquals(new String[]{ "foo \"bar(),biz)\"" },
            Filters.splitExpressions("foo \"bar(),biz)\"", ',', 3));
        assertEquals(new String[]{ "foo 'bar(),\"biz)'", "boo" },
            Filters.splitExpressions("foo 'bar(),\"biz)', boo", ',', 3));
    }

    public void testCommaInFunction() {
        assertEquals(new String[]{ "(foo(bar, biz))",
            "boo(biz, baz('xxx,yyy'))" },
            Filters.splitExpressions("(foo(bar, biz)), "
                + "boo(biz, baz('xxx,yyy'))", ',', 3));
    }

    public void testEscapedString() {
        assertEquals(new String[]{ "foo \"bar\\\", biz(\"",
            "\"baz\\\", boo\"" },
            Filters.splitExpressions("foo \"bar\\\", biz(\", "
                + "\"baz\\\", boo\"", ',', 3));
    }

    private void assertEquals(String[] ans, List test) {
        List l = Arrays.asList(ans);
        assertEquals(l + " != " + test, l, test);
    }
}
