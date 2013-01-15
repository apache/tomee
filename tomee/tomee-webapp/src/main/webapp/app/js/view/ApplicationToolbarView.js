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

    var requirements = ['ApplicationChannel', 'ApplicationTemplates', 'util/DelayedTask'];

    define(requirements, function (channel, templates, DelayedTask) {
        function newObject() {
            var el = $(templates.getValue('application-toolbar', {}));
            var userNameMenu = el.find('.tomee-user-name');
            var loginMenu = el.find('.user-login-dropdown');
            var loginBtn = el.find('.tomee-login-btn');
            var logoutBtn = $(templates.getValue('application-toolbar-logout-btn', {}));

            el.find('.toolbar-item').on('click', function (ev) {
                var tabEl = $(ev.currentTarget),
                    tabKey = tabEl.attr("tab-key");

                channel.send('ui-actions', 'toolbar-click', {
                    key: tabKey
                });
            });

            userNameMenu.on('click', function () {
                DelayedTask.newObject().delay(function () {
                    var user = el.find('.tomee-login');

                    if (loginMenu.hasClass('open') && user) {
                        user.focus();
                    }
                }, 500);
            });

            channel.bind('ui-actions', 'locked-change', function (data) {
                el.find('.toolbar-item').each(function (index, htmlElement) {
                    var element = $(htmlElement);
                    if (data.panel === element.attr('tab-key')) {
                        if (data.locked) {
                            element.addClass('hidden');
                        } else {
                            element.removeClass('hidden');
                        }
                    }
                });
            });

            channel.bind('ui-actions', 'panel-switch', function (data) {
                el.find('.toolbar-item').removeClass('active');
                el.find('.toolbar-item').each(function (index, htmlEl) {
                    var tabEl = $(htmlEl),
                        tabKey = tabEl.attr("tab-key");

                    if (tabKey === data.key) {
                        tabEl.addClass('active');
                    }
                });
            });

            loginBtn.on('click', function () {
                var user = el.find('.tomee-login').val();
                var pass = el.find('.tomee-password').val();

                channel.send('ui-actions', 'login-btn-click', {
                    user: user,
                    pass: pass
                });
                loginBtn.prop('disabled', true);
            });

            logoutBtn.on('click', function () {
                channel.send('ui-actions', 'logout-btn-click', {});
            });

            channel.bind('server-command-callback-success', 'Login', function (params) {
                var btnsArea = el.find('.login-buttons');
                var user = el.find('.tomee-login');
                var pass = el.find('.tomee-password');

                if (!params.output.loginSuccess) {
                    loginBtn.prop('disabled', false);
                    return;
                }

                loginBtn.remove();
                user.remove();
                pass.remove();

                btnsArea.append(logoutBtn);

                loginMenu.removeClass('open');
                userNameMenu.html(user.val());

                el.find('.login-menu').addClass('logout');
            });

            channel.bind('server-command-callback-success', 'session', function (params) {
                var btnsArea = el.find('.login-buttons');
                var user = el.find('.tomee-login');
                var pass = el.find('.tomee-password');

                if (!params.data.userName) {
                    return;
                }

                loginBtn.remove();
                user.remove();
                pass.remove();
                btnsArea.append(logoutBtn);

                userNameMenu.html(params.data.userName);

                el.find('.login-menu').addClass('logout');
                el.find('.toolbar-item').removeClass('disabled');
            });

            channel.bind('server-command-callback-error', 'Login', function (params) {
                loginBtn.prop('disabled', false);
            });

            return {
                getEl: function () {
                    return el;
                }
            };
        }

        return {
            newObject: newObject
        };
    });
}());

