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

TOMEE.ApplicationToolbarView = function () {
    "use strict";

    var channel = TOMEE.ApplicationChannel,
        el = $(TOMEE.ApplicationTemplates.getValue('application-toolbar', {})),
        logoutBtn = $(TOMEE.ApplicationTemplates.getValue('application-toolbar-logout-btn', {}));

    el.find('.toolbar-item').on('click', function (ev) {
        var tabEl = $(ev.currentTarget),
            tabKey = tabEl.attr("tab-key");

        channel.send('ui-actions', 'toolbar-click', {
            key:tabKey
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

    el.find('.tomee-login-btn').on('click', function () {
        var user = el.find('.tomee-login').val(),
            pass = el.find('.tomee-password').val(),
            btn = el.find('.tomee-login-btn');

        channel.send('ui-actions', 'login-btn-click', {
            user:user,
            pass:pass
        });
        btn.prop('disabled', true);
    });

    logoutBtn.on('click', function () {
        channel.send('ui-actions', 'logout-btn-click', {});
    });

    channel.bind('server-command-callback-success', 'Login', function (params) {
        var btn = el.find('.tomee-login-btn'),
            btnsArea = el.find('.login-buttons'),
            menu = el.find('.user-login-dropdown'),
            userNameMenu = el.find('.tomee-user-name'),
            user = el.find('.tomee-login'),
            pass = el.find('.tomee-password');

        if (!params.output.loginSuccess) {
            btn.prop('disabled', false);
            return;
        }

        btn.remove();
        user.remove();
        pass.remove();

        btnsArea.append(logoutBtn);

        menu.removeClass('open');
        userNameMenu.html(user.val());

        el.find('.login-menu').addClass('logout');
    });

    channel.bind('server-command-callback-success', 'session', function (params) {
        var btn = el.find('.tomee-login-btn'),
            btnsArea = el.find('.login-buttons'),
            menu = el.find('.user-login-dropdown'),
            userNameMenu = el.find('.tomee-user-name'),
            user = el.find('.tomee-login'),
            pass = el.find('.tomee-password');

        if (!params.data.userName) {
            return;
        }

        btn.remove();
        user.remove();
        pass.remove();
        btnsArea.append(logoutBtn);

        userNameMenu.html(params.data.userName);

        el.find('.login-menu').addClass('logout');
        el.find('.toolbar-item').removeClass('disabled');
    });

    channel.bind('server-command-callback-error', 'Login', function (params) {
        var btn = el.find('.tomee-login-btn');
        btn.prop('disabled', false);
    });

    return {
        getEl:function () {
            return el;
        }
    };
};