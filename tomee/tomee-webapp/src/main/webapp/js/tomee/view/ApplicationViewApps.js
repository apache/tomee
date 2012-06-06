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

TOMEE.ApplicationViewApps = function (cfg) {
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
                    style:'float:left; width:66%; min-width:170px; margin-left: 5px; margin-right: 5px;'
                }
            }
        ]
    });

    var deployments = (function () {
        var panel = TOMEE.components.Panel({
            title:TOMEE.I18N.get('application.deployments')
        });

        var table = TOMEE.components.Table({
            channel:channel
        });

        panel.getContentEl().append(table.getEl());

        return {
            getEl:function () {
                return panel.getEl();
            },
            load:function (data) {
                table.load(data, function (bean) {
                    return [bean.name, bean.value];
                });
            }
        };
    })();


    var log = (function () {
        var panel = TOMEE.components.Panel({
            title:'-'
        });
        panel.getContentEl().append('Log here!');

        return {
            getEl:function () {
                return panel.getEl();
            }
        };
    })();

    elMapContent['left'].append(deployments.getEl());
    elMapContent['center'].append(log.getEl());


    return {
        getEl: function() {
            return elMapContent.main;
        }
    };
};