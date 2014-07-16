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
 "use strict";
 */

define(['app/js/log', 'lib/jquery'], function () {
    'use strict';

    var DELAY = 1000 * 60 * 4; // 4 minutes
    var timeoutKey = null;

    function scheduleNext() {
        if (timeoutKey !== null) {
            window.clearInterval(timeoutKey);
            window.console.log('keep-alive callback canceled.', timeoutKey);
            timeoutKey = null;
        }
        function timeoutCallback() {
            $.ajax({
                type: 'GET',
                'url': window.ux.ROOT_URL + 'rest/keep-alive',
                global: false,
                data: {},
                success: function () {
                    scheduleNext();
                },
                error: function () {
                    window.console.error('keep-alive callback error.');
                    window.setTimeout(function () {
                        window.location.reload();
                    }, 10000);
                }
            });
        }

        timeoutKey = window.setTimeout(timeoutCallback, DELAY);
        window.console.log('keep-alive callback created.', timeoutKey);
    }

    $(window.document).bind("ajaxSend", function () {
        scheduleNext();
    });
    scheduleNext();
});
