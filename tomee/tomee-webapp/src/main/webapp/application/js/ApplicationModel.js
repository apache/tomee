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

/**
 * This object handles all the data manipulation.
 *
 * @param cfg
 */
TOMEE.ApplicationModel = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var systemInfo = {};
    var logInfo = {};
    var sessionData = {};

    var request = function (params) {
        $.ajax({
                url:params.url,
                type:params.method,
                data:params.data,
                dataType:'json',
                success:params.success,
                error:params.error
            }
        );
    };

    return {
        logout:function () {
            request({
                method:'GET',
                url:TOMEE.baseURL('logout'),
                success:function () {
                    channel.send('app.logout.bye', {});
                }
            });
        },
        deployApp:function (path) {
            request({
                method:'POST',
                url:TOMEE.baseURL('deploy'),
                data:{
                    path:path
                },
                success:function (data) {
                    channel.send('app.deployment.result', data);
                }
            });
        },
        loadSystemInfo:function (callback) {
            request({
                method:'GET',
                url:TOMEE.baseURL('system'),
                success:function (data) {
                    systemInfo = data;
                    channel.send('app.system.info', data);

                    if (callback) {
                        callback(data);
                    }
                }
            });
        },
        getSystemInfo:function () {
            return systemInfo;
        },
        execute:function (codeType, codeText) {
            request({
                method:'POST',
                url:TOMEE.baseURL('console'),
                data:{
                    engineName:codeType,
                    scriptCode:codeText
                },
                success:function (data) {
                    systemInfo = data;
                    channel.send('app.console.executed', data);
                }
            });

        },
        loadLog:function (file, tail) {
            var data = {
                escapeHtml:true
            };

            if (file) {
                data.file = file;
            }

            if (tail) {
                data.tail = tail;
            }

            request({
                method:'GET',
                url:TOMEE.baseURL('log'),
                data:data,
                success:function (data) {
                    logInfo = data;
                    channel.send('app.new.log.data', data);
                }
            });
        },
        getLogInfo:function () {
            return logInfo;
        },
        loadSessionData:function () {
            request({
                method:'GET',
                url:TOMEE.baseURL('data'),
                success:function (data) {
                    sessionData = data;
                    channel.send('app.new.session.data', data);
                }
            });
        },
        loadJndi:function (params) {
            //params.path, params.bean, params.parentEl
            request({
                method:'GET',
                url:TOMEE.baseURL('jndi'),
                data:{
                    path:TOMEE.utils.getSafe(params.path, []).join(',')
                },
                success:function (data) {
                    var namesArr = TOMEE.utils.getArray(data.names);

                    (function () {
                        var current = null;
                        for (var i = 0; i < namesArr.length; i++) {
                            current = namesArr[i];
                            current.parent = params.bean;
                        }
                    })();

                    channel.send('app.new.jndi.data', {
                        names:data.names,
                        path:params.path,
                        bean:params.bean,
                        parentEl:params.parentEl
                    });
                }
            });
        },
        loadJndiClass:function (params) {
            //params.path, params.bean, params.parentEl
            request({
                method:'GET',
                url:TOMEE.baseURL('jndi'),
                data:{
                    name:params.name,
                    path:TOMEE.utils.getSafe(params.path, []).join(',')
                },
                success:function (data) {
                    channel.send('app.new.jndi.class.data', {
                        cls: data.cls
                    });
                }
            });
        }
    };
}