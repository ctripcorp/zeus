//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic': {
                link = "/portal/statistics#?env=" + G.env;
                break;
            }
            case 'policy': {
                link = "/portal/statistics/statistics-netjava#?env=" + G.env;
                break;
            }
            case 'hc': {
                link = "/portal/statistics/statistics-hc#?env=" + G.env;
                break;
            }
            case 'dashboard': {
                link = "/portal/statistics/dashboard#?env=" + G.env;
                break;
            }
            case 'abtest': {
                link = "/portal/statistics/statistics-normalpolicy#?env=" + G.env;
                break;
            }
            case 'pie': {
                link = "/portal/statistics/charts#?env=" + G.env;
                break;
            }
            case 'traffic': {
                link = "/portal/statistics/traffic#?env=" + G.env;

                break;
            }
            case 'bu-traffic': {
                link = "/portal/statistics/butraffic#?env=" + G.env + '&bu=All';
                break;
            }
            case 'version': {
                link = "/portal/statistics/release#?env=" + G.env;
                break;
            }
            case 'health': {
                link = "/portal/statistics/slbhealth#?env=" + G.env;
                break;
            }
            case 'log': {
                link = "/portal/statistics/opslog#?env=" + G.env;
                break;
            }
            case 'database': {
                link = "/portal/statistics/dbtraffic#?env=" + G.env;
                break;
            }
            case 'deploy': {
                link = "/portal/statistics/deployment#?env=" + G.env;
                break;
            }
            case 'ctripprogress': {
                link = "/portal/statistics/statistics-ctrip-netjava#?env=" + G.env;
                break;
            }
            case 'cert': {
                link = "/portal/statistics/certificates#?env=" + G.env;
                break;
            }
            case 'ctriplanguage': {
                link = "/portal/statistics/statistics-ctrip-language#?env=" + G.env;
                break;
            }
            case 'comments': {
                link = "/portal/statistics/statistics-feedback#?env=" + G.env;
                break;
            }
            case 'unhealthy': {
                link = "/portal/statistics/statistics-unhealthy#?env=" + G.env;
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
    }
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

//StatisticsComponent: Statistics
var statisticsApp = angular.module('statisticsApp', ['ui.bootstrap', 'ui.bootstrap.datetimepicker', 'ngSanitize', 'http-auth-interceptor']);
statisticsApp.controller('statisticsController', function ($scope, $http, $filter) {
    $scope.summary = {
        idcs: [],
        bus: [],
        info: {}
    };

    //Build Data Modal
    $scope.dataLoaded = false;
    $scope.env = '';
    $scope.loadData = function (hashData) {
        if ($scope.env == hashData.env && $scope.dataLoaded) return;
        $scope.dataLoaded = true;
        $scope.env = hashData.env;

        $scope.summary = {};
        $scope.summary.info = {
            idcCount: 0,
            slbCount: 0,
            slbServerCount: 0,
            vsCount: 0,
            groupCount: 0,
            serverCount: 0,
            memberCount: 0,
            qps: 0,
            buCount: 0
        };

        $http.get(G.baseUrl + "/api/statistics/idc").success(function (res) {
            var idcs = $scope.summary.idcs = res['idc-metas'];

            //$('#idcs-table').bootstrapTable($scope.idcsTableOption);
            $('#idcs-table').bootstrapTable('load', idcs ? idcs : []);

            var info = $scope.summary.info;
            info.idcCount = idcs.length;

            function extracted() {
                _.each(idcs, function (val) {
                    info.slbCount += val['slb-count'];
                    info.slbServerCount += val['slb-server-count'];
                    info.vsCount += val['vs-count'];
                    info.groupCount += val['group-count'];
                    info.serverCount += val['group-server-count'];
                    info.memberCount += val['member-count'];
                    info.qps += Math.floor(val['qps']);
                });
            }

            extracted();
            $('.idc-text').text(info.idcCount);

            $('.slb-text').text(info.slbCount);
            if (info.slbCount > 0) {
                $('.slb-text').prop('href', $scope.navigateTo('slb'));
            }

            $('.vs-text').text(T.getText(info.vsCount));
            if (info.vsCount > 0) {
                $('.vs-text').prop('href', $scope.navigateTo('vs'));
            }
            var qps = T.getText(info.qps);
            $('.qps-text').text(qps);
            if (info.qps > 0) {
                $('.qps-text').prop('href', $scope.navigateTo('qps'));
            }
            $('.groups-text').text(T.getText(info.groupCount));
            if (info.groupCount > 0) {
                $('.groups-text').prop('href', $scope.navigateTo('group'));
            }
            $('.servers-text').text(T.getText(info.serverCount));
            $('.members-text').text(T.getText(info.memberCount));
        });

        $http.get(G.baseUrl + "/api/statistics/sbu").success(function (res) {
            $scope.summary.bus = res['sbu-metas'];
            $('#bu-table').bootstrapTable('load', $scope.summary.bus ? $scope.summary.bus : []);
            $scope.summary.info.buCount = $scope.summary.bus.length;
            $('.bu-text').text($scope.summary.info.buCount);
            if ($scope.summary.info.buCount > 0)
                $('.bu-text').prop('href', $scope.navigateTo('bu'));
        });
        $http.get(G.baseUrl + "/api/groups?type=extended").success(function (res) {
            var countByStatusItem = _.countBy(res.groups, function (item) {
                var status = _.find(item.properties, function (a) {
                    return a.name == 'status';
                });
                if (status) return status.value.toLowerCase();
                else return 'unknown';
            });
            var countByHealthyItem = _.countBy(res.groups, function (item) {
                var status = _.find(item.properties, function (a) {
                    return a.name == 'healthy';
                });
                if (status) return status.value.toLowerCase();
                else return 'unknown';
            });

            var activated = countByStatusItem['activated'] || 0;
            var tobeactivated = countByStatusItem['tobeactivated'] || 0;
            var deactivated = countByStatusItem['deactivated'] || 0;

            var healthy = countByHealthyItem['healthy'] || 0;
            var unhealthy = countByHealthyItem['unhealthy'] || 0;
            var broken = countByHealthyItem['broken'] || 0;

            $('.activate-group-text').text(activated);
            if (activated > 0) {
                $('.activate-group-text').prop('href', $scope.navigateTo('activated'));
            }
            $('.tobeactivated-group-text').text(tobeactivated);
            if (tobeactivated > 0) {
                $('.tobeactivated-group-text').prop('href', $scope.navigateTo('tobeactivated'));
            }
            $('.deactivated-group-text').text(deactivated);
            if (deactivated) {
                $('.deactivated-group-text').prop('href', $scope.navigateTo('deactivated'));
            }
            $('.healthy-text').text(healthy);
            if (healthy > 0) {
                $('.healthy-text').prop('href', $scope.navigateTo('healthy'));
            }
            $('.unhealthy-text').text(unhealthy);
            if (unhealthy > 0) {
                $('.unhealthy-text').prop('href', $scope.navigateTo('unhealthy'));
            }
            $('.broken-text').text(broken);
            if (broken > 0) {
                $('.broken-text').prop('href', $scope.navigateTo('broken'));
            }

        });
    };

    $scope.navigateTo = function (item) {
        var link = '';

        switch (item) {
            case 'slb': {
                link = "/portal/slbs#?env=" + $scope.env;
                break;
            }
            case 'vs': {
                link = "/portal/vses#?env=" + $scope.env;
                break;
            }
            case 'domain': {
                link = "/portal/vses#?env=" + $scope.env;
                break;
            }
                ;
            case 'bu': {
                link = "/portal/bus#?env=" + $scope.env;

                break;
            }
            case 'qps': {
                link = "/portal/statistics/traffic#?env=" + $scope.env;
                break;
            }
            case 'group': {
                link = "/portal/groups#?env=" + $scope.env + '&groupType=Group';
                break;
            }
            case 'activated': {
                link = '/portal/groups#?env=' + G.env + '&groupStatus=已激活&groupType=Group';
                break;
            }
            case 'tobeactivated': {
                link = '/portal/groups#?env=' + G.env + '&groupStatus=有变更&groupType=Group';
                break;
            }
            case 'deactivated': {
                link = '/portal/groups#?env=' + G.env + '&groupStatus=未激活&groupType=Group';
                break;
            }
            case 'healthy': {
                link = '/portal/groups#?env=' + G.env + '&groupHealthy=healthy:healthy&groupType=Group';
                break;
            }
            case 'unhealthy': {
                link = '/portal/groups#?env=' + G.env + '&groupHealthy=serverHealthy:Server拉出,memberHealthy:Member拉出,pullHealthy:发布拉出,healthCheckHealthy:健康拉出&groupType=Group';
                break;
            }
            case 'broken': {
                link = '/portal/groups#?env=' + G.env + '&groupHealthy=healthy:Broken&groupType=Group';
                break;
            }
            case 'qps': {
                link = '/portal/statistics/traffic#?env=' + G.env;
                break;
            }
            default:
                break;
        }
        return link;
    };

    //Display Summary Description
    $scope.generateDescriptionHtml = function () {
        function link(t, c) {
            if (isNaN(c)) {
                c = '-';
            }

            if (t == 'slb') {
                return ' <a href="/portal/slbs#?env=' + G.env + '"><strong>' + c + '</strong></a>';
            } else if (t == 'vs') {
                return ' <a href="/portal/vses#?env=' + G.env + '"><strong>' + c + '</strong></a>';
            } else if (t == 'group') {
                return ' <a href="/portal/groups#?env=' + G.env + '"><strong>' + c + '</strong></a>';
            } else if (t == 'slbServer') {
                return ' <strong>' + c + '</strong>';
            } else if (t == 'server') {
                return ' <strong>' + c + '</strong>';
            } else if (t == 'member') {
                return ' <strong>' + c + '</strong>';
            } else if (t == 'bu') {
                return ' <strong>' + c + '</strong>';
            } else if (t == 'qps') {
                return ' <a href="/portal/statistics/traffic#?env=' + G.env + '"><strong>' + c + '</strong></a>';
            }
            return ' <strong>' + c + '</strong>';
        }

        var res = '';
        var info = $scope.summary.info;
        if (info.idcCount) {
            res += '<h4>共有' + link('idc', info.idcCount) + '个IDC,  '
                + link('slb', info.slbCount) + '套SLB('
                + link('slbServer', info.slbServerCount) + '台服务器)，'
                + link('vs', info.vsCount) + '个VS，'
                + link('group', info.groupCount) + '个Group，'
                + link('server', info.serverCount) + '台Server，'
                + link('member', info.memberCount) + '个Member实例,'
                + link('bu', info.buCount) + '个BU接入，总QPS:'
                + link('qps', info.qps) + '</h4>';
        }
        return res;
    };

        $scope.resource = H.resource;

    function initTable() {

        table1 = resource["statistics"]["statistics_statisticsApp_idcs"]["table"];
        table2 = resource["statistics"]["statistics_statisticsApp_bus"]["table"];

        $('#idcs-table').bootstrapTable(
            {
                toolbar: "#idcs-table-toolbar",
                columns: [[
                    {
                        field: 'idc',
                        title: table1["idc"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true
                    },
                    {
                        field: 'slb-count',
                        title: table1["slbcount"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            var hashStr = H.generateHashStr({
                                env: G.env,
                                idcs: row.idc
                            });
                            return '<a href="/portal/slbs' + hashStr + '">' + value + '</a>';
                        }
                    },
                    {
                        field: 'slb-server-count',
                        title: table1["slbservercount"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'vs-count',
                        title: table1["vscount"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'app-count',
                        title: table1["appcount"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'group-count',
                        title: table1["groupcount"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'group-server-count',
                        title: table1["servercount"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'member-count',
                        title: table1["membercount"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'qps',
                        title: table1["qps"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return Math.floor(value);
                        }
                    },
                    {
                        field: 'Links',
                        title: table1["links"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            var env = $scope.env;
                            if (!G[env].urls['es']) return '-';

                            var idc = row['idc'];

                            var mapping = {
                                金桥: 'SHAJQ',
                                欧阳: 'SHAOY',
                                南通: 'NTGXH',
                                福泉: 'SHAFQ',
                                金钟: 'SHAJZ'
                            };

                            idc = mapping[idc];

                            var esLink = G[env].urls['es'] + "?query=idc%3D'" + idc + "'";

                            var str = '<div class="">' +
                                '<div class="system-link">' +
                                '<a class="pull-left es" title="ES" target="_blank" href="' + esLink + '">ES</a>' +
                                '</div>'
                            '</div>';

                            return str;
                        }
                    }
                ], []],
                sortName: 'slb-count',
                sortOrder: 'desc',
                classes: "table-bordered  table-hover table-striped table",
                search: true,
                showRefresh: false,
                showColumns: true,
                minimumCountColumns: 2,
                pagination: false,
                idField: 'id',
                pageSize: 20,
                sidePagination: 'client',
                pageList: [20, 40, 80, 200],
                formatLoadingMessage: function () {
                    return "<img class='loading-img' src='/static/img/loading_100.gif' /> "+table1['loading'];
                },
                formatNoMatches: function () {
                    return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> '+table1["nodata"];
                }
            }
        );
        $('#bu-table').bootstrapTable(
            {
                toolbar: "#bu-table-toolbar",
                columns: [[
                    {
                        field: 'sbu',
                        title: table2['bu'],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return '<a target="_blank" title="' + value + '"href="/portal/bu#?env=' + G.env + '&buName=' + value + '">' + value + '</a>';
                        }
                    },
                    {
                        field: 'vs-count',
                        title: table2['vscount'],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'app-count',
                        title: table2['appcount'],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return '<a target="_blank" title="' + value + '"href="/portal/bu/bu-app#?env=' + G.env + '&buName=' + row.sbu + '">' + value + '</a>';
                        }
                    },
                    {
                        field: 'group-count',
                        title: table2["groupcount"],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return '<a target="_blank" title="' + value + '"href="/portal/bu/bu-group#?env=' + G.env + '&buName=' + row.sbu + '">' + value + '</a>';
                        }
                    },
                    {
                        field: 'group-server-count',
                        title: table2['servercount'],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'member-count',
                        title: table2['membercount'],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            return value;
                        }
                    },
                    {
                        field: 'qps',
                        title: table2['qps'],
                        align: 'left',
                        valign: 'middle',
                        sortable: true,
                        formatter: function (value, row, index) {
                            var hashStr = H.generateHashStr({
                                env: G.env,
                                bu: row.sbu
                            });
                            return '<a href="/portal/statistics/butraffic' + hashStr + '">' + Math.floor(value) + '</a>';
                        }
                    },
                    {
                        field: 'Links',
                        title: table2['links'],
                        align: 'left',
                        valign: 'middle',
                        width: '120px',
                        sortable: true,
                        formatter: function (value, row, index) {
                            var env = $scope.env;

                            if (!G[env].urls['es']) return '-';
                            var sbu = row['sbu'];

                            var esLink = G[env].urls['es'] + "?query=sbu%3D'" + sbu + "'";

                            var str = '<div class="">' +
                                '<div class="system-link">' +
                                '<a class="pull-left es" title="ES" target="_blank" href="' + esLink + '">ES</a>' +
                                '</div>'
                            '</div>';

                            return str;
                        }
                    }
                ], []],
                sortName: 'group-count',
                sortOrder: 'desc',

                classes: "table-bordered  table-hover table-striped table",
                search: true,
                showRefresh: false,
                showColumns: true,
                minimumCountColumns: 2,
                pagination: true,
                idField: 'id',
                pageSize: 20,
                sidePagination: 'client',
                pageList: [20, 40, 80, 200],
                formatLoadingMessage: function () {
                    return "<img class='loading-img' src='/static/img/loading_100.gif' /> "+table2["loading"];
                },
                formatNoMatches: function () {
                    return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> '+table2["nodata"];
                }
            }
        );

    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        resource = H.resource;

        initTable();

        $scope.loadData(hashData);
    };
    H.addListener("statisticsApp", $scope, $scope.hashChanged);
    var resource;
    var table1;
    var table2;
});
angular.bootstrap(document.getElementById("statistics-area"), ['statisticsApp']);

var stasticSummaryApp = angular.module('stasticSummaryApp', ['http-auth-interceptor']);
stasticSummaryApp.controller('stasticSummaryController', function ($scope, $http, $q) {
    $scope.hashData = {};

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        $scope.env = hashData.env;
    };
    $scope.navigateTo = function (item) {
        var link = '';

        switch (item) {
            case 'slb': {
                link = "/portal/slbs#?env=" + $scope.env;
                break;
            }
            case 'vs': {
                link = "/portal/vses#?env=" + $scope.env;
                break;
            }
            case 'domain': {
                link = "/portal/vses#?env=" + $scope.env;
                break;
            }
                ;
            case 'bu': {
                link = "/portal/bus#?env=" + $scope.env;

                break;
            }
            case 'qps': {
                link = "/portal/statistics/traffic#?env=" + $scope.env;
                break;
            }
            case 'group': {
                link = "/portal/groups#?env=" + $scope.env;
                break;
            }
            case 'activated': {
                link = '/portal/groups#?env=' + G.env + '&groupStatus=已激活';
                break;
            }
            case 'tobeactivated': {
                link = '/portal/groups#?env=' + G.env + '&groupStatus=有变更';
                break;
            }
            case 'deactivated': {
                link = '/portal/groups#?env=' + G.env + '&groupStatus=未激活';
                break;
            }
            case 'healthy': {
                link = '/portal/groups#?env=' + G.env;
                break;
            }
            case 'unhealthy': {
                link = '/portal/groups#?env=' + G.env + '&groupHealthy=serverHealthy:Server拉出,memberHealthy:Member拉出,pullHealthy:发布拉出,healthCheckHealthy:健康拉出';
                break;
            }
            case 'broken': {
                link = '/portal/groups#?env=' + G.env + '&groupTags=user_' + $scope.userInfo['name'] + ',owner_' + $scope.userInfo['name'] + '&groupHealthy=healthy:Broken';
                break;
            }
            case 'qps': {
                link = '/portal/statistics/traffic#?env=' + G.env;
                break;
            }
            default:
                break;
        }
        return link;
    };
    H.addListener("stasticSummaryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("apps-summary-area"), ['stasticSummaryApp']);

