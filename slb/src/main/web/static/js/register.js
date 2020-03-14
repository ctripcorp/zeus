var loginApp = angular.module('regApp', ['http-auth-interceptor']);
loginApp.controller('regController', function ($scope, $http) {
    $scope.data = {
        user: '',
        password: ''
    };

    var success = false;

    $scope.registerNewAccount = function () {
        var user = $scope.data.user;
        var password = $scope.data.password;

        if (!user || !password) return;


        var params = {
            userName: user.trim(),
            password: password
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/user/signup',
            params: params
        };
        $http(request).success(function (response, code) {
            if (code != 200) {
                $('#confirmDialog').modal('show').find('#message').html('<div class="warning-important">创建失败,失败原因:' + (response.message || response.code || code) + '</div>');
                return;
            }
            $('#confirmDialog').modal('show').find('#message').html('<div class="status-green">创建成功，点击确定进入登陆页面.</div>');
            success = true;
        });
    };

    $scope.confirmClick = function () {
        if (!success) {
            return;
        }
        window.location.href = '/portal/login';
    };

    $scope.hashChanged = function () {
    };
    H.addListener("regApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("regArea"), ['regApp']);
