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
package org.apache.openejb.client;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;
public class BlacklistClassResolverTest {
    @Test
    public void isBlacklisted() throws Exception {
        Assert.assertFalse(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[[B"));
        Assert.assertFalse(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[[C"));
        Assert.assertFalse(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[[D"));
        Assert.assertFalse(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[[F"));
        Assert.assertFalse(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[[I"));
        Assert.assertFalse(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[[J"));
        Assert.assertFalse(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[[S"));
        Assert.assertFalse(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[[V"));
        Assert.assertFalse(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[[Z"));
        Assert.assertTrue(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("[Ljava.lang.Process;"));
        Assert.assertTrue(new EjbObjectInputStream.BlacklistClassResolver().isBlacklisted("java.lang.Process;"));
    }

}
