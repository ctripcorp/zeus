var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {

    $scope.resource = H.resource;
    var resource =  $scope.resource;
    $scope.query = {};
    // Auto complete
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteUrl = function () {
        return G.baseUrl + "/api/meta/apps";
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.id;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs['appId'] = toId;
                $scope.target.id = toId;
                H.setData(pairs);
                messageNotify((angular.equals(resource, {}) ? "切换应用:": resource.appLog.js.msg1), (angular.equals(resource, {}) ? "成功切换至应用： ": resource.appLog.js.msg2) + toId, null);
            }
        }
    };
    $scope.clickTarget= function () {
        $('#targetSelector_value').css('width','250px');
    };
    $scope.setInputsDisplay = function () {
        $('#targetSelector_value').val($scope.query.buName);
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.appId) {
            $scope.query.appId = hashData.appId;
        }
        $scope.target = {};
        $scope.target.name = $scope.query.appId;
    };
    H.addListener("summaryInfoApp", $scope, $scope.hashChanged);
    function messageNotify(title, message, url) {
        var notify = $.notify({
            icon: '',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
            type: 'success',
            allow_dismiss: true,
            newest_on_top: true,
            placement: {
                from: 'top',
                align: 'center'
            },
            offset: {
                x: 0,
                y: 0
            },
            animate: {
                enter: 'animated fadeInDown',
                exit: 'animated fadeOutUp'
            },
            delay: 1000,
            spacing: 5,
            z_index: 1031,
            mouse_over: 'pause'
        });
    }
});
angular.bootstrap(document.getElementById("summary-area"), ['summaryInfoApp']);

//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    
    $scope.resource = H.resource;
    var resource =  $scope.resource;
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic':
            {
                link = "/portal/app#?env=" + G.env + "&appId=" + $scope.query.appId;
                break;
            }
            case 'log':
            {
                link = "/portal/app/log#?env=" + G.env + "&appId=" + $scope.query.appId;
                break;
            }
            case 'traffic':
            {
                link = "/portal/app/traffic#?env=" + G.env + "&appId=" + $scope.query.appId;
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

var operationLogApp = angular.module('operationLogApp', ['http-auth-interceptor']);
operationLogApp.controller('operationLogController', function($scope, $http){

    $scope.resource = H.resource;
    var resource =  $scope.resource;
    $scope.paramName = "appId";
    $scope.target = "apps";
    $scope.targetType = "APP";
    $scope.targetLink = '/portal/app';
    $scope.tableId = '#operation-log-table';
    $scope.targetInfo = '';
    $scope.data = {
    };
    $scope.generateTargetLink = function(){
        return $scope.targetLink + "#?env=" + G.env + "&" + $scope.paramName + "=" +$scope[$scope.paramName];
    };
    $scope.getLogInfo = function (id) {
        $($scope.tableId).bootstrapTable('removeAll');
        $($scope.tableId).bootstrapTable("showLoading");
        $http.get(G.baseUrl + "/api/logs?type=" + $scope.targetType + "&targetId=" + $scope[$scope.paramName]).success(
            function (res) {
                $scope.data = res['operation-log-datas'];
                if(!$scope.data) {
                    $scope.data = [];
                }
                $($scope.tableId).bootstrapTable("load",  $scope.data);
                $($scope.tableId).bootstrapTable("hideLoading");
            }
        );
        $http.get(G.baseUrl + "/api/"+$scope.target+"?" + $scope.paramName+ "=" + $scope[$scope.paramName]).success(
            function (res) {
                if(res && res.total && res.total > 0 && res['apps'])
                res = res['apps'][0];
                $scope.targetInfo = " " + res['app-id'] + " / " + res['chinese-name'] + "";
            }
        );
    };

    $scope.initTable = function() {
        var resource= $scope.resource;
        $($scope.tableId).bootstrapTable({
            toolbar: "#operation-log-toolbar",
            columns: [[
                {
                    field: 'operation',
                    title: (angular.equals(resource, {}) ? '操作': resource.appLog.js.msg3),
                    align: 'left',
                    valign: 'middle'
                },{
                    field: 'user-name',
                    title: (angular.equals(resource, {}) ? '用户': resource.appLog.js.msg4),
                    align: 'center',
                    valign: 'middle'
                },{
                    field: 'client-ip',
                    title: (angular.equals(resource, {}) ? '客户端IP': resource.appLog.js.msg5),
                    align: 'left',
                    valign: 'middle'
                },{
                    field: 'success',
                    title: (angular.equals(resource, {}) ? '结果': resource.appLog.js.msg6),
                    align: 'left',
                    valign: 'middle'
                }, {
                    field: 'data',
                    title: (angular.equals(resource, {}) ? '数据': resource.appLog.js.msg7),
                    align: 'left',
                    valign: 'middle'
                }, {
                    field: 'date-time',
                    title: (angular.equals(resource, {}) ? '时间': resource.appLog.js.msg8),
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                }
            ], []],
            data: $scope.data,
            search:true,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pagination: true,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return  (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Application Operation Logs": resource.appLog.js.msg9);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Application Operation Logs': resource.appLog.js.msg10);
            }
        });
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if(hashData.env) {
            $scope.env = hashData.env;
        }
        if(hashData[$scope.paramName]) {
            $scope.initTable();
            $scope[$scope.paramName] = hashData[$scope.paramName];
            $scope.getLogInfo(hashData[$scope.paramName]);
        }
    };
    H.addListener("operationLogApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("log-area"), ['operationLogApp']);
