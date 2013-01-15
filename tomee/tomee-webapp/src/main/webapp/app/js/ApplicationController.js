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

(function () {
    'use strict';

    var requirements = ['ApplicationChannel', 'ApplicationModel', 'view/ApplicationView', 'view/GrowlNotification', 'util/I18N'];

    define(requirements, function (channel, model, view, growl, I18N) {
        function newObject() {
            view.render();

            channel.bind('ui-actions', 'execute-script', function (data) {
                model.sendMessage({
                    cmdName: 'RunScript',
                    scriptCode: data.text
                });
            });

            channel.bind('ui-actions', 'logout-btn-click', function () {
                model.sendMessage({
                    cmdName: 'Logout'
                });
            });

            channel.bind('ui-actions', 'login-btn-click', function (data) {
                model.sendMessage({
                    cmdName: 'Login',
                    user: data.user,
                    pass: data.pass,
                    port: window.location.port,
                    protocol: (function () {
                        var protocol = window.location.protocol;
                        protocol = protocol.replace(':', '');
                        return protocol;
                    }())
                });
            });

            channel.bind('ui-actions', 'load-file-names', function () {
                model.sendMessage({
                    cmdName: 'GetLogFiles'
                });
            });

            channel.bind('ui-actions', 'load-status', function () {
                model.sendMessage({
                    cmdName: 'GetStatus'
                });
            });

            channel.bind('ui-actions', 'log-file-selected', function (param) {
                model.sendMessage({
                    cmdName: 'GetLog',
                    file: param.file
                });
            });

            channel.bind('ui-actions', 'reload-webservices-table', function () {
                model.sendMessage({
                    cmdName: 'GetWebServices'
                });
            });

            channel.bind('ui-actions', 'reload-jndi-table', function () {
                model.sendMessage({
                    cmdName: 'GetJndi'
                });
            });

            channel.bind('ui-actions', 'openejb-installer-clicked', function (data) {
                data.cmdName = 'RunInstaller';
                model.sendMessage(data);
            });

            channel.bind('server-command-callback-error', 'RunScript', function (data) {
                growl.showNotification(I18N.get('application.console.run.error'), 'error');
            });

            channel.bind('ui-actions', 'show-notification', function (data) {
                growl.showNotification(data.message, data.messageType);
            });

            channel.bind('server-command-callback', 'Logout', function (data) {
                window.location.reload();
            });

            channel.bind('server-connection', 'session-ready', function () {
                model.connectSocket();
            });

            channel.bind('server-command-callback-success', 'Login', function (params) {
                if (params.output.loginSuccess) {
                    growl.showNotification(I18N.get('application.log.hello', {
                        userName: params.params.user
                    }), 'success');
                } else {
                    growl.showNotification(I18N.get('application.log.bad'), 'error');
                }
            });

            channel.bind('server-command-callback-error', 'Login', function (params) {
                growl.showNotification(I18N.get('application.log.error'), 'error');
            });

            if (model.isSessionReady()) {
                model.connectSocket();
            }

            return {

            };
        }

        return {
            newObject: newObject
        };
    });
}());