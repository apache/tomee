/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(function () {
    var installBtn = $('.ux-install-btn');
    var catalinaHome = $('.ux-catalinaHome-txt');
    var catalinaBase = $('.ux-catalinaBase-txt');
    var serverXmlFile = $('.ux-serverXmlFile-txt');
    var notification = $('.ux-install-notification');

    installBtn.on('click', function (evt) {
        evt.preventDefault();
        installBtn.addClass('disabled');
        $.ajax({
            url: 'installer',
            data: {
                catalinaBaseDir: catalinaBase.val(),
                catalinaHome: catalinaHome.val(),
                serverXmlFile: serverXmlFile.val()
            },
            method: 'POST',
            dataType: 'json',
            success: function (data) {
                window.location.reload();
            }
        });
    });

    function loop(list, callback) {
        if (!list) {
            return;
        }
        var i;
        for (i = 0; i < list.length; i += 1) {
            callback(list[i], i);
        }
    }

    function setData(data) {
        var table = $($('.ux-status-table').get(0));
        table.empty();
        var systemStatus = {};
        loop(data, function (item) {
            systemStatus[item.key] = item.value;
            table.append('<tr><td>' + item.key + '</td><td>' + item.value + '</td></tr>')
        });
        if (systemStatus.status === 'INSTALLED') {
            $('.ux-server-ready-panel').removeClass('ux-hidden');
            var providerLink = $($('.ux-provider-url').get(0));
            providerLink.attr('href', window.location.href + 'ejb');
            providerLink.html(window.location.origin + window.location.pathname + 'ejb');
        } else if (systemStatus.status === 'REBOOT_REQUIRED') {
            $('.ux-installer-reboot-panel').removeClass('ux-hidden');
        } else {
            $('.ux-installer-panel').removeClass('ux-hidden');
        }
        catalinaHome.val(systemStatus.catalinaHomeDir);
        catalinaBase.val(systemStatus.catalinaBaseDir);
        serverXmlFile.val(systemStatus.serverXmlFile);
    }

    $.ajaxSetup({ cache: false });
    $.ajax({
        url: 'installer?ts=' + (new Date()).getTime(),
        method: 'GET',
        dataType: 'json',
        success: setData
    });
});