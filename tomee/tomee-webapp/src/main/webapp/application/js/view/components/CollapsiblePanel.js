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

TOMEE.components.CollapsiblePanel = function (cfg) {
    "use strict";

    var panelID = TOMEE.Sequence.next('accordion');

    var myBody = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        cls:'accordion',
        attributes:{
            id:panelID
        }
    });

    var buildAccordionGroup = function () {
        var grpID = TOMEE.Sequence.next('accordion_grp');
        var group = TOMEE.el.getElMap({
            elName:'main',
            tag:'div',
            cls:'accordion-group',
            children:[
                {
                    tag:'div',
                    cls:'accordion-heading',
                    attributes: {
                        style: 'padding: 9px 15px; border-bottom: 1px solid #EEE;'
                    },
                    children:[
                        {
                            tag:'h3',
                            children:[
                                {
                                    tag:'a',
                                    cls:'accordion-toggle',
                                    attributes:{
                                        href:'#' + grpID,
                                        'data-parent':'#' + panelID,
                                        'data-toggle':'collapse',
                                        style: 'text-decoration: none!important; color: #222; padding: 0px;'
                                    },
                                    children:[
                                        {
                                            elName:'titleEl',
                                            tag:'span'
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                },
                {
                    tag:'div',
                    cls:'accordion-body collapse',
                    attributes:{
                        id:grpID,
                        style:'height: 0px;'
                    },
                    children:[
                        {
                            elName:'bodyEl',
                            tag:'div',
                            cls:'accordion-inner',
                            attributes:{
                                style:'padding: 0px;'
                            }
                        }
                    ]
                }
            ]
        });
        return group;
    }

    var children = TOMEE.utils.getArray(cfg.children);
    (function () {
        var child = null;
        var childEl = null;
        for (var i = 0; i < children.length; i++) {
            child = children[i];

            childEl = buildAccordionGroup();
            childEl.titleEl.html(child.getTitle());
            childEl.bodyEl.append(child.getEl());

            myBody.main.append(childEl.main);
        }
    })();


    return {
        getEl:function () {
            return myBody.main;
        }
    };
};