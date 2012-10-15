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

    (function (keys) {
        TOMEE.utils.forEach(keys, function (key) {
            el.find('.' + key).bind('click', (function () {
                updateSelected(key);
            }));
        });

    })(['home', 'console', 'log']);

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

        if(!params.output.loginSuccess) {
            btn.prop('disabled', false);
            return;
        }

        btn.remove();
        btnsArea.append(logoutBtn);

        menu.removeClass('open');
        userNameMenu.html(user.val());
        pass.html('123456');
        user.prop('disabled', true);
        pass.prop('disabled', true);
    });

    channel.bind('server-command-callback-error', 'Login', function (params) {
        var btn = el.find('.tomee-login-btn');
        btn.prop('disabled', false);
    });

    function updateSelected(key) {
        el.find('.toolbar-item').removeClass('active');
        el.find('.' + key).addClass('active');

        channel.send('ui-actions', 'toolbar-click', {
            key:key
        });
    }

    return {
        getEl:function () {
            return el;
        }
    };
};