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

    var elMapContent = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        attributes:{
            style:'padding: 5px;'
        },
        children:[
            {
                elName:'left',
                tag:'div',
                attributes:{
                    style:'float:left; width:33%; min-width:170px;'
                }
            },
            {
                elName:'center',
                tag:'div',
                attributes:{
                    style:'float:left; width:33%; min-width:170px; margin-left: 5px; margin-right: 5px;'
                }
            },
            {
                elName:'right',
                tag:'div',
                attributes:{
                    style:'float:left; width:33%; min-width:170px;'
                }
            }
        ]
    });

    var jndiPanel = (function () {
        var jndi = TOMEE.components.Panel({
            title:TOMEE.I18N.get('application.jdni')
        });

        var tree = TOMEE.components.Tree({
            key:'jndi',
            channel:channel,
            getText:function (data) {
                return data.text;
            },
            getChildren:function (data) {
                return data.children;
            }
        });

        var treeEl = tree.getEl();
        jndi.getContentEl().append(treeEl);

        return {
            getEl:function () {
                return jndi.getEl();
            },
            load:function (data) {
                tree.load(data);
            }
        };
    })();

    var savedPanel = (function () {
        var saved = TOMEE.components.Panel({
            title:TOMEE.I18N.get('application.saved.objects')
        });

        var table = TOMEE.components.Table({
            channel:channel,
            columns:['colA', 'colB']
        });

        saved.getContentEl().append(table.getEl());

        return {
            getEl:function () {
                return saved.getEl();
            },
            load:function (data) {
                table.load(data, function (bean) {
                    return [bean.name, bean.value];
                });
            }
        };
    })();

    elMapContent['left'].append(jndiPanel.getEl());
    elMapContent['left'].append(savedPanel.getEl());


    var consolePanel = (function () {
        var console = TOMEE.components.Panel({
            title:TOMEE.I18N.get('application.console'),
            extraStyles:{
                height:'500px'
            }
        });

        var el = console.getContentEl();
        el.append('<textarea style="height: 469px; width: 100%;border: 0px;padding: 0px;margin: 0px;"></textarea>');
        el.append('<div style="background-color:#EEE; border-top: 1px solid #E5E5E5; height: 30px;"><div class="t-action-btn"></div><div class="t-action-btn"></div></div>');


        return {
            getEl:function () {
                return console.getEl();
            }
        };
    })();

    elMapContent['center'].append(consolePanel.getEl());

    var mdbsPanel = (function () {
        var mdbs = TOMEE.components.Panel({
            title:TOMEE.I18N.get('application.mdbs')
        });

        return {
            getEl:function () {
                return mdbs.getEl();
            }
        };
    })();

    var wsPanel = (function () {
        var ws = TOMEE.components.Panel({
            title:TOMEE.I18N.get('application.ws')
        });

        return {
            getEl:function () {
                return ws.getEl();
            }
        };
    })();

    elMapContent['right'].append(mdbsPanel.getEl());
    elMapContent['right'].append(wsPanel.getEl());

    return {
        setLoggedUser:function (name) {
            toolbar.setLoggedUser(name);
        },
        loadJndi:function (data) {
            jndiPanel.load(data);
        },
        loadSavedObjects:function (data) {
            savedPanel.load(data);
        },
        getEl: function() {
            return elMapContent.main;
        }
    };
};