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

TOMEE.ApplicationView = function (cfg) {
    "use strict";

    var channel = cfg.channel;
    var groups = cfg.groups;
    var currentTab = TOMEE.utils.getSafe(cfg.initTab, 'home');

    var windowEl = $(window);

    var toolbar = TOMEE.ApplicationToolbar({
        channel:channel
    });

    channel.bind('toolbar.click', function (params) {
        if (currentTab === params.tab) {
            return;
        }
        showTab(params.tab);
    });

    $('body').append(toolbar.getEl());

    var elMapContent = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        attributes:{
            style:'float:left; width:100%;'
        }
    });

    $('body').append(elMapContent.main);

    var elMapFooter = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        attributes:{
            style:'clear: both;'
        },
        children:[
            {
                tag:'hr',
                attributes:{
                    style:'margin-top: 0px; margin-bottom: 0px;'
                },
                children:[
                    {
                        tag:'footer',
                        html:'<p style="text-align: center;margin-bottom: 0px;">' + TOMEE.I18N.get('application.footer') + '</p>'
                    }
                ]
            }
        ]
    });
    $('body').append(elMapFooter.main);

    var calculateContentSize = function () {
        var myDiv = elMapContent.main;

        var availableSpace = (function () {
            var windowSize = windowEl.height();
            var footerSize = elMapFooter.main.height();
            return windowSize - footerSize - TOMEE.el.getBorderSize(myDiv) - TOMEE.el.getBorderSize($('body'));
        })();

        myDiv.height(availableSpace);

        var group = null;
        for(var key in groups) {
            group = groups[key];
            if(group.setHeight) {
                group.setHeight(availableSpace);
            }
        }
    };

    var showTab = function (tab) {
        var showingTab = groups[currentTab].getEl();
        showingTab.detach();

        var newTab = groups[tab].getEl();
        elMapContent.main.append(newTab);

        currentTab = tab;
    };

    //show current tab
    showTab(currentTab);
    toolbar.setActive(currentTab);

    var delayResize = TOMEE.DelayedTask({
        callback: function() {
            calculateContentSize();
        }
    });
    windowEl.resize(function () {
        delayResize.delay(100);
    });
    calculateContentSize();

    return {
        setLoggedUser:function (name) {
            toolbar.setLoggedUser(name);
        }
    };
};