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

    var model = TOMEE.ApplicationModel({
        channel:channel
    });

    var logView = TOMEE.ApplicationViewLog({
        channel:channel
    });

    var appsView = TOMEE.ApplicationViewApps({
        channel:channel
    });

    var homeView = TOMEE.ApplicationViewHome({
        channel:channel
    });

    channel.bind('application.name.click', function (params) {
        window.open('http://openejb.apache.org/', 'OpenEJB');
    });

    channel.bind('application.logout', function (params) {
        model.logout();
    });

    channel.bind('tree_leaf_click', function (params) {
        //params.panelKey, params.bean
        if (params.panelKey === 'jndi') {

        }
    });

    channel.bind('tree_load_children', function (params) {
        //params.panelKey, params.bean, params.parentEl
        if (params.panelKey === 'jndi') {

        }
    });

    channel.bind('deploy.file.uploaded', function (params) {
        model.deployApp(params.file);
    });

    channel.bind('app.deployment.result', function (params) {
        alert('appId: ' + params.appId + '; path: ' + params.path + ';');
    });

    channel.bind('app.logout.bye', function (params) {
        window.location.reload();
    });

    channel.bind('app.system.info', function (params) {
        view.setLoggedUser(params.user);
        homeView.setSupportedScriptLanguages(params.supportedScriptLanguages);
    });

    channel.bind('trigger.console.exec', function (params) {
        model.execute(params.codeType, params.codeText);
    });

    channel.bind('app.console.executed', function (params) {
        //TODO Implement me
        throw "app.console.executed not implemented";
    });

    channel.bind('app.new.log.data', function (params) {
        logView.loadData(params);
    });

    channel.bind('trigger.log.load', function (params) {
        model.loadLog(params.file, params.tail);
    });

    channel.bind('app.new.session.data', function (params) {
        homeView.loadSavedObjects(params);
    });

    channel.bind('app.new.jndi.data', function (params) {
        homeView.loadJndi(params);
    });

    channel.bind('application.saved.objects.load', function (params) {
        model.loadSessionData();
    });

    var view = TOMEE.ApplicationView({
        channel:channel,
        groups:{
            'home':homeView,
            'apps':appsView,
            'log':logView
        },
        initTab:'home'
    });

    model.loadSystemInfo(function (data) {
        view.setTomeeVersion(data.tomee);
        homeView.setTomeeVersion(data.tomee);
        view.render();
    });

    model.loadLog(null, null);
    model.loadJndi("");

    return {

    };
};