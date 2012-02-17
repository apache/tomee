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
            title: TOMEE.ApplicationI18N.get('app.toolbar.home'),
            callback: function () {
                channel.send('toolbar_button_executed', {
                    key: 'home'
                });
            }
        },
        {
            title: TOMEE.ApplicationI18N.get('app.toolbar.help'),
            callback: function () {
                channel.send('toolbar_button_executed', {
                    key: 'help'
                });
            }
        }
    ];

    $.each(buttons, function (i, button) {
        var liUid = TOMEE.Sequence.next();
        var anchorUid = TOMEE.Sequence.next();
        elements.list.append($('<li id="' + liUid + '"><a id="' + anchorUid + '" href="#">' + button.title + '</a></li>'));

        var li = elements.list.find('#' + liUid);
        var anchor = elements.list.find('#' + anchorUid);

        anchor.on("click", function () {
            var allLinks = elements.list.find('li');
            allLinks.removeClass('active');

            li.addClass('active');

            button.callback();
        });
    });

    return {
        getEl: function () {
            return elements.all;
        }
    };
};