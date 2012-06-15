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

TOMEE.ApplicationViewLog = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var panel = TOMEE.components.Panel({
        title:'-',
        bbar:[
            {
                elName:'limitSelector',
                tag:'select',
                children: [
                    {
                        tag: 'option',
                        html: TOMEE.I18N.get('application.log.load.everything'),
                        attributes: {
                            'value': -1
                        }
                    },
                    {
                        tag: 'option',
                        html: TOMEE.utils.stringFormat(TOMEE.I18N.get('application.log.load.last'), {'number': 1000}),
                        attributes: {
                            'value': 1000
                        }
                    },
                    {
                        tag: 'option',
                        html: TOMEE.utils.stringFormat(TOMEE.I18N.get('application.log.load.last'), {'number': 500}),
                        attributes: {
                            'value': 500
                        }
                    },
                    {
                        tag: 'option',
                        html: TOMEE.utils.stringFormat(TOMEE.I18N.get('application.log.load.last'), {'number': 250}),
                        attributes: {
                            'value': 250
                        }
                    },
                    {
                        tag: 'option',
                        html: TOMEE.utils.stringFormat(TOMEE.I18N.get('application.log.load.last'), {'number': 100}),
                        attributes: {
                            'value': 100
                        }
                    },
                    {
                        tag: 'option',
                        html: TOMEE.utils.stringFormat(TOMEE.I18N.get('application.log.load.last'), {'number': 50}),
                        attributes: {
                            'value': 50
                        }
                    },
                    {
                        tag: 'option',
                        html: TOMEE.utils.stringFormat(TOMEE.I18N.get('application.log.load.last'), {'number': 10}),
                        attributes: {
                            'value': 10
                        }
                    }
                ]
            },
            {
                elName:'fileSelector',
                tag:'select',
                attributes:{
                    //style:'margin-right: 2px;'
                }
            },
            {
                elName:'loadBtn',
                tag:'a',
                cls:'btn',
                html:TOMEE.I18N.get('application.log.load'),
                listeners:{
                    'click':function () {
                        var file = panel.getElement('fileSelector').val();
                        var tail = panel.getElement('limitSelector').val();
                        if(tail < 0) {
                            tail = null;
                        }

                        channel.send('trigger.log.load', {
                            file:file,
                            tail:tail
                        });
                        panel.setTitle(file);
                    }
                }
            }
        ]
    });
    panel.getElement('limitSelector').val(100);

    var loadFilesField = function (files) {
        var getOption = function (fileName) {
            var option = $('<option></option>');
            option.attr('value', fileName);
            option.append(fileName);
            return option;
        };

        var selector = panel.getElement('fileSelector');
        selector.empty();
        if (!files) {
            return;
        }
        for (var i = 0; i < files.length; i++) {
            selector.append(getOption(files[i]));
        }
    };

    var loadLogTable = function (data) {
        if (!data || !data.lines) {
            return;
        }

        (function (lines) {
            panel.getContentEl().empty();

            var pre = $('<pre></pre>');
            pre.css('border', '0px');
            pre.css('background-color', 'white');
            pre.css('margin', '0px');

            for (var i = 0; i < lines.length; i++) {
                pre.append(lines[i]);
                pre.append('<br/>');
            }

            panel.getContentEl().append(pre);
            panel.getContentEl().animate({
                scrollTop:pre.height()
            }, 500);
        })(data.lines);

        panel.getElement('fileSelector').val(data.name);
    };

    var loadData = function (data) {
        loadFilesField(data.files);
        loadLogTable(data.log);
    };

    var wrapper = $('<div style="padding: 5px;"></div>')
    wrapper.append(panel.getEl());

    return {

        getEl:function () {
            return wrapper;
        },
        setHeight:function (height) {
            wrapper.height(height);
            var innerSize = height - TOMEE.el.getBorderSize(wrapper);
            panel.setHeight(innerSize);
        },
        loadData:loadData
    };
};