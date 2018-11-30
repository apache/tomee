$(function () {
    function init() {
        var pageWidth = $(window).width();
        var numberOfColumns = 1;

        switch (true) {
            case pageWidth >= 978 && pageWidth < 1350:
                numberOfColumns = 2;
                break;
            case pageWidth >= 1350 && pageWidth < 1800:
                numberOfColumns = 3;
                break;
            case pageWidth >= 1800 && pageWidth < 2210:
                numberOfColumns = 4;
                break;
            case pageWidth >= 2210 && pageWidth < 2640:
                numberOfColumns = 5;
                break;
            case pageWidth >= 2640:
                numberOfColumns = 6;
                break;
        }
        if (numberOfColumns < 2) {
            return;
        }

        var page = $("#clearing-div");

        $('div.horizontal-block').each(function () {
            var container = $(this);
            var id = container.attr('id');

            var newContainer = $('<div class="columns-container" id="container-' + id + '" />');
            newContainer.insertBefore(page);
            newContainer.append($('<h2>' + id + '</h2>'));

            var columns = [];
            for (var i = 1; i <= numberOfColumns; i++) {
                column = $('<div class="columns" id="column-' + i + '-' + id + '"/>');
                newContainer.append(column);
                columns.push(column);
            }
            newContainer.append($('<div style="clear: both;" />'));
            newContainer.append($('<hr />'));

            $("#" + id + " > div.col").each(function () {
                var element = $(this);
                for (var i = 1; i <= numberOfColumns; i++) {
                    var moveTo = "c" + numberOfColumns + "-" + i;
                    if (element.hasClass(moveTo)) {
                        var clone = element.clone(true);
                        columns[i - 1].append(clone);
                        element.addClass("moved");
                    }
                }
            });
        });
    }

    init();
    $(window).resize(function () {
        $("div.columns").remove();
        $("div.columns-container").remove();
        $('div.horizontal-block').each(function () {
            var id = $(this).attr('id');
            $("#" + id + " > div.col").each(function () {
                $(this).removeClass("moved");
            })
        });
        init();
    });
});

hljs.initHighlightingOnLoad()
