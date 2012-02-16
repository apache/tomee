/**
 *
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

/**
 * This is the application controller. This is the central point for logic and to forward actions to the views.
 * It contains all the views and model instances.
 */
TOMEE.ApplicationController = function () {
    "use strict";

    //the application communication channel
    //The views communicate with the controller (or other components) through this object
    var channel = TOMEE.ApplicationChannel({});

    //this object handles all the data manipulation.
    var testPanelModel = TOMEE.TestModel({
        channel: channel
    });

    var jndiPanelModel = TOMEE.JndiModel({
        channel: channel
    });

    var view = TOMEE.ApplicationView({
        channel: channel,
        testModel: testPanelModel,
        jndiModel: jndiPanelModel
    });

    //The user clicked in one of the buttons in the application toolbar
    channel.bind('toolbar_button_executed', function (params) {
        var a = 0;

    });

    //The user clicked in one of the items in the home panel
    channel.bind('home_menu_executed', function (params) {
        var menuKey = params.menu;
        view.getHome().getBody().showPanel(menuKey);
    });

    //a panel is about to be removed from the view
    //you have a chance to do some closure here (stop Ajax calls, for example)
    channel.bind('hiding_panel', function (params) {
        var panel = params.panel;
        //placeholder
    });

    //"test" -> data loaded event
    channel.bind('test_connection_exception', function (params) {

    });
    channel.bind('test_connection_new_data', function (params) {
        view.getHome().getBody().loadData('test');
    });

    //"jndi" -> data loaded event
    channel.bind('jndi_connection_exception', function (params) {

    });
    channel.bind('jndi_connection_new_data', function (params) {
        view.getHome().getBody().loadData('jndi');
    });

    channel.bind('panel_show', function (params) {
        var panel = params.panel;
        panel.getMyModel().load();
    });

    view.render(function () {
        view.getHome().getMenu().selectMenu('test');
    });

    return {

    };
};