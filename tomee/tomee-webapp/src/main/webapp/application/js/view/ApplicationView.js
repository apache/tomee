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

    //disable default contextmenu
    $(document).bind("contextmenu",function(e){
        return false;
    });

    var toolbar = TOMEE.ApplicationToolbar({
        channel:channel
    });

    channel.bind('toolbar.click', function (params) {
        if (currentTab === params.tab) {
            return;
        }
        showTab(params.tab);
    });

    var elMapContent = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        attributes:{
            style:'float:left; width:100%;'
        }
    });

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
                }
            },
            {
                tag:'footer',
                html:'<p style="text-align: center;margin-bottom: 0px;">' + TOMEE.I18N.get('application.footer') + '</p>'
            }
        ]
    });

    var calculateContentSize = function () {
        var myDiv = elMapContent.main;

        var availableSpace = (function () {
            var windowSize = windowEl.height();
            var footerSize = elMapFooter.main.height();
            return windowSize - footerSize - toolbar.getEl().outerHeight(true) - TOMEE.el.getBorderSize(myDiv) - TOMEE.el.getBorderSize($('body'));
        })();

        myDiv.height(availableSpace);

        var group = null;
        for (var key in groups) {
            group = groups[key];
            if (group.setHeight) {
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

        //adjust the size
        calculateContentSize();
    };

    //show current tab
    showTab(currentTab);
    toolbar.setActive(currentTab);

    var delayResize = TOMEE.DelayedTask({
        callback:function () {
            calculateContentSize();
        }
    });
    windowEl.resize(function () {
        delayResize.delay(100);
    });
    calculateContentSize();

    delayResize.delay(1000);

    return {
        setLoggedUser:function (name) {
            toolbar.setLoggedUser(name);
        },
        render:function () {
            var myBody = $('body');
            myBody.append(toolbar.getEl());
            myBody.append(elMapContent.main);
            myBody.append(elMapFooter.main);
        },
        setTomeeVersion:function (myTomee) {
            toolbar.setAppType(myTomee.name);
        }
    };
};