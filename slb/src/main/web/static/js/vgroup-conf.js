/**
 * Created by ygshen on 2017/3/6.
 */

//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp2', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController2', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'basic':
            {
                link = "/portal/vgroup#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'log':
            {
                link = "/portal/vgroup/log#?env=" + G.env + "&groupId=" + $scope.query.groupId;
                break;
            }
            case 'conf':
            {
                link = "/portal/vgroup/conf#?env=" + G.env + "&groupId=" + $scope.query.groupId;
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
        if (hashData.groupId) {
            $scope.query.groupId = hashData.groupId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area2"), ['headerInfoApp2']);
