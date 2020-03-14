var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource = H.resource;
    var resource = $scope.resource;
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'list': {

                link = "/portal/tools#?env=" + G.env;
                break;
            }
            case 'url': {
                link = "/portal/tools/visiturl#?env=" + G.env;
                break;
            }
            case 'test': {
                link = "/portal/tools/test#?env=" + G.env;
                break;
            }
            case 'http-https': {
                link = "/portal/tools/redirect/redirects#?env=" + G.env;
                break;
            }
            case 'bug': {
                link = "/portal/tools/problem#?env=" + G.env;
                break;
            }
            case 'verify': {
                link = "/portal/tools/verify#?env=" + G.env;
                break;
            }
            case 'slb-migration': {
                link = "/portal/tools/smigration/migrations#?env=" + G.env;
                break;
            }
            case 'slb-sharding': {
                link = "/portal/tools/sharding/shardings#?env=" + G.env;
                break;
            }
            case 'vs-migration': {
                link = "/portal/tools/vmigration/migrations#?env=" + G.env;
                break;
            }
            case 'vs-merge': {
                link = "/portal/tools/vs/merges#?env=" + G.env;
                break;
            }
            case 'vs-seperate': {
                link = "/portal/tools/vs/seperates#?env=" + G.env;
                break;
            }

            case 'dr': {
                link = "/portal/tools/dr/drs#?env=" + G.env;
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
    };
    H.addListener("headerInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var selfInfoApp = angular.module('selfInfoApp', ["ngSanitize", "angucomplete-alt", "http-auth-interceptor"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    $scope.resource = H.resource;
    var resource = $scope.resource;
    $scope.data = {};
    var slbShardingApplication;

    $scope.filterSharing = function (status) {
        var pair = {
            status: status,
            timeStamp: new Date().getTime()
        };
        H.setData(pair);
    };
    $scope.getAllSharing=function () {
        var pair = {
            status: '',
            timeStamp: new Date().getTime()
        };
        H.setData(pair);
    };

    $scope.startNewSharding = function () {
        var resource = $scope.resource;
        var hasRight = A.canDo('Flow', 'NEW', '*') || A.canDo('Flow', 'FLOW', '*');
        if (!hasRight) {
            alert(resource.tools.sharding.js.token1);
            return;
        }
        window.location.replace('/portal/tools/sharding/edit#?env=' + $scope.env);
    };

    $scope.loadData = function () {
        var promise = slbShardingApplication.getSlbShardings();
        promise.then(function (data) {
            $scope.total = data.length;
            if ($scope.status) {
                data = _.filter(data, function (v) {
                    return v.status === $scope.status;
                });
            }
            slbShardings.init(data);
            $scope.data.shardings.statusCountMap = slbShardings.countByStatus($scope.resource);
            $('#shardings').bootstrapTable("load", slbShardings.data);
        });
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        var env = 'uat';
        if (hashData.env) {
            env = hashData.env;
        }
        $scope.env = env;

        var type = '';
        if (hashData.status) {
            type = hashData.status;
        }
        $scope.status = type;

        $scope.data.shardings = slbShardings;
        slbShardingApplication = SlbShardingApplication.create($http, $q, env);
        initTable();
        $scope.loadData();
    };

    function initTable() {
        var resource = $scope.resource;
        $('#shardings').bootstrapTable({
            toolbar: "#table-toolbar",
            columns: [[
                {
                    field: 'id',
                    title: (!resource.tools ? "拆分": resource.tools.sharding.js.token2),
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a href="/portal/tools/sharding/edit#?env=' + $scope.env + '&shardingId=' + row.id + '">' + value + '(' + row.name + ')' + '</a>';
                    }
                },
                {
                    field: 'fromSlb',
                    title: (!resource.tools ? "源 SLB": resource.tools.sharding.js.token3),
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value) return '-';
                        return '<a href="/portal/slb#?env=' + $scope.env + '&slbId=' + value.id + '">' + value.id + '(' + value.name + ')</a>';
                    }
                },
                {
                    field: 'targetSlb',
                    title: (!resource.tools ? "SLB 新建": resource.tools.sharding.js.token4),
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (v, row, index) {
                        var str = '<div class="row">';
                        $.each(v, function (i, item) {
                            if (item.slb) {
                                str += '<div class="col-md-12"><a href="/portal/slb#?env=' + $scope.env + '&slbId=' + item.slb.id + resource.tools.sharding.js.token9 + item.slb.id + '/' + item.slb.name + ')</a></div>';
                            } else {
                                str += '<div class="col-md-12"><a href="/portal/tools/smigration/edit#?env=' + $scope.env + '&migrationId=' + v[0].id + '">正在' + v[0].status + '...</a></div>';
                            }
                        });

                        str += '</div>';
                        return str;
                    }
                },

                {
                    field: 'vs-migration',
                    title: (!resource.tools ? "VS 迁移": resource.tools.sharding.js.token5),
                    align: 'left',
                    valign: 'middle',
                    formatter: function (v, row, index) {
                        if (!v) return (!resource.tools) ? "<span class=\"status-red\">未开始</span>": resource.tools.sharding.js.token6;
                        return '<a href="/portal/tools/vmigration/edit#?env=' + $scope.env + '&migrationId=' + v.id + '" class="' + (constants.shardingColor[v.migrationStatus] || 'status-yellow') + '">' + resource.tools.sharding.js[v.migrationStatus] + '</a>';
                    }
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var c = constants.shardingColor[value];
                        return '<span class="' + c + '">' + resource.tools.sharding.js[value] + '</span>';
                    }
                }
            ], []],
            sortName: 'id',
            sortOrder: 'desc',
            data: slbShardings.data,
            classes: "table-bordered  table-hover table-striped table",
            search: true,
            showRefresh: false,
            showColumns: true,
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            resizable: true,
            resizeMode: 'overflow',
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            formatLoadingMessage: function () {
                return (!resource.tools ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 SLB 信息": resource.tools.sharding.js.token7);
            },
            formatNoMatches: function () {
                return (!resource.tools ? "<i class=\"fa status-alert fa-exclamation-triangle\" aria-hidden=\"true\"></i> 没有找到匹配的 SLB新建信息": resource.tools.sharding.js.token8);
            }
        });
        $('#slbs').bootstrapTable("showLoading");
    }

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);