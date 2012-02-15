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

    var btnGroups = [
        {
            title:TOMEE.ApplicationI18N.get('app.home.menu.setup'),
            btns:[
                {
                    title:TOMEE.ApplicationI18N.get('app.home.menu.setup.test'),
                    callback:function () {

                    }
                }
            ]
        },

        {
            title:TOMEE.ApplicationI18N.get('app.home.menu.tools'),
            btns:[
                {
                    title:TOMEE.ApplicationI18N.get('app.home.menu.tools.jndi'),
                    callback:function () {

                    }
                },

                {
                    title:TOMEE.ApplicationI18N.get('app.home.menu.tools.class'),
                    callback:function () {

                    }
                },

                {
                    title:TOMEE.ApplicationI18N.get('app.home.menu.tools.ejb'),
                    callback:function () {

                    }
                },

                {
                    title:TOMEE.ApplicationI18N.get('app.home.menu.tools.obj'),
                    callback:function () {

                    }
                }
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
                var liUid = TOMEE.Sequence.next();
                var anchorUid = TOMEE.Sequence.next();
                elements.list.append($('<li id="' + liUid + '"><a id="' + anchorUid + '" href="#">' + btn.title + '</a></li>'));
                return {
                    li:elements.list.find('#' + liUid),
                    anchor:elements.list.find('#' + anchorUid)
                };
            })();
            el.anchor.on('click', function () {
                var allLinks = elements.list.find('li');
                allLinks.removeClass('active');

                el.li.addClass('active');

                btn.callback();
            });
        });

        var anchor = elements.list.find('#' + anchorUid);

        anchor.on("click", function () {
            var allLinks = elements.list.find('li');
            allLinks.removeClass('active');

            li.addClass('active');

            button.callback();
        });
    });

    return {
        getEl:function () {
            return elements.all;
        }

    };
};