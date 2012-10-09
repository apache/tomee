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

TOMEE.ApplicationView = function () {
    "use strict";

    var channel = TOMEE.ApplicationChannel,
        panelMap = {
            'console':TOMEE.ApplicationTabConsole(),
            'log':TOMEE.ApplicationTabLog()
        },
        selected = null,
        container = $(TOMEE.ApplicationTemplates.getValue('application', {})),
        containerWrapper = $('<div class="tomee-container-wrapper"></div>'),
        toolbar = TOMEE.ApplicationToolbarView(),
        myWindow = $(window),
        delayedContainerResize = TOMEE.DelayedTask();

    container.appendTo(containerWrapper);

    channel.bind('ui-actions', 'toolbar-click', function (data) {
        switchPanel(data.key);
    });

    //disable default contextmenu
    $(document).bind("contextmenu", function (e) {
        return false;
    });

    myWindow.on('resize', function () {
        delayedContainerResize.delay(updateContainerSize, 100);
    });

    function switchPanel(key) {
        if (selected) {
            selected.getEl().detach();
            selected.onDetach();
        }
        selected = panelMap[key];
        selected.getEl().appendTo(container);
        selected.onAppend();
    }

    function updateContainerSize() {
        var containerHeight,
            containerWidth,
            toolbarHeight = toolbar.getEl().height();
        containerWrapper.css('top', toolbarHeight + 'px');

        containerHeight = (myWindow.height() - toolbarHeight);
        containerWidth = myWindow.width();

        containerWrapper.css('height', containerHeight + 'px');
        containerWrapper.css('width', containerWidth + 'px');

        channel.send('ui-actions', 'container-resized', {
            containerHeight:containerHeight,
            containerWidth:containerWidth,
            toolbarHeight: toolbarHeight
        });
    }

    return {
        render:function () {
            var myBody = $('body'),
                emptyDiv = $('<div></div>');
            myBody.append(emptyDiv);
            myBody.append(toolbar.getEl());
            myBody.append(containerWrapper);

            emptyDiv.height(toolbar.getEl().height());

            switchPanel('console');
            updateContainerSize();
        }
    };
};