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


    var deployments = TOMEE.Applications({
        channel:channel
    });

    var deploymentsLog = TOMEE.ApplicationsLog({
        channel:channel
    });

    var appsView = TOMEE.ApplicationViewApps({
        leftPanel:deployments,
        centerPanel:deploymentsLog
    });


    var jndiPanel = TOMEE.Jndi({
        channel:channel
    });

    var savedPanel = TOMEE.Saved({
        channel:channel
    });

    var mdbsPanel = TOMEE.MDBs({
        channel:channel
    });

    var wsPanel = TOMEE.WebServices({
        channel:channel
    });

    var consolePanel = TOMEE.Console({
        channel:channel
    });

    var homeView = TOMEE.ApplicationViewHome({
        center:consolePanel,
        children:[
            jndiPanel, savedPanel

            //TODO mdbsPanel and wsPanel are not implemented yet. Add them when done.
            //, mdbsPanel, wsPanel
        ]
    });

    channel.bind('default.ajax.error.handler.triggered', function (params) {
        TOMEE.ErrorPanel({
            channel:channel
        }).show(params);
    });

    (function () {
        channel.bind('application.name.click', function (params) {
            window.open('http://openejb.apache.org/', 'OpenEJB');
        });

        channel.bind('application.logout', function (params) {
            model.logout();
        });

        channel.bind('app.logout.bye', function (params) {
            window.location.reload();
        });
    })();

    //JNDI tree
    (function () {
        channel.bind('tree_leaf_click', function (params) {
            //params.panelKey, params.bean
            if (params.panelKey === 'jndi') {

            }
        });

        var pathArrayBuilder = (function () {
            var path = [];
            var buildPathArray = function (bean) {
                if (!bean) {
                    return;
                }

                if (bean.parent) {
                    buildPathArray(bean.parent);

                    path.push(bean.parent.name);
                }
                path.push(bean.name);
            };

            return {
                build:function (bean) {
                    path = [];
                    buildPathArray(bean);
                    return path;
                }
            }
        })();

        channel.bind('tree_load_children', function (params) {

            //params.panelKey, params.bean, params.parentEl
            if (params.panelKey === 'jndi') {
                model.loadJndi({
                    path:pathArrayBuilder.build(params.bean),
                    bean:params.bean,
                    parentEl:params.parentEl
                });
            }
        });

        channel.bind('app.new.jndi.data', function (params) {
            //params.path, params.bean, params.parentEl
            jndiPanel.loadJndi(params);
        });

        channel.bind('app.new.jndi.class.data', function (params) {
            //params.cls, params.name, params.path
            jndiPanel.showClassPanel(params);
        });

        channel.bind('element.right.click', function (params) {
            //params.data, params.left, params.top
            if (params.panelKey === 'jndi') {
                jndiPanel.jndiContextMenu(params);
            }
        });

        channel.bind('show.class.panel', function (params) {
            var data = params.data;
            model.loadJndiClass({
                name:data.name,
                parent:data.parent,
                path:pathArrayBuilder.build(data.parent)
            });
        });

        channel.bind('lookup.and.save.object', function (params) {
            model.lookupJndi({
                name:params.showParams.name,
                path:pathArrayBuilder.build(params.showParams.parent),
                saveKey:params.saveKey
            });
        });

        channel.bind('application.jdni.load', function (params) {
            model.loadJndi({
                path:['']
            });
        });


    })();


    (function () {
        channel.bind('deploy.file.uploaded', function (params) {
            model.deployApp(params.file);
        });

        channel.bind('app.deployment.result', function (params) {
            model.loadDeployedApps();
        });

        channel.bind('app.new.deployment.data', function (params) {
            deployments.loadDeployeApps(params);
        });
    })();


    channel.bind('app.system.info', function (params) {
        view.setLoggedUser(params.user);
        consolePanel.loadScriptsField(params.supportedScriptLanguages);
    });


    (function () {
        channel.bind('trigger.console.exec', function (params) {
            model.execute(params.codeType, params.codeText);
        });

        channel.bind('app.console.executed', function (params) {
            //TODO Implement me
            throw "app.console.executed not implemented";
        });

        channel.bind('app.console.executed.error', function (params) {
            //TODO Implement me
            //Handle an eventual script execution error
            throw "app.console.executed.error not implemented";
        });
    })();


    (function () {
        channel.bind('app.new.log.data', function (params) {
            logView.loadData(params);
        });

        channel.bind('trigger.log.load', function (params) {
            model.loadLog(params.file, params.tail);
        });
    })();


    (function () {
        channel.bind('app.new.session.data', function (params) {
            savedPanel.load(params);
        });

        channel.bind('application.saved.objects.load', function (params) {
            model.loadSessionData();
        });
    })();


    var view = TOMEE.ApplicationView({
        channel:channel,
        groups:{
            'home':homeView,
            'apps':appsView,
            'log':logView
        },
        initTab:TOMEE.utils.getSafe(function () {
            return model.getUrlVars().initTab;
        }, 'home')
    });

    model.loadSystemInfo(function (data) {
        view.setTomeeVersion(data.tomee);
        homeView.setTomeeVersion(data.tomee);
        view.render();
    });

    model.loadLog(null, null);
    model.loadJndi({
        path:['']
    });
    model.loadDeployedApps();

    return {

    };
};