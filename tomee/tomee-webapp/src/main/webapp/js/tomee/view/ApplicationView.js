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

    var toolbar = TOMEE.ApplicationToolbar({
        channel: channel
    });

    $('body').append(toolbar.getEl());

    var elMapContent = TOMEE.el.getElMap({
        elName: 'main',
        tag: 'div',
        attributes: {
            style:'padding: 5px;'
        },
        children: [{
            elName: 'left',
            tag: 'div',
            attributes: {
                style: 'float:left; width:33%; min-width:170px;'
            }
        }, {
            elName: 'center',
            tag: 'div',
            attributes: {
                style: 'float:left; width:33%; min-width:170px; margin-left: 5px; margin-right: 5px;'
            }
        }, {
            elName: 'right',
            tag: 'div',
            attributes: {
                style: 'float:left; width:33%; min-width:170px;'
            }
        }]
    });

    var jndiPanel = (function() {
        var jndi = TOMEE.components.Panel({
            title: TOMEE.I18N.get('application.jdni')
        });

        var tree = TOMEE.components.Tree({
            channel:channel
        });

        var treeEl = tree.getEl();
        jndi.getContentEl().append(treeEl);

        return {
            getEl: function() {
                return jndi.getEl();
            },
            load: function(data) {
                tree.load(data, function (data) {
                    return data.text;
                }, function (data) {
                    return data.children;
                });
            }
        };
    })();

    var savedPanel = (function() {
        var saved = TOMEE.components.Panel({
            title: TOMEE.I18N.get('application.saved.objects')
        });

        var table = TOMEE.components.Table({
            channel:channel,
            columns:['colA', 'colB']
        });

        saved.getContentEl().append(table.getEl());

        return {
            getEl: function() {
                return saved.getEl();
            },
            load: function(data) {
                table.load(data, function (bean) {
                    return [bean.name, bean.value];
                });
            }
        };
    })();

    elMapContent['left'].append(jndiPanel.getEl());
    elMapContent['left'].append(savedPanel.getEl());


    var consolePanel = (function() {
        var console = TOMEE.components.Panel({
            title: TOMEE.I18N.get('application.console'),
            extraStyles: {
                height: '500px'
            }
        });

        var el = console.getContentEl();
        el.append('<textarea style="height: 470px; width: 100%;border: 0px;padding: 0px;margin: 0px;"></textarea>');
        el.append('<div style="height: 30px; background-color: red;"><div style="float: right; position: relative; background-color: blue; width: 30px; height: 30px"></div><div style="float: right; position: relative; background-color: yellow; width: 30px; height: 30px"></div></div>');


        return {
            getEl: function() {
                return console.getEl();
            }
        };
    })();

    elMapContent['center'].append(consolePanel.getEl());

    var mdbsPanel = (function() {
        var mdbs = TOMEE.components.Panel({
            title: TOMEE.I18N.get('application.mdbs')
        });

        return {
            getEl: function() {
                return mdbs.getEl();
            }
        };
    })();

    var wsPanel = (function() {
        var ws = TOMEE.components.Panel({
            title: TOMEE.I18N.get('application.ws')
        });

        return {
            getEl: function() {
                return ws.getEl();
            }
        };
    })();

    elMapContent['right'].append(mdbsPanel.getEl());
    elMapContent['right'].append(wsPanel.getEl());

    $('body').append(elMapContent.main);

    var elMapFooter = TOMEE.el.getElMap({
        elName: 'main',
        tag: 'div',
        attributes: {
            style: 'clear: both;'
        },
        children: [{
            tag: 'hr',
            attributes: {
                style: 'margin-top: 0px; margin-bottom: 0px;'
            },
            children:[{
                tag: 'footer',
                html: '<p style="text-align: center">' + TOMEE.I18N.get('application.footer') + '</p>'
            }]
        }]
    });
    $('body').append(elMapFooter.main);

    return {
        setLoggedUser:function (name) {
            toolbar.setLoggedUser(name);
        } ,
        loadJndi: function(data) {
            jndiPanel.load(data);
        },
        loadSavedObjects: function(data) {
            savedPanel.load(data);
        }

    };
};