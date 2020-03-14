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
            case 'dashboard': {
                link = "/portal/statistics/dashboard#?env=" + G.env;
                break;
            }
            case 'abtest': {
                link = "/portal/statistics/statistics-normalpolicy#?env=" + G.env;
                break;
            }
            case 'rule': {
                link = "/portal/statistics/rule#?env=" + G.env;
                break;
            }
            case 'hc': {
                link = "/portal/statistics/statistics-hc#?env=" + G.env;
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
    $scope.hashChanged = function (hashData) {
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


var resultApp = angular.module('resultApp', ['http-auth-interceptor']);
resultApp.controller('resultController', function ($scope, $http, $q) {
    $scope.query = {};
    $scope.data = {
        cert: {}
    };
    var env;

    function loadData(hashData) {
        var certId = hashData.certId;

        var params = {
            certId: certId,
            withBlobs: true
        };

        var request = {
            method: 'GET',
            params: params,
            url: G.baseUrl+'/api/certs/get'
        };

        var vsesRequest = {
            url: G.baseUrl+'/api/vses',
            method: 'GET'
        };

        var vses;
        var cert;

        var query2 = $http(vsesRequest).success(function (response, code) {
            if (code == 200) {
                vses = _.indexBy(response['virtual-servers'], 'id');
            }
        });

        var query1 = $http(request).success(function (response, code) {
            if (code == 200) {
                cert = response;
                $scope.data.cert = cert;
            }
        });

        $q.all([query1, query2]).then(function () {
            var vsIds = cert['vs-ids'];
            var certVses = _.map(vsId,function (v) {
                return {
                    id: v,
                    name: vses[v].name
                };
            });
            $('#related-vses').bootstrapTable('load',certVses);
        });
    };

    function processing() {
        $('#progressDialog').modal('show').find('.modal-body').html('<span class=""><img src="/static/img/spinner.gif">正在上传</span>');
    }

    function succeed() {
        $('#progressDialog').modal('show').find('.modal-body').html('<span class="fa fa-check status-green">上传成功</span>');
        setTimeout(function () {
            close();
            H.setData({timeStamp: new Date().getTime()})
        }, 2000);
    }

    function close() {
        $('#progressDialog').modal('hide');
    }

    function fail(err) {
        $('#progressDialog').modal('show').find('.modal-body').html('<span class="fa fa-times status-red">上传失败,错误信息: ' + err + '</span>');
        setTimeout(function () {
            close();
        }, 2000);
    }

    function initTable() {
        $('#related-vses').bootstrapTable({
            toolbar: "#toolbar",
            columns: [[{
                field: 'state',
                checkbox: true,
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
                        return '<a class="editVS" target="_self" title="' + value + '" href="/portal/vs#?env=' + G.env + '&vsId=' + value + '" style="text-decoration: none; margin-left: 5px; word-break: break-all">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a class="editVS" target="_self" title="' + value + '" href="/portal/vs#?env=' + G.env + '&vsId=' + value + '" style="text-decoration: none; margin-left: 5px; word-break: break-all">' + value + '</a>';
                    }
                },
                {
                    field: 'domains',
                    title: 'Domain列表',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var domains = "";
                        domains += '<div style="width: auto;">';
                        $.each(value, function (i, val) {
                            domains += '<a class="editVS" target="_self" title="' + val.name + '" href="/portal/vs#?env=' + G.env + '&vsId=' + row.id + '" style="text-decoration: none; margin-left: 5px">' + val.name + '</a><br>'
                        });
                        domains += '</div>'
                        return domains;
                    }
                }], []],
            sortName: 'qps',
            sortOrder: 'desc',
            showRefresh: true,
            search: true,
            showColumns: true,
            data: [],
            classes: "table table-bordered  table-hover table-striped",
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Virtual Servers";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Virtual Servers';
            }
        });
    };

    $scope.hashChanged = function (hashData) {
        env = hashData.env || 'pro';
        $scope.query.certId = hashData.certId;

        initTable();
        loadData(hashData);
    };
    H.addListener("resultApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("result-area"), ['resultApp']);