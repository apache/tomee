$(function () {
    var installBtn = $('.ux-install-btn');
    var catalinaHome = $('.ux-catalinaHome-txt');
    var catalinaBase = $('.ux-catalinaBase-txt');
    var serverXmlFile = $('.ux-serverXmlFile-txt');
    var notification = $('.ux-install-notification');

    installBtn.on('click', function (evt) {
        evt.preventDefault();
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
                populateGrid(data);
                notification.modal({});
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

    function populateGrid(data) {
        var table = $($('.ux-status-table').get(0));
        table.empty();
        var systemStatus = {};
        loop(data, function (item) {
            systemStatus[item.key] = item.value;
            table.append('<tr><td>' + item.key + '</td><td>' + item.value + '</td></tr>')
        });
        if (data && data.length > 0) {
            if (systemStatus.status === 'NONE') {
                installBtn.removeClass('disabled');
                catalinaHome.removeClass('disabled');
                catalinaBase.removeClass('disabled');
                serverXmlFile.removeClass('disabled');
            }
            catalinaHome.val(systemStatus.catalinaHomeDir);
            catalinaBase.val(systemStatus.catalinaBaseDir);
            serverXmlFile.val(systemStatus.serverXmlFile);
        }
    }

    $.ajaxSetup({ cache: false });
    $.ajax({
        url: 'installer',
        method: 'GET',
        dataType: 'json',
        success: populateGrid
    });
});