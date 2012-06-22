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
    var sessionData = {};
    var executions = [];

    var request = function (params) {
        var errorHandler = params.error;
        if (!errorHandler) {
            errorHandler = function (jqXHR, textStatus, errorThrown) {
                channel.send('default.ajax.error.handler.triggered', {
                    jqXHR:jqXHR,
                    textStatus:textStatus,
                    errorThrown:errorThrown
                });
            }
        }
        $.ajax({
                url:params.url,
                type:params.method,
                data:params.data,
                dataType:'json',
                success:function (data) {
                    if (params.success) {
                        params.success(data);
                    }

                    channel.send('new.data', data);
                },
                error:errorHandler
            }
        );
    };

    var getUrlVars = function () {
        var vars = {};
        var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');

        var hash = null;
        for (var i = 0; i < hashes.length; i++) {
            hash = hashes[i].split('=');
            vars[hash[0]] = hash[1];
        }
        return vars;
    };

    return {
        getUrlVars:getUrlVars,
        logout:function () {
            request({
                method:'GET',
                url:TOMEE.baseURL('command'),
                data:{
                    cmd:'Logout'
                },
                success:function () {
                    channel.send('app.logout.bye', {});
                }
            });
        },
        deployApp:function (path) {
            request({
                method:'POST',
                url:TOMEE.baseURL('command'),
                data:{
                    cmd:'DeployApplication,GetDeployedApplications',
                    path:path
                }
            });
        },
        loadDeployedApps:function () {
            request({
                method:'GET',
                url:TOMEE.baseURL('command'),
                data:{
                    cmd:'GetDeployedApplications'
                }
            });
        },
        loadSystemInfo:function (callback) {
            request({
                method:'GET',
                url:TOMEE.baseURL('command'),
                data:{
                    cmd:'GetSystemInfo'
                },
                success:function (data) {
                    systemInfo = data;
                    channel.send('app.system.info', data['GetSystemInfo']);

                    if (callback) {
                        callback(data);
                    }
                }
            });
        },
        execute:function (codeType, codeText) {
            var executionBean = {
                codeType:codeType,
                codeText:codeText,
                start:(new Date())
            };
            executions.push(executionBean);

            request({
                method:'POST',
                url:TOMEE.baseURL('command'),
                data:{
                    cmd:'RunScript,GetSessionData',
                    engineName:codeType,
                    scriptCode:codeText
                },
                success:function (data) {
                    executionBean.success = true;
                    executionBean.data = data['GetSystemInfo'];
                    executionBean.end = (new Date());

                    channel.send('app.console.executed', executionBean);
                }
//                ,
//                error:function (data) {
//                    executionBean.success = false;
//                    executionBean.data = data['GetSystemInfo'];
//                    executionBean.end = (new Date());
//
//                    channel.send('app.console.executed.error', executionBean);
//                }
            });

        },
        loadLog:function (file, tail) {
            var data = {
                cmd:'GetLog',
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
                url:TOMEE.baseURL('command'),
                data:data
            });
        },
        loadSessionData:function () {
            request({
                method:'GET',
                url:TOMEE.baseURL('command'),
                data:{
                    cmd:'GetSessionData'
                },
                success:function (data) {
                    sessionData = data;
                }
            });
        },
        loadJndi:function (params) {
            //params.path, params.bean, params.parentEl
            request({
                method:'GET',
                url:TOMEE.baseURL('command'),
                data:{
                    cmd:'GetJndiTree',
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
                        names:data['GetJndiTree'].names,
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
                url:TOMEE.baseURL('command'),
                data:{
                    cmd:'GetJndiTree',
                    name:params.name,
                    path:TOMEE.utils.getSafe(params.path, []).join(',')
                },
                success:function (data) {
                    channel.send('app.new.jndi.class.data', {
                        cls:data['GetJndiTree'].cls,
                        name:params.name,
                        parent:params.parent,
                        path:params.path
                    });
                }
            });
        },
        lookupJndi:function (params) {
            //params.path, params.bean, params.parentEl
            request({
                method:'POST',
                url:TOMEE.baseURL('command'),
                data:{
                    cmd:'JndiLookup',
                    name:params.name,
                    path:TOMEE.utils.getSafe(params.path, []).join(','),
                    saveKey:params.saveKey
                },
                success:function (data) {
                    channel.send('app.new.jndi.bean', {
                        name:params.name,
                        path:params.path,
                        saveKey:params.saveKey
                    });
                }
            });
        }
    };
}