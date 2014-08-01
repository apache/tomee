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

(function () {
    'use strict';

    var deps = [
        'app/js/view/container',
        'app/js/view/scripting', 'app/js/view/logfiles', 'app/js/view/sessions', 'app/js/view/growl',
        'lib/underscore',
        'app/js/i18n',
        'app/js/handlebarsHelpers',
        'lib/less', 'lib/backbone', 'lib/jquery', 'lib/bootstrap',
        'app/js/keep-alive'
    ];
    define(deps, function (containerView, scriptingView, logfilesView, sessionsView) {
        containerView.render();

        $.ajaxSetup({ cache: false });

        function start() {
            //Starting the backbone router.
            var Router = Backbone.Router.extend({
                routes: {
                    '': 'showSessions',
                    '/': 'showSessions',
                    'sessions': 'showSessions',
                    'scripting': 'showScripting',
                    'scripting/': 'showScripting',
                    'scripting/:scriptType': 'showScripting',
                    'log-files': 'showLogFile',
                    'log-files/': 'showLogFile',
                    'log-files/:fileName': 'showLogFile'
                },

                showSessions: function () {
                    containerView.showView(sessionsView);
                    $.ajax({
                        url: window.ux.ROOT_URL + 'rest/session',
                        method: 'GET',
                        dataType: 'json',
                        data: {},
                        success: function (data) {
                            sessionsView.render({
                                sessions: data
                            });
                        }
                    });
                    $.ajax({
                        url: window.ux.ROOT_URL + 'rest/context',
                        method: 'GET',
                        dataType: 'json',
                        data: {},
                        success: function (data) {
                            sessionsView.render({
                                contexts: data
                            });
                        }
                    });
                },

                showScripting: function (scriptType) {
                    containerView.showView(scriptingView);
                    if (scriptType) {
                        scriptingView.showSourceType(scriptType);
                    }
                },

                showLogFile: function (fileName) {
                    containerView.showView(logfilesView);
                    if (fileName) {
                        logfilesView.triggerFileLoad(fileName);
                    }
                }
            });
            var router = new Router();

            containerView.on('navigate', function (data) {
                router.navigate(data.href, {
                    trigger: true
                });
            });

            sessionsView.on('expire-session', function (data) {
                $.ajax({
                    url: window.ux.ROOT_URL + 'rest/session/expire/' + data.context + '/' + data.sessionId,
                    method: 'DELETE',
                    dataType: 'json',
                    data: {},
                    success: function (data) {
                        router.showSessions();
                    }
                });
            });


            sessionsView.on('kill-context', function (data) {
                $.ajax({
                    url: window.ux.ROOT_URL + 'rest/context/' + data.basename + '/',
                    method: 'DELETE',
                    dataType: 'json',
                    data: {},
                    success: function (data) {
                        router.showSessions();
                    }
                });
            });

            scriptingView.on('execute-action', function (data) {
                $.ajax({
                    url: window.ux.ROOT_URL + 'rest/scripting/',
                    method: 'POST',
                    dataType: 'json',
                    data: {
                        engine: data.engine,
                        script: data.script,
                        user: data.user,
                        password: data.password,
                        realm: data.realm
                    },
                    success: function (data) {
                        scriptingView.appendOutput(data.output);
                    }
                });
            });

            scriptingView.on('type-chosen', function (data) {
                router.navigate('scripting/' + data.name, {
                    trigger: false
                });
                scriptingView.showSourceType(data.name);
            });

            logfilesView.on('load-file-options', function () {
                $.ajax({
                    url: window.ux.ROOT_URL + 'rest/log/list-files',
                    method: 'GET',
                    dataType: 'json',
                    data: {},
                    success: function (data) {
                        logfilesView.showList(data.files);
                    }
                });
            });

            logfilesView.on('load-file', function (data) {
                router.navigate('log-files/' + data.href, {
                    trigger: false
                });
                $.ajax({
                    url: window.ux.ROOT_URL + 'rest/log/load/' + data.href,
                    method: 'GET',
                    dataType: 'json',
                    data: {
                        href: data.href
                    },
                    success: function (data) {
                        logfilesView.showFile(data.content);
                    }
                });
            });

            //Starting the backbone history.
            Backbone.history.start({
                pushState: true,
                root: window.ux.ROOT_URL // This value is set by <c:url>
            });
        }

        return {
            start: start
        };
    });
}());