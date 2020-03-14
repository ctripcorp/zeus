var loginApp = angular.module('loginApp', ['http-auth-interceptor']);
loginApp.controller('loginController', function ($scope, $http) {
    $scope.data = {
        user: '',
        password: ''
    };

    $scope.forgetPwd = function () {
        $('#confirmDialog').modal('show').find('#message').html('<div class="warning-important">请联系系统管理员重置密码</div>');
    };

    $scope.loginClick = function () {
        var user = $scope.data.user;
        var password = $scope.data.password;

        if (!user || !password) return;

        var params = {
            userName: user.trim(),
            password: password
        };
        var request = {
            method: 'GET',
            url: '/api/user/login',
            params: params
        };
        $http(request).success(function (response, code) {
            if (code != 200) {
                $('#confirmDialog').modal('show').find('#message').html('<div class="warning-important">登陆失败,失败原因:' + (response.message || response.code || code) + '</div>');
                return;
            } else {
                // redirect to user page
                window.open("/portal", '_self');
            }
        });
    };

    $scope.hashChanged = function () {

    };
    H.addListener("loginApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("loginArea"), ['loginApp']);
