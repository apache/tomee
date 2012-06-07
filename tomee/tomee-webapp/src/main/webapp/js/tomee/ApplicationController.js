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

    homeView.loadJndi([
        { text:TOMEE.Sequence.next('a'), children:[] },
        { text:TOMEE.Sequence.next('a') },
        { text:TOMEE.Sequence.next('a') },
        { text:TOMEE.Sequence.next('a') },
        { text:TOMEE.Sequence.next('a') },
        { text:TOMEE.Sequence.next('a'), children:[
            { text:TOMEE.Sequence.next('a') },
            { text:TOMEE.Sequence.next('a'), children:[
                { text:TOMEE.Sequence.next('a') },
                { text:TOMEE.Sequence.next('a'), children:[
                    { text:TOMEE.Sequence.next('a') }
                ] }
            ] }
        ] },
        { text:TOMEE.Sequence.next('a'), children:[] },
        { text:TOMEE.Sequence.next('a'), children:[] },
        { text:TOMEE.Sequence.next('a'), children:[
            { text:TOMEE.Sequence.next('a') },
            { text:TOMEE.Sequence.next('a'), children:[
                { text:TOMEE.Sequence.next('a') },
                { text:TOMEE.Sequence.next('a'), children:[
                    { text:TOMEE.Sequence.next('a') }
                ] }
            ] }
        ] }
    ]);

    homeView.loadSavedObjects([
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') },
        { name:TOMEE.Sequence.next('name'), value:TOMEE.Sequence.next('value') }

    ]);

    channel.bind('application.name.click', function (params) {
        window.open('http://openejb.apache.org/', 'OpenEJB');
    });

    channel.bind('application.logout', function (params) {
        model.logout();
    });

    channel.bind('jndi_leaf_click', function (params) {
        alert('name: ' + params.bean.text);
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
    });

    channel.bind('trigger.console.exec', function (params) {
        model.execute(params.codeType, params.codeText);
    });

    channel.bind('app.console.executed', function (params) {
        //TODO Implement me
        throw "app.console.executed not implemented";
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

    model.loadSystemInfo();

    return {

    };
};