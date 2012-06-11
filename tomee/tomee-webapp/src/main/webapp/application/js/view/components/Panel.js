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

TOMEE.components.Panel = function (cfg) {
    "use strict";

    var channel = cfg.channel;
    var avoidOverflow = TOMEE.utils.getSafe(cfg.avoidOverflow, false);

    var elMapToolbar = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        cls:'navbar',
        attributes:{
            style:'margin-bottom: 0px;'
        },
        children:[
            {
                tag:'div',
                cls:'navbar-inner t-navbar',
                attributes:{
                    style:'padding-left: 0px; padding-right: 0px;'
                },
                children:[
                    {
                        elName:'menuItems',
                        tag:'div',
                        children:[
                            {
                                elName:'appName',
                                tag:'a',
                                cls:'brand',
                                attributes:{
                                    href:'#',
                                    style:'padding-left: 10px; margin-left: 0px;'
                                },
                                html:TOMEE.utils.getSafe(cfg.title, '-')
                            }
                        ]
                    }
                ]
            }
        ]
    });

    if (cfg.actions) {
        var commands = TOMEE.el.getElMap({
            elName:'actionsMenu',
            tag:'div',
            cls:'btn-group pull-right',
            children:[
                {
                    tag:'a',
                    cls:'btn dropdown-toggle',
                    attributes:{
                        'data-toggle':'dropdown',
                        href:'#'
                    },
                    children:[
                        {
                            tag:'i',
                            cls:'icon-cog'
                        },
                        {
                            tag:'span',
                            attributes:{
                                style:'padding-left: 5px; padding-right: 5px;'
                            }
                        },
                        {
                            tag:'span',
                            cls:'caret'
                        }
                    ]
                }
            ]
        });
        elMapToolbar.menuItems.append(commands.actionsMenu);

        (function () {
            var actions = TOMEE.el.getElMap({
                elName:'main',
                tag:'ul',
                cls:'dropdown-menu',
                attributes:{
                    style:'right: 5px;'
                }

            });

            var actionItem = null;
            for (var i = 0; i < cfg.actions.length; i++) {
                actionItem = cfg.actions[i];
                actions.main.append(TOMEE.el.getElMap({
                    elName:'actionButton',
                    tag:'a',
                    attributes:{
                        href:'#'
                    },
                    html:actionItem.text,
                    listeners:actionItem.listeners
                }).actionButton);
            }
            commands.actionsMenu.append(actions.main);
        })();

    }

    var map = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        children:[
            {
                tag:'div',
                children:[
                    {
                        tag:'div',
                        cls:'well t-panel',
                        children:[
                            {
                                elName:'toolbar',
                                tag:'div',
                                attributes:{
                                    style:'position: relative;'
                                }
                            },
                            {
                                elName:'content',
                                tag:'div',
                                attributes:{
                                    style:'height: 250px; position: relative; overflow: auto;'
                                },
                                createCallback:function (el) {
                                    if (avoidOverflow) {
                                        el.css('overflow', '');
                                    }
                                }
                            }
                        ]
                    }
                ]
            }
        ]
    });

    map.toolbar.append(elMapToolbar.main);


    var extraStyles = cfg.extraStyles;
    if (extraStyles) {
        (function () {
            var content = map['content'];

            for (var key in extraStyles) {
                content.css(key, extraStyles[key]);
            }
        })();
    }

    var setHeight = function (height) {
        var toolbarSize = elMapToolbar.main.height();
        var mySize = height - toolbarSize - TOMEE.el.getBorderSize(map.main) - TOMEE.el.getBorderSize(map.content);
        map.content.height(mySize);
    };

    return {
        getEl:function () {
            return map.main;
        },
        getContentEl:function () {
            return map.content;
        },
        setHeight:setHeight,
        showAt:function (config) {
            var main = map.main;

            main.css('left', config.left + 'px');
            main.css('top', config.top + 'px');

            var myBody = $('body');
            myBody.append(main);
        }
    };
};