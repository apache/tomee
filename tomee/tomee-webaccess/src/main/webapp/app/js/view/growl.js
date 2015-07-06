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

(function () {
    'use strict';

    var deps = ['app/js/templates', 'lib/underscore'];
    define(deps, function (templates, underscore) {

        var GrowlContainerView = Backbone.View.extend({
            el: 'body',
            render: function () {
                if (this.options.isRendered) {
                    return this;
                }

                var me = this;
                var container = $(templates.getValue('growl-container', {}));
                me.$el.append(container[0]);
                me.containerEl = $(container[0]);

                // render it only once
                this.options.isRendered = true;
                return this;
            }
        });
        var container = new GrowlContainerView({});

        function showNotification(messageType, messageText) {
            container.render();

            var alert = $(templates.getValue('growl', {
                messageType: messageType,
                messageText: messageText
            }));

            container.containerEl.append(alert[0]);
            alert.fadeIn();
            var faceOutCallback = function () {
                alert.fadeOut(null, function () {
                    try {
                        alert.remove();
                    } catch (ignore) { /* noop */
                    }
                });
            };
            var faceOut = underscore.debounce(faceOutCallback, 5000);
            faceOut();
        }

        return {
            showNotification: showNotification
        };
    });
}());
