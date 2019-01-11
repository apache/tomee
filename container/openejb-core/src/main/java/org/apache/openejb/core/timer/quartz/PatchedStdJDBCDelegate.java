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

package org.apache.openejb.core.timer.quartz;

import org.apache.openejb.quartz.impl.jdbcjobstore.StdJDBCDelegate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PatchedStdJDBCDelegate extends StdJDBCDelegate {
    @Override
    protected Object getObjectFromBlob(final ResultSet rs, final String colName)
        throws ClassNotFoundException, IOException, SQLException {
        Object obj = null;

        final Blob blobLocator = rs.getBlob(colName);
        if (blobLocator != null && blobLocator.length() != 0) {
            final InputStream binaryInput = blobLocator.getBinaryStream();

            if (null != binaryInput) {
                if (!(binaryInput instanceof ByteArrayInputStream) || ((ByteArrayInputStream) binaryInput).available() != 0) {
                    try (ObjectInputStream in = new QuartzObjectInputStream(binaryInput, classLoadHelper)) {
                        obj = in.readObject();
                    }
                }
            }

        }
        return obj;
    }
}

