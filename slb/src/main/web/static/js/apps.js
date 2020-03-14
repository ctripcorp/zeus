var appsQueryApp = angular.module('appsQueryApp', ["angucomplete-alt","http-auth-interceptor"]);
appsQueryApp.controller('appsQueryController', function ($scope, $http) {
    
    $scope.resource = H.resource;
    var resource = $scope.resource;
    $scope.query = {
        "appid": "",
        "bus": {}
    };
    $scope.data = {
        buArr:[]
    };
    $scope.dataLoaded=false;
    //Load all select data
    $scope.loadData = function (hashData) {
        if($scope.env == hashData.env && $scope.dataLoaded) return;
        $scope.dataLoaded = true;
        $scope.env = hashData.env;

        // refresh the query data object
        $scope.data = {
            buArr: []
        };

        //Load bus
        $http.get(G.baseUrl + '/api/apps').success(
            function (res) {
                var bus = [];
                $.each(res['apps'], function (i, val) {
                    if ($scope.buNameValidation(val.sbu) && bus.indexOf(val.sbu) == -1) {
                        bus.push(val.sbu);
                    }
                });
                $scope.data.buArr = bus;
            }
        );
    };
    $scope.showClear = function (type) {
        if (type == "bu") {
            return _.keys($scope.query.bus).length > 0?"link-show":"link-hide";
        }
    };
    $scope.buClear = function () {
        $scope.query.bus = {};
    };
    $scope.toggleShowMoreBU = function () {
        $scope.showMoreBU = !$scope.showMoreBU;
    };
    $scope.collapseBtnClass = function () {
        return $scope.showMoreBU ? 'fa fa-chevron-down' : 'fa fa-chevron-left';
    };
    $scope.buNameValidation = function (pname) {
        var illegals = ['%', '�', '/', '(', '^', 'э', ')', ';', '}', '-', ':'];
        var needBreak = false;

        $.each(pname.split(''), function (i, v) {
            if (illegals.indexOf(v) > 0) needBreak = true;
        });

        if (needBreak) return false;
        return true;
    };
    $scope.multiTagsClass = function () {
        return $scope.showMoreBU ? '' : 'multi-tags-collapse';
    };
    $scope.toggleBu = function (bu) {
        if ($scope.query.bus[bu]) {
            delete $scope.query.bus[bu];
        } else {
            $scope.query.bus[bu] = bu;
        }
    };
    $scope.isSelectedBu = function (bu) {
        if ($scope.query.bus[bu]) {
            return "label-info";
        }
    };

    //Load cache
    $scope.cacheRequestFn = function(str){
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteAppsUrl = function() {
        return G.baseUrl + "/api/meta/apps";
    };

    $scope.applyHashData = function (hashData) {
        $scope.query.appid = hashData.appId;
        $scope.query.bus = {};
        if (hashData.bus) {
            $.each(hashData.bus.split(","), function (i, val) {
                $scope.query.bus[val] = val;
            });
        }
    };

    $scope.selectAppId = function (o) {
        if (o) {
            $scope.query.appid = o.originalObject.id;
        }
    };

    $scope.clearQuery = function () {
        $scope.query.appid = "";
        $scope.query.bus = {};
        $scope.setInputsDisplay();
    };
    $scope.executeQuery = function () {
        var hashData = {};
        hashData.appId = $scope.query.appid || "";
        hashData.bus = _.values($scope.query.bus);
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    };
    //Init input field while hashChanged
    $scope.setInputsDisplay = function () {
        $('#appIdSelector_value').val($scope.query.appid);
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.loadData(hashData);
        $scope.applyHashData(hashData);
        $scope.setInputsDisplay();
    };

    H.addListener("appsQueryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("apps-query-area"), ['appsQueryApp']);

var appsResultApp = angular.module('appsResultApp', ['http-auth-interceptor']);
appsResultApp.controller('appsResultController', function ($scope, $http, $q,$rootScope) {

    $scope.resource = H.resource;
    var resource = $scope.resource;
    $scope.query = {
        "appid": "",
        "bus": ""
    };
    //Table  DataSource
    $scope.apps = [];
    $scope.summaryInfo = {};
    //Init appList table rows
    $scope.initTable = function() {
        var resource= $scope.resource;
        $('#apps-table').bootstrapTable({
            toolbar: "#appList-table-toolbar",
            columns: [[
                {
                    field: 'app-id',
                    title: 'AppId',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index) {
                        return '<a title="'+ value +'"href="/portal/app#?env=' + G.env + '&appId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'chinese-name',
                    title: 'AppName',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function(value, row, index) {
                        return '<a title="'+ value +'"href="/portal/app#?env=' + G.env + '&appId=' + row['app-id'] + '">' + value + '</a>';
                    }
                },
                {
                    field: 'owner',
                    title: 'Owner',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        if(!value || value == "unknown")
                            return "-";
                        return '<a href=mailto:' + row['owner-email'] + '>' + value + '</a>';
                    }
                },
                {
                    field: 'sbu',
                    title: 'SBU',
                    align: 'left',
                    valign: 'middle',
                    sortable:true,
                    formatter: function (value, row, index) {
                        if(!value)
                            return '-';
                        return '<a href="' + '/portal/bu#?env=' + G.env + '&buName=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'groupCount',
                    title: 'Groups',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if(!value)
                            return '-';
                        return '<a href="' + '/portal/groups#?env=' + G.env + '&appId=' + row['app-id'] + '">' + value + '</a>';
                    }
                },
                {
                    field: 'memberCount',
                    title: 'Members',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if(!value)
                            return '-';
                        return value;
                    }
                },
                {
                    field: 'serverCount',
                    title: 'Servers',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if(!value)
                            return '-';
                        return value;
                    }
                },
                {
                    field: 'qps',
                    title: 'QPS',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter:function(v, row, index){
                        return v?'<a target="_blank" href="/portal/app/traffic'+ H.generateHashStr({env: G.env,appId:row['app-id']})+'">'+ Math.floor(v) +'</a>':'-';
                    }
                }

            ], []],

            search: true,
            showRefresh: true,
            showColumns: true,
            sortName:'qps',
            sortOrder:'desc',
            data: $scope.apps,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return  (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Applications": resource.apps.js.msg1);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Applications': resource.apps.js.msg2);
            }
        });
    };
    function responseHandler(res) {
        $.each(res.rows, function (i, row) {
            row.state = $.inArray(row.id, selections) !== -1;
        });
        return res;
    }
    $scope.spellQueryString = function(queryString, key, singleValue) {
        if (queryString.endsWith('?')) {
            queryString += key + "=" + singleValue;
        }
        else {
            queryString += "&" + key + "=" + singleValue;
        }
        return queryString;
    };
    $scope.spellQueryData = function(hashData) {
        var appId = "";
        if(hashData.appId)
            appId = hashData.appId;
        var queryString = G.baseUrl + "/api/apps?";

        if(appId != "")
            queryString = $scope.spellQueryString(queryString, "appId", appId);

        return queryString;
    };
    $scope.loadData = function(queryString) {
        $scope.initTable();
        $scope.apps = [];

        $('#apps-table').bootstrapTable('removeAll');
        $('#apps-table').bootstrapTable("showLoading");
        $q.all(
            [
                $http.get(queryString).success(
                    function(res) {
                        $scope.apps = res['apps'];
                        var appResult = [];
                        if($scope.bus && $scope.bus.length > 0) {
                            $.each($scope.apps, function(i, val) {
                                if(val && $scope.bus.indexOf(val.sbu) != -1) {
                                    appResult.push(val);
                                }
                            });
                            $scope.apps = appResult;
                        }
                        $('#apps-table').bootstrapTable("load", $scope.apps);
                    }
                ),
                $http.get(G.baseUrl + "/api/statistics/app").success(
                    function(res) {
                        $scope.appStatistics = res['app-metas'];
                        $scope.appStatisticsMap = {};
                        $.each($scope.appStatistics, function(i, val) {
                            $scope.appStatisticsMap[val['app-id']] = val;
                        });
                    }
                )
            ]
        ).then(
            function() {
                var totalAppCount = $scope.apps.length, totalGroupCount = 0, totalServerCount= 0, totalMemberCount = 0;
                $.each($scope.apps, function(i, val) {
                    if($scope.appStatisticsMap[val['app-id']]) {
                        var newMember = $scope.appStatisticsMap[val['app-id']];
                        val.groupCount = newMember['group-count'];
                        val.memberCount = newMember['member-count'];
                        val.serverCount = newMember['group-server-count'];
                        val.qps = newMember['qps'] || 0;
                        totalGroupCount += val.groupCount;
                        totalMemberCount += val.memberCount;
                        totalServerCount += val.serverCount;
                    }else{
                        val.qps=0;
                    }
                });
                $scope.summaryInfo.totalAppCount=totalAppCount;
                $scope.summaryInfo.totalGroupCount=totalGroupCount;
                $scope.summaryInfo.totalServerCount=totalServerCount;
                $scope.summaryInfo.totalMemberCount=totalMemberCount;
                var qps_count = _.reduce(_.pluck($scope.apps,'qps'), function (left, right) {
                    return left+right;
                });

                $('.servers-text').text(T.getText(totalServerCount));
                $('.qps-text').text(T.getText(qps_count));
                $('.members-text').text(T.getText(totalMemberCount));
                $('.apps-text').text(totalAppCount);
                $('.groups-text').text(totalGroupCount);
                $('#apps-table').bootstrapTable("load", $scope.apps);
                $('#apps-table').bootstrapTable("hideLoading");
            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.env = G.env;
        $scope.appId = "";
        if(hashData.appId) {
            $scope.appId = hashData.appId;
        }

        $scope.bus = [];
        if(hashData.bus) {
            $scope.bus = hashData.bus.split(',');
        }
        var queryString = $scope.spellQueryData(hashData);

        $scope.loadData(queryString);
    };
    H.addListener("appsResultApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("apps-result-area"), ['appsResultApp']);

var appsSummaryApp = angular.module('appsSummaryApp', ['http-auth-interceptor']);
appsSummaryApp.controller('appsSummaryController', function ($scope, $http, $q) {
    
    $scope.resource = H.resource;
    var resource = $scope.resource;

    $scope.navigateTo= function (item) {
        var url='/portal/groups#?env='+$scope.query.env+'&groupType=Group';
        switch (item){
            case 'group':{
                if($scope.query.appId){
                    url+='&appId='+$scope.query.appId;
                }
                if($scope.query.bus){
                    url+='&groupBues='+$scope.query.bus;
                }
                break;
            }
            default : break;
        }

        window.location.href=url;
    };

    $scope.query={};
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if(hashData){
            if(hashData.appId){
                $scope.query.appId=hashData.appId;
            }
            if(hashData.bus){
                $scope.query.bus=hashData.bus;
            }
            if(hashData.env){
                $scope.query.env=hashData.env;
            }
        }
    };

    H.addListener("appsSummaryApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("apps-summary-area"), ['appsSummaryApp']);