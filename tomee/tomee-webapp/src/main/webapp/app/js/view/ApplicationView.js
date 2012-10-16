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

    myWindow.on('keyup', function (ev) {
        if (ev.keyCode === 18) { //ALT
            channel.send('ui-actions', 'window-alt-released', {});
        } else if (ev.keyCode === 17) { //CONTROL
            channel.send('ui-actions', 'window-ctrl-released', {});
        } else if (ev.keyCode === 16) { //SHIFT
            channel.send('ui-actions', 'window-shift-released', {});
        }

        ev.preventDefault();
    });

    myWindow.on('keydown', function (ev) {
        var key = [],
            keyStr = null;

        if (ev.altKey) {
            key.push('alt');
        } else if (ev.ctrlKey) {
            key.push('ctrl');
        } else if (ev.shiftKey) {
            key.push('shift');
        }

        if (key.length === 0 &&
            !ev.keyCode === 27 && // ESC
            !(ev.keyCode >= 112 && ev.keyCode <= 123)) { // F1 -> F12
            return; //nothing to do
        }

        keyStr = TOMEE.utils.keyCodeToString(ev.keyCode);
        if(!keyStr) {
            keyStr = ev.keyCode;
        }
        key.push(keyStr);

        channel.send('ui-actions', 'window-' + key.join('-') + '-pressed', {});
        ev.preventDefault();
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
            toolbarHeight:toolbarHeight
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