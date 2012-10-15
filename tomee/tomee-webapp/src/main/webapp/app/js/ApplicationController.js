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

    var channel = TOMEE.ApplicationChannel,
        model = TOMEE.ApplicationModel(),
        view = TOMEE.ApplicationView();

    view.render();

    channel.bind('ui-actions', 'execute-script', function (data) {
        model.sendMessage({
            cmdName:'RunScript',
            scriptCode:data.text
        });
    });

    channel.bind('ui-actions', 'logout-btn-click', function () {
        window.location.reload();
    });

    channel.bind('ui-actions', 'login-btn-click', function (data) {
        model.sendMessage({
            cmdName:'Login',
            user:data.user,
            pass:data.pass
        });
    });

    channel.bind('ui-actions', 'log-file-selected', function (param) {
        model.sendMessage({
            cmdName:'GetLog',
            file:param.file
        });
    });

    channel.bind('server-command-callback', 'RunScript', function (data) {
        $.meow({
            message:TOMEE.I18N.get('application.console.done')
        });
    });

    channel.bind('server-command-callback-success', 'Login', function (data) {
        model.sendMessage({
            cmdName:'GetLog',
            aNumber:1
        });
    });

    return {

    };
};