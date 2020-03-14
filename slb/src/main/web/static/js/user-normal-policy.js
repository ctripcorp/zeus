/**
 * Created by ygshen on 2017/4/24.
 */
var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.resource = H.resource;

    $scope.query = {
        userId: ''
    };
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };

    $scope.remoteUrl = function () {
        return $scope.context.targetsUrl;
    };

    $scope.context = {
        targetIdName: 'userId',
        targetNameArr: ['email', 'id', 'name'],
        targetsUrl: G.baseUrl + '/api/meta/users',
        targetsName: 'users'
    };

    $scope.target = {
        id: null,
        name: ''
    };
    $scope.targets = {};
    $scope.clickTarget = function () {
        $('#targetSelector_value').css('width', '250px');
    };
    $scope.getAllTargets = function () {
        var c = $scope.context;
        $http.get(c.targetsUrl).success(
            function (res) {
                if (res.length > 0) {
                    $.each(res, function (i, val) {
                        $scope.targets[val.id] = val;
                    });
                }
                if ($scope.target.id) {
                    if ($scope.targets[$scope.target.id])
                        $scope.target.name = $scope.target.id;
                    else {
                        $http.get("/api/auth/user?userId=" + $scope.target.id).success(
                            function (res) {
                                $scope.target.name = $scope.target.id;
                            }
                        );
                    }
                }
            }
        );
    };
    $scope.selectTarget = function (t) {
        if (t) {
            var toId = t.originalObject.name;
            if ($scope.target.id != toId) {
                $scope.$broadcast('angucomplete-alt:clearInput', 'targetSelector');
                var pairs = {};
                pairs[$scope.context.targetIdName] = toId;
                H.setData(pairs);
                messageNotify((angular.equals(resource, {}) ? "切换用户:" : resource.userNormalPolicy.js.msg1), (angular.equals(resource, {}) ? "成功切换至用户: " : resource.userNormalPolicy.js.msg2) + toId, null);
            }
        }
    };
    $scope.data = {
        current: '基本信息',
        links: ['基本信息', '权限', '操作日志'],
        hrefs: {
            '基本信息': '/portal/user',
            '操作日志': '/portal/user/log',
            '权限': '/portal/user/user-access'
        }
    };

    $scope.isCurrentInfoPage = function (link) {
        return $scope.data.current == link ? 'current' : '';
    };

    $scope.generateInfoLink = function (link) {
        var b = $scope.data.hrefs[link] + "#?env=" + G.env;
        if ($scope.query.userId) {
            b += '&userId=' + $scope.query.userId;
        }
        return b;
    };

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        var n = $scope.context.targetIdName;
        if (hashData[n]) {
            $scope.target.id = hashData[n];
            $scope.getAllTargets();
        }
        if (hashData.userId) {
            $scope.query.userId = hashData.userId;
        }
        $scope.target = {};
        if (hashData.userId) {
            $scope.target.name = hashData.userId;
        } else {
            $scope.target.name = 'Me';
        }
    };
    H.addListener("summaryInfoApp", $scope, $scope.hashChanged);

    function messageNotify(title, message, url) {
        var notify = $.notify({
            icon: '',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
            type: 'success',
            allow_dismiss: true,
            newest_on_top: true,
            placement: {
                from: 'top',
                align: 'center'
            },
            offset: {
                x: 0,
                y: 0
            },
            animate: {
                enter: 'animated fadeInDown',
                exit: 'animated fadeOutUp'
            },
            delay: 1000,
            spacing: 5,
            z_index: 1031,
            mouse_over: 'pause'
        });
    }
});
angular.bootstrap(document.getElementById("summary-area"), ['summaryInfoApp']);

var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource = H.resource;

    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'home': {
                link = "/portal/user-home#?env=" + G.env;
                break;
            }
            case 'basic': {

                link = "/portal/user#?env=" + G.env;
                break;
            }
            case 'access': {
                link = "/portal/user/user-access#?env=" + G.env;
                break;
            }
            case 'log': {
                link = "/portal/user/log#?env=" + G.env;

                break;
            }
            case 'policy': {
                link = "/portal/user/user-trafficpolicy#?env=" + G.env;
                break;
            }
            case 'AB': {
                link = "/portal/user/user-normalpolicy#?env=" + G.env;
                break;
            }
            case 'drs': {
                link = "/portal/user/user-drs#?env=" + G.env;
                break;
            }
            case 'unhealthy': {
                link = "/portal/user/user-unhealthy#?env=" + G.env;
                break;
            }

            default:
                break;
        }
        if ($scope.query.userId) {
            link += '&userId=' + $scope.query.userId;
        }
        return link;
    }
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.userId) {
            $scope.query.userId = hashData.userId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var selfInfoApp = angular.module('selfInfoApp', ["angucomplete-alt", "http-auth-interceptor", "ngSanitize"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q, $window) {
    $scope.resource = H.resource;
    var resource = $scope.resource;

    var userPolicyApp;
    $scope.query = {
        login: '',
        user: '',
        appId: '',
        javaGroups: [],
        netGroups: []
    };
    $scope.ownedTraffics = [];
    $scope.focusedTraffics = [];
    $scope.vses = {};
    $scope.groups = {};
    $scope.apps = {};
    $scope.loginUserInfo = {
        user: '',
        policies: {},
        owned: {}
    };
    $scope.onlinePolicyData = {};
    $scope.tobeActivatedPolicyData = {};
    $scope.init = function () {
        $scope.query = {
            user: '',
            javaGroups: [],
            netGroups: []
        };
        $scope.ownedTraffics = [];
        $scope.focusedTraffics = [];
        $scope.vses = {};
        $scope.groups = {};
        $scope.apps = {};
        $scope.loginUserInfo = {
            user: '',
            policies: {},
            owned: {}
        };
        $scope.onlinePolicyData = {};
        $scope.tobeActivatedPolicyData = {};
        $scope.validate = undefined;
        $scope.validate2 = undefined;
        $scope.validate3 = undefined;
        $scope.isActivating = false;
        $scope.firstToSecondErrorMessage = '';
        $scope.read = false;

        $scope.firstshow = false;
        $scope.deactivatepolicyobj = {};
        $scope.selecteIndex = 0;
        $scope.allowToDeactivate = false;
        $scope.confirmDeactivate = false;
        $scope.logs = [];
        $scope.appidNotHasPolicyError = '';
    };
    // Create new policy Area
    $scope.startNewTrafficPolicy = function () {
        $('#appIdSelector_value').attr('disabled', false);
        $('#appIdSelector_value').val('');
        $scope.validate = undefined;
        $scope.validate2 = undefined;
        $scope.validate3 = undefined;
        $scope.showFirstSuggestGroup = undefined;
        $scope.showFirstGroupError = undefined;
        $scope.showSecondGroupError = undefined;
        $scope.showSecondGroupErrorMessage = undefined;
        $('#newTrafficPolicyDialog').modal('show');
    };
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteAppsUrl = function () {
        return G.baseUrl + "/api/meta/apps";
    };

    $scope.selectAppId = function (o) {
        if (o) {
            $scope.query.appId = o.originalObject.id;
            $scope.changeAppIdSelection();
        }
    };
    $scope.changeAppIdSelection = function () {
        var appId = $scope.query.appId;
        var param = {
            type: 'extended',
            appId: appId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/groups',
            params: param
        };
        $http(request).success(function (response) {
            var data = _.map(response['groups'], function (item) {
                var appId = item['app-id'];
                var app = appId + '(' + ($scope.apps[appId]?$scope.apps[appId]['chinese-name']:'-') + ')';
                var id = item.id;
                var name = item.name;
                var ravailable = false;
                var savailable = false;
                var pavailable = false;
                var vsavailable = false;
                var lavailable = false;

                var idcItem = _.find(item.properties, function (v) {
                    return v.name == 'idc';
                });
                var idc = idcItem ? idcItem.value : '-';

                var statusItem = _.find(item.properties, function (v) {
                    return v.name == 'status' && v.value.toLowerCase() == 'activated';
                });
                if (statusItem) savailable = true;

                var relatedItem = _.find(item.properties, function (m) {
                    return m.name.toLowerCase() == 'relatedappid'/* && $scope.apps[m.value]*/;
                });
                if (relatedItem) ravailable = true;

                var isJava = false;
                var languageItem = _.find(item.properties, function (m) {
                    return m.name == 'language' && m.value == 'java';
                });
                if (languageItem) isJava = true;

                if (isJava) {
                    if (ravailable) {
                        var targetAppId = relatedItem.value;
                        var targetGroupsCollection = _.filter($scope.groups, function (v) {
                            return v['app-id'] == targetAppId;
                        });

                        var targetGroups = _.filter($scope.groups, function (v) {
                            var vproperties = _.indexBy(v['properties'], function (s) {
                                return s.name.toLowerCase();
                            });
                            return v['app-id'] == targetAppId && vproperties['language'].value == '.net';
                        });
                        if (targetGroups.length == targetGroupsCollection.length) {
                            lavailable = false;
                        } else {
                            lavailable = true;
                        }
                    } else {
                        lavailable = false;
                    }
                } else {
                    lavailable = true;
                }


                var hasPolicy = _.find($scope.existingPolicies, function (b) {
                    var gs = _.map(b.controls, function (d) {
                        return d.group.id;
                    });
                    if (gs.indexOf(id) != -1) return true;
                    return false;
                });
                if (!hasPolicy) pavailable = true;

                var groupvses = _.indexBy(item['group-virtual-servers'], function (v) {
                    return v.path;
                });
                vsavailable = _.keys(groupvses).length == 1;

                var errorMsg = '';

                if (!lavailable) {
                    errorMsg = (angular.equals(resource, {}) ? '当前Group是Java应用，请到 <a href="/portal/user/user-trafficpolicy#?env=' : resource.userNormalPolicy.js.msg3) + G.env + (angular.equals(resource, {}) ? '">.NET转Java页面</a>申请流量灰度' : resource.userNormalPolicy.js.msg4);
                } else {
                    if (!(ravailable && savailable && pavailable && vsavailable)) {
                        if (!ravailable) errorMsg = (angular.equals(resource, {}) ? '当前Group找不到相关联的应用，请联系 <a href="mailto:OPS_APP@Ctrip.com?subject=SLB上新建应用迁移策略失败&body=我正在通过SLB新建应用迁移策略。新应用的AppId =' : resource.userNormalPolicy.js.msg5) + appId + (angular.equals(resource, {}) ? ', 系统提示尚未关联我的老应用。:%E2%80%A8%E2%80%A8%E2%80%A8%E2%80%A8">OPS Team</a> 添加关联' : resource.userNormalPolicy.js.msg6);
                        else if (!savailable) errorMsg = (angular.equals(resource, {}) ? '当前Group尚未激活，请联系<a href="mailto:OPS_APP@Ctrip.com?subject=SLB新建应用迁移策略&body=我正在通过SLB新建应用迁移策略,新应用AppId=' : resource.userNormalPolicy.js.msg7) + appId + (angular.equals(resource, {}) ? ',系统提示，当前应用尚未激活%E2%80%A8%E2%80%A8%E2%80%A8%E2%80%A8">OPS Team</a>激活后再创建应用迁移策略测试应用' : resource.userNormalPolicy.js.msg8);
                        else if (!pavailable) errorMsg = (angular.equals(resource, {}) ? '当前Group已经存在 <a href="/portal/policy#?env=' : resource.userNormalPolicy.js.msg9) + $scope.env + '&policyId=' + hasPolicy.id + (angular.equals(resource, {}) ? '">应用迁移</a>，不能再创建了!' : resource.userNormalPolicy.js.msg11);
                        else if (!vsavailable) errorMsg = (angular.equals(resource, {}) ? '当前Group存在多个访问入口，但是多个访问入口之间的Path不相同,请联系<a href="mailto:OPS_APP@Ctrip.com?subject=SLB上新建应用迁移&body=我正在通过SLB新建应用迁移策略。 新应用的AppId=' : resource.userNormalPolicy.js.msg12) + appId + (angular.equals(resource, {}) ? '系统提示:存在多个Virtual Servers，但是多个Virtual Server之间的Path不相同 %E2%80%A8%E2%80%A8%E2%80%A8%E2%80%A8">OPS Team</a> 统一Path' : resource.userNormalPolicy.js.msg13);
                    }
                }

                return {
                    appId: appId,
                    app: app,
                    id: id,
                    name: name,
                    idc: idc,
                    status: status,
                    available: ravailable && savailable && pavailable && vsavailable && lavailable,
                    error: errorMsg,
                    order: (ravailable && savailable && pavailable) ? 0 : 1
                }
            });
            $scope.javaGroups = _.sortBy(data, 'order');
        });
    };

    // Create new NET to Java
    $scope.canMoveNext = function (index) {
        var can = false;
        var j;

        switch (index) {
            case 1: {
                j = $scope.javaGroups;
                var c = _.find(j, function (item) {
                    return item.available == true;
                });
                can = (c != undefined);
                break;
            }
            case 2: {
                j = $scope.netGroups;
                var c = _.find(j, function (item) {
                    return item.available == true;
                });
                can = (c != undefined);
                break;
            }
            default :
                break;
        }
        return can;
    };
    $scope.moveNext = function (target, index) {
        switch (index) {
            case 1: {
                $scope.firstToSecond(target);
                break;
            }
            case 2: {
                $scope.secondToThird(target);
                break;
            }
            case 3: {
                $scope.thirdToForth(target);
                break;
            }
            default:
                break;
        }
    };
    $scope.showStep = function (index) {
        switch (index) {
            case 2: {
                if ($scope.query.appId && $scope.validate) return true;
                return false;
            }
            case 3: {
                if ($scope.validate2) return true;
                return false;
            }
            case 4: {
                if ($scope.validate3) return true;
                return false;
            }
            default :
                break;
        }
    };

    $scope.selectedTileClass = function (g, step, index) {
        if (!g.available) return 'tile-warning';
        if (index == 0 && g.available)
            return 'tile-selected';
    };
    $scope.clickTile = function (item, event, index) {
        var v = index == 1 ? $scope.validate : $scope.validate2;
        if (v == true || item.available != true) return;
        var target = event.target;
        var t = $(target).closest('.group-item');
        var p = t.closest('li');
        $(p).find('.tile-selected').removeClass('tile-selected');

        if ($(target).hasClass('group-item')) {
            $(target).removeClass('tile-selected').addClass('tile-selected');
        } else {
            $(t).removeClass('tile-selected').addClass('tile-selected');
        }
    };

    // Deactivate an existing policy
    $scope.firstshow = false;
    $scope.deactivatepolicyobj = {};
    $scope.selecteIndex = 0;
    $scope.allowToDeactivate = false;
    $scope.confirmDeactivate = false;
    $scope.logs = [];
    $scope.appidNotHasPolicyError = '';

    $scope.dropTrafficPolicy = function () {
        $('#appIdSelectorDeactivate_value').attr('disabled', false);
        $('#appIdSelectorDeactivate_value').val('');
        $scope.firstshow = false;

        $('#deactivateTrafficPolicyDialog').modal('show');
    };
    $scope.selectDeactivateAppId = function (o) {
        if (o) {
            $scope.query.appId = o.originalObject.id;
            $scope.changeAppIdDeactivateSelection();
        }
    };
    $scope.changeAppIdDeactivateSelection = function () {
        var appId = $scope.query.appId;
        var param = {
            type: 'extended',
            appId: appId
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/policies',
            params: param
        };

        $scope.deactivatedpolicies = [];
        var rightUrl = G[$scope.env].urls.api + '/api/auth/apply/mail?userName=' + $scope.query.login + '&op=ACTIVATE,DEACTIVATE,UPDATE,READ,DELETE&type=Policy&env=' + $scope.env;
        $q.all(
            userPolicyApp.getPolicyByAppId(request, appId, $scope.apps, $scope.groups, rightUrl, function (result) {
                $scope.firstshow = true;
                if (result && result.length > 0) {
                    $scope.deactivatedpolicies = result;
                    $scope.appidNotHasPolicyError = '';
                } else {
                    $scope.appidNotHasPolicyError = (angular.equals(resource, {}) ? '当前应用没有关联任何灰度策略!' : resource.userNormalPolicy.js.msg14);
                }
            })
        );

    };
    $scope.showSelectedPolicies = function () {
        return $scope.deactivatedpolicies && $scope.deactivatedpolicies.length > 0;
    };
    $scope.getPolicyTileClass = function (p, index) {
        var selecteIndex = $scope.selecteIndex || 0;
        if (p.available && index == selecteIndex) return 'tile-selected';
        if (!p.available) return 'tile-warning';
    };

    $scope.getPolicyStatusClass = function (status) {
        return userPolicyApp.getStatusClass(status);
    };
    $scope.getPolicyStatusText = function (status) {
        return userPolicyApp.getStatusText(status);
    };
    $scope.clickPolicyTile = function (index, p) {
        if ($scope.hasDecide) return;
        $scope.selecteIndex = index;
    };
    $scope.showPolicyDeactivateFirstToSecond = function () {
        var ds = $scope.deactivatedpolicies;
        var a = _.filter(ds, function (item) {
            return item.available == true;
        });
        var index = $scope.selecteIndex || 0;
        return a && a.length > 0 && ds[index].available;
    };

    $scope.hasDecide = false;
    $scope.deactivatePolicyFirstToSecond = function (event) {
        $scope.hasDecide = true;
        var target = event.target;
        var t = $(target).closest('.btn-info');
        $(t).attr('disabled', true);
        var index = $scope.selecteIndex || 0;
        var policy = $scope.deactivatedpolicies[index];

        $scope.allowToDeactivate = true;
        $scope.reservedgroups = policy.apps;
    };
    $scope.getReservedGroupClass = function (group) {
        if (group.weight == 0) return 'status-red';
        return 'status-green';
    };
    $scope.getReservedGroupText = function (group) {
        if (group.weight == 0) return '下线';
        return '保留';
    };
    $scope.showConfirmDeactivatePolicy = function () {
        return $scope.allowToDeactivate && $scope.firstshow;
    };
    $scope.confirmDeactivatePolicy = function () {
        $scope.logs = [];
        var index = $scope.selecteIndex || 0;
        var policy = $scope.deactivatedpolicies[index];

        var group = _.find($scope.reservedgroups, function (item) {
            return item.relatedappid == undefined;
        });

        var actions = function (success, text, index) {
            $scope.logs.push({
                success: success,
                text: text,
                index: index
            });
        };

        var deactivatePolicyRequest = {
            url: G.baseUrl + '/api/deactivate/policy?policyId=' + policy.id + "&description=" + $scope.query.login,
            method: 'GET'
        };
        var deletePolicyRequest = {
            url: G.baseUrl + '/api/policy/delete?policyId=' + policy.id + "&description=" + $scope.query.deletepolicyreason,
            method: 'GET'
        };

        $q.all(userPolicyApp.deactivatePolicy(deactivatePolicyRequest, function () {
            return userPolicyApp.deletePolicy(deletePolicyRequest, function () {
                var logs = userPolicyApp.addStatusLog($scope.logs, group);
                $scope.logs = logs;
            }, actions);
        }, actions));

    };
    $scope.showLogsDeactivatePolicy = function () {
        return $scope.logs.length > 0 && $scope.firstshow;
    };
    $scope.getLogIcon = function (log) {
        if (log.success) return 'fa fa-check-circle status-green';
        return 'fa fa-times status-red';
    };

    // ----- First Step -----
    $scope.validate = undefined;
    $scope.firstToSecond = function (event) {

        $scope.firstToSecondErrorMessage = '';

        var target = event.target;
        var row = $(target).closest('.row');
        var firstSelected = $('.first-step .tile-selected')[0];
        var firstGroupId = $(firstSelected).attr('tag');
        var firstGroup = $scope.groups[firstGroupId];

        var p = _.find(firstGroup.properties, function (item) {
            return item.name.toLowerCase() == 'relatedappid';
        });
        var p2 = _.find(firstGroup.properties, function (item) {
            return item.name.toLowerCase() == 'idc';
        });

        var firstGroupTargetedApp;
        var firstGroupIdc;
        if (p) firstGroupTargetedApp = p.value;
        if (p2) firstGroupIdc = p2.value;

        var firstGroupRight = A.canDo('Group', 'UPDATE', firstGroupId);
        var firstGroupAppId = firstGroup['app-id'];

        var firstVses = _.map(firstGroup['group-virtual-servers'], function (item) {
            return {vs: item['virtual-server'].id, path: item.path ? item.path.toLowerCase() : ""}
        });
        firstVses = _.object(_.map(firstVses, _.values));

        var param = {
            type: 'extended',
            appId: firstGroupTargetedApp
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/groups',
            params: param
        };

        $scope.firstToSecondErrorMessage;
        $http(request).success(function (response, code) {
            var cgroups = _.filter(response['groups'], function (item) {
                return item.id != firstGroupId;
            });
            if (cgroups.length == 0) {
                $scope.firstToSecondErrorMessage = (angular.equals(resource, {}) ? 'A应用所关联的B应用AppId: ' : resource.userNormalPolicy.js.msg15) + firstGroupTargetedApp + (angular.equals(resource, {}) ? '. 当前找不到该关联的应用。　请联系 SLB Team获取帮助' : resource.userNormalPolicy.js.msg17);
                $scope.validate = false;
                return;
            }
            $scope.validate = true;
            var type = 'Group';
            var userName = $scope.query.userId;
            var op = 'UPDATE';
            var mailLink = G[$scope.env].urls.api + '/api/auth/apply/mail?userName=' + $scope.query.login + '&op=' + op + '&' + 'type=' + type + '&env=' + $scope.env;

            var data = _.map(cgroups, function (item) {
                var appId = item['app-id'];
                var appName = $scope.apps[appId] ? $scope.apps[appId]['chinese-name'] : '-';
                var app = appId + '(' + appName + ')';
                var id = item.id;
                var name = item.name;

                var rightavailable = false;
                var statusavailable = false;
                var policyavailable = false;
                var vsavailable = false;

                rightavailable = A.canDo('Group', 'UPDATE', id);

                var idcItem = _.find(item.properties, function (v) {
                    return v.name == 'idc';
                });
                var idc = idcItem ? idcItem.value : '-';

                // status=activated
                var statusItem = _.find(item.properties, function (v) {
                    return v.name == 'status' && v.value.toLowerCase() == 'activated';
                });
                if (statusItem) statusavailable = true;

                // has policy related to current group
                var hasPolicy = _.find($scope.existingPolicies, function (b) {
                    var gs = _.map(b.controls, function (d) {
                        return d.group.id;
                    });
                    if (gs.indexOf(id) != -1) return true;
                    return false;
                });
                if (!hasPolicy) policyavailable = true;

                var secondVses = _.map(item['group-virtual-servers'], function (t) {
                    return {vs: t['virtual-server'].id, path: t.path ? t.path.toLowerCase() : ""}
                });
                secondVses = _.object(_.map(secondVses, _.values));
                var kd = _.intersection(_.keys(firstVses), _.keys(secondVses));
                if (kd.length != 0) {
                    // same vs's path are same
                    for (var m = 0; m < kd.length; m++) {
                        if (firstVses[kd[m]] == secondVses[kd[m]]) {
                            vsavailable = true;
                        }
                    }
                }

                var errorMsg = '';
                var isAccessLink = false;
                if (!(firstGroupRight && rightavailable && statusavailable && policyavailable && vsavailable)) {
                    if (!(rightavailable && firstGroupRight)) {
                        isAccessLink = true;
                        errorMsg = (angular.equals(resource, {}) ? '您没有操作当前应用的权限，<a class="fast-apply">点此快速申请权限</a>' : resource.userNormalPolicy.js.msg18);
                    }
                    else if (!statusavailable) errorMsg = (angular.equals(resource, {}) ? '当前Group尚未激活，请联系<a href="mailto:OPS_APP@Ctrip.com?subject=SLB上迁移应用&body=请添加咨询问题列表:%E2%80%A8%E2%80%A8%E2%80%A8%E2%80%A8">OPS Team</a>激活后再创建应用迁移策略' : resource.userNormalPolicy.js.msg19);
                    else if (!policyavailable) errorMsg = (angular.equals(resource, {}) ? '当前Group已经创建过 <a href="/portal/policy#?env=' + $scope.env + '&policyId=' + hasPolicy.id + '">应用迁移策略</a>，不能再创建了!' : resource.userNormalPolicy.js.msg21);
                    else if (!vsavailable) errorMsg = (angular.equals(resource, {}) ? '当前Group对应的Virtual Server信息与Java Group不一致，<a href="mailto:OPS_APP@Ctrip.com?subject=SLB上应用迁移策略&body=请添加咨询问题列表:%E2%80%A8%E2%80%A8%E2%80%A8%E2%80%A8">OPS Team</a> 统一Path' : resource.userNormalPolicy.js.msg22);
                }
                return {
                    appId: appId,
                    app: app,
                    id: id,
                    name: name,
                    idc: idc,
                    status: status,
                    accessLink: mailLink,
                    netGroup: firstGroupId,
                    isAccessLink: isAccessLink,
                    available: firstGroupRight && rightavailable && statusavailable && policyavailable && vsavailable,
                    error: errorMsg,
                    order: (firstGroupRight && rightavailable && statusavailable && policyavailable) ? 0 : 1
                }
            });

            $scope.netGroups = _.sortBy(data, 'order');
            if (data.length > 0)
                $('.first-step-bt').attr('disabled', true);
        });
    };
    $scope.applyRightClick = function (event, right) {
        var target = event.currentTarget;
        var nearestTarget = $(target).parents('.java-group-class')[0];
        var netGroupId = right.netGroup;
        var javaGroupId = $(nearestTarget).find('.tile-warning').attr('tag');
        var groupIdsQuery = '';
        if (netGroupId && javaGroupId) {
            groupIdsQuery = '&targetId=' + netGroupId + '&targetId=' + javaGroupId;
        }
        var link = $(target).find('.fast-apply');
        var d = $(link).attr('disabled');
        if (d == 'disabled') return;
        if (right && right.isAccessLink && right.accessLink) {
            var request = {
                method: 'GET',
                url: right.accessLink + groupIdsQuery
            };
            $http(request).success(function (response, code) {
                if (code == 200) {
                    alert((angular.equals(resource, {}) ? '已成功申请权限，请等待管理员审批！' : resource.userNormalPolicy.js.msg23));
                    $(link).attr('disabled', true);
                } else {
                    alert('Failed to send apply mail with Error message: ' + response.message);
                }
            });
        }
    };
    // ----- Second Step -----
    $scope.validate2 = undefined;
    $scope.bothGroups = {};
    $scope.secondToThird = function (event) {
        var target = event.target;
        var secondSelected = $('.second-step .tile-selected')[0];
        var secondGroupId = $(secondSelected).attr('tag');
        var secondGroup = $scope.groups[secondGroupId];

        secondGroup.type = 'B';
        secondGroup.desc = secondGroup['app-id'] + (angular.equals(resource, {}) ? '(老)' : resource.userNormalPolicy.js.msg54);
        secondGroup.appId = secondGroup['app-id'];
        secondGroup.app = $scope.apps[secondGroup['app-id']] ? $scope.apps[secondGroup['app-id']]['chinese-name'] : '-';
        secondGroup.idc = _.find(secondGroup['properties'], function (i) {
            return i.name == 'idc';
        }).value;
        secondGroup.percent = 100;


        var firstSelected = $('.first-step .tile-selected')[0];
        var firstGroupId = $(firstSelected).attr('tag');
        var firstGroup = $scope.groups[firstGroupId];
        firstGroup.appId = firstGroup['app-id'];
        firstGroup.app = $scope.apps[firstGroup['app-id']] ? $scope.apps[firstGroup['app-id']]['chinese-name'] : '-';
        firstGroup.type = 'A';
        firstGroup.desc = firstGroup['app-id'] + (angular.equals(resource, {}) ? '(新)' : resource.userNormalPolicy.js.msg53);
        firstGroup.percent = 0;

        firstGroup.idc = _.find(firstGroup['properties'], function (i) {
            return i.name == 'idc';
        }).value;
        $scope.bothGroups = [firstGroup, secondGroup];

        $scope.policyGroupsObj.java = firstGroup.id;
        $scope.policyGroupsObj.net = firstGroup.id;

        $scope.validate2 = true;
        $('.second-step-bt').attr('disabled', true);
        $(target).attr('disabled', true);
    };
    $scope.getTypeCss = function (type) {
        switch (type) {
            case 'A':
                return 'status-red ion-flask';
                break;
            case 'B':
                return 'status-green ion-flask ';
                break;
            default :
                return 'status-green ion-flask';
                break;
        }
    };
    $scope.getDescByType = function (type) {
        switch (type) {
            case 'A':
                return (angular.equals(resource, {}) ? '(新应用默认权重0%，不接受任何流量，创建完成后可调整)' : resource.userNormalPolicy.js.msg24);
                break;
            case 'B':
                return (angular.equals(resource, {}) ? '(老应用默认权重100%，暂时接收所有流量)' : resource.userNormalPolicy.js.msg25);
                break;
            default :
                break;
        }
    };

    // ----- Third Step -----
    $scope.validate3 = undefined;

    $scope.policyGroupsObj = {};
    $scope.thirdToForth = function (event) {
        var target = event.target;
        $(target).attr('disabled', true);

        var controls = _.map($scope.bothGroups, function (item) {
            return {
                group: {
                    id: item.id
                },
                weight: item.percent
            }
        });

        var existingGroupVses = {};

        $.each(controls, function (i, item) {
            existingGroupVses[item.group.id] = $scope.abstractGroupVsPaths($scope.groups[item.group.id])
        });
        var t = $scope.getGroupSameAndDiffrentVsPaths(existingGroupVses);
        var sames = t.same;
        var vses = _.map(sames, function (item) {
            return {
                path: item.path,
                'virtual-server': {
                    id: item.vsId
                },
                priority: item.priority
            }
        });


        var idcItem = _.find($scope.groups[controls[0].group.id].properties, function (i) {
            return i.name == 'idc';
        });
        var idc = '-';
        if (idcItem) idc = idcItem.value;
        var time = new Date();
        var newPolicyName = 'traffic_transform_between_app_' + _.pluck($scope.bothGroups, 'app-id').join('-') + '_' + idc + time.getTime();
        $scope.policyName = newPolicyName;

        var policy = {
            controls: controls,
            'policy-virtual-servers': vses,
            name: newPolicyName,
            properties: [],
            tags: []
        };

        var targetStr = '应用迁移';
        var groups = _.map(policy.controls, function (v) {
            return v.group.id;
        });
        var ts = _.countBy(groups, function (item) {
            var group = $scope.groups[item];
            var ps = group['tags'];
            if (ps && ps.indexOf('soa') != -1) {
                return 'soa';
            } else {
                return 'site';
            }
        });
        var isSoa = false;
        if (ts['soa'] && ts['soa'] == groups.length) isSoa = true;

        var psandtags = $scope.getTagsAndPolicyForPolicy(targetStr, controls, vses, isSoa);

        policy.tags = psandtags.tags;
        policy.properties = psandtags.properties;


        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/policy/new?description=' + $scope.query.newreason,
            data: policy
        };
        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在创建" : resource.userNormalPolicy.js.msg27);
        $('#operationConfrimModel').find(".modal-body").html(loading);
        $scope.do(request,
            function () {
                $('#operationConfrimModel').modal('show')
            },
            function (policy) {
                $('#operationConfrimModel').modal('hide');
                $scope.validate3 = true;
                $scope.postCreatePolicy(policy);
            },
            function (message) {
                $('#operationConfrimModel').modal('hide');
                swal({
                    title: (angular.equals(resource, {}) ? "创建失败，是否强制创建?" : resource.userNormalPolicy.js.msg28),
                    text: (angular.equals(resource, {}) ? "创建失败，错误信息：" : resource.userNormalPolicy.js.msg29) + message,
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: (angular.equals(resource, {}) ? "强制创建!" : resource.userNormalPolicy.js.msg31),
                    closeOnConfirm: true
                }, function (isConfirm) {
                    if (isConfirm) {
                        $scope.forceDo(request, function () {
                            $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
                        }, function (policy) {
                            $('#operationConfrimModel').modal('hide');
                            $scope.postCreatePolicy(policy);
                            $scope.validate3 = true;
                            var p = $.extend({}, true, policy);
                            p.properties = [
                                {
                                    name: 'status',
                                    value: 'deactivated'
                                }
                            ];
                            var canvas = $('#diagram-create');
                            var c = {};
                            c[policy.id] = policy;
                            // show the diagram
                            $scope.reloadData(canvas, c, true);
                        }, function (message) {
                            swal({
                                title: (angular.equals(resource, {}) ? "强制操作仍然失败!" : resource.userNormalPolicy.js.msg32),
                                text: (angular.equals(resource, {}) ? "强制操作失败。错误信息：" : resource.userNormalPolicy.js.msg33) + message + (angular.equals(resource, {}) ? ',请联系SLB Team寻求帮助！' : resource.userNormalPolicy.js.msg34),
                                imageUrl: "/static/img/fail.png"
                            });
                            $('#operationConfrimModel').modal('hide');
                        });
                    }
                });
            });
    };

    // Params:
    // Soapath: SOA 返回的URL
    // Slbpath: SLB 的Path
    // Ops: SOA的OP列表
    $scope.approveCreateValue = false;
    $scope.thirdStepNextDisabled = function () {
        return !$scope.approveCreateValue;
    };

    // Start create
    $scope.postCreatePolicy = function (policy) {
        var groups = _.map(policy.controls, function (v) {
            return v.group.id;
        });
        var pvalue = '应用迁移';
        $scope.addTagsForGroups(groups, pvalue);
    };
    $scope.addTagsForGroups = function (groups, target) {
        $.each(groups, function (index, group) {
            var param = {
                type: 'group',
                pname: target,
                pvalue: 'true',
                targetId: group
            };
            var request = {
                method: 'GET',
                url: G.baseUrl + "/api/property/set",
                params: param
            };

            $http(request).success(function (code, response) {

            });
        });
    };
    $scope.addPropertyForGroups = function (pname, pvalue, target) {
        var param = {
            type: 'policy',
            pname: pname,
            pvalue: pvalue,
            targetId: target
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/property/set",
            params: param
        };
        $http(request).success(function (code, response) {
        });
    };
    $scope.getTagsAndPolicyForPolicy = function (target, controls, vses, issoa) {
        // summarize the tags and properties
        var pname = 'target';
        var properties = [];
        var tags = [];

        properties.push({
            name: pname,
            value: target
        });

        var idcs = _.map(vses, function (r) {
            var vsId = r['virtual-server'].id;
            var vs = $scope.vses[vsId];
            var u = _.find(vs.properties, function (t) {
                return t.name.toLowerCase() == 'idc';
            });
            if (u) return u.value;
            return '-';
        });
        idcs = _.uniq(idcs);

        idcs = idcs.join(',');
        properties.push({
            name: 'idc',
            value: idcs
        });

        var v = _.map(controls, function (item) {
            var id = item.group.id;
            var owner;
            var bu;

            var o = _.find($scope.groups[id].tags, function (y) {
                return y.toLowerCase().indexOf('owner_') != -1;
            });
            if (o) owner = o.substring(6, o.length);
            else owner = '-';

            var p = _.find($scope.groups[id].properties, function (z) {
                return z.name.toLowerCase() == 'sbu';
            });
            if (p) bu = p.value;
            else bu = '-';

            return {
                group: id,
                owner: owner,
                bu: bu
            }
        });
        var users = [];
        var bus = [];
        if (v && v.length != 0) {
            users = _.uniq(_.pluck(v, 'owner'));
            bus = _.uniq(_.pluck(v, 'bu'));
        }
        $.each(users, function (i, item) {
            if (item == '-') return;
            tags.push('owner_' + item);
        });

        $.each(bus, function (i, item) {
            if (item == '-') return;
            properties.push({
                name: 'SBU',
                value: item
            });
        });

        tags.push('user_' + $scope.loginUserInfo.user);

        if (issoa) {
            tags.push('soa');
        }


        return {
            tags: tags,
            properties: properties
        }
    };

    // ------Activate Traffic Policy -----
    $scope.isActivating = false;
    $scope.activatePolicy = function (pid) {

        var id;
        if (!pid) id = $scope.policyId;
        else {
            id = parseInt(pid);
            $scope.policyId = id;
        }
        $scope.getPolicyById(id, function () {
            var target = document.getElementById('fileDiffForm');
            var NewText = JSON.stringify(U.sortObjectFileds($scope.onlinePolicyData), null, "\t");
            var baseText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedPolicyData), null, "\t");
            var ptext = (angular.equals(resource, {}) ? '线下版本' : resource.userNormalPolicy.js.msg36) + ($scope.tobeActivatedPolicyData.version ? $scope.tobeActivatedPolicyData.version : '-');
            var ctext = (angular.equals(resource, {}) ? "线上版本" : resource.userNormalPolicy.js.msg37) + ($scope.onlinePolicyData.version ? $scope.onlinePolicyData.version : '-');

            $('#newTrafficPolicyDialog').modal('hide');
            $('#weightAdjustDialog').modal('hide');

            $scope.isActivating = true;
            $('#activateVSModal').modal('show');
            diffTwoSlb(target, baseText, NewText, ptext, ctext);
        });
    };
    $scope.tobeDeactivatedGroup;
    $scope.deactivateGroupByPolicyId = function (pid) {
        var policies = _.extend($scope.focusedTraffics, $scope.ownedTraffics);
        var p = policies[pid];
        if (p) {
            var netGroup = _.find(p.controls, function (item) {
                var a = $scope.groups[item.group.id];
                var ps = _.indexBy(a.properties, function (v) {
                    return v.name;
                });
                if (ps['language']) {
                    return ps['language'].value == '.net';
                } else return false;
            });
            if (netGroup) {
                var netGroupId = netGroup.group.id;
                var netGroup = $scope.groups[netGroupId];
                var netGroupPs = _.indexBy(netGroup.properties, function (item) {
                    return item.name;
                });
                var cnetGroup = {
                    id: netGroup.id,
                    name: netGroup.name,
                    idc: netGroupPs['idc'] ? netGroupPs['idc'].value : '-',
                    appId: netGroup['app-id'],
                    weight: '0',
                    app: $scope.apps[netGroup['app-id']] ? $scope.apps[netGroup['app-id']]['chinese-name'] : '-'
                };

                var h = $scope.getAppHtml('NET', cnetGroup);
                $('.deactivated-group').html(h);
                $scope.tobeDeactivatedGroup = netGroup.id;
            }

            $('#confirmDeactivateNetAppModel').modal('show');
        }
    };
    $scope.confirmDeactivateNetApp = function () {
        if (!$scope.tobeDeactivatedGroup) return;
        var id = $scope.tobeDeactivatedGroup;
        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在下线.NET应用" : resource.userNormalPolicy.js.msg38);
        $('#operationConfrimModel').find(".modal-body").html(loading);
        var success = (angular.equals(resource, {}) ? '<i class="status-green fa fa-check" /> 下线成功,将于2s后自动关闭' : resource.userNormalPolicy.js.msg39);

        var param = {
            groupId: id
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/deactivate/group',
            params: param
        };
        $scope.do(request, function () {
            $('#operationConfrimModel').modal('show');
        }, function (s) {
            $('#operationConfrimModel').find(".modal-body").html(success);
            setTimeout(function () {
                $('#operationConfrimModel').modal('hide');
                $scope.resetView();
            }, 2000);
        }, function (message) {
            $('#operationConfrimModel').modal('hide');
            swal({
                title: (angular.equals(resource, {}) ? "下线失败，是否强制下线?" : resource.userNormalPolicy.js.msg41),
                text: (angular.equals(resource, {}) ? "下线失败，错误信息：" : resource.userNormalPolicy.js.msg42) + message,
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: (angular.equals(resource, {}) ? "强制下线!" : resource.userNormalPolicy.js.msg43),
                closeOnConfirm: true
            }, function (isConfirm) {
                if (isConfirm) {
                    $scope.forceDo(request, function () {
                        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
                    }, function () {
                        $('#operationConfrimModel').find(".modal-body").html(success);
                        setTimeout(function () {
                            $('#operationConfrimModel').modal("hide");
                            $scope.resetView();
                        }, 2000);
                    }, function (message) {
                        swal({
                            title: (angular.equals(resource, {}) ? "强制操作仍然失败!" : resource.userNormalPolicy.js.msg32),
                            text: (angular.equals(resource, {}) ? "强制操作失败。错误信息：" : resource.userNormalPolicy.js.msg33) + message + (angular.equals(resource, {}) ? ',请联系SLB Team寻求帮助！' : resource.userNormalPolicy.js.msg34),
                            imageUrl: "/static/img/fail.png"
                        });
                        $('#operationConfrimModel').modal('hide');
                    });
                }
            });
        });
    };
    $scope.getPolicyById = function (id, successFunc) {
        $q.all(
            [
                $http.get(G.baseUrl + "/api/policy?policyId=" + id).success(
                    function (res) {
                        $scope.tobeActivatedPolicyData = res;
                    }
                ),
                $http.get(G.baseUrl + "/api/policy?policyId=" + id + "&mode=online").then(
                    function (res) {
                        if (res.status == 200 || res.status == 202) {
                            $scope.onlinePolicyData = res.data;
                        } else {
                            $scope.onlinePolicyData = "No online version!!!";
                        }
                    }
                )
            ]
        ).then(
            function () {
                successFunc();
            }
        )
    };
    $scope.confirmActivatePolicy = function () {
        var can = A.canDo('Policy', 'ACTIVATE', $scope.policyId);
        if (!can) return;

        $('#activateVSModal').modal('hide');
        var policyId = $scope.policyId;

        var param = {
            policyId: policyId,
            description: $scope.query.login
        };
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/activate/policy',
            params: param
        };

        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在激活" : resource.userNormalPolicy.js.msg44);
        var success = (angular.equals(resource, {}) ? '<i class="status-green fa fa-check" /> 激活成功,将于2s后自动关闭' : resource.userNormalPolicy.js.msg45);
        $('#operationConfrimModel').find(".modal-body").html(loading);

        // Params 1: request
        // 2: before
        // 3: Success
        // 4: failed
        $scope.do(request,
            function () {
                $('#operationConfrimModel').modal('show')
            },
            function () {
                $('#operationConfrimModel').find(".modal-body").html(success);
                setTimeout(function () {
                    $('#operationConfrimModel').modal('hide');
                    $scope.resetView();
                }, 2000);
            },
            function (message) {
                $('#operationConfrimModel').modal('hide');
                swal({
                    title: (angular.equals(resource, {}) ? "激活失败，是否强制激活?" : resource.userNormalPolicy.js.msg46),
                    text: (angular.equals(resource, {}) ? "激活失败，错误信息：" : resource.userNormalPolicy.js.msg47) + message,
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: (angular.equals(resource, {}) ? "强制激活!" : resource.userNormalPolicy.js.msg48),
                    closeOnConfirm: true
                }, function (isConfirm) {
                    if (isConfirm) {
                        $scope.forceDo(request, function () {
                            $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);
                        }, function () {
                            $('#operationConfrimModel').find(".modal-body").html(success);
                            setTimeout(function () {
                                $('#operationConfrimModel').modal("hide");
                                $scope.resetView();
                            }, 2000);
                        }, function (message) {
                            swal({
                                title: (angular.equals(resource, {}) ? "强制操作仍然失败!" : resource.userNormalPolicy.js.msg32),
                                text: (angular.equals(resource, {}) ? "强制操作失败。错误信息：" : resource.userNormalPolicy.js.msg33) + message + (angular.equals(resource, {}) ? ',请联系SLB Team寻求帮助！' : resource.userNormalPolicy.js.msg34),
                                imageUrl: "/static/img/fail.png"
                            });
                            $('#operationConfrimModel').modal('hide');
                        });
                    }
                });
            });
    };

    $scope.canUserActivatePolicy = function () {
        var policyId = $scope.policyId;
        return !A.canDo('Policy', 'ACTIVATE', policyId);
    };

    $scope.getActivatePolicyTitle = function () {
        var policyId = $scope.policyId;
        var can = A.canDo('Policy', 'ACTIVATE', policyId);
        if (!can) {
            return (angular.equals(resource, {}) ? '没有权限' : resource.userNormalPolicy.js.msg49);
        } else {
            return '';
        }
    };
    $scope.leftGroupId;
    $scope.rightGroupId;
    $scope.selectedPolicy;
    // --------- Adjust the traffic policy weight
    $scope.leftAppModel;
    $scope.rightAppModel;
    $scope.changePolicyWeight = function (id) {
        var resource = $scope.resource;
        $scope.$apply(function () {
            $scope.policyId = id;
        });

        $('.origin-footer').show();
        var c = $scope.existingPolicies;
        var p = c[id];
        if (!p) alert('Could not find the traffic policy');
        else {
            $scope.selectedPolicy = p;
            var controls = _.sortBy(p.controls, 'weight');
            controls = _.map(controls, function (item) {
                var group = $scope.groups[item.group.id];
                var groupproperties = _.indexBy(group.properties, function (v) {
                    return v.name.toLowerCase();
                });
                return {
                    id: group.id,
                    name: group.name,
                    weight: item.weight,
                    appId: group['app-id'],
                    properties: groupproperties
                }
            });

            var left = _.find(controls, function (w) {
                return w.properties['relatedappid'] != undefined;
            });
            var right = _.find(controls, function (w) {
                return w.properties['relatedappid'] == undefined;
            });

            if (!left) {
                alert((angular.equals(resource, {}) ? '当前Policy中不存在 relatedappid映射，请联系 OPS Team获取帮助！' : resource.userNormalPolicy.js.msg51));
                return;
            }


            var leftId = left.id;
            var rightId = right.id;
            $scope.leftGroupId = leftId;
            $scope.rightGroupId = rightId;

            var path = p['policy-virtual-servers'][0].path;

            var leftGroupIdc = left.properties['idc'];
            var rightGroupIdc = right.properties['idc'];

            var leftAppId = left['appId'];
            var leftGroupName = left.name;
            var leftAppName = leftAppId + '/' + ($scope.apps[leftAppId] ? $scope.apps[leftAppId]['chinese-name'] : '-');
            leftGroupIdc = leftGroupIdc ? leftGroupIdc.value : '-';

            var rightGroupName = right.name;
            var rightAppId = right['appId'];
            var rightAppName = rightAppId + '/' + ($scope.apps[rightAppId] ? $scope.apps[rightAppId]['chinese-name'] : '-');
            rightGroupIdc = rightGroupIdc ? rightGroupIdc.value : '-';

            var l = $scope.getAppHtml('Java', {
                id: leftId,
                name: leftGroupName,
                appId: leftAppId,
                app: leftAppName,
                idc: leftGroupIdc,
                weight: left.weight
            });
            var r = $scope.getAppHtml('NET', {
                id: rightId,
                name: rightGroupName,
                appId: rightAppId,
                app: rightAppName,
                idc: rightGroupIdc,
                weight: right.weight
            });
            $('.left-app').html(l);
            $('.right-app').html(r);


            $scope.leftAppModel = {
                id: leftId,
                name: leftGroupName,
                appId: leftAppId,
                app: leftAppName,
                idc: leftGroupIdc,
                weight: left.weight
            };
            $scope.rightAppModel = {
                id: rightId,
                name: rightGroupName,
                appId: rightAppId,
                app: rightAppName,
                idc: rightGroupIdc,
                weight: right.weight
            };

            $('.result-left').html(left.weight + '%');
            $('.result-right').html((100 - left.weight) + '%');
            var slider = $("#range_02").data("ionRangeSlider");
            if (!slider) {
                $("#range_02").ionRangeSlider({
                    min: 0,
                    max: 100,
                    from: left.weight,
                    onChange: function (v) {
                        $('.Java').html(v.from + '%');
                        $('.NET').html((100 - v.from) + '%');
                        $scope.leftAppModel.weight = v.from;
                        $scope.rightAppModel.weight = 100 - v.from;
                    }
                });
            } else {
                slider.update({
                    from: left.weight
                });
            }

            $('#weightAdjustDialog .operation-result').hide();
            $('#weightAdjustDialog').modal('show');
        }
    };

    $scope.canUserChangePolicy = function () {
        if (!$scope.policyId) {
            return false;
        }

        var id = $scope.policyId;
        var can = A.canDo('Policy', 'UPDATE', id);
        return !can;
    };
    $scope.getUserChangePolicyTitle = function () {
        var id = $scope.policyId;
        var can = A.canDo('Policy', 'UPDATE', id);
        if (!can) {
            return (angular.equals(resource, {}) ? '您还没有更新当前策略的权限' : resource.userNormalPolicy.js.msg52);
        }
    };
    $scope.getAppHtml = function (language, group) {
        var resource = $scope.resource;
        var languageCss = (language == 'Java' ? 'ion-flask status-red' : 'ion-flask status-green');
        var newlanguage = language == 'Java' ? group.appId + (angular.equals(resource, {}) ? '(新)' : resource.userNormalPolicy.js.msg53) : group.appId + (angular.equals(resource, {}) ? '(老)' : resource.userNormalPolicy.js.msg54);
        if (group)
            var result = '<div class="panel group-item">' +
                '<div class="panel-body" style="padding-top: 0;"> ' +
                '<div class="media-main"> ' +
                '<div class="form form-group">  ' +
                '<div><div class="pull-left"><span class="app-type"> ' + newlanguage + ' <i class="' + languageCss + '"></i></span></div><div class="pull-right"><p class="status-red ' + language + '">' + group.weight + '%</p></div> ' +
                '</div> ' +
                '<div class="form form-group"> ' +
                '<div><span class="text-black">Group:</span><a href="/portal/group#?env=' + $scope.env + '&groupId=' + group.id + '" target="_blank">' + group.name + '</a> ' +
                '</div> ' +
                '<p class="text-black">App: <a href="/portal/app#?env=' + $scope.env + '&appId=' + group.appId + '" target="_blank">' + group.app + '</a> ' +
                '</p> ';

        result += '</div> ';

        result += '<p class="text-black">IDC: ' + group.idc + '</p>' +
            '</div> ' +
            '</div> ' +
            '<div class="clearfix"></div> ' +
            '</div> ' +
            '</div>';

        return result;
    };
    $scope.getPolicyAuthLink = function (target) {
        var t = target.currentTarget;
        if ($(t).is("a[disabled]")) {
            alert((angular.equals(resource, {}) ? '您已经申请过权限了，请等待SLB Team批准。' : resource.userNormalPolicy.js.msg55));
            return;
        }


        var p = $scope.policyId;
        var url = G[$scope.env].urls.api + '/api/auth/apply/mail?userName=' + $scope.query.login + '&targetId=' + p + '&op=ACTIVATE,DEACTIVATE,UPDATE,READ&type=Policy&env=' + $scope.env;

        var request = {
            method: 'GET',
            url: url
        };
        $http(request).success(
            function (response, code) {
                alert((angular.equals(resource, {}) ? '权限申请已经发出，请等待SLB Team审批通过。' : resource.userNormalPolicy.js.msg56));
                $(t).attr('disabled', 'disabled');
            }
        );
    };
    $scope.showAbTestTooltop = function () {
        var percentBool = false;
        var opsBool = false;

        var l = $scope.leftGroupId;
        var r = $scope.rightGroupId;

        var p = $scope.selectedPolicy;
        if (!p) return;
        var both = _.indexBy(p.controls, function (item) {
            return item.group.id;
        });
        if (both[l].weight == 0 && both[r].weight == 100) percentBool = true;

        if ($scope.originupdateops && $scope.selectedupdateops) {
            if ($scope.originupdateops.join('|') == $scope.selectedupdateops.join('|')) {

            } else {
                opsBool = true;
            }
            ;
        }

        return percentBool || opsBool;
    };

    $scope.canUserSaveWeight = function () {
        var can = false;
        var read = $scope.read;
        var show = $scope.showAbTestTooltop();
        if (show) {
            if (read) can = false;
            else can = true;
        } else {
            can = false;
        }
        return can;
    };
    $scope.policyChanged = false;
    $scope.initWeight = function () {
        $scope.validate = undefined;
        $scope.validate2 = undefined;
        $scope.validate3 = undefined;
        $scope.isActivating = false;
        $scope.firstToSecondErrorMessage = '';
        $scope.read = false;

        $scope.firstshow = false;
        $scope.deactivatepolicyobj = {};
        $scope.selecteIndex = 0;
        $scope.allowToDeactivate = false;
        $scope.confirmDeactivate = false;
    };
    $scope.saveWeight = function (event) {
        $scope.policyChanged = true;
        var can = A.canDo('Policy', 'UPDATE', $scope.policyId);
        if (!can) return;
        var target = event.target;
        var row = $(target).closest('.modal-footer');

        delete $scope.selectedPolicy.properties;
        var p = $scope.selectedPolicy;
        var left = $scope.leftAppModel;
        var right = $scope.rightAppModel;

        $(row).hide();
        var l = _.find(p.controls, function (item) {
            return item.group.id == left.id;
        });
        var r = _.find(p.controls, function (item) {
            return item.group.id == right.id;
        });
        l.weight = left.weight;
        r.weight = right.weight;

        var request = {
            method: 'POST',
            url: G.baseUrl + '/api/policy/update?description=' + $scope.query.updatereason,
            data: p
        };
        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在更新" : resource.userNormalPolicy.js.msg57);
        $('#operationConfrimModel').find(".modal-body").html(loading);
        $('#operationConfrimModel').find(".modal-title").html((angular.equals(resource, {}) ? "正在更新权重" : resource.userNormalPolicy.js.msg58));

        $scope.do(request, function () {
                $('#operationConfrimModel').modal('show');
            },
            function (policy) {
                $('#operationConfrimModel').modal('hide');
                $('.operation-result').slideDown();
            }, function (message) {
                $('#operationConfrimModel').modal('hide');
                swal({
                    title: (angular.equals(resource, {}) ? "更新失败，是否强制更新?" : resource.userNormalPolicy.js.msg59),
                    text: (angular.equals(resource, {}) ? "更新失败，错误信息：" : resource.userNormalPolicy.js.msg61) + message,
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "强制更新!",
                    closeOnConfirm: true
                }, function (isConfirm) {
                    if (isConfirm) {
                        $scope.forceDo(request, function () {
                            $('#operationConfrimModel').modal('show');
                        }, function () {
                            $('#operationConfrimModel').modal('hide');
                            $('.operation-result').slideDown();
                        }, function (message) {
                            swal({
                                title: (angular.equals(resource, {}) ? "强制操作仍然失败!" : resource.userNormalPolicy.js.msg32),
                                text: (angular.equals(resource, {}) ? "强制操作失败。错误信息：" : resource.userNormalPolicy.js.msg33) + message + (angular.equals(resource, {}) ? ',请联系SLB Team寻求帮助！' : resource.userNormalPolicy.js.msg34),
                                imageUrl: "/static/img/fail.png"
                            });
                            $('#operationConfrimModel').modal('hide');
                        });
                    }
                });
            });
    };
    $('#weightAdjustDialog, #newTrafficPolicyDialog').on('hidden.bs.modal', function (a) {
        $('.first-step-bt, .second-step-bt, .third-step-bt, .fast-apply').attr('disabled', false);
        $.each($('#newTrafficPolicyDialog').find('input[type="checkbox"]'), function (i, item) {
            $(item).attr('checked', false);
        });
        $.each($('#newTrafficPolicyDialog').find('.create-ops-label'), function (i, item) {
            $(item).removeClass('system-color');
        });

        $scope.initWeight();
        if (a.target.id.toLowerCase() == 'weightadjustdialog' && !$scope.policyChanged) {
            return;
        }
        $scope.resetView();
    });
    $('#newTrafficPolicyDialog').on('show.bs.modal', function () {
        $scope.approveCreateValue = false;
        $scope.showAdvanceRow = false;
        $scope.javaGroups = [];
        $scope.netGroups = [];
    });

    $scope.abstractGroupVsPaths = function (group) {
        var groupvses = group['group-virtual-servers'];
        var vsPaths = _.map(groupvses, function (item) {
            var vsId = item['virtual-server'].id;
            return {
                'vsId': vsId,
                'path': item.path,
                priority: item['priority']
            }
        });
        return vsPaths;
    };
    $scope.getGroupSameAndDiffrentVsPaths = function (all) {
        var result = {
            same: [],
            diffs: []
        };
        var keys = _.keys(all);
        var values = [];
        $.each(keys, function (i, key) {
            values = values.concat(all[key]);
        });
        var len = keys.length;

        if (len == 1) {
            $.each(values, function (i, item) {
                item.priority = item.priority + 1;
            });
            result.same = values;
        } else {
            var countByItems = _.countBy(values, function (item) {
                return item.vsId;
            });

            var vsIds = _.map(countByItems, function (num, key) {
                if (num == len) return key;
            });

            vsIds = _.reject(vsIds, function (item) {
                return item == undefined;
            });

            $.each(vsIds, function (i, item) {
                var v = _.filter(values, function (t) {
                    return t.vsId == item;
                });
                result.same.push(
                    {
                        vsId: item,
                        path: _.pluck(v, 'path')[0],
                        available: true,
                        priority: $scope.abstractVsPriority(v)
                    }
                );
            });
        }
        return result;
    };
    $scope.abstractVsPriority = function (array) {
        var result = _.max(_.pluck(array, 'priority')) + 1;
        return result;
    };
    $scope.getGroupPath = function (group) {
        var path = group['group-virtual-servers'][0].path;

        if (path == '/' || path == '~* ^/') path = '/';

        var start = 0;
        var end = path.length - 1;

        if (path.endsWith('($|/|\\?)')) end = path.length - 8;

        return path.substring(start, end);
    };
    $scope.do = function (request, beforeCallBack, successCallBack, failedCallBack) {
        $scope.processRequest(request, function () {
            beforeCallBack();
        }, function (policy) {
            successCallBack(policy);
        }, function (message) {
            failedCallBack(message);
        });
    };
    $scope.forceDo = function (request, beforeCallBack, successCallBack, failedCallBack) {
        if(request.url.indexOf('?')>0){
            request.url += '&force=true';
        }else{
            request.url += '?force=true';
        }
        $scope.processRequest(request, function () {
            beforeCallBack();
        }, function (policy) {
            successCallBack(policy);
        }, function (message) {
            failedCallBack(message);
        });
    };
    $scope.processRequest = function (request, beforeCallBack, successCallBack, failedCallBack) {
        beforeCallBack();
        $http(request).success(
            function (res, code) {
                if (code != 200) {
                    failedCallBack(res.message);
                }
                else {
                    $scope.policyId = res.id;
                    successCallBack(res);
                }
            }
        );
    };
    $scope.resetView = function () {
        var date = new Date().getTime();
        var pair = {
            timeStamp: date
        };
        H.setData(pair)
    };
    $scope.searchPolicy = function () {
        var keyword = $scope.searchtext;
        if (!keyword || keyword.trim().length == 0) {
            keyword = '';
        }

        var date = new Date().getTime();
        var pair = {
            timeStamp: date,
            keyword: keyword
        };
        H.setData(pair);
    };
    $scope.searchtext = '';
    $scope.loaded = false;
    $scope.loadData = function (hashData, owner) {
        $scope.ownedTraffics = [];
        $scope.focusedTraffics = [];
        $scope.existingPolicies = [];

        var result = {};

        var trafficsOwnerString = G.baseUrl + '/api/policies?type=extended&anyProp=target:应用迁移&anyTag=owner_' + owner;
        var trafficsFocusedString = G.baseUrl + '/api/policies?type=extended&anyProp=target:应用迁移&anyTag=user_' + owner;
        var existingString = G.baseUrl + '/api/policies?type=extended';

        var vsesStr = G.baseUrl + '/api/vses?type=extended';
        var groupsStr = G.baseUrl + '/api/groups?type=extended&groupType=ALL';
        var appsStr = G.baseUrl + '/api/apps';

        var sys = userPolicyApp.getSystemData(vsesStr, groupsStr, appsStr, result);
        var policies = userPolicyApp.getPolicyCollection(trafficsFocusedString, trafficsOwnerString, existingString, result);
        if (hashData.keyword) {
            $scope.searchtext = hashData.keyword;
        }
        var collection = sys.concat(policies);
        $q.all(
            collection
        ).then(function () {
            var canvas = $('#diagram-canvas');
            $scope.groups = result.groups;
            $scope.vses = result.vses;
            $scope.apps = result.apps;
            $scope.existingPolicies = result.existingPolicies;
            $scope.ownedTraffics = result.ownedTraffics;
            $scope.focusedTraffics = result.focusedTraffics;

            var targetJavaAppId = hashData.appId;
            var type = hashData.type;

            if ((!type || type.toLowerCase() == 'new') && hashData.appId && !$scope.loaded) {
                // is this appid available
                if ($scope.apps[targetJavaAppId]) {
                    $scope.query.appId = targetJavaAppId;
                    $('#appIdSelector_value').val(targetJavaAppId);
                    $scope.changeAppIdSelection();
                    $('#newTrafficPolicyDialog').modal('show');
                } else {
                    $('#errorConfrimModel').modal('show').find('.modal-body').html(targetJavaAppId + (angular.equals(resource, {}) ? ', 在SLB 上找不到，可能不存在或者尚未接入SLB. 请联系 <a href="mailto:slb@test.com">SLB Team</a>需求帮助' : resource.userNormalPolicy.js.msg62));
                }
                $scope.loaded = true;
            }
            if (type && type.toLowerCase() == 'deactivate' && hashData.appId && !$scope.loaded) {
                if ($scope.apps[targetJavaAppId]) {
                    $scope.query.appId = targetJavaAppId;

                    $('#appIdSelectorDeactivate_value').val(targetJavaAppId);
                    $scope.changeAppIdDeactivateSelection();
                    $('#deactivateTrafficPolicyDialog').modal('show');
                } else {
                    $('#errorConfrimModel').modal('show').find('.modal-body').html(targetJavaAppId + (angular.equals(resource, {}) ? ', 在SLB 上找不到，可能不存在或者尚未接入SLB. 请联系 <a href="mailto:slb@test.com">SLB Team</a>需求帮助' : resource.userNormalPolicy.js.msg62));
                }
                $scope.loaded = true;
            }


            var c = _.extend(result.ownedTraffics, result.focusedTraffics);
            if ($scope.searchtext) {
                var appId = $scope.searchtext.trim();
                c = _.filter(c, function (item) {
                    var name = item.name;
                    return name.indexOf(appId) != -1;
                });
            }
            if (targetJavaAppId) {
                var existingPolicy = _.values($scope.existingPolicies);

                var t = _.filter(existingPolicy, function (v) {
                    return v.name.indexOf(targetJavaAppId) != -1;
                });

                c = {};
                $.each(t, function (i, item) {
                    if (!c[item.id]) c[item.id] = item;
                });

                // if the policy is a .net to java policy
                // .NET 转 Java
                var hasNetJavaPolicy = _.filter(_.values(c), function (v) {
                    var p = _.indexBy(v.properties, function (s) {
                        return s.value;
                    });
                    return p['.NET 转 Java'];
                });
                if (hasNetJavaPolicy) {
                    window.location.href = '/portal/user/user-trafficpolicy#?env=' + $scope.env + '&appId=' + targetJavaAppId + '&type=' + type;
                }
            }

            var summary = userPolicyApp.getSummaryData(_.values(c));
            $scope.summaryInfo = summary.text;

            $('.activated-text').text(summary['activate']);
            if (summary['activate'] > 0) {
                $('.activated-text').prop('href', $scope.navigateTo('activated'));
            }

            $('.deactivated-text').text(summary['deactivate']);
            if (summary['deactivate'] > 0) {
                $('.deactivated-text').prop('href', $scope.navigateTo('deactivated'));
            }

            $('.tobeactivated-text').text(summary['tobeactivated']);
            if (summary['tobeactivated'] > 0) {
                $('.tobeactivated-text').prop('href', $scope.navigateTo('tobeactivated'));
            }

            $scope.reloadData(undefined, c);
        });
    };
    $scope.reloadData = function (canvas, c, diagramOnly) {
        if (canvas) canvas.html('');
        var mappings = _.map(_.keys(c), function (key) {
            var policy = c[key];
            return {
                key: key,
                value: $scope.getGraphicData(policy)
            };
        });
        var array = _.pluck(mappings, 'value');
        var t = [];
        $.each(array, function (i, c) {
            t = t.concat(c);
        });

        var scopegroups = $scope.groups;
        var scopeapps = $scope.apps;
        var env = $scope.env;
        var dashboardUrl = G[G.env].urls.dashboard + "/data";
        var http = $http;

        userPolicyApp.drawListOfPolicyGraphics(c, t, http, env, scopegroups, scopeapps, dashboardUrl, function (id) {
                $scope.activatePolicy(id);
            }, function (id) {
                $scope.changePolicyWeight(id);
            },
            function (id) {
                window.open('/portal/policy/traffic#?env=' + env + '&policyId=' + id, '_blank');
            }, function (id) {
                window.open('/portal/policy/traffic#?env=' + env + '&policyId=' + id, '_blank');
            }, canvas, diagramOnly
        )
    };
    $scope.getGraphicData = function (policy) {
        var policy_clone = $.extend({}, true, policy);
        var domains = [];
        var idc = '-';
        var pollicyname = '-';
        var vses = policy_clone['policy-virtual-servers'];

        var pId = policy.id;
        var canActivatePolicy = A.canDo('Policy', 'ACTIVATE', pId);
        var canUpdatePolicy = A.canDo('Policy', 'UPDATE', pId);


        var result = _.map(vses, function (item) {
                var vsId = item['virtual-server'].id;
                var path = item.path.trim();
                if (path == '/' || path == '~* ^/') path = path;
                else {
                    // standard paths?
                    if (path.startsWith('~* ^/') && path.endsWith('($|/|\\?)')) {
                        path = path.substring(5, path.length - 8);
                    } else {
                        if (!path.endsWith('($|/|\\?)')) {
                            path = path.substring(5, path.length);
                        }
                    }
                }
                var vs = $scope.vses[vsId];
                if (vs) {
                    var p = _.find(vs.properties, function (r) {
                        return r.name == 'idc';
                    });
                    if (p) idc = p.value;

                    var protocol = vs.ssl ? 'https://' : 'http://';
                    domains = _.map(vs.domains, function (t) {
                        return protocol + t.name + '/' + path;
                    });
                }
                pollicyname = policy_clone.name;

                var totalWeight = _.reduce(_.pluck(policy_clone.controls, 'weight'), function (memo, num) {
                    return memo + num;
                });
                var groups_temp = _.map(policy_clone.controls, function (s) {
                    var groupidc = '-';
                    var a = _.find($scope.groups[s.group.id].properties, function (t) {
                        return t.name == 'idc';
                    });
                    if (a) groupidc = a.value;

                    var groupdeployidc = '-';
                    var c = _.find($scope.groups[s.group.id].properties, function (t) {
                        return t.name == 'idc_code';
                    });
                    if (c) groupdeployidc = c.value;

                    var grouplanguage = '-';
                    var b = _.find($scope.groups[s.group.id].properties, function (t) {
                        return t.name == 'language';
                    });
                    if (b) grouplanguage = b.value;

                    var appid = $scope.groups[s.group.id]['app-id'];
                    return {
                        name: $scope.groups[s.group.id].name,
                        id: s.group.id,
                        weight: T.getPercent(s.weight, totalWeight),
                        idc: groupidc,
                        idc_code: G['idcs'][groupdeployidc] || groupdeployidc,
                        language: grouplanguage,
                        'app-id': appid,
                        'app-name': $scope.apps[appid] ? $scope.apps[appid]['chinese-name'] : '-'
                    };
                });

                var canDeactivateNetGroup = false;
                var netGroupItem = _.find(groups_temp, function (v) {
                    return v.language == '.NET';
                });
                if (netGroupItem) {
                    canDeactivateNetGroup = A.canDo('Group', 'DEACTIVATE', netGroupItem.id);
                }

                return {
                    vs: {
                        id: vsId,
                        domains: domains,
                        idc: idc,
                        soaops: [],
                        slbops: []
                    },
                    policy: {
                        id: policy.id,
                        name: pollicyname
                    },
                    rights: {
                        canActivatePolicy: canActivatePolicy,
                        canUpdatePolicy: canUpdatePolicy,
                        canDeactivateNetGroup: canDeactivateNetGroup
                    },
                    groups: groups_temp
                }
            }
        );
        return result;
    };
    $scope.navigateTo = function (item) {
        var url = '';
        switch (item) {
            case 'activated': {
                url = '/portal/policies#?env=' + G.env + '&policyTarget=应用迁移&policyStatus=已激活&policyTags=user_' + $scope.loginUserInfo.user + ',owner_' + $scope.loginUserInfo.user;
                break;
            }
            case 'tobeactivated': {
                url = '/portal/policies#?env=' + G.env + '&policyTarget=应用迁移&policyStatus=有变更&policyTags=user_' + $scope.loginUserInfo.user + ',owner_' + $scope.loginUserInfo.user;
                break;
            }
                ;
            case 'deactivated': {
                url = '/portal/policies#?env=' + G.env + '&policyTarget=应用迁移&policyStatus=未激活&policyTags=user_' + $scope.loginUserInfo.user + ',owner_' + $scope.loginUserInfo.user;
                break;
            }
            case 'domain':
                break;
            case 'apps':
                break;
            case 'groups':
                break;
            default:
                break;
        }
        return url;
    };
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.policyChanged = false;
        $('#diagrams-area').html('');
        $scope.init();
        $scope.env = 'pro';
        $scope.hasDecide = false;
        if (hashData.env) $scope.env = hashData.env;
        // get current login user
        var user;
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/current/user'
        };

        userPolicyApp = new UserPolicyApp(hashData, $http, $q, $scope.env, $scope.resource['policynew']["policynew_trafficEditApp_diagram"]);

        $http(request).success(function (response, code) {
            if (code != 200) {
                return;
            } else {
                $scope.loginUserInfo.user = response.name;
                if (hashData.userId) {
                    user = hashData.userId;
                    $scope.query.userId = user;
                    $scope.query.login = user;
                    $scope.loadData(hashData, user);
                }
                else {
                    user = response.name;
                    $scope.query.userId = user;
                    $scope.query.login = user;
                    $scope.loadData(hashData, user);
                }
            }
        });
    };
    H.addListener("selfInfoApp", $scope, $scope.hashChanged);

    function diffTwoSlb(targetDiv, baseText, newText, baseVersion, newVersion) {
        var base = difflib.stringAsLines(baseText);
        var newtxt = difflib.stringAsLines(newText);
        var sm = new difflib.SequenceMatcher(base, newtxt);
        var opcodes = sm.get_opcodes();

        targetDiv.innerHTML = "";

        targetDiv.appendChild(diffview.buildView({
            baseTextLines: base,
            newTextLines: newtxt,
            opcodes: opcodes,
            baseTextName: baseVersion,
            newTextName: newVersion,
            viewType: 0
        }));
    }
});

selfInfoApp.filter('soaFilter', function () {
    return function (input, param1) {
        if (!param1 || param1.trim() == "") {
            return input;
        }

        param1 = param1.trim().toLowerCase();

        return _.filter(input, function (item) {
            return item.toLowerCase().indexOf(param1) != -1;
        });
    }
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);
