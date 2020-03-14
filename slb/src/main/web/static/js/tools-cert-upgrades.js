﻿var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope) {
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
            case 'cert-upgrade': {
                link = "/portal/tools/cert/migrations#?env=" + G.env;
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
    $scope.hashChanged = function (hashData) {
        if (hashData.env) {
            $scope.env = hashData.env;
        }
    };
    H.addListener("headerInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var selfInfoApp = angular.module('selfInfoApp', ["angucomplete-alt", "http-auth-interceptor", "ngSanitize"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    $scope.data = {};

    $scope.loadData = function () {
        var request = {
            method: 'GET',
            url: '/api/tools/cert/upgrades'
        };
        $http(request).success(function (response, code) {
            if (code == 200) {
                $scope.data.upgrades = response;

                $('#upgrades').bootstrapTable("load", response);
            }
        });
    };

    function initTable() {
        $('#upgrades').bootstrapTable({
            toolbar: "#upgrade-tool",
            columns: [[
                {
                    field: 'state',
                    checkbox: 'true',
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="' + value + '"href="/portal/tools/cert/edit#?env=' + G.env + '&upgradeId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="' + value + '"href="/portal/tools/cert/edit#?env=' + G.env + '&upgradeId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var v='-';
                        switch (value){
                            case 'Activate':{
                                v='证书灰度中';
                                break;
                            }
                            case 'Test':{
                                v='灰度测试中';
                                break;
                            }
                            case 'Done':{
                                v='灰度完成';
                                break;
                            }
                        }
                        return v;
                    }
                }
            ], []],
            sortName: 'id',
            sortOrder: 'desc',
            data: [],
            classes: "table-bordered  table-hover table-striped table",
            search: true,
            showRefresh: true,
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
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载全部证书升级流程";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的证书升级流程';
            }
        });
    };

    $scope.hashChanged = function (hashData) {
        $scope.env = hashData.env ? hashData.env : 'pro';
        initTable();
        $scope.loadData();
    };

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);