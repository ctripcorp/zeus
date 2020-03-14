var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource = H.resource;
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

var selfInfoApp = angular.module('selfInfoApp', ["ngSanitize", "angucomplete-alt", "http-auth-interceptor"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    $scope.resource = H.resource;

    var slbShardingApplication;
    $scope.data = {
        slbCreationStages: ['已创建', '资源申请', '成功创建'],
        vsesStages: [
            '已创建', '待发布', '正在测试', 'DNS切换中', '持续监控', '配置待清理', '迁移待结束', '已完成'
        ]
    };
    $scope.query = {};

    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };

    $scope.remoteSLBsUrl = function () {
        return G.baseUrl + "/api/meta/slbs";
    };

    $scope.selectSlbId = function (o) {
        if (o) {
            var id = o.originalObject.id;
            $scope.query.slbId = id;
            slbSharding.setTargetSlbId(id);
        }
    };

    $scope.getSlbDetail = function (slbId) {
        return slbId;
    };

    $scope.slbIdInputChanged = function (o) {
        $scope.query.slbId = o;
        slbSharding.setTargetSlbId(o);
    };

    $scope.startNewSharding = function () {
        var hasRight = A.canDo('Flow', 'NEW', '*') || A.canDo('Flow', 'FLOW', '*');
        ;
        if (!hasRight) {
            alert('还没有权限，权限申请流程正在建设中...');
            return;
        }
        window.location.replace('/portal/tools/sharding/edit#?env=' + $scope.env);
        window.location.reload();
    };

    $scope.filterSharing = function (status) {
        window.location.replace('/portal/tools/sharding/shardings#?env=' + $scope.env + '&status=' + status);
    };
    $scope.getAllSharing = function () {
        window.location.replace('/portal/tools/sharding/shardings#?env=' + $scope.env);
    };

    $scope.creationDisabled = function () {
        var hasRight = true;
        var shardingId = $scope.query.shardingId;
        if (shardingId) {
            hasRight = A.canDo('Flow', 'UPDATE', shardingId);
        } else {
            hasRight = A.canDo('Flow', 'NEW', '*') || A.canDo('Flow', 'FLOW', '*');
            ;
        }
        return !hasRight;
    };

    $scope.startSlbCreation = function () {
        // show confirm dialog
        var selectedSlbId = slbSharding.getTargetSlbId();
        var promise = slbShardingApplication.getSlbDetail(selectedSlbId);
        promise.then(function (data) {
            var slb = data.data;
            if (slb.code) {
                alert('Failed to get slb data with exception: ' + slb.message);
                return;
            } else {
                var indexed = _.mapObject(_.indexBy(slb.properties, function (v) {
                    return v.name.toLowerCase();
                }), function (value, key, item) {
                    value = value.value;
                    return value;
                });

                slb.properties = indexed;
                $scope.data.targetSlb = slb;
            }
        });


        $('#confirmCreateSlbDialog').modal('show');

    };

    $scope.confirmCreateSlb = function () {
        var createSlbShardingPromise = slbShardingApplication.saveSlbSharding(slbSharding.toShardingDo());
        createSlbShardingPromise.then(function (data) {

            if (data.status != 200) {
                alert('Failed to save slb sharding');
                return;
            }
            var sharding = data.data;
            $scope.query.createShardingId = sharding.id;

            // call create slb method to create an slb
            var createNewSlbDataPromise = slbShardingApplication.newSlbData(sharding.id);
            createNewSlbDataPromise.then(function (data) {
                if (data.status != 200) {
                    alert('create new slb for sharding failed with error message: ' + data.message);
                    return;
                }

                var migrationId = data.data['slb-creating-entity'].id;

                // go to slb creating process for the rest steps
                window.open('/portal/tools/smigration/edit#?env='
                    + $scope.env + '&migrationId='
                    + migrationId + '&slbId=' + $scope.query.slbId,
                    '_blank');

                // becomes edit slb sharding page
                var pair = {
                    timeStamp: new Date().getTime(),
                    shardingId: sharding.id
                };
                H.setData(pair);
            });
        });
    };

    $scope.goToSlbCreation = function () {
        var migrationId = slbSharding.getSlbEntity().id;
        window.open('/portal/tools/smigration/edit#?env='
            + $scope.env + '&migrationId='
            + migrationId,
            '_blank');
    };

    $scope.getSlbCreationStatus = function (index, status) {
        var slbData = $scope.query.slbStage;
        if (!slbData) {
            return;
        }
        var slbStatuses = $scope.data.slbCreationStages;
        var statusIndex = slbStatuses.indexOf(slbData);

        if (index < statusIndex) {
            return 'success';
        }
        if (index == statusIndex) {
            if (index == 1) {
                return 'warning'
            } else {
                return 'success'
            }
        }
    };

    $scope.slbDataRefresh = function () {
        $('#slb-area').showLoading();

        setTimeout(function () {
            $('#slb-area').hideLoading();
        }, 5000);
    };

    $scope.startVsCreation = function () {
        $('#confirmCreateVsDialog').modal('show');
    };

    $scope.confirmCreateVs = function () {
        // create the migration first
        var promise = slbShardingApplication.newVsMigration($scope.query.shardingId);
        promise.then(function (data) {
            if (data.status != 200) {
                alert('Failed to create vs migration with error message: ' + data.data.message);
                return;
            } else {
                var vsMigrationId = data.data['vs-migration'].id;
                window.open('/portal/tools/vmigration/edit#?env=' + $scope.env + '&migrationId=' + vsMigrationId, '_blank');
            }
        });
    };

    $scope.vsDataRefresh = function () {
        $('#vs-area').showLoading();

        setTimeout(function () {
            $('#vs-area').hideLoading();
        }, 5000);
    };

    $scope.getVsCreationStatus = function (index, status) {
        var vsStage = $scope.query.vsStage;

        if (!vsStage) {
            return;
        }

        var vsStatuses = $scope.data.vsesStages;
        if (vsStage.toLowerCase() == 'precreate') {
            vsStage = '已创建';
        }
        var statusIndex = vsStatuses.indexOf(vsStage);

        if (index < statusIndex || index == 0 || statusIndex == vsStatuses.length - 1) {
            return 'success';
        }
        if (index == statusIndex) {
            return 'warning'
        }

    };

    $scope.goToVsCreation = function () {
        var migrationId = slbSharding.vsMigrationEntity.id;
        window.open('/portal/tools/vmigration/edit#?env='
            + $scope.env + '&migrationId='
            + migrationId,
            '_blank');
    };

    $scope.loadData = function () {
        var time = new Date().getTime().toString().substring(0, 3);
        var shardingId = $scope.query.shardingId;

        if (!shardingId) {
            var name = 'shard_' + time;
            slbSharding.init(name);
        } else {
            refreshSlbData(shardingId);
            if ($scope.query.interval) return;
            var intervalId = setInterval(function () {
                refreshSlbData(shardingId);
            }, 5000);
            $scope.query.interval = intervalId;
        }

        var promise = slbShardingApplication.getSlbShardings();
        promise.then(function (data) {
            $scope.total = data.length;
            var shardings = data;
            $scope.data.shardings.statusCountMap = slbShardings.countByStatus($scope.resource);
            slbShardings.init(shardings);
        });
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        var env = 'uat';
        var shardingId;

        // env
        if (hashData.env) {
            env = hashData.env;
        }
        $scope.env = env;

        // sharding id
        if (hashData.shardingId) {
            shardingId = hashData.shardingId;
        }
        $scope.query.shardingId = shardingId;

        // app creation
        slbShardingApplication = SlbShardingApplication.create($http, $q, env);

        // Data binding
        $scope.data.sharding = slbSharding;
        $scope.data.shardings = slbShardings;

        $scope.loadData(env);
    };

    function refreshSlbData(shardingId) {
        var promise = slbShardingApplication.getSlbShardingById(shardingId);
        promise.then(
            function (data) {
                if (data.status != 200) {
                    alert('Failed to get slb sharding with id: ' + shardingId + ', Error message: ' + data.data.message);
                    return;
                } else {
                    slbSharding.toSharding(data.data);
                    var slbEntity = slbSharding.getSlbEntity();
                    if (slbEntity) {
                        $scope.query.slbStage = slbEntity.status;
                    }
                    var vsEntity = slbSharding.getVsMigrationEntity();
                    if (vsEntity) {
                        $scope.query.vsStage = vsEntity.status;
                    }
                }
            });
    }

    H.addListener("selfInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);