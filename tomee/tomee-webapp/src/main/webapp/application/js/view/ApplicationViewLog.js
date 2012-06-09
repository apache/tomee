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

    var elMapContent = TOMEE.el.getElMap({
        elName:'main',
        tag:'div'
    });

    var lines = (function () {
        var innerPanel = $('<div style="height: 250px; position: relative; overflow: auto;"></div>');

        return {
            getEl:function () {
                return innerPanel;
            },
            load:function (data) {
                innerPanel.empty();

                var newData = $('<div></div>');
                for (var i = 0; i < data.length; i++) {
                    newData.append(data[i]);
                    newData.append('<br/>');
                }

                innerPanel.append(newData);
                //innerPanel.scrollTop(newData.height());
                innerPanel.animate({
                    scrollTop: newData.height()
                }, 500);
            }
        }
    })();

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
                        elName:'fileSelector',
                        tag:'select',
                        attributes:{
                            style:'margin-right: 2px;'
                        }
                    },
                    {
                        elName:'loadBtn',
                        tag:'button',
                        cls:'btn',
                        html:TOMEE.I18N.get('application.log.load')
                    }
                ]
            }
        ]
    });

    elBottomBar.loadBtn.bind('click', function () {
        var file = elBottomBar.fileSelector.val();
        var tail = 100; //TODO
        channel.send('trigger.log.load', {
            file:file,
            tail:tail
        });
    });

    elMapContent.main.append(lines.getEl());
    elMapContent.main.append(elBottomBar.main);

    var setHeight = function (height) {
        var mySize = height - TOMEE.el.getBorderSize(elMapContent.main);
        var gridSize = mySize - elBottomBar.main.outerHeight(true);

        elMapContent.main.height(mySize);
        lines.getEl().height(gridSize);
    };

    var loadFilesField = function (files) {
        var getOption = function (fileName) {
            var option = $('<option></option>');
            option.attr('value', fileName);
            option.append(fileName);
            return option;
        };

        var selector = elBottomBar.fileSelector;
        selector.empty();
        if (!files) {
            return;
        }
        for (var i = 0; i < files.length; i++) {
            selector.append(getOption(files[i]));
        }
    };

    var loadLogTable = function (data) {
        if (!data) {
            return;
        }
        lines.load(data.lines);
        elBottomBar.fileSelector.val(data.name);
    };

    var loadData = function (data) {
        loadFilesField(data.files);
        loadLogTable(data.log);
    };

    return {

        getEl:function () {
            return elMapContent.main;
        },
        setHeight:setHeight,
        loadData:loadData
    };
};