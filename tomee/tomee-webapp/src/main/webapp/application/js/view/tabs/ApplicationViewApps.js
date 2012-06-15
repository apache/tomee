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

TOMEE.ApplicationViewApps = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var left = cfg.leftPanel;
    var center = cfg.centerPanel;

    var elMapContent = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        attributes:{
            style:'padding-top: 5px; padding-bottom: 5px; padding-left: 5px;'
        },
        children:[
            {
                elName:'left',
                tag:'div',
                attributes:{
                    style:'float:left; width:33%; min-width:170px; margin-right: 5px;'
                }
            },
            {
                elName:'center',
                tag:'div',
                attributes:{
                    style:'float:left; width:66%; min-width:170px;'
                }
            }
        ]
    });

    elMapContent['left'].append(left.getEl());
    elMapContent['center'].append(center.getEl());

    var setHeight = function (height) {
        var mySize = height - TOMEE.el.getBorderSize(elMapContent.main);
        elMapContent.main.height(mySize);

        var childrenSize = mySize - TOMEE.el.getBorderSize(elMapContent.left);
        elMapContent.left.height(childrenSize);
        elMapContent.center.height(childrenSize);

        left.setHeight(childrenSize);
        center.setHeight(childrenSize);
    };

    return {
        getEl:function () {
            return elMapContent.main;
        },
        setHeight:setHeight
    };
};