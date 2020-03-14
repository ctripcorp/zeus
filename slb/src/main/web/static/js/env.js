var envApp = angular.module('envApp', ['http-auth-interceptor']);
envApp.controller('envController', function ($scope, $http, $q) {
    $scope.query = {};
    $scope.data = {};
    $scope.addEnv = function () {
        $('#newEnv').modal('show');
    };

    $scope.saveApi = function () {
        var api = $scope.data.api;

        var url = hasConfig ? '/api/config/batchupdate' : '/api/config/batchinsert';
        var request = {
            method: 'POST',
            url: api + url,
            data: [{
                'property-key': 'env',
                'property-value': {
                    api: api
                }
            }]
        };
        $http(request).success(function (response, code) {
            if (code == 200) {
                $.cookie("base", api);
            }
        });
    };

    $scope.showEnvs = function () {
        return $scope.data.api;
    };

    $scope.showInitDB = function () {
        return $scope.query.inited;
    };

    $scope.saveSettings = function () {
        var api = $scope.data.host;
        var db = $scope.data.db;
        var user = $scope.data.user;
        var pwd = $scope.data.pwd;

        var requests = [];

        var request = {
            method: 'GET',
            url: api + '/api/init/db?initConfigs=agent.api.host=' + api
        };

        requests.push($http(request).success(function (response, code) {
            if (code == 200) {

                $scope.query.initStatus = 1;
                $.cookie("base", api);

            } else {
                $scope.query.initStatus = 0;
                // $scope.query.status = '初始化失败';
            }

        }));

        $scope.query.status = '正在初始化...';

        if (db && user && pwd) {
            requests.push($http({
                method: 'POST',
                url: api + '/api/init/connection',
                data: {
                    "url": db,
                    "username": user,
                    "password": pwd
                }
            }).success(function (response, code) {
                if (code == 200) {
                    $scope.query.dbstatus = 1;
                } else {
                    $scope.query.dbstatus = 0;
                }
            }));
        }
        $('#loading').showLoading();

        $q.all(requests).then(function () {
            var s1 = $scope.query.dbstatus;
            if (s1) {
                $scope.query.status = '初始化成功. 3秒后跳转到首页!';
                setTimeout(function () {
                    window.location.href = '/portal';
                }, 3000);
            } else {
                $scope.query.status = '初始化失败';
            }
            $('#loading').hideLoading();
        });
    };

    $scope.statusClass = function (v) {
        if (v && v.indexOf('初始化成功') != -1) return 'status-green';
        return 'status-red';
    };

    $scope.hashChanged = function (hashData) {
        var host = '';
        var cookie = $.cookie("base");
        if (cookie) {
            host = cookie;
        }

        var request = {
            method: 'GET',
            url: host + '/api/init/host'
        };
        $http(request).success(function (response, code) {
            if (code == 200 && _.keys(response).length > 0) {
                $scope.data.host = response['api'];
            }
        });

        var request2 = {
            method: 'GET',
            url: host + '/api/init/check'
        };
        $http(request2).success(function (response, code) {
            if (code == 200 && _.keys(response).length > 0) {
                $scope.query.inited = response.result;
            }
        });
    };
    H.addListener("envApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("env-id"), ['envApp']);
