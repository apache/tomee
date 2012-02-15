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

TOMEE.ApplicationHomePanelBody = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var myBody = $('<div class="span9"/>');

    var currentPanel = null;
    var constructors = {
        'test': function () {
            return TOMEE.ApplicationHomePanelTest(cfg);
        },
        'jndi': function () {
            return TOMEE.ApplicationHomePanelJndi(cfg);
        },
        'ejb': function () {
            return TOMEE.ApplicationHomePanelEJB(cfg);
        },
        'class': function () {
            return TOMEE.ApplicationHomePanelClass(cfg);
        },
        'obj': function () {
            return TOMEE.ApplicationHomePanelInvoker(cfg);
        }
    };

    var showPanel = function (key) {
        if (currentPanel) {
            channel.send('dying_panel', {
                panel: currentPanel
            });
            currentPanel = null;
        }
        myBody.empty();

        var innerConstructor = constructors[key];
        if (innerConstructor) {
            currentPanel = innerConstructor();
            myBody.append(currentPanel.getEl());
        } else {
            var tpl = [
                '<div class="row">',
                '<legend>' + TOMEE.ApplicationI18N.get('app.home.menu.unknown') + '</legend>',
                '</div>'
            ];
            myBody.append($(tpl.join('')));
        }
    };

    return {
        getEl: function () {
            return myBody;
        },
        showPanel: showPanel
    };
};