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

    var getLastScript = function () {
        return TOMEE.utils.getSafe(TOMEE.storage.getSession('lastScript_code'), (function () {
            $.ajax({
                    url:'application/js/SampleScript.js',
                    method:'GET',
                    dataType:'text',
                    success:function (data) {
                        channel.send('default.script.loaded', data);
                    }
                }
            );

            //for now just return ''
            return '';
        })());
    };

    var setLastScript = function (code) {
        TOMEE.storage.setSession('lastScript_code', code);
    };

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

    var executeCommands = function () {
        var myCommands = [];
        for (var i = 0; i < arguments.length; i++) {
            var myArg = TOMEE.utils.getArray(arguments[i]);
            for (var ii = 0; ii < myArg.length; ii++) {
                myCommands.push(myArg[ii]);
            }
        }

        var cmdNames = [];
        var asyncCmdNames = [];

        var successCallbacks = [];
        var errorCallbacks = [];

        var executeCallbacks = function (callbacks, data) {
            for (var i = 0; i < callbacks.length; i++) {
                callbacks[i](data);
            }
        }

        var myFinalCommand = {
            method:'POST',
            url:TOMEE.baseURL('command'),
            data:{},
            success:function (data) {
                executeCallbacks(successCallbacks, data);
            },
            error:function (data) {
                executeCallbacks(errorCallbacks, data);
            }
        };

        var cmdConfig = null;
        var paramsData = null;
        for (var i = 0; i < myCommands.length; i++) {
            cmdConfig = myCommands[i];
            if (cmdConfig.cmd) {
                if (cmdConfig.async) {
                    asyncCmdNames.push(cmdConfig.cmd);
                } else {
                    cmdNames.push(cmdConfig.cmd);
                }
            }

            paramsData = TOMEE.utils.getObject(cmdConfig.data);
            for (var key in paramsData) {
                myFinalCommand.data[key] = paramsData[key];
            }

            if (cmdConfig.success) {
                successCallbacks.push(cmdConfig.success);
            }

            if (cmdConfig.error) {
                errorCallbacks.push(cmdConfig.error);
            }
        }

        if (cmdNames.length > 0) {
            myFinalCommand.data.cmd = cmdNames.join(',');
        }

        if (asyncCmdNames.length > 0) {
            myFinalCommand.data.asyncCmd = asyncCmdNames.join(',');
        }

        if (errorCallbacks.length === 0) {
            delete myFinalCommand.error;
        }

        request(myFinalCommand);
    };

    return {
        getLastScript:getLastScript,
        getUrlVars:getUrlVars,
        executeCommands:executeCommands,
        logout:function () {
            return {
                cmd:'Logout',
                success:function () {
                    channel.send('app.logout.bye', {});
                }
            };
        },
        deployApp:function (path) {
            return [
                {
                    cmd:'DeployApplication',
                    data:{
                        path:path
                    }

                },
                {
                    cmd:'GetDeployedApplications'
                }
            ];
        },
        loadDeployedApps:function () {
            return {
                cmd:'GetDeployedApplications'
            };
        },
        loadSystemInfo:function (callback) {
            return {
                cmd:'GetSystemInfo',
                success:function (data) {
                    systemInfo = data;
                    channel.send('app.system.info', data['GetSystemInfo']);

                    if (callback) {
                        callback(data);
                    }
                }
            };
        },
        execute:function (params) {
            var executionBean = {
                params:params,
                start:(new Date())
            };
            executions.push(executionBean);

            return [
                {
                    cmd:'RunScript',
                    data:params,
                    success:function (data) {
                        setLastScript(params.scriptCode);

                        executionBean.success = true;
                        executionBean.data = data['GetSystemInfo'];
                        executionBean.end = (new Date());

                        channel.send('app.console.executed', executionBean);
                    }
                },
                {
                    cmd:'GetSessionData'
                }
            ];
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

            return {
                cmd:'GetLog',
                data:data
            };
        },
        loadSessionData:function () {
            return {
                cmd:'GetSessionData',
                success:function (data) {
                    sessionData = data;
                }
            };
        },
        loadJndi:function (params) {
            //params.path, params.bean, params.parentEl
            return {
                cmd:'GetJndiTree',
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
                        names:data['GetJndiTree'].names,
                        path:params.path,
                        bean:params.bean,
                        parentEl:params.parentEl
                    });
                }
            };
        },
        loadJndiClass:function (params) {
            //params.path, params.bean, params.parentEl
            return {
                cmd:'GetJndiTree',
                data:{
                    name:params.data.name,
                    path:TOMEE.utils.getSafe(params.data.ctxPath, '')
                },
                success:function (data) {
                    channel.send('app.new.jndi.class.data', {
                        cls:data['GetJndiTree'].cls,
                        name:params.data.name,
                        path:TOMEE.utils.getSafe(params.data.ctxPath, '')
                    });
                }
            };
        },
        lookupJndi:function (params) {
            //params.path, params.bean, params.parentEl
            return [
                {
                    cmd:'JndiLookup',
                    data:{
                        name:params.name,
                        path:params.path,
                        saveKey:params.saveKey
                    },
                    success:function (data) {
                        channel.send('app.new.jndi.bean', {
                            name:params.name,
                            path:params.path,
                            saveKey:params.saveKey
                        });
                    }
                },
                {
                    cmd:'GetSessionData'
                }
            ];
        }
    };
}