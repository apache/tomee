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

    var jndiMenu = TOMEE.components.Menu({
        commands:[
            {
                text:'commandA',
                callback:function () {
                }
            },
            {
                text:'commandB',
                callback:function () {
                }
            },
            {
                text:'commandC',
                callback:function () {
                }
            }
        ]
    });
//    jndiMenu.showAt({
//        left:100,
//        top:100
//    });

    var elMapContent = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        attributes:{
            style:'position:relative; padding: 5px;'
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
                return data.name;
            },
            childrenPropertyName:'children'
        });

        var treeEl = tree.getEl();
        jndi.getContentEl().append(treeEl);

        return {
            getEl:function () {
                return jndi.getEl();
            },
            load:function (data) {
                tree.load(data.parentEl, data.names);
            }
        };
    })();

    var savedPanel = (function () {
        var saved = TOMEE.components.Panel({
            title:TOMEE.I18N.get('application.saved.objects'),
            actions:[
                {
                    text:TOMEE.I18N.get('application.saved.objects.load'),
                    listeners:{
                        'click':function () {
                            channel.send('application.saved.objects.load', {});
                        }
                    }

                }
            ]
        });

        var table = TOMEE.components.Table({
            channel:channel
        });

        saved.getContentEl().append(table.getEl());

        return {
            getEl:function () {
                return saved.getEl();
            },
            load:function (data) {
                var arr = TOMEE.utils.toArray(data, function (key, obj) {
                    return {
                        name:key,
                        value:obj
                    };
                });

                table.load(arr, function (bean) {
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

        var elText = TOMEE.el.getElMap({
            elName:'main',
            tag:'textarea',
            attributes:{
                style:'height: 469px; width: 100%;border: 0px;padding: 0px;margin: 0px;'
            }
        });

        var elBottomBar = TOMEE.el.getElMap({
            elName:'main',
            tag:'form',
            cls:'well form-inline',
            attributes:{
                style:'height: 27px;margin-bottom: 0px;padding-top: 1px;padding-left: 1px;padding-bottom: 1px;padding-right: 1px;'
            },
            children:[
                {
                    tag:'div',
                    cls:'pull-right',
                    children:[
                        {
                            elName:'scriptSelector',
                            tag:'select'
                        },
                        {
                            elName:'executeBtn',
                            tag:'button',
                            cls:'btn',
                            html:TOMEE.I18N.get('application.console.execute')
                        }
                    ]
                }
            ]
        });

        var el = console.getContentEl();
        el.append(elText.main);
        el.append(elBottomBar.main);

        elBottomBar.main.bind('click', function () {
            var text = elText.main.val();
            var script = elBottomBar.scriptSelector.val();
            channel.send('trigger.console.exec', {
                codeType:script,
                codeText:text
            });
        });

        return {
            getEl:function () {
                return console.getEl();
            },
            scriptSelector:elBottomBar.scriptSelector
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

    var loadScriptsField = function (languages) {
        var getOption = function (lang) {
            var option = $('<option></option>');
            option.attr('value', lang);
            option.append(lang);
            return option;
        };

        var selector = consolePanel.scriptSelector;
        selector.empty();
        if (!languages) {
            return;
        }
        for (var i = 0; i < languages.length; i++) {
            selector.append(getOption(languages[i]));
        }
    };

    return {
        loadJndi:function (data) {
            jndiPanel.load(data);
        },
        loadSavedObjects:function (data) {
            savedPanel.load(data);
        },
        getEl:function () {
            return elMapContent.main;
        },
        setSupportedScriptLanguages:loadScriptsField,
        setTomeeVersion:function (myTomee) {

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
        }
    };
};