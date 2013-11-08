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
package org.apache.openjpa.trader.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * A bundle  of resources (a couple of tiny images for now). However, GWT framework
 * seems to have generalized and added performance boost to handle heavy resources
 * such as images to be loaded from the web server. 
 * <br>
 * This sample application does not explore this feature of GWT, only other than
 * applying a singleton pattern. 
 * 
 * @author Pinaki Poddar
 *
 */
public interface OpenTradeImageBundle extends ClientBundle {
    
    @Source("images/login.gif")
    public ImageResource login();
    
    @Source("images/logo.gif")
    public ImageResource logo();
}
