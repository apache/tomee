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

TOMEE.ApplicationHomePanelMenu = function (cfg) {
    var channel = cfg.channel;

    var elements = (function () {
        var ulUid = TOMEE.Sequence.next();
        var tpl = [
            '<div class="span3">',
            '<div class="well sidebar-nav">',
            '<ul id="' + ulUid + '" class="nav nav-list">',
            '</ul>',
            '</div>',
            '</div>'
        ];

        //create the element
        var all = $(tpl.join(''));
        var list = all.find("#" + ulUid);
        return {
            all:all,
            list:list
        };
    })();

    var buttonMap = {};

    var createButtonCfg = function (key, title) {
        var liId = TOMEE.Sequence.next();
        var anchorId = TOMEE.Sequence.next();
        var result = {
            liId:liId,
            anchorId:anchorId,
            title:title,
            callback:function () {
                var allLinks = elements.list.find('li');
                allLinks.removeClass('active');

                var li = elements.list.find('#' + liId);
                li.addClass('active');

                channel.send('home_menu_executed', {
                    menu:key
                });
            }
        };

        //map the new button
        buttonMap[key] = result;

        return result;
    };

    var btnGroups = [
        {
            title:TOMEE.ApplicationI18N.get('app.home.menu.setup'),
            btns:[
                createButtonCfg('test', TOMEE.ApplicationI18N.get('app.home.menu.setup.test'))
            ]
        },

        {
            title:TOMEE.ApplicationI18N.get('app.home.menu.tools'),
            btns:[
                createButtonCfg('jndi', TOMEE.ApplicationI18N.get('app.home.menu.tools.jndi')),
                createButtonCfg('class', TOMEE.ApplicationI18N.get('app.home.menu.tools.class')),
                createButtonCfg('ejb', TOMEE.ApplicationI18N.get('app.home.menu.tools.ejb')),
                createButtonCfg('obj', TOMEE.ApplicationI18N.get('app.home.menu.tools.obj'))
            ]
        }
    ];


    $.each(btnGroups, function (i, grp) {

        var anchorUid = TOMEE.Sequence.next();

        (function () {
            var liUid = TOMEE.Sequence.next();
            elements.list.append($('<li class="nav-header" id="' + liUid + '">' + grp.title + '</li>'));
            return elements.list.find('#' + liUid);
        })();

        $.each(grp.btns, function (ii, btn) {
            var el = (function () {
                elements.list.append($('<li id="' + btn.liId + '"><a id="' + btn.anchorId + '" href="#">' + btn.title + '</a></li>'));
                return elements.list.find('#' + btn.liId);
            })();
            el.on('click', function () {
                btn.callback();
            });
        });
    });

    return {
        getEl:function () {
            return elements.all;
        },
        selectMenu:function(key) {
            var menu = buttonMap[key];
            if(!menu) {
                return;
            }

            menu.callback();
        }
    };
};