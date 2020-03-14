/**
 * Created by ygshen on 2017/7/5.
 */
var headerInfoApp = angular.module('headerInfoApp', []);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic':
            {
                link = "/portal/statistics";
                break;
            }
            case 'hc': {
                link = "/portal/statistics/statistics-hc";
                break;
            }
            case 'policy':
            {
                link = "/portal/statistics/statistics-netjava";
                break;
            }
            case 'abtest':
            {
                link = "/portal/statistics/statistics-normalpolicy";
                break;
            }
            case 'pie':
            {
                link = "/portal/statistics/charts";
                break;
            }
            case 'traffic':
            {
                link = "/portal/statistics/traffic";

                break;
            }
            case 'bu-traffic':
            {
                link = "/portal/statistics/butraffic" + '&bu=All';
                break;
            }
            case 'version':
            {
                link = "/portal/statistics/release";
                break;
            }
            case 'health':
            {
                link = "/portal/statistics/slbhealth";
                break;
            }
            case 'log':
            {
                link = "/portal/statistics/opslog";
                break;
            }
            case 'database':
            {
                link = "/portal/statistics/dbtraffic";
                break;
            }
            case 'deploy':
            {
                link = "/portal/statistics/deployment";
                break;
            }
            case 'ctripprogress':
            {
                link = "/portal/statistics/statistics-ctrip-netjava";
                break;
            }
            case 'ctriplanguage':
            {
                link = "/portal/statistics/statistics-ctrip-language";
                break;
            }
            case 'comments':
            {
                link = "/portal/statistics/statistics-feedback";
                break;
            }
            case 'cert': {
                link = "/portal/statistics/certificates";
                break;
            }
            case 'unhealthy':
            {
                link = "/portal/statistics/statistics-unhealthy";
                break;
            }
            case 'rule': {
                link = "/portal/statistics/rule#?env=" + G.env;
                break;
            }
            default:
                break;
        }
        return link;
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.appId) {
            $scope.query.appId = hashData.appId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var selfInfoApp = angular.module('selfInfoApp', ["http-auth-interceptor", "ngSanitize", "highcharts-ng"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
   $scope.env = 'pro';
    $scope.query = {
        userId: '',
        issueTime: ''
    };
    $scope.data = {
        comments: [],
        charts: []
    };

    $scope.initTable = function () {
        var ta = $scope.resource['feedback']['table'];
        $('#table').bootstrapTable({
            toolbar: "#toolbar",
            columns: [[
                {
                    width: '10%',
                    field: 'user',
                    title: ta["提出者"],
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value) {
                        return '<a target="_blank" href="' + '/portal/user#?env=' + G.env + '&userId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    width: '10%',
                    field: 'create-time',
                    title:  ta['提出日期'],
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'description',
                    title: ta['意见简要'],
                    align: 'left',
                    events: operateEvents,
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        if (value.length > 200) {
                            value = value.substring(0, 200) + '<a class="system-padding-left current" id="current' + index + '">全部显示....</a>';
                        }

                        return '<span>' + value + '</span>';
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            detailView: true,
            showColumns: true,
            data: $scope.data.comments,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'owner',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> "+ta['loading'];
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> '+ta['nodata'];
            },
            detailFormatter: function (arg1, arg2) {
                var id = arg2.id;

                var str = '<div class="row system-padding-top">' +
                    '<div class="col-md-1">' +
                    '<i class="fa fa-user"></i><b class="system-padding-left">'+ta["提出者"]+':</b>' +
                    '</div>' +
                    '<div class="col-md-11">' +
                    arg2.user +
                    '</div>' +
                    '</div>' +
                    '<div class="row system-padding-top">' +
                    '<div class="col-md-1">' +
                    '<i class="fa fa-calendar-check-o"></i><b class="system-padding-left">'+ta["提出日期"]+':</b>' +
                    '</div>' +
                    '<div class="col-md-11">' +
                    arg2['create-time'] +
                    '</div>' +
                    '' +
                    '</div>' +
                    '<div class="row system-padding-top">' +
                    '<div class="col-md-1">' +
                    '<i class="fa fa fa-newspaper-o"></i><b class="system-padding-left">'+ta["意见内容"]+':</b>' +
                    '</div>' +
                    '<div class="col-md-11">' +
                    arg2.description +
                    '</div>' +
                    '</div>' +
                    '';


                return str;
            }
        });
    };
    window.operateEvents = {
        'click .current': function (e, value, row) {
            var target = e.target;
            var span = $(target).parent();
            $(span).text(row.description);
        }

    };
    $scope.loadData = function () {
        var ta = $scope.resource['feedback'];
        var request = {
            url: G.baseUrl + '/api/feedback/get'
        };
        $http(request).success(function (response, code) {
            if (code == 200) {

                $scope.data.comments = response;

                if ($scope.query.userId) {
                    $scope.data.comments = _.filter($scope.data.comments, function (v) {
                        return v.user == $scope.query.userId;
                    });
                }
                if ($scope.query.issueTime) {
                    $scope.data.comments = _.filter($scope.data.comments, function (v) {
                        return v['create-time'] == $scope.query.issueTime;
                    });
                }


                var countByDate = _.countBy($scope.data.comments, function (v) {
                    return v['create-time'];
                });

                var countByOwner = _.countBy($scope.data.comments, function (v) {
                    return v.user;
                });

                var data1 =
                {
                    chart: {
                        type: 'column',
                        options3d: {
                            enabled: true,
                            alpha: 0,
                            beta: 10,
                            depth: 70
                        }
                    },

                    xAxis: {
                        categories: _.keys(countByDate),
                        crosshair: true
                    },
                    yAxis: {
                        min: 0,
                        title: {
                            text: ta["feedback_selfInfoApp_yname"]
                        }
                    },
                    tooltip: {
                        headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
                        pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                        '<td style="padding:0"><b>{point.y:.0f}</b></td></tr>',
                        footerFormat: '</table>',
                        shared: true,
                        useHTML: true
                    },
                    plotOptions: {
                        series: {
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: true,
                                allowOverlap: true,
                                format: '{point.name}'
                            }
                        },
                        column: {
                            depth: 25
                        }
                    },

                    series: [
                        {
                            name: ta["feedback_selfInfoApp_seriesname"],
                            data: _.values(countByDate),
                            maxPointWidth: 50,
                            type: 'column',
                            events: {
                                click: function (event) {
                                    var c = event.point.category;
                                    H.setData({timeStamp: new Date().getTime(), time: c});
                                }
                            }
                        }

                    ],
                    title: {
                        text: ta["feedback_selfInfoApp_history"]
                    },
                    credits: {
                        enabled: false
                    }
                };


                var feedfrom = [];
                $.each(_.keys(countByOwner), function (i, item) {
                    feedfrom.push([item, countByOwner[item]]);
                });


                var data2 = {
                    chart: {
                        type: 'column',
                        options3d: {
                            enabled: true,
                            alpha: 0,
                            beta: 10,
                            depth: 70
                        }
                    },

                    xAxis: {
                        categories: _.keys(countByOwner),
                        crosshair: true
                    },
                    yAxis: {
                        min: 0,
                        title: {
                            text: ta["feedback_selfInfoApp_yname"]
                        }
                    },
                    tooltip: {
                        headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
                        pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                        '<td style="padding:0"><b>{point.y:.0f}</b></td></tr>',
                        footerFormat: '</table>',
                        shared: true,
                        useHTML: true
                    },
                    plotOptions: {
                        series: {
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: true,
                                allowOverlap: true,
                                format: '{point.name}'
                            }
                        },
                        column: {
                            depth: 25
                        }
                    },

                    series: [
                        {
                            name: 'Owner',
                            data: _.values(countByOwner),
                            maxPointWidth: 50,
                            type: 'column',
                            events: {
                                click: function (event) {
                                    var c = event.point.category;
                                    H.setData({timeStamp: new Date().getTime(), userId: c});
                                }
                            }
                        }
                    ],
                    title: {
                        text: ta["feedback_selfInfoApp_owner"]
                    },
                    credits: {
                        enabled: false
                    }
                };

                $scope.data.charts = [data1, data2];


                $('#table').bootstrapTable("load", $scope.data.comments);
            }
        });

    };

    var resource;
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        resource = H.resource;
        $scope.query = {
            userId: '',
            issueTime: ''
        };

        if (hashData.userId) {
            $scope.query.userId = hashData.userId;
        }
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.time) {
            $scope.query.issueTime = hashData.time;
        }
        $scope.initTable();
        $scope.loadData();
    };
    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);
