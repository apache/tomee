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

TOMEE.ApplicationToolbar = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var elements = (function () {
        var ulUid = TOMEE.Sequence.next();
        var tpl = [
            '<div class="navbar navbar-fixed-top">',
            '<div class="navbar-inner">',
            '<div class="container-fluid">',
            '<a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">',
            '<span class="icon-bar"></span>',
            '<span class="icon-bar"></span>',
            '<span class="icon-bar"></span>',
            '</a>',
            '<a class="brand" href="#">' + TOMEE.ApplicationI18N.get('application.name') + '</a>',
            '<div class="nav-collapse">',
            '<ul id="' + ulUid + '" class="nav"></ul>',
            '</div>',
            '</div>',
            '</div>',
            '</div>'
        ];

        //create the element
        var all = $(tpl.join(''));
        var ul = all.find("#" + ulUid);
        return {
            all: all,
            list: ul
        };
    })();

    var buttons = [
        {
            key: 'home',
            title: TOMEE.ApplicationI18N.get('app.toolbar.home'),
            callback: function () {
                channel.send('toolbar_button_executed', {
                    key: 'home'
                });
            }
        },
        {
            key: 'jndi',
            title: TOMEE.ApplicationI18N.get('app.toolbar.jndi'),
            callback: function () {
                channel.send('toolbar_button_executed', {
                    key: 'jndi'
                });
            }
        },
        {
            key: 'test',
            title: TOMEE.ApplicationI18N.get('app.toolbar.test'),
            callback: function () {
                channel.send('toolbar_button_executed', {
                    key: 'test'
                });
            }
        },
        {
            key: 'help',
            title: TOMEE.ApplicationI18N.get('app.toolbar.help'),
            callback: function () {
                channel.send('toolbar_button_executed', {
                    key: 'help'
                });
            }
        }
    ];

    var buttonsMap = {};

    var clickButtonCallback = function (mappedButton) {
        var allLinks = elements.list.find('li');
        allLinks.removeClass('active');

        mappedButton.li.addClass('active');
        mappedButton.button.callback();
    };

    $.each(buttons, function (i, button) {
        var liUid = TOMEE.Sequence.next();
        var anchorUid = TOMEE.Sequence.next();
        elements.list.append($('<li id="' + liUid + '"><a id="' + anchorUid + '" href="#">' + button.title + '</a></li>'));

        var li = elements.list.find('#' + liUid);
        var anchor = elements.list.find('#' + anchorUid);

        buttonsMap[button.key] = {
            button: button,
            li: li,
            anchor: anchor
        };

        anchor.on("click", function () {
            clickButtonCallback(buttonsMap[button.key]);
        });
    });

    var clickButton = function (key) {
        clickButtonCallback(buttonsMap[key]);
    };

    return {
        getEl: function () {
            return elements.all;
        },
        clickButton: clickButton
    };
};