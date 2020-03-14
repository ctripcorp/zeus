//InfoLinksComponent: info links
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
            case 'rule': {
                link = "/portal/statistics/rule#?env=" + G.env;
                break;
            }
            case 'policy': {
                link = "/portal/statistics/statistics-netjava#?env=" + G.env;
                break;
            }
            case 'abtest': {
                link = "/portal/statistics/statistics-normalpolicy#?env=" + G.env;
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

var problemIssuesApp = angular.module('problemIssuesApp', ["angucomplete-alt", 'ngSanitize', "http-auth-interceptor"]);
problemIssuesApp.controller('problemIssuesController', function ($scope, $http, $q) {

    $scope.data = {
        clusters: {},
        clusterIps: [],
        clusterConfigs: []
    };

    $scope.query = {
        selectedClusterIndex: -1
    };

    $scope.disableSet = function () {
        var clusters = $scope.data.clusters;
        var index = $scope.query.selectedClusterIndex;
        var cluster = clusters[names[index]];
        if (!cluster) return true;

        var configs = cluster.configs;
        for (var i = 0; i < configs.length; i++) {
            var cname = configs[i].name;
            var cvalue = configs[i].value;

            if (cname && cvalue) continue;
            else return true;
        }

        return false;
    };


    $scope.saveSettings = function () {
        var clusters = $scope.data.clusters;
        var index = $scope.query.selectedClusterIndex;

        var name = names[index];
        var cluster = clusters[name];
        if (!cluster) return;


        saved = _.indexBy(saved, 'name');
        var current = saved[name];


        var configs = getTobeChanged(current, cluster);
        if (configs.length > 0) {
            var request = {
                method: 'POST',
                url: G.baseUrl + '/api/hc/properties/update',
                data: {
                    name: name,
                    configs: configs
                }
            };

            $http(request).success(function (response, code) {
                if (code == 200) {
                    alert('保存成功');
                    H.setData({timeStamp: new Date().getTime()});
                }
            });
        } else {
            return;
        }
    };

    function getTobeChanged(a, b) {
        var ac = _.indexBy(a.configs, 'name');
        var bc = _.indexBy(b.configs, 'name');

        var result = [];

        var keys = _.keys(ac);
        for (var i = 0; i < keys.length; i++) {
            if (ac[keys[i]].value == bc[keys[i]].value) continue;

            result.push({
                'name': keys[i],
                'value': bc[keys[i]].value
            });
        }

        return result;
    };


    $scope.resetSetting = function () {
        H.setData({timeStamp: new Date().getTime()});
    };


    $scope.isSelectedCluster = function (index, x) {
        if ($scope.query.selectedClusterIndex == index) return 'btn btn-info';
        return 'btn btn-default';
    };

    $scope.toggleCluster = function (index, x) {
        $scope.query.selectedClusterIndex = index;
    };

    $scope.$watch('query.selectedClusterIndex', function (newVal, oldVal) {
        if (newVal < 0) return;

        var name = names[newVal];
        $scope.data.clusterIps = $scope.data.clusters[name]['ips'];
        $scope.data.clusterConfigs = $scope.data.clusters[name]['configs'];

    });

    $scope.loadData = function () {
        var query = {
            method: 'GET',
            url: G.baseUrl + '/api/hc/list?format=json'
        };

        $http(query).success(function (response, code) {
            if (code == 200) {
                var clusters = response;

                names = _.pluck(clusters, 'name');
                $scope.query.selectedClusterIndex = 0;
                $scope.data.clusters = _.indexBy(clusters, 'name');
                saved = $.extend(true, [], clusters);
            }
        });


        // var clusters = [
        //     {
        //         "name": "clustor name",
        //         "ips": [
        //             "127.0.0.1",
        //             "127.0.0.1"
        //         ],
        //         "configs": [
        //             {
        //                 "name": "config 1",
        //                 "value": "value 1"
        //             },
        //             {
        //                 "name": "config 2",
        //                 "value": "value 2"
        //             }
        //         ]
        //     },
        //     {
        //         "name": "clustor name2",
        //         "ips": [
        //             "127.0.0.1"
        //         ],
        //         "configs": [
        //             {
        //                 "name": "config 1",
        //                 "value": "value 1"
        //             },
        //             {
        //                 "name": "config 2",
        //                 "value": "value 2"
        //             }
        //         ]
        //     }
        // ];
        // names = _.pluck(clusters, 'name');
        // $scope.query.selectedClusterIndex = 0;
        // $scope.data.clusters = _.indexBy(clusters, 'name');


    };
    var names;
    var saved;
    $scope.hashChanged = function (hashData) {
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        $scope.loadData(hashData);
    };
    H.addListener("problemIssuesApp", $scope, $scope.hashChanged);
})
;
angular.bootstrap(document.getElementById("tools-area"), ['problemIssuesApp']);


