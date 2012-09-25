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
package org.apache.webbeans.newtests.profields.beans.stringproducer;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

public class MultipleListProducer
{
    @Produces @SessionScoped List<String> produceList1 = new ArrayList<String>();
    @Produces @SessionScoped List<Integer> produceList2 = new ArrayList<Integer>(); 
    
    @Produces @SessionScoped @Named(value="name1") List<Double> producerList3 = new ArrayList<Double>();
    @Produces @SessionScoped @Named(value="name2") List<Double> producerList4 = new ArrayList<Double>();
}
