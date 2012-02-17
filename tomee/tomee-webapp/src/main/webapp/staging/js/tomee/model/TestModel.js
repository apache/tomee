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
TOMEE.TestModel = function (cfg) {
    var channel = cfg.channel;

    var myData = null;

    var myModel = (function () {
        var myChannel = TOMEE.ApplicationChannel({});

        myChannel.bind('connection_new_data', function (params) {
            channel.send('test_connection_new_data', params);
        });
        myChannel.bind('connection_exception', function (params) {
            channel.send('test_connection_exception', params);
        });

        return TOMEE.ApplicationModel({
            methodType: 'GET',

            url: '/tomee/ws/test/test',
            //url: 'js/tomee/mock/test.json',

            channel: myChannel,
            prepareDataMethod: function (data) {
                myData = data;
            }
        });
    })();

    var iterateTestBeans = function (callback) {
        $.each(myData.test, function (i, bean) {
            callback(bean);
        });
    };

    return $.extend({}, {
        iterateTestBeans: iterateTestBeans
    }, myModel);
};