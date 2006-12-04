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
package org.openejb;

import org.apache.openejb.spi.ApplicationServer;

import javax.transaction.TransactionManager;

/**
 * @version $Revision: 454211 $ $Date: 2006-10-08 14:09:12 -0700 (Sun, 08 Oct 2006) $
 * @deprecated use org.apache.openejb.OpenEJB
 */
public final class OpenEJB {
    public static ApplicationServer getApplicationServer() {
        return org.apache.openejb.OpenEJB.getApplicationServer();
    }

    public static TransactionManager getTransactionManager(){
        return org.apache.openejb.OpenEJB.getTransactionManager();
    }

}
