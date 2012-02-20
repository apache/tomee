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

TOMEE.ApplicationJndiPanel = function (cfg) {
    "use strict";

    var channel = cfg.channel;
    var model = cfg.jndiModel;

    var elements = (function () {
        var carouselUid = TOMEE.Sequence.next('CAROUSEL');
        var tbodyUid = TOMEE.Sequence.next();
        var tpl = [
            '<div class="well">',

            '<div class="carousel slide">',
            '    <div id="' + carouselUid + '" class="carousel-inner">',
            '        <div class="item active">',
            '          <div style="overflow:auto; height: 300px">',
            '          <table class="table table-striped table-bordered table-condensed">',
            '              <tbody id="' + tbodyUid + '"/>',
            '          </table>',
            '          </div>',

            '          <br/><br/><br/><br/><br/><br/>',
            '          <div class="carousel-caption">',
            '              <h4>' + TOMEE.ApplicationI18N.get('app.home.menu.tools.jndi.browser') + '</h4>',
            '              <p>' + TOMEE.ApplicationI18N.get('app.home.menu.tools.jndi.browser.info') + '</p>',
            '          </div>',
            '        </div>',
            '    </div>',
            '</div>',
            '</div>'
        ];

        //create the element
        var all = $(tpl.join(''));
        var tbody = all.find("#" + tbodyUid);
        var carousel = all.find("#" + carouselUid);
        return {
            all: all,
            tbody: tbody,
            carousel: carousel
        };
    })();

    var addCarouselItem = function (carouselParams) {
        var captionTpl = [
            '          <br/><br/><br/><br/><br/><br/>',
            '          <div class="carousel-caption" style="padding: 5px;">',
            '{0}',
            '          </div>'
        ].join('');

        var tpl = [
            '        <div class="item">',
            '          <div style="overflow:auto; height: 300px">',
            '{0}',
            '          </div>',
            '{1}',
            '        </div>'
        ].join('');

        var result = '';
        if (carouselParams.captionTpl) {
            var caption = TOMEE.utils.stringFormat(captionTpl, carouselParams.captionTpl);
            result = TOMEE.utils.stringFormat(tpl, carouselParams.bodyTpl, caption);

        } else {
            result = TOMEE.utils.stringFormat(tpl, carouselParams.bodyTpl, '');
        }

        var el = $(result);
        elements.carousel.append(el);
        elements.carousel.carousel('next');
        return el;
    };

    var showBeanPanel = function (bean) {
        var bodyTpl = [
            '<p>deploymentId: ' + bean.deploymentId + '</p>',
            '<p>path: ' + bean['path'] + '</p>',
            '<p>name: ' + bean['name'] + '</p>',
            '<p>value: ' + bean['value'] + '</p>',
            '<p>beanType: ' + bean['beanType'] + '</p>'
        ].join('');

        var backUid = TOMEE.Sequence.next('CAROUSEL-BACK');
        var captionTpl = [
            '<div class="btn-group">',
            '<a class="btn" id="' + backUid + '" href="#">Back</a>',
            '<a class="btn" href="#">Invoke</a>',
            '<a class="btn" href="#">View class</a>',
            '</div>'
        ].join('');

        var item = addCarouselItem({
            captionTpl: captionTpl,
            bodyTpl: bodyTpl
        });

        var backBtn = item.find("#" + backUid);
        backBtn.on('click', function() {
            elements.carousel.carousel('prev');

            var task = TOMEE.DelayedTask({
                callback:function() {
                    item.remove();
                }
            });
            task.delay(1000);
        });
    };

    var addRow = function (bean) {
        var trUid = TOMEE.Sequence.next("JNDI-TR");
        var row = [
            '        <tr id="' + trUid + '">',
            '            <td><a href="#">' + bean.name + '</a></td>',
            '            <td><i class="icon-chevron-right"></i></td>',
            '        </tr>'
        ].join('');
        elements.tbody.append($(row));
        var tr = elements.tbody.find("#" + trUid);
        tr.on('click', function () {
            showBeanPanel(bean);
        });
    };

    var loadData = function () {
        //remove the current rows if any
        elements.tbody.empty();

        //The user should give a "getData" method that iterates over
        //the objects that will be used to populate the grid
        model.iterateJndiBeans(function (bean) {
            addRow(bean);
        });
    };

    return {
        getEl: function () {
            return elements.all;
        },
        loadData: loadData
    };
};