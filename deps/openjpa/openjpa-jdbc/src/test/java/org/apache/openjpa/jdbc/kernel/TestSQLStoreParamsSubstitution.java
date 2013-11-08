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
package org.apache.openjpa.jdbc.kernel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * <p>
 * Tests SQLStoreQuery.substituteParams() behavior.
 * </p>
 */

public class TestSQLStoreParamsSubstitution extends TestCase {

    /**
     * Tests parameter substitution algorithm to make sure the input sql is NOT transformed
     * especially escape characters.
     */
    public void testParamSubstitute() {

        String sqlNrtns[][] = { 
                {
                    "like '5@%' escape '@'",
                    "[]"
                },
//                {
//                    "like '%#####_#%%' escape '#' WHERE ? = 'ab",
//                    "[1]"
//                },
                {
            		"SELECT 'TRUE' AS VAL FROM DUAL WHERE '\\' = ? AND 'Y' = 'Y'",
                    "[1]"
                },
                {
                    "like '*_n' escape '*' WHERE x = ? and y = ?",
                    "[1, 2]"
                },
                {
                    "like '%80@%%' escape '@' WHERE x = ? and y = ? or z=?" ,
                    "[1, 2, 3]"
                },
                {
                    "like '*_sql**%' escape '*" ,
                    "[]"
                },
                {   "SELECT 'TRUE' AS VAL FROM DUAL WHERE '\\' = ?",
                    "[1]",
                },
                {  "SELECT * FROM ("
                     + "SELECT FOLDER_ID, SYS_CONNECT_BY_PATH(NAME,'\\') AS PATH FROM PROJECT_FOLDER "
                     + "START WITH PARENT_ID IS NULL CONNECT BY PRIOR FOLDER_ID = PARENT_ID"
                     + ") WHERE PATH LIKE ?",
                   "[1]"
                }
            };
        try {
            List<Integer> paramOrder = new ArrayList<Integer>();

            for (String sqlNrtn[] : sqlNrtns) {
                paramOrder.clear();
                String rtnSql = SQLStoreQuery.substituteParams(sqlNrtn[0], paramOrder);
                assertEquals(sqlNrtn[0], rtnSql);
                assertEquals(sqlNrtn[1], paramOrder.toString());
            }
        } catch (IOException e) {
            fail("Unexpected Exception:" + e);
        }
    }
}
