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

    var getText = cfg.getText;
    var childrenPropertyName = cfg.childrenPropertyName;
    var myKey = cfg.key;

    var elements = TOMEE.el.getElMap({
        elName:'container',
        tag:'div',
        children:[
            {
                elName:'ul',
                tag:'ul',
                cls:'tree'
            }
        ]
    });

    var loadTree = function (parent, data) {
        if ($.isArray(data)) {

            for (var i = 0; i < data.length; i++) {
                loadTree(parent, data[i]);
            }

            return;
        }

        var span = $('<span></span>');
        var myI = $('<i style="padding-right: 5px;"></i>');

        span.append(myI);
        span.append(getText(data));

        span.bind('contextmenu', function (event) {
            channel.send('element.right.click', {
                panelKey:myKey,
                data:data,
                left: event.clientX,
                top: event.clientY
            });
        });

        var li = TOMEE.el.getElMap({
            elName:'el',
            tag:'li',
            attributes:{
                style:'padding-left: 0px;'
            }
        }).el;
        li.append(span);

        if (data[childrenPropertyName] === undefined) {
            myI.addClass('icon-leaf');

            li.bind('click', {
                bean:data
            }, function (event) {

                channel.send('tree_leaf_click', {
                    panelKey:myKey,
                    bean:event.data.bean
                });
            });

        } else {
            myI.addClass('icon-folder-close');

            span.bind('click', {
                li:li,
                i:myI,
                bean:data

            }, function (event) {
                var i = event.data.i;

                if (i.hasClass("icon-folder-open")) {
                    event.data.li.find('ul').empty();

                    i.removeClass('icon-folder-open');
                    i.addClass('icon-folder-close');

                } else {
                    i.removeClass('icon-folder-close');
                    i.addClass('icon-folder-open');

                    var ul = li.find('ul').first();
                    if (ul.length === 0) {
                        ul = $('<ul></ul>');
                        event.data.li.append(ul);
                    }

                    channel.send('tree_load_children', {
                        panelKey:myKey,
                        bean:event.data.bean,
                        parentEl:ul
                    });
                }
            });
        }

        //add LI to the parent UL
        parent.append(li);
    };

    return {
        getEl:function () {
            return elements.container;
        },

        load:function (ul, newData) {
            if (ul) {
                loadTree(ul, newData);
            } else {
                elements.ul.empty();
                loadTree(elements.ul, newData);
            }

        }
    };
};