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

TOMEE.ApplicationView = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var appToolbar = TOMEE.ApplicationToolbar(cfg);
    var home = TOMEE.ApplicationHomePanel(cfg);
    var jndi = TOMEE.ApplicationJndiPanel(cfg);
    var test = TOMEE.ApplicationTestPanel(cfg);
    var help = TOMEE.ApplicationHelpPanel(cfg);

    var elements = (function () {
        var containerUid = TOMEE.Sequence.next();
        var tpl = [
            '<div class="container-fluid">',
            '<div id="' + containerUid + '" class="row-fluid"/>',
            '<hr>',
            '<footer><p>' + TOMEE.ApplicationI18N.get('application.footer') + '</p></footer>',
            '</div>'
        ];

        //create the element
        var all = $(tpl.join(''));
        var body = all.find("#" + containerUid);
        return {
            all: all,
            body: body
        };
    })();

    var render = function () {
        document.title = TOMEE.ApplicationI18N.get('application.name');

        $('body').append(appToolbar.getEl());
        $('body').append(elements.all);

        channel.send('application_view_rendered', {});
    };

    var renderPanel = function (key, panel) {
        elements.body.append(panel.getEl());
        channel.send('application_panel_rendered', {
            key: key
        });
    };

    var getPanel = function (key) {
        if (key === 'jndi') {
            return jndi;

        } else if (key === 'test') {
            return test;

        } else if (key === 'help') {
            return help;

        } else if (key === 'home') {
            return home;
        }
        return null;
    };

    var showPanel = function (key) {
        elements.body.empty();
        var panel = getPanel(key);
        if (panel) {
            renderPanel(key, panel);
        }
    };

    return {
        render: render,
        getToolbar: function () {
            return appToolbar;
        },
        showPanel: showPanel,
        getPanel: getPanel
    };
};