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
    var channel = cfg.channel;

    //holder for all the request parameters.
    var requestParameters = {};

    //keep tracking of the current request
    //so we can cancel it if necessary
    var currentRequest = null;

    var getArray = function (obj) {
        if (!obj) {
            return [];
        }

        if (obj instanceof Array) {
            return obj;
        }

        return [obj];
    };

    var getObject = function (obj) {
        if (!obj) {
            return {};
        }
        return obj;
    };

    /**
     * Prepare internal values.
     *
     * @param data request json value
     */
    var prepareData = function (data) {

    };

    /**
     * Delayed task for the remote request.
     */
    var load = new TOMEE.DelayedTask({
        callback:function () {
            //if we already have a running request, cancel it.
            if (currentRequest) {
                currentRequest.abort();
            }

            //start a new request
            currentRequest = $.ajax({
                type:'GET',
                url:'some.servlet',
                success:function (data) {

                },
                error:function (data) {

                }
            });
        }
    });

    var getRequestParameter = function (key) {
        return requestParameters[key];
    };

    var setRequestParameter = function (key, value) {
        requestParameters[key] = value;
    };

    return {
        setRequestParameter:setRequestParameter,
        getRequestParameter:getRequestParameter,
        load:function () {
            //wait 1 second before triggering this request
            //the user may be still selecting his parameters
            //the last calling thread will trigger the request
            load.delay(1000);
        }
    };
};