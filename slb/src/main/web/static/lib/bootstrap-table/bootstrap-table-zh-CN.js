/**
 * Bootstrap Table Chinese translation
 * Author: Zhixin Wen<wenzhixin2010@gmail.com>
 */
(function ($) {
    'use strict';

    $.fn.bootstrapTable.locales['zh-CN'] = {
        formatLoadingMessage: function () {
            return 'Loaing……';
        },
        formatRecordsPerPage: function (pageNumber) {
            return 'Display ' + pageNumber + ' rows of record';
        },
        formatShowingRows: function (pageFrom, pageTo, totalRows) {
            return 'Display page ' + pageFrom + ' to page ' + pageTo + ' ,Total records ' + totalRows + ' row of record';
        },
        formatSearch: function () {
            return 'Search';
        },
        formatNoMatches: function () {
            return 'No result find';
        },
        formatPaginationSwitch: function () {
            return 'Hide/Display';
        },
        formatRefresh: function () {
            return 'Refresh';
        },
        formatToggle: function () {
            return 'Switch';
        },
        formatColumns: function () {
            return 'Columns';
        }
    };

    $.extend($.fn.bootstrapTable.defaults, $.fn.bootstrapTable.locales['zh-CN']);

})(jQuery);