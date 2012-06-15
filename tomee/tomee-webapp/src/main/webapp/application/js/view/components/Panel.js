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

    var dragPanel = false;
    var isFloatingPanel = false;
    var panelX = 0;
    var panelY = 0;

    var title = TOMEE.utils.getSafe(cfg.title, '-');

    var map = null;
    var createMap = function () {
        map = null;
        map = TOMEE.el.getElMap(myBodyCfg);
    };

    var myBodyChildren = [];
    if (!cfg.isCollapsiblePanel) {
        myBodyChildren.push(
            {
                elName:'header',
                tag:'div',
                cls:'modal-header',
                children:[
                    {
                        elName:'appName',
                        tag:'h3',
                        html:title
                    }
                ],
                listeners:{
                    'mousedown':function () {
                        dragPanel = true;
                    },
                    'mouseup':function () {
                        dragPanel = false;
                    },
                    'mouseout':function () {
                        dragPanel = false;
                    },
                    'mousemove':function (event) {
                        //TODO fix me
                        return;

                        if (!isFloatingPanel || !dragPanel) {
                            //nothing to do
                            return;
                        }

                        var main = map.main;

                        panelX = panelX - event.offsetY;
                        panelY = panelY - event.offsetX;

                        main.css('left', panelX + 'px');
                        main.css('top', panelY + 'px');
                    }
                }
            }

        );

    }
    myBodyChildren.push({
        elName:'myBody',
        tag:'div',
        cls:'modal-body',
        attributes:{
            style:'padding: 0px; max-height: 2000px;'
        }
    });

    var myBodyCfg = {
        elName:'main',
        tag:'div',
        cls:'t-panel',
        children:myBodyChildren,
        attributes:{
            style:(function () {
                if(cfg.isCollapsiblePanel) {
                    return 'border-top-width: 0px; border-right-width: 0px; border-bottom-width: 0px; border-left-width: 0px;';
                }
                return '';
            })()
        }
    };
    if (cfg.bbar) {
        (function () {

            var style = 'margin-bottom: 0px;'
            var formAttributes = {
                style:style
            };
            if (cfg.bbarFormAttributes) {
                formAttributes = cfg.bbarFormAttributes;
                if (!formAttributes.style) {
                    formAttributes.style = style;
                }
            }


            var childrenDiv = [];
            var footerCfg = {
                elName:'footer',
                tag:'div',
                cls:'modal-footer',
                children:[
                    {
                        elName:'bbarForm',
                        tag:'form',
                        cls:'form-inline',
                        attributes:formAttributes,
                        children:childrenDiv
                    }
                ]
            };
            myBodyCfg.children.push(footerCfg);

            var arr = TOMEE.utils.getArray(cfg.bbar);

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

                childrenDiv.push({
                    el:newEl
                });
            }
        })();
    }

    //create the initial map
    createMap();

    var extraStyles = cfg.extraStyles;
    if (extraStyles) {
        (function () {
            var content = map['myBody'];

            for (var key in extraStyles) {
                content.css(key, extraStyles[key]);
            }
        })();
    }

    var setHeight = function (height) {
        map.main.height(height);

        var toolbarSize = TOMEE.utils.getSafe(function () {
            return map.header.outerHeight();
        }, 0);
        var footerSize = TOMEE.utils.getSafe(function () {
            return  map.footer.outerHeight();
        }, 0);

        var mySize = height - toolbarSize - footerSize - TOMEE.el.getBorderSize(map.main) - TOMEE.el.getBorderSize(map.myBody);
        map.myBody.height(mySize);

        if (cfg.onResize) {
            cfg.onResize(mySize);
        }
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
        getBbarForm:function () {
            return map.bbarForm;
        },

        setTitle:function (title) {
            map['appName'].html(title);
        },
        getTitle:function (title) {
            return map['appName'].html();
        },

        getElement:function (key) {
            return elementsPointers[key];
        },
        getEl:function () {
            return map.main;
        },
        getContentEl:function () {
            return map.myBody;
        },
        setHeight:setHeight,
        showAt:function (config) {
            if (!config) {
                throw 'missing parameters';
            }

            isFloatingPanel = true;

            var main = map.main;
            main.css('position', 'absolute');

            var myBody = $('body');
            myBody.append(main);

            if (config.left || config.top) {
                panelX = config.left;
                panelY = config.top;
            } else {
                var center = getCenter();

                panelX = center.left;
                panelY = center.top;
            }

            main.css('left', panelX + 'px');
            main.css('top', panelY + 'px');

            if (config.modal) {
                //TODO: add the modal feature
            }
        },
        close:function (killIt) {
            if (killIt) {
                if (map && map.main) {
                    map.main.remove();
                }
                createMap();
            } else {
                if (map && map.main) {
                    map.main.detach();
                }
            }
        }
    };
};