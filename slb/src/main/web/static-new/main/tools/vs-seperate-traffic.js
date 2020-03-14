var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        var id = $scope.query.id;
        switch (a) {
            case 'basic': {
                link = "/portal/tools/vs/seperate-edit#?env=" + G.env;
                if (id) {
                    link += '&id=' + id;
                }
                break;
            }
            case 'traffic': {
                link = "/portal/tools/vs/seperate-traffic#?env=" + G.env;
                break;
            }
            default:
                break;
        }
        if (id) {
            link += '&id=' + id;
        }
        return link;
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.id) {
            $scope.query.id = hashData.id;
        }
    };
    H.addListener("headerInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);


var selfInfoApp = angular.module('selfInfoApp', ["ngSanitize", "angucomplete-alt", "http-auth-interceptor"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    $scope.query = {};

    $scope.data = {};

    $scope.applyHashData = function (hashData) {
        var id = hashData.id;
        $scope.query.id = id;
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.applyHashData(hashData);
    };

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);