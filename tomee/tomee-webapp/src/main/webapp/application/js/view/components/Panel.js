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

    var elementsPointers = {};

    var parentEl = cfg.parent;
    if (!parentEl) {
        parentEl = $(window);
    }

    var header = TOMEE.el.getElMap({
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

    var elBottomBar = null;
    if (cfg.bbar) {
        elBottomBar = TOMEE.el.getElMap({
            elName:'main',
            tag:'form',
            cls:'well form-inline',
            attributes:{
                style:'height: 27px;margin-bottom: 0px;padding-top: 1px;padding-left: 1px;padding-bottom: 1px;padding-right: 1px;'
            },
            children:[
                {
                    elName:'childrenDiv',
                    tag:'div',
                    cls:'pull-right'
                }
            ]
        });

        (function () {
            var arr = TOMEE.utils.getArray(cfg.bbar);
            var childrenDiv = elBottomBar.childrenDiv;

            var tempKey = TOMEE.Sequence.next('temp');
            var newEl = null;
            var current = null;
            for (var i = 0; i < arr.length; i++) {
                current = arr[i];

                var keepIt = false;
                if (current.elName) {
                    keepIt = true;

                } else {
                    current.elName = tempKey;
                }


                newEl = TOMEE.el.getElMap(current)[current.elName];
                if (keepIt) {
                    elementsPointers[current.elName] = newEl;
                }

                childrenDiv.append(newEl);
            }
        })();
    }

    if (cfg.headerActions) {
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
        header.menuItems.append(commands.actionsMenu);

        (function () {
            var actionsEl = TOMEE.el.getElMap({
                elName:'main',
                tag:'ul',
                cls:'dropdown-menu',
                attributes:{
                    style:'right: 5px;'
                }

            });

            var actionItem = null;
            for (var i = 0; i < cfg.headerActions.length; i++) {
                actionItem = cfg.headerActions[i];
                actionsEl.main.append(TOMEE.el.getElMap({
                    elName:'actionButton',
                    tag:'a',
                    attributes:{
                        href:'#'
                    },
                    html:actionItem.text,
                    listeners:actionItem.listeners
                }).actionButton);
            }
            commands.actionsMenu.append(actionsEl.main);
        })();

    }

    var map = null;
    var createMap = function () {
        map = null;
        map = TOMEE.el.getElMap({
            elName:'main',
            tag:'div',
            children:[
                {
                    tag:'div',
                    children:[
                        {
                            elName:'elements',
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
    };
    createMap();

    if (elBottomBar) {
        map.elements.append(elBottomBar.main);
    }

    map.toolbar.append(header.main);


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
        var toolbarSize = header.main.height();
        var mySize = height - toolbarSize - TOMEE.el.getBorderSize(map.main) - TOMEE.el.getBorderSize(map.content);
        map.content.height(mySize);
    };

    var getCenter = function () {
        var winCenterX = parentEl.height() / 2;
        var winCenterY = parentEl.width() / 2;

        var panelX = map.main.height() / 2;
        var panelY = map.main.width() / 2;

        return {
            left:winCenterY - panelY,
            top:winCenterX - panelX
        };
    };

    return {
        getElement:function (key) {
            return elementsPointers[key];
        },
        getEl:function () {
            return map.main;
        },
        getContentEl:function () {
            return map.content;
        },
        setHeight:setHeight,
        showAt:function (config) {
            if (!config) {
                throw 'missing parameters';
            }

            var main = map.main;
            main.css('position', 'absolute');

            var myBody = $('body');
            myBody.append(main);

            if (config.left || config.top) {
                main.css('left', TOMEE.el.getLocationValue(config.left));
                main.css('top', TOMEE.el.getLocationValue(config.top));
            } else {
                var center = getCenter();

                main.css('left', center.left + 'px');
                main.css('top', center.top + 'px');
            }

            if (config.modal) {
                //TODO: add the modal feature
            }
        },
        close:function (killIt) {
            if (killIt) {
                if(map && map.main) {
                    map.main.remove();
                }
                createMap();
            } else {
                if(map && map.main) {
                    map.main.detach();
                }
            }
        }
    };
};