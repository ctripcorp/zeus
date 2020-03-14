/**
 * Created by ygshen on 2017/2/6.
 */

var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
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
            case 'cert-upgrade': {
                link = "/portal/tools/cert/migrations";
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

var toolsApp = angular.module('toolsApp', ["angucomplete-alt", "http-auth-interceptor"]);
toolsApp.controller('toolsController', function ($scope, $http, $q) {
   $scope.env = 'pro';

    $scope.tools = [];

    $scope.navigateTo = function (id) {
        switch (id) {
            case 1: {
                window.location.href = '/portal/tools/visiturl#?env=' + $scope.env;
                break;
            }
            case 2: {
                window.location.href = '/portal/tools/cert/migrations#?env=' + $scope.env;
                break;
            }
            case 4: {
                window.location.href = '/portal/tools/vmigration/migrations#?env=' + $scope.env;
                break;
            }
            case 5: {
                window.location.href = '/portal/tools/sharding/shardings#?env=' + $scope.env;
                break;
            }
            case 6: {
                window.location.href = '/portal/tools/dr/drs#?env=' + $scope.env;
                break;
            }
            case 7: {
                window.location.href = '/portal/tools/vs/seperates#?env=' + $scope.env;
                break;
            }
            case 8: {
                window.location.href = '/portal/tools/vs/merges#?env=' + $scope.env;
                break;
            }
            case 3: {
                window.location.href = '/portal/tools/smigration/migrations#?env=' + $scope.env;
                break;
            }
            default :
                break;
        }
    };
    $scope.shareThis = function (url) {
        $scope.sharetext = url;
        $('#shareToolModal').modal('show');
    };
    $scope.loadData = function () {
        var resource = $scope.resource;

        $scope.tools = [
            {
                id: 1,
                name: (angular.equals(resource, {}) ? 'URL探测工具': resource.tools.tools.js.msg1),
                description: (angular.equals(resource, {}) ? 'URL请求是由哪个Group来处理的': resource.tools.tools.js.msg2),
                thumb: 'fa fa-tripadvisor fa-4',
                url: window.location.protocol + '//' + window.location.hostname + '/portal/tools/visiturl#?env=' + $scope.env
            },
            {
                id: 2,
                name: "证书管理工具",
                description: "证书管理工具",
                thumb: 'fa fa-certificate fa-4',
                url: window.location.protocol + '//' + window.location.hostname + '/portal/tools/cert/migrations#?env=' + $scope.env
            },
            {
                id: 5,
                name: (angular.equals(resource, {}) ? 'SLB拆分工具': resource.tools.tools.js.msg5),
                description: (angular.equals(resource, {}) ? '新建SLB拆分': resource.tools.tools.js.msg6),
                thumb: 'fa fa-cogs fa-4',
                url: window.location.protocol + '://' + window.location.hostname + '/tools/sharding/shardings#?env='+$scope.env
            },


            {
                id: 4,
                name: (angular.equals(resource, {}) ? 'VS迁移工具': resource.tools.tools.js.msg13),
                description: (angular.equals(resource, {}) ? '域名在相同IDC不同SLB之间迁移': resource.tools.tools.js.msg18),
                thumb: 'fa fa-cloud fa-4',
                url: window.location.protocol + '://' + window.location.hostname + '/tools/vmigration/migrations#?env='+$scope.env
            },

            {
                id: 7,
                name: (angular.equals(resource, {}) ? 'VS拆分工具': resource.tools.tools.js.msg14),
                description: (angular.equals(resource, {}) ? '拆分多域名VS': resource.tools.tools.js.msg15),
                thumb: 'fa fa-delicious fa-4',
                url: window.location.protocol + '://' + window.location.hostname + '/tools/vs/seperates#?env='+$scope.env
            },
            {
                id: 8,
                name: (angular.equals(resource, {}) ? 'VS合并工具': resource.tools.tools.js.msg16),
                description: (angular.equals(resource, {}) ? '合并VS': resource.tools.tools.js.msg17),
                thumb: 'fa fa-steam fa-4',
                url: window.location.protocol + '://' + window.location.hostname + '/tools/vs/merges#?env='+$scope.env
            },

        ];
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        $scope.loadData();
    };
    H.addListener("toolsApp", $scope, $scope.hashChanged);
});
toolsApp.filter('toolFilter', function () {
    return function (input, param1) {
        if (!param1 || param1.trim() == "") {
            return input;
        }

        param1 = param1.trim().toLowerCase();

        return _.filter(input, function (item) {
            return item.name.toLowerCase().indexOf(param1) != -1 || item.description.toLowerCase().indexOf(param1) != -1;
        });
    }
});
angular.bootstrap(document.getElementById("tools-area"), ['toolsApp']);

