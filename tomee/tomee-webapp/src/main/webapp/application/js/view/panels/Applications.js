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

TOMEE.Applications = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var frameId = TOMEE.Sequence.next('uploadFrame');

    var panel = TOMEE.components.Panel({
        title:TOMEE.I18N.get('application.deployments'),
        avoidOverflow:true,
        bbar:[
            {
                elName:'myFrame',
                tag:'iframe',
                attributes:{
                    id:frameId,
                    style:'display: none'
                }
            },
            {
                elName:'fileField',
                tag:'input',
                attributes:{
                    //style:'padding-left: 5px; float: left; position: relative;',
                    type:'file',
                    name:'file'
                },
                listeners:{
                    'change':function (event) {
                        panel.getElement('myFrame').bind('load', fileUploadedHandler);
                        panel.getBbarForm().submit();
                    }
                }
            }
        ],
        bbarFormAttributes:{
            method:'post',
            enctype:'multipart/form-data',
            action:TOMEE.baseURL('upload'),
            target:frameId
        }
    });

    var table = TOMEE.components.Table({
        channel:channel,
        columns:['appName']
    });

    var map = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        attributes:{
            style:'height: 220px;'
        },
        children:[
            {
                el:table.getEl()
            }
        ]
    });

    var content = panel.getContentEl();
    content.append(map.main);


    var fileUploadedHandler = function (event) {
        panel.getElement('myFrame').unbind('load', fileUploadedHandler);
        var text = TOMEE.utils.getSafe(function () {
            return panel.getElement('myFrame').contents().first()[0].body.innerText;
        }, '');

        var json = jQuery.parseJSON(text);

        channel.send('deploy.file.uploaded', json);
    };

    return {
        getEl:function () {
            return panel.getEl();
        },
        loadDeployeApps:function (data) {
            table.load(data.uids, function (bean) {
                return bean;
            });
        },
        setHeight:function (height) {
            panel.setHeight(height);

            var myHeight = panel.getContentEl().height() - TOMEE.el.getBorderSize(panel.getContentEl());
            map.main.height(myHeight);
        }
    };
};