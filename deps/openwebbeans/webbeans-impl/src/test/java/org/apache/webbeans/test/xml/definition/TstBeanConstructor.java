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
package org.apache.webbeans.test.xml.definition;

/**
 * This test WebBean has a constructor which should be injected via XML
 */
public class TstBeanConstructor
{
    private int val1 = 0;
    private int val2 = 0;

    public TstBeanConstructor()
    {

    }

    public TstBeanConstructor(CtParameter ctParam)
    {
        this.val1 = ctParam.getValue() * 100;
    }

    public TstBeanConstructor(Integer ctParam)
    {
        this.val1 = ctParam.intValue() * 100;
    }

    public TstBeanConstructor(int ctParam, int multiplier)
    {
        this.val1 = ctParam * multiplier;
    }

    public int getVal1()
    {
        return val1;
    }

    public void setVal1(int val1)
    {
        this.val1 = val1;
    }

    public int getVal2()
    {
        return val2;
    }

    public void setVal2()
    {
        this.val2 = 40;
    }
}
