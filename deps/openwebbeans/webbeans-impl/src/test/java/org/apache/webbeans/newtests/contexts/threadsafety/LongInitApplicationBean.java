/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.contexts.threadsafety;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * This sample bean takes a long time to initialize (sleep for 0.5 second)
 * Each time this bean gets created it will increase the {@link #initCounter}.
 */
@ApplicationScoped
public class LongInitApplicationBean
{
    private static int initCounter = 0;

    private final static Logger log = Logger.getLogger(LongInitApplicationBean.class.getName());

    @PostConstruct
    public void init()
    {
        log.info("starting LongInitApplicationBean");

        initCounter++;
        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        log.info("LongInitApplicationBean initialisation finished");
    }

    public int getI()
    {
        return initCounter;
    }

    public LongInitApplicationBean getThis()
    {
        return this;
    }
}
