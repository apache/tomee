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

TOMEE.components.Tree = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var currentData = null;

    var getText = cfg.getText;
    var getChildren = cfg.getChildren;
    var myKey = cfg.key;

    var elements = (function () {
        var container = $('<div></div>');
        var ul = $('<ul></ul>');
        ul.addClass('tree');

        container.append(ul);

        return {
            container:container,
            ul:ul
        };
    })();


    var loadTree = function (parent, data) {
        if ($.isArray(data)) {

            for (var i = 0; i < data.length; i++) {
                loadTree(parent, data[i]);
            }

            return;
        }

        var span = $('<span class="t-tree-link"></span>');
        span.append(getText(data));

        var li = $('<li></li>');
        li.append(span);

        var children = getChildren(data);
        if (children) {
            li.addClass('closed');

            span.bind('click', {
                children:children,
                li:li
            }, function (event) {
                if (event.data.li.hasClass("opened")) {
                    event.data.li.find('ul').empty();

                    event.data.li.removeClass('opened');
                    event.data.li.addClass('closed');
                } else {
                    event.data.li.removeClass('closed');
                    event.data.li.addClass('opened');

                    var ul = li.find('ul').first();
                    if (ul.length === 0) {
                        ul = $('<ul></ul>');
                        event.data.li.append(ul);
                    }

                    loadTree(ul, event.data.children);
                }
            });

        } else {
            li.addClass('leaf');

            span.bind('click', {
                bean:data
            }, function (event) {

                channel.send(myKey + '_leaf_click', {
                    bean: event.data.bean
                });
            });
        }

        //add LI to the parent UL
        parent.append(li);
    };

    return {
        getEl:function () {
            return elements.container;
        },

        load:function (newData) {
            currentData = TOMEE.utils.getArray(newData);
            loadTree(elements.ul, currentData);
        }
    };
};