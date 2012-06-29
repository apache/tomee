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

TOMEE.Console = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var elText = TOMEE.el.getElMap({
        elName:'main',
        tag:'textarea',
        attributes:{
            style:'width: 100%;border: 0px;padding: 0px;margin: 0px;'
        }
    });

    var console = TOMEE.components.Panel({
        title:TOMEE.I18N.get('application.console'),
        extraStyles:{
            height:'500px'
        },
        onResize:function (height) {
            elText.main.height(height);
        },
        bbar:[
            {
                elName:'appSelector',
                tag:'select'
            },
            {
                elName:'scriptSelector',
                tag:'select'
            },
            {
                tag:'a',
                cls:'btn',
                html:TOMEE.I18N.get('application.console.execute'),
                listeners:{
                    'click':function () {
                        var text = elText.main.val();
                        var script = console.getElement('scriptSelector').val();
                        var app = console.getElement('appSelector').val();
                        channel.send('trigger.console.exec', {
                            engineName:script,
                            scriptCode:text,
                            appName:app
                        });
                    }
                }
            }
        ]
    });

    var el = console.getContentEl();
    el.append(elText.main);

    var loadSelector = function (values, getValueBean, selector) {
        var getOption = function (valueBean) {
            var option = $('<option></option>');
            option.attr('value', valueBean.value);
            option.append(valueBean.text);
            return option;
        };

        selector.empty();
        if (!values) {
            return;
        }
        for (var i = 0; i < values.length; i++) {
            selector.append(getOption(getValueBean(values[i])));
        }
    };

    var loadScriptsField = function (languages) {
        loadSelector(languages, function (bean) {
            return {
                value:bean,
                text:bean
            };
        }, console.getElement('scriptSelector'));
    };

    var loadAppsField = function (apps) {
        loadSelector(apps, function (bean) {
            return {
                value:bean,
                text:bean
            };
        }, console.getElement('appSelector'));
    };

    return {
        setScript:function (script) {
            elText.main.html(script);
        },
        setHeight:function (height) {
            console.setHeight(height);
        },
        getEl:function () {
            return console.getEl();
        },
        loadScriptsField:loadScriptsField,
        loadAppsField:loadAppsField
    };
};