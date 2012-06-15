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

TOMEE.ApplicationViewHome = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var jndiPanel = cfg.jndiPanel;
    var savedPanel = cfg.savedPanel;
    var mdbsPanel = cfg.mdbsPanel;
    var wsPanel = cfg.wsPanel;
    var consolePanel = cfg.consolePanel;

    var elMapContent = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        attributes:{
            style:'position:relative; padding-top: 5px; padding-bottom: 5px; padding-left: 5px;'
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
                    style:'float:left; width:33%; min-width:170px;'
                }
            },
            {
                elName:'right',
                tag:'div',
                attributes:{
                    style:'float:left; width:33%; min-width:170px; margin-left: 5px;'
                }
            }
        ]
    });

    elMapContent['left'].append((function () {
        var wrapper = $('<div style="padding-bottom: 5px"></div>');
        wrapper.append(jndiPanel.getEl());
        return wrapper;
    })());
    elMapContent['left'].append(savedPanel.getEl());


    elMapContent['center'].append(consolePanel.getEl());

    elMapContent['right'].append(mdbsPanel.getEl());
    elMapContent['right'].append(wsPanel.getEl());

    var setHeight = function (height) {
        var mySize = height - TOMEE.el.getBorderSize(elMapContent.main);
        elMapContent.main.height(mySize);

        var childrenSize = mySize - TOMEE.el.getBorderSize(elMapContent.left);
        elMapContent.left.height(childrenSize);
        elMapContent.center.height(childrenSize);

        consolePanel.setHeight(childrenSize);
    };

    return {
        setHeight:setHeight,
        getEl:function () {
            return elMapContent.main;
        },
        setTomeeVersion:function (myTomee) {

            mdbsPanel.getEl().detach();
            wsPanel.getEl().detach();
            elMapContent['center'].css('width', '66%');

            //TODO mdbsPanel and wsPanel are not implemented yet. Add them when done.

            /*

            if (!myTomee.hasMdbs && !myTomee.hasWebservices) {
                elMapContent['right'].detach();
                elMapContent['center'].css('width', '66%');
                elMapContent['center'].css('margin-right', '0px');

            } else {
                if (!myTomee.hasMdbs) {
                    mdbsPanel.getEl().detach();
                }

                if (!myTomee.hasWebservices) {
                    wsPanel.getEl().detach();
                }
            }

            */
        }
    };
}
;