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

    elBottomBar.executeBtn.bind('click', function () {
        var text = elText.main.val();
        var script = elBottomBar.scriptSelector.val();
        channel.send('trigger.console.exec', {
            codeType:script,
            codeText:text
        });
    });

    var loadScriptsField = function (languages) {
        var getOption = function (lang) {
            var option = $('<option></option>');
            option.attr('value', lang);
            option.append(lang);
            return option;
        };

        var selector = elBottomBar.scriptSelector;
        selector.empty();
        if (!languages) {
            return;
        }
        for (var i = 0; i < languages.length; i++) {
            selector.append(getOption(languages[i]));
        }
    };

    return {
        getEl:function () {
            return console.getEl();
        },
        loadScriptsField:loadScriptsField
    };
};