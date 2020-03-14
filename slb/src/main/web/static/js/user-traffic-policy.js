var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.resource = H.resource;
    let resource = $scope.resource;
    $scope.query = {
        userId: '',
        userName: ''
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
                messageNotify((angular.equals(resource, {}) ? "切换用户:": resource.userTrafficpolicy.js.msg1), (angular.equals(resource, {}) ? "成功切换至用户: ": resource.userTrafficpolicy.js.msg2) + toId, null);
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

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
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
    let resource = $scope.resource;
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
            case 'unhealthy': {
                link = "/portal/user/user-unhealthy#?env=" + G.env;
                break;
            }
            case 'drs': {
                link = "/portal/user/user-drs#?env=" + G.env;
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
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
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
        displayName: '',
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
        // $scope.tobeActivatedPolicyData = {};
        $scope.validate = undefined;
        $scope.validate2 = undefined;
        $scope.validate3 = undefined;
        $scope.showSoaError = false;
        $scope.clickedornot = false;
        $scope.showsoaAdvanced = false;
        $scope.showslbAdvanced = false;
        $scope.showSoaUpdateError = false;
        $scope.soaUpdateError = '';
        $scope.showSlbUpdateError = false;
        $scope.slbUpdateError = '';
        $scope.isSoaValide = true;
        $scope.isUpdateSoaValide = true;
        $scope.selectedops = [];
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

        $q.all(userPolicyApp.getGroupsByAppId(request, $scope.env, $scope.apps, $scope.existingPolicies, function (result) {
            $scope.javaGroups = result;
        })).then(function () {
            console.log($scope.javaGroups);
        });
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
                    $scope.appidNotHasPolicyError = (angular.equals(resource, {}) ? '当前应用没有关联任何灰度策略!': resource.userTrafficpolicy.js.msg3);
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
            return {vs: item['virtual-server'].id, path: item.path.toLowerCase()}
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
                $scope.firstToSecondErrorMessage = (angular.equals(resource, {}) ? 'Java应用所关联的.NET应用AppId: ': resource.userTrafficpolicy.js.msg4) + firstGroupTargetedApp + (angular.equals(resource, {}) ? '. 当前找不到该关联的应用。　请联系 SLB Team获取帮助': resource.userTrafficpolicy.js.msg5);
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

                var firstSecondApp = firstGroupAppId + '、' + appId;
                var rightavailable = false;
                var netavailable = false;
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

                // language = .net
                var languageItem = _.find(item.properties, function (v) {
                    return v.name.toLowerCase() == 'language' && v.value.toLowerCase() == '.net';
                });
                if (languageItem) netavailable = true;


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
                    return {vs: t['virtual-server'].id, path: t.path ? t.path.toLowerCase() : t.path}
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
                if (!(firstGroupRight && rightavailable && statusavailable && netavailable && policyavailable && vsavailable)) {
                    if (!(rightavailable && firstGroupRight)) {
                        isAccessLink = true;
                        errorMsg = (angular.equals(resource, {}) ? '您没有操作当前应用的权限，<a class="fast-apply">点此快速申请权限</a>': resource.userTrafficpolicy.js.msg6);
                    }
                    else if (!netavailable) errorMsg = (angular.equals(resource, {}) ? '当前Group不是.NET应用，不可用于.NET转Java': resource.userTrafficpolicy.js.msg7);
                    else if (!statusavailable) errorMsg = (angular.equals(resource, {}) ? '当前Group尚未激活，请联系<a href="mailto:OPS_APP@Ctrip.com?subject=SLB上Java项目关联.NET项目&body=请添加咨询问题列表:%E2%80%A8%E2%80%A8%E2%80%A8%E2%80%A8">OPS Team</a>激活后再创建.net转Java应用': resource.userTrafficpolicy.js.msg8);
                    else if (!policyavailable) errorMsg = (angular.equals(resource, {}) ? '当前Group已经创建过 <a href="/portal/policy#?env=' + $scope.env + '&policyId=' + hasPolicy.id + '">.NET转Java的应用</a>，不能再创建了!': resource.userTrafficpolicy.js.msg9);
                    else if (!vsavailable) errorMsg = (angular.equals(resource, {}) ? '当前Group对应的访问入口信息与Java应用的访问入口不一致，<a href="mailto:OPS_APP@Ctrip.com?subject=SLB上Java项目关联.NET项目&body=请添加咨询问题列表:%E2%80%A8%E2%80%A8%E2%80%A8%E2%80%A8">OPS Team</a> 统一Path': resource.userTrafficpolicy.js.msg11);
                }
                return {
                    appId: appId,
                    app: app,
                    id: id,
                    name: name,
                    idc: idc,
                    status: status,
                    available: firstGroupRight && rightavailable && statusavailable && netavailable && policyavailable && vsavailable,
                    error: errorMsg,
                    accessLink: mailLink,
                    netGroup: firstGroupId,
                    isAccessLink: isAccessLink,
                    order: (firstGroupRight && rightavailable && statusavailable && netavailable && policyavailable) ? 0 : 1
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
                    alert((angular.equals(resource, {}) ? '已成功申请权限，请等待管理员审批！': resource.userTrafficpolicy.js.msg12));
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

        secondGroup.type = 'NET';
        secondGroup.appId = secondGroup['app-id'];
        secondGroup.app = $scope.apps[secondGroup['app-id']] ? $scope.apps[secondGroup['app-id']]['chinese-name'] : '-';
        secondGroup.idc = _.find(secondGroup['properties'], function (i) {
            return i.name == 'idc';
        }).value;
        secondGroup.percent = 100;

        var soaItem = _.find(secondGroup.tags, function (v) {
            return v.toLowerCase() == 'soa';
        });
        var isSecondGroupSoa = soaItem ? true : false;
        if (isSecondGroupSoa) {
            secondGroup.desc = '.NET(SOA)';
        } else {
            secondGroup.desc = '.NET';
        }


        var firstSelected = $('.first-step .tile-selected')[0];
        var firstGroupId = $(firstSelected).attr('tag');
        var firstGroup = $scope.groups[firstGroupId];
        firstGroup.appId = firstGroup['app-id'];
        firstGroup.app = $scope.apps[firstGroup['app-id']] ? $scope.apps[firstGroup['app-id']]['chinese-name'] : '-';
        firstGroup.type = 'Java';
        firstGroup.percent = 0;

        var soaItem2 = _.find(firstGroup.tags, function (v) {
            return v.toLowerCase() == 'soa';
        });
        var isFirstGroupSoa = soaItem2 ? true : false;
        if (isFirstGroupSoa) {
            firstGroup.desc = 'Java(SOA)';
        } else {
            firstGroup.desc = 'Java';
        }

        firstGroup.idc = _.find(firstGroup['properties'], function (i) {
            return i.name == 'idc';
        }).value;
        $scope.bothGroups = [firstGroup, secondGroup];

        var j = _.find($scope.bothGroups, function (item) {
            return item.type == 'Java';
        });
        var n = _.find($scope.bothGroups, function (item) {
            return item.type == 'NET';
        });
        $scope.policyGroupsObj.java = j.id;
        $scope.policyGroupsObj.net = n.id;

        // Third step advanced item showing method
        $scope.isSoa = isFirstGroupSoa && isSecondGroupSoa;

        $scope.validate2 = true;
        $('.second-step-bt').attr('disabled', true);
        $(target).attr('disabled', true);
    };
    $scope.getTypeCss = function (type) {
        switch (type) {
            case 'Java':
                return 'status-red fa fa-coffee';
                break;
            case 'NET':
                return 'status-green fa fa-windows ';
                break;
            default :
                break;
        }
    };
    $scope.getDescByType = function (type) {
        switch (type) {
            case 'Java':
                return (angular.equals(resource, {}) ? '(Java应用默认权重0%，创建完成后可调整)': resource.userTrafficpolicy.js.msg13);
                break;
            case 'NET':
                return (angular.equals(resource, {}) ? '(.NET应用默认权重100%，暂时接收所有流量)': resource.userTrafficpolicy.js.msg14);
                break;
            default :
                break;
        }
    };

    // ----- Third Step -----
    $scope.validate3 = undefined;
    $scope.showAdvanceRow = false;

    //高级（只迁移指定流量）
    $scope.advancedOperationText = function () {
        if (!$scope.showAdvanceRow) {
            return (angular.equals(resource, {}) ? '高级设置，迁移指定流量': resource.userTrafficpolicy.js.msg15);
        } else {
            return (angular.equals(resource, {}) ? '放弃高级设置，迁移所有流量': resource.userTrafficpolicy.js.msg16);
        }
    };
    $scope.advancedOperationClass = function () {
        if (!$scope.showAdvanceRow) {
            return 'fa fa-arrow-left system-color system-padding-right';
        } else {
            return 'fa fa-arrow-down system-color system-padding-right';
        }
    };
    $scope.netOps = [];
    $scope.netPath;
    $scope.groupPaths = [];
    $scope.clickAdvancedOption = function () {
        $scope.showAdvanceRow = !$scope.showAdvanceRow;
        if ($scope.showAdvanceRow) {
            if ($scope.isSoa) {
                $('.loadingrow').removeClass('hide');
                // get service operations from soa
                var netAppId = _.find($scope.bothGroups, function (item) {
                    return item.type == 'NET';
                }).appId;
                var request = {
                    url: G[$scope.env].urls['altermis']['get'],
                    method: 'POST',
                    withCredentials: false,
                    data: {
                        appId: netAppId
                    }
                };
                $http(request).success(function (response, code) {
                    $('.loadingrow').addClass('hide');
                    if (!response) {
                        $scope.isSoaValide = false;
                        return;
                    }
                    if (response.responseStatus.status == 'success') {
                        $scope.isSoaValide = true;
                        $scope.netOps = _.reject(response.operations, function (i) {
                            return i.toLowerCase() == 'checkhealth';
                        });
                        $scope.netPath = response.targetUrl;
                    } else {
                        $scope.isSoaValide = false;
                    }
                });
            } else {
                $scope.groupPaths = [''];
            }
        }
    };

    // SOA
    $scope.isSoaValide = false;
    $scope.isSoa = false;
    $scope.selectedops = [];
    $scope.showSoaError = false;
    $scope.soaErrorMessage;

    $scope.showSlbError = false;
    $scope.slbErrorMessage;

    $scope.showSoaMethod = function () {
        return $scope.isSoa && $scope.isSoaValide;
    };
    $scope.showSoaErrorMethod = function () {
        return $scope.isSoa && !$scope.isSoaValide;
    };
    $scope.showSlbMethod = function () {
        return !$scope.isSoa;
    };
    $scope.getSlbPath = function () {
        var netGroup = $scope.bothGroups[1];
        var str = '';
        // TODO: only get the first vs's path
        if (netGroup == undefined) return;
        if (netGroup['group-virtual-servers'] == undefined || netGroup['group-virtual-servers'].length == 0) return;
        var str = $scope.getVirtualPath(netGroup);
        return str;
    };
    $scope.getGroupPath = function (group) {
        var path = group['group-virtual-servers'][0].path;

        if (path == '/' || path == '~* ^/') return '/';

        var start = 0;
        var end = path.length - 1;

        if (path.endsWith('($|/|\\?)')) end = path.length - 8;

        return path.substring(start, end);
    };
    $scope.getVirtualPath = function (group) {
        var path = group['group-virtual-servers'][0].path;

        if (path == '/' || path == '~* ^/') return '/';

        var start = 0;
        var end = path.length - 1;

        if (path.startsWith('~* ^/')) start = 5;
        if (path.endsWith('($|/|\\?)')) end = path.length - 8;

        return path.substring(start, end);
    };

    $scope.updateSelectedOps = function (event, op) {
        var target = event.target;
        var action = target.checked ? 'add' : 'remove';
        if (action == 'add') {
            $scope.selectedops.push(op);
            //$(parent).removeClass('system-color').addClass('system-color');
        } else {
            var index = $scope.selectedops.indexOf(op);
            $scope.selectedops.splice(index, 1);
            // $(parent).removeClass('system-color');
        }
    };
    $scope.isSelectedNewLable = function (op) {
        var l = $scope.selectedops.indexOf(op) != -1;
        if (l) return 'system-color';
    };
    $scope.isSelectedNewOps = function (op) {
        var l = $scope.selectedops.indexOf(op) != -1;
        return l;
    };
    $scope.showSoaOpsError = function () {
        return $scope.showSoaError;
    };
    $scope.getSoaOpError = function () {
        var error;
        if ($scope.selectedops.length == 0) {
            error = (angular.equals(resource, {}) ? '高级模式下请至少选择一个SOA Operation': resource.userTrafficpolicy.js.msg17);
        } else {
            if ($scope.soaErrorMessage) {
                error = (angular.equals(resource, {}) ? 'SOA分流策略失败，错误信息: ' : resource.userTrafficpolicy.js.msg18)+ $scope.soaErrorMessage;
            }
        }
        if (error) return error;
    };

    // SLB
    $scope.addSubPath = function (event, path) {
        var target = event.target;
        var node = $(target).closest('.groupnode');
        path = path.trim();

        if (path == '' || path == undefined) {
            $(node).find('.path-text').removeClass('alert-border').addClass('alert-border');
            return;
        }
        if ($scope.groupPaths.indexOf(path) != -1) {
            $(node).find('.path-text').removeClass('alert-border').addClass('alert-border');
            return;
        }
        $(node).find('.path-text').removeClass('alert-border');

        $scope.groupPaths.pop();
        $scope.groupPaths.push(path);
        $scope.groupPaths.push('');
    };
    $scope.removeSubPath = function (path, index) {
        $scope.groupPaths.splice(index, 1);
    };

    $scope.showSlbOpsError = function () {
        return $scope.showSlbError;
    };
    $scope.getSlbOpError = function () {
        return $scope.slbErrorMessage;
    };

    $scope.policyGroupsObj = {};
    $scope.thirdToForth = function (event) {
        var target = event.target;
        $(target).attr('disabled', true);
        $scope.showSoaError = false;
        $scope.showSlbError = false;

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
            existingGroupVses[item.group.id] = userPolicyApp.getGroupVsPathObj($scope.groups[item.group.id])
        });
        var t = userPolicyApp.getGroupSameAndDiffrentVsPaths(existingGroupVses);
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
        var newPolicyName = 'app_migration_between_app_' + _.pluck($scope.bothGroups, 'app-id').join('-') + '_' + idc + time.getTime();
        $scope.policyName = newPolicyName;
        var groupPath = $scope.getGroupPath($scope.groups[controls[0].group.id]);

        if ($scope.showAdvanceRow && $scope.isSoa) {
            if ($scope.selectedops.length == 0) {
                $scope.showSoaError = true;
            } else {
                // change the path based on the service op
                var result;
                $.each(vses, function (i, item) {
                    result = $scope.unionServiceOps($scope.netPath, groupPath, $scope.selectedops, $scope.netOps);
                    if (!result) {
                        $scope.showSoaError = true;
                        $scope.soaErrorMessage = (angular.equals(resource, {}) ? '验证错误: 提取Path失败': resource.userTrafficpolicy.js.msg19);
                        return;
                    } else {
                        item.path = result;
                    }
                });
            }
        }
        if ($scope.showAdvanceRow && !$scope.isSoa) {
            // slb path
            var passed = true;
            if ($('.path-text').length == 1 && $('.path-text').val().trim() == '') {
                $scope.showSlbError = true;
                $scope.slbErrorMessage = '校验错: 子目录不能为空!';
            }
            else {
                var newslbPath = '';

                $.each($('.path-text'), function (i, item) {
                    item = $(item).val().trim();
                    if (item == '' || item.indexOf('[') != -1 || item.indexOf(']') != -1 || item.indexOf('*') != -1 || item.indexOf(' ') != -1 || item.indexOf('{') != -1 || item.indexOf('}') != -1) {
                        passed = false;
                    } else {
                        if (i != $('.path-text').length - 1) {
                            newslbPath += item + '|';
                        } else {
                            newslbPath += item;
                        }
                    }
                });

                if (!passed) {
                    $scope.showSlbError = true;
                    $scope.slbErrorMessage = '校验错：子目录中不能包含以下字符*,[,],{,},[,] 并且不能为空';
                }

                if (!$scope.showSlbError) {
                    // assemble the new slb path
                    newslbPath = '(' + newslbPath + ')';
                    var p = $scope.getVirtualPath($scope.bothGroups[0]);

                    $.each(vses, function (i, item) {
                        if (p == '/') {
                            // it is root
                            item.path = '~* ^/' + newslbPath + '($|/|\\?)';
                        } else {
                            item.path = '~* ^/' + p + '/' + newslbPath + '($|/|\\?)';
                        }
                    });
                }
            }
        }

        if ($scope.showSoaError || $scope.showSlbError) return;
        var policy = {
            controls: controls,
            'policy-virtual-servers': vses,
            name: newPolicyName,
            properties: [],
            tags: []
        };

        var targetStr = (angular.equals(resource, {}) ? '.NET 转 Java': resource.userTrafficpolicy.js.msg29);
        var groups = _.map(policy.controls, function (v) {
            return v.group.id;
        });
        var ts = _.countBy(groups, function (item) {
            var group = $scope.groups[item];
            var ps = group['tags'];
            if (ps.indexOf('soa') != -1) {
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
        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在创建": resource.userTrafficpolicy.js.msg22);
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
                    title: (angular.equals(resource, {}) ? "创建失败，是否强制创建?": resource.userTrafficpolicy.js.msg23),
                    text: (angular.equals(resource, {}) ? "创建失败，错误信息：": resource.userTrafficpolicy.js.msg24) + message,
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: (angular.equals(resource, {}) ? "强制创建!": resource.userTrafficpolicy.js.msg25),
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
                                title: (angular.equals(resource, {}) ? "强制操作仍然失败!": resource.userTrafficpolicy.js.msg26),
                                text: (angular.equals(resource, {}) ? "强制操作失败。错误信息：" : resource.userTrafficpolicy.js.msg41)+ message + (angular.equals(resource, {}) ? ',请联系SLB Team寻求帮助！': resource.userTrafficpolicy.js.msg28),
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
    $scope.unionServiceOps = function (soapath, grouppath, ops, allops) {
        var s = soapath.split('/');
        var n = '';
        for (var i = 3; i < s.length; i++) {
            if (s[i] != undefined && s[i] != '') {
                n += s[i] + '/';
            }
        }

        var vd = '';

        if (ops.length == 1)
            vd = ops[0];
        else
            vd = "(" + ops.join('|') + ")";

        var lastslash = n.lastIndexOf('/');
        n = n.substring(0, lastslash);

        var paths = n.split('/');
        var appPath = n.split('/')[0];

        var soaPaths = [];
        for (var i = 1; i < paths.length; i++) {
            soaPaths.push(paths[i]);
        }
        var subPaths = '';
        if (soaPaths.length > 0) {
            subPaths = '/' + soaPaths.join('/');
        }
        var prefix = appPath;
        if (subPaths) {
            prefix = '(' + appPath + '|' + appPath + subPaths + ')'
        }

        var path = '~* ^/' + prefix + '(/xml/|/json/|/bjjson/|/x-protobuf/|/)' + vd + '($|/|\\?)';

        if (!prefix) {
            path = '~* ^' + '(/xml/|/json/|/bjjson/|/x-protobuf/|/)' + vd + '($|/|\\?)';
        }

        if (ops.length == allops.length) {
            if (grouppath == '/') return '~* ^/';

            return '~* ^/' + appPath + '($|/|\\?)';
        }

        return path;
    };
    $scope.approveCreateValue = false;

    $scope.thirdStepNextDisabled = function () {
        return !$scope.approveCreateValue;
    };

    // Start create
    $scope.postCreatePolicy = function (policy) {
        var groups = _.map(policy.controls, function (v) {
            return v.group.id;
        });
        var pvalue = (angular.equals(resource, {}) ? '.NET 转 Java': resource.userTrafficpolicy.js.msg29);
        $scope.addTagsForGroups(groups, pvalue);
        $scope.savePolicyToSoa(policy);
    };
    $scope.savePolicyToSoa = function (policy, isactivate) {
        var groupmapping = _.map(policy.controls, function (i) {
            var gid = i.group.id;
            var app = $scope.groups[gid]['app-id'];
            var groupps = _.indexBy($scope.groups[gid].properties, function (item) {
                return item.name.toLowerCase();
            });
            var grouptags = $scope.groups[gid].tags;

            return {
                group: gid,
                app: app,
                idc: groupps['idc'] ? groupps['idc'].value : '-',
                weight: i.weight,
                issoa: grouptags.indexOf('soa') != -1,
                language: groupps['language'] ? groupps['language'].value : '-'
            }
        });

        var hasnonsoa = _.find(groupmapping, function (v) {
            return !v.issoa;
        });

        if (!hasnonsoa) {
            var path = policy['policy-virtual-servers'][0].path;
            if (path) {
                if (path.endsWith('($|/|\\?)')) path = path.substring(0, path.length - 8);
                // ops
                var ops = [];
                var reg = /(.*)\/(\(xml\|json\|bjjson\|x-protobuf\))\/(.*)/g;
                var m = reg.exec(path);
                if (m) {
                    ops = m[3].substring(1, m[3].length - 1).split('|');
                }
                // service id
                var netappid = _.find(groupmapping, function (v) {
                    return v.language.toLowerCase() == '.net';
                });
                netappid = netappid.app;

                if (!netappid) {
                    alert('failed to write soa altermis service, because no java app in the policy');
                } else {
                    var request = {
                        url: G[$scope.env].urls['altermis']['get'],
                        method: 'POST',
                        withCredentials: false,
                        data: {
                            appId: netappid
                        }
                    };
                    var serviceId = undefined;

                    $http(request).success(function (response, code) {
                        if (!response) {
                            alert('failed to write soa altermis service, because could not find service id from soa. reponse is blank');
                            return;
                        }
                        if (response.responseStatus.status == 'success') {
                            serviceId = response.serviceId ? response.serviceId : 1;
                            if (!serviceId) {
                                alert('failed to write soa altermis service, because could not find service id from soa');
                                return;
                            }
                            var url = G[$scope.env].urls['altermis']['update'];
                            var action = '-更新';
                            if (isactivate) {
                                url = G[$scope.env].urls['altermis']['activate'];
                                action = '-上线';
                            }

                            var data = {
                                action: 'SLB .NET转Java同步写SOA' + action,
                                serviceId: serviceId,
                                operatorId: $scope.loginUserInfo.displayName,
                                reason: (angular.equals(resource, {}) ? 'SLB 创建的Policy 相同数据供SOA 服务更新': resource.userTrafficpolicy.js.msg31),
                                operations: ops,
                                groups: _.map(groupmapping, function (item) {
                                    var priority = 1000;
                                    var lan = item.language;
                                    var app = item.app;
                                    if (lan == 'java') priority = -1000;

                                    return {
                                        appId: app,
                                        priority: priority,
                                        weight: item.weight,
                                        idc: $scope.getIdcEnglishName(item.idc)
                                    }
                                })
                            };


                            var request = {
                                method: 'POST',
                                url: url,
                                withCredentials: false,
                                data: data
                            };

                            $http(request).success(function (postresponse, code2) {
                                if (!postresponse) {
                                    alert('failed to save the policy to soa altermis service. response is blank');
                                    return;
                                } else {
                                    if (response.responseStatus.status == 'success') {

                                    } else {
                                        alert('failed to save the policy to soa altermis service. response is ' + response.responseStatus.message);
                                    }
                                }
                            });
                        } else {
                            alert('failed to write soa altermis service, because could not find service id from soa');
                        }
                    });
                }
            } else {
                alert('failed to write soa altermis service, because current policy path is blank');
            }
        }
    };
    $scope.getIdcEnglishName = function (name) {
        var english = '';
        switch (name) {
            case '金桥':
                english = 'SHAJQ';
                break;
            case '欧阳':
                english = 'SHAOY';
                break;
            case '福泉':
                english = 'SHAFQ';
                break;
            case '南通':
                english = 'NTGXH';
                break;
            case '金钟':
                english = 'SHAJZ';
                break;

            default:
                'unknown';
                break;
        }
        return english;
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
        var resource = $scope.resource;
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
            var ptext = (angular.equals(resource, {}) ? '线下版本': resource.userTrafficpolicy.js.msg32) + ($scope.tobeActivatedPolicyData.version ? $scope.tobeActivatedPolicyData.version : '-');
            var ctext = (angular.equals(resource, {}) ? "线上版本": resource.userTrafficpolicy.js.msg33) + ($scope.onlinePolicyData.version ? $scope.onlinePolicyData.version : '-');

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
        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在下线.NET应用": resource.userTrafficpolicy.js.msg34);
        $('#operationConfrimModel').find(".modal-body").html(loading);
        var success = (angular.equals(resource, {}) ? '<i class="status-green fa fa-check" /> 下线成功,将于2s后自动关闭': resource.userTrafficpolicy.js.msg35);

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
                title: (angular.equals(resource, {}) ? "下线失败，是否强制下线?": resource.userTrafficpolicy.js.msg36),
                text: (angular.equals(resource, {}) ? "下线失败，错误信息：": resource.userTrafficpolicy.js.msg37) + message,
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: (angular.equals(resource, {}) ? "强制下线!": resource.userTrafficpolicy.js.msg38),
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
                            title: (angular.equals(resource, {}) ? "强制操作仍然失败!": resource.userTrafficpolicy.js.msg26),
                            text: (angular.equals(resource, {}) ? "强制操作失败。错误信息：" : resource.userTrafficpolicy.js.msg41)+ message + (angular.equals(resource, {}) ? ',请联系SLB Team寻求帮助！': resource.userTrafficpolicy.js.msg28),
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
        var resource = $scope.resource;
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

        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在激活": resource.userTrafficpolicy.js.msg43);
        var success = (angular.equals(resource, {}) ? '<i class="status-green fa fa-check" /> 激活成功,将于2s后自动关闭': resource.userTrafficpolicy.js.msg44);
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
                $scope.savePolicyToSoa($scope.tobeActivatedPolicyData, true);
                setTimeout(function () {
                    $('#operationConfrimModel').modal('hide');
                    $scope.resetView();
                }, 2000);
            },
            function (message) {
                $('#operationConfrimModel').modal('hide');
                swal({
                    title: (angular.equals(resource, {}) ? "激活失败，是否强制激活?": resource.userTrafficpolicy.js.msg45),
                    text: (angular.equals(resource, {}) ? "激活失败，错误信息：" : resource.userTrafficpolicy.js.msg46)+ message,
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: (angular.equals(resource, {}) ? "强制激活!": resource.userTrafficpolicy.js.msg47),
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
                                title: (angular.equals(resource, {}) ? "强制操作仍然失败!": resource.userTrafficpolicy.js.msg26),
                                text: (angular.equals(resource, {}) ? "强制操作失败。错误信息：" : resource.userTrafficpolicy.js.msg41)+ message + (angular.equals(resource, {}) ? ',请联系SLB Team寻求帮助！': resource.userTrafficpolicy.js.msg28),
                                imageUrl: "/static/img/fail.png"
                            });
                            $('#operationConfrimModel').modal('hide');
                        });
                    }
                });
            });
    };

    $scope.canUserActivatePolicy = function () {
        if (!$scope.policyId) {
            return false;
        }

        var policyId = $scope.policyId;
        return !A.canDo('Policy', 'ACTIVATE', policyId);
    };

    $scope.getActivatePolicyTitle = function () {
        var policyId = $scope.policyId;
        var can = A.canDo('Policy', 'ACTIVATE', policyId);
        if (!can) {
            return (angular.equals(resource, {}) ? '没有权限': resource.userTrafficpolicy.js.msg52);
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
    $scope.updateNetOps = [];
    $scope.selectedupdateops = [];
    $scope.clickedornot = false;
    $scope.showsoaAdvanced = false;
    $scope.showslbAdvanced = false;
    $scope.showSoaUpdateError = false;
    $scope.soaUpdateError = '';
    $scope.showSlbUpdateError = false;
    $scope.slbUpdateError = '';
    $scope.advancedUpdateOperationText = function () {
        var resource = $scope.resource;
        var a = $scope.clickedornot;
        if (a) return (angular.equals(resource, {}) ? '放弃高级设置，迁移所有流量': resource.userTrafficpolicy.js.msg16);
        return (angular.equals(resource, {}) ? '高级设置，迁移指定流量': resource.userTrafficpolicy.js.msg15);
    };
    $scope.advancedUpdateOperationClass = function () {
        var a = $scope.clickedornot;
        if (!a) {
            return 'fa fa-arrow-left system-color system-padding-right';
        } else {
            return 'fa fa-arrow-down system-color system-padding-right';
        }
    };
    $scope.showSaveAsk = function () {
        if ($scope.clickedornot) return $scope.isUpdateSoaValide;
        return true;
    };
    $scope.clickUpdateAdvancedOption = function () {
        $scope.clickedornot = !$scope.clickedornot;
    };

    $scope.showUpdateAdvanceRow = function () {
        return $scope.clickedornot;
    };

    $scope.showUpdateSoaOpsError = function () {
        return $scope.showSoaUpdateError;
    };
    $scope.getUpdateSoaOpError = function () {
        return $scope.soaUpdateError;
    };
    $scope.updateUpdatedSelectedOps = function (event, op) {
        var target = event.target;
        var parent = $(target).parent()[0];

        var action = target.checked ? 'add' : 'remove';
        if (action == 'add') {
            $scope.selectedupdateops.push(op);
            $(parent).removeClass('system-color').addClass('system-color');
        } else {
            var index = $scope.selectedupdateops.indexOf(op);
            $scope.selectedupdateops.splice(index, 1);
            $(parent).removeClass('system-color');
        }
        var l = $scope.leftAppModel;
        var r = $scope.rightAppModel;

        l.soaops = $scope.selectedupdateops;
        r.soaops = $scope.selectedupdateops;
    };
    $scope.showUndoOpsChange = function () {
        if ($scope.originupdateops && $scope.selectedupdateops) {
            if ($scope.originupdateops.join('|') == $scope.selectedupdateops.join('|')) return false;
            return true;
        }
    };
    $scope.undoOpChange = function () {
        $scope.selectedupdateops = $.extend(true, [], $scope.originupdateops);
        $scope.leftAppModel.soaops = $scope.originupdateops;
    };
    $scope.isSelectedOps = function (op) {
        op = op.toLowerCase();
        var existingOps = _.map($scope.leftAppModel.soaops, function (v) {
            return v.toLowerCase();
        });

        var l = existingOps.indexOf(op) != -1;
        return l;
    };
    $scope.isSelectedLable = function (op) {
        op = op.toLowerCase();
        var existingOps = _.map($scope.leftAppModel.soaops, function (v) {
            return v.toLowerCase();
        });
        var l = existingOps.indexOf(op) != -1;
        if (l) return 'system-color';
    };
    $scope.getUpdateSlbPath = function () {
        var id = $scope.leftAppModel ? $scope.leftAppModel.id : undefined;
        if (!id) return;

        var netGroup = $scope.groups[$scope.leftAppModel.id];
        // TODO: only get the first vs's path
        if (netGroup == undefined) return;

        if (netGroup['group-virtual-servers'] == undefined || netGroup['group-virtual-servers'].length == 0) return;

        var str = $scope.getVirtualPath(netGroup);

        return str;
    };
    $scope.blurUpdateText = function (event, value) {
        var target = event.target;

        if (value == '' || value.trim() == '' || value == undefined) {
            $(target).removeClass('alert-border').addClass('alert-border');
            return;
        } else {
            $(target).removeClass('alert-border');
        }

        var u = _.unique(_.pluck($scope.updateGroupPaths, 'value'));

        if (u.length != $('.path-update-text').length) {
            $(target).removeClass('alert-border').addClass('alert-border');
            return;
        } else {
            $(target).removeClass('alert-border');
        }

        if ($('#slb-new-area').find('.alert-border').length != 0) return;

        var l = $scope.leftAppModel;
        var r = $scope.rightAppModel;

        l.slbops = u;
        r.slbops = u;
        /*
         var s = $scope.getAppHtml('Java', l);
         var t = $scope.getAppHtml('NET', r);
         $('.left-app').html(s);
         $('.right-app').html(t);*/
    };
    $scope.addUpdateSubPath = function (path, index) {
        var origin = $.extend(true, [], $scope.updateGroupPaths);
        if (path == undefined || path == '' || path.trim() == '') {
            $scope.showSlbUpdateError = true;
            $scope.slbUpdateError = (angular.equals(resource, {}) ? '验证错误：子入口的值不能为空': resource.userTrafficpolicy.js.msg55);
            return;
        }
        origin.splice(index, 1);

        var a = _.unique(_.pluck(origin, 'value'));
        a = _.each(a, function (item) {
            if (item == '' || item == undefined || item.trim() == '') return item;
            return item.toLowerCase();
        });

        if (a.indexOf(path) != -1) {
            $scope.showSlbUpdateError = true;
            $scope.slbUpdateError = (angular.equals(resource, {}) ? '验证错误：已经存在子入口:': resource.userTrafficpolicy.js.msg56) + path;
            return;
        }

        $scope.updateGroupPaths.splice(index, 1);
        $scope.updateGroupPaths.push({
            value: path
        });
        $scope.updateGroupPaths.push({
            value: ''
        });
    };
    $scope.removeUpdateSubPath = function (value, index) {
        if ($scope.updateGroupPaths.length == 1) {
            $scope.showSlbUpdateError = true;
            $scope.slbUpdateError = (angular.equals(resource, {}) ? '验证错误：子入口个数不能为0!': resource.userTrafficpolicy.js.msg57);
            return;
        }
        $scope.updateGroupPaths.splice(index, 1);
    };
    $scope.showUpdateSlbOpsError = function () {
        return $scope.showSlbUpdateError;
    };
    $scope.getUpdateSlbOpError = function () {
        return $scope.slbUpdateError;
    };
    $scope.changePolicyWeight = function (id) {
        let resource = $scope.resource;
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
            // if both groups are java and net
            var m = _.map(controls, function (item) {
                var l = _.find($scope.groups[item.group.id]['properties'], function (v) {
                    return v.name == 'language';
                });
                return {
                    id: item.group.id,
                    language: l ? l.value : '-',
                    weight: item.weight
                }
            });
            m = _.indexBy(m, 'language');

            var left = m['java'];
            var right = m['.net'];

            if (!left || !right) {
                alert((angular.equals(resource, {}) ? '当前Policy中的应用并非Java或者.NET!!!': resource.userTrafficpolicy.js.msg58));
                return;
            }
            var leftId = left.id;
            var rightId = right.id;
            $scope.leftGroupId = leftId;
            $scope.rightGroupId = rightId;

            var path = p['policy-virtual-servers'][0].path;
            if (path.endsWith('($|/|\\?)')) path = path.substring(0, path.length - 8);
            var slbops = [];
            var soaops = [];
            var reg = /(.*)\/(\(xml\|json\|bjjson\|x-protobuf\))\/(.*)/g;
            var reg2 = /(.*)(\(\/xml\/\|\/json\/\|\/bjjson\/\|\/x-protobuf\/\|\/\))(.*)/g;
            var m = reg.exec(path);
            if (!m) {
                m = reg2.exec(path);
            }
            if (m) {
                var s = m[1];
                var t = m[3];
                if (s) {
                    path = s;
                }
                if (t) {
                    var l = t.length;
                    var start = t[0] == '(' ? 1 : 0;
                    var end = start == 0 ? l : l - 1;
                    soaops = t.substring(start, end).split('|');
                }
            } else {
                var reg2 = /(.*)\/\((.*)\)/g
                var n = reg2.exec(path);
                if (n) {
                    var u = n[1];
                    var v = n[2];
                    if (u) path = u;
                    if (v) slbops = v.split('|');
                }
            }
            var leftGroupIdc = _.find($scope.groups[leftId].properties, function (item) {
                return item.name == 'idc';
            });
            var rightGroupIdc = _.find($scope.groups[rightId].properties, function (item) {
                return item.name == 'idc';
            });

            var leftAppId = $scope.groups[leftId]['app-id'];
            var leftGroupName = $scope.groups[leftId].name;
            var leftAppName = leftAppId + '/' + ($scope.apps[leftAppId] ? $scope.apps[leftAppId]['chinese-name'] : '-');
            leftGroupIdc = leftGroupIdc ? leftGroupIdc.value : '-';

            var rightGroupName = $scope.groups[rightId].name;
            var rightAppId = $scope.groups[rightId]['app-id'];
            var rightAppName = rightAppId + '/' + ($scope.apps[rightAppId] ? $scope.apps[rightAppId]['chinese-name'] : '-');
            rightGroupIdc = rightGroupIdc ? rightGroupIdc.value : '-';

            var l = $scope.getAppHtml('Java', {
                id: leftId,
                name: leftGroupName,
                appId: leftAppId,
                app: leftAppName,
                idc: leftGroupIdc,
                soaops: soaops,
                slbops: slbops,
                weight: left.weight
            });
            var r = $scope.getAppHtml('NET', {
                id: rightId,
                name: rightGroupName,
                appId: rightAppId,
                app: rightAppName,
                idc: rightGroupIdc,
                soaops: soaops,
                slbops: slbops,
                weight: right.weight
            });
            $('.left-app').html(l);
            $('.right-app').html(r);

            if (soaops.length > 0) $scope.showsoaAdvanced = true;

            if (slbops.length > 0) {
                $scope.showslbAdvanced = true;
                $scope.updateGroupPaths = _.map(slbops, function (item) {
                    return {
                        value: item
                    }
                });
            }

            $scope.leftAppModel = {
                id: leftId,
                name: leftGroupName,
                appId: leftAppId,
                app: leftAppName,
                idc: leftGroupIdc,
                soaops: soaops,
                slbops: slbops,
                weight: left.weight
            };
            $scope.rightAppModel = {
                id: rightId,
                name: rightGroupName,
                appId: rightAppId,
                app: rightAppName,
                idc: rightGroupIdc,
                soaops: soaops,
                slbops: slbops,
                weight: right.weight
            };


            $scope.selectedupdateops = $.extend(true, [], soaops);
            $scope.originupdateops = $.extend(true, [], soaops);

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

            // if current supports soa/slb
            if (soaops.length > 0) {
                var request = {
                    url: G[$scope.env].urls['altermis']['get'],
                    method: 'POST',
                    withCredentials: false,
                    data: {
                        appId: rightAppId
                    }
                };
                $http(request).success(function (response, code) {
                    if (!response) {
                        $scope.isUpdateSoaValide = false;
                        return;
                    }
                    if (response.responseStatus.status == 'success') {
                        $scope.isUpdateSoaValide = true;
                        $scope.updateNetOps = _.reject(response.operations, function (i) {
                            return i.toLowerCase() == 'checkhealth';
                        });
                        $scope.updateNetPath = response.targetUrl;
                    } else {
                        $scope.isUpdateSoaValide = false;
                    }
                });
            } else {
                // if current not support soa and slb
                var lg = $scope.groups[$scope.leftAppModel.id];
                var rg = $scope.groups[$scope.rightAppModel.id];
                var lgs = _.find(lg.tags, function (i) {
                    return i.toLowerCase() == 'soa';
                });
                var rgs = _.find(rg.tags, function (i) {
                    return i.toLowerCase() == 'soa';
                });
                if (lgs && rgs) {
                    // soa
                    $scope.showsoaAdvanced = true;
                    var request = {
                        url: G[$scope.env].urls['altermis']['get'],
                        method: 'POST',
                        withCredentials: false,
                        data: {
                            appId: rightAppId
                        }
                    };
                    $http(request).success(function (response, code) {
                        if (!response) {
                            $scope.isUpdateSoaValide = false;
                            return;
                        }
                        if (response.responseStatus.status == 'success') {
                            $scope.isUpdateSoaValide = true;
                            $scope.updateNetOps = _.reject(response.operations, function (i) {
                                return i.toLowerCase() == 'checkhealth';
                            });
                            $scope.updateNetPath = response.targetUrl;
                        } else {
                            $scope.isUpdateSoaValide = false;
                        }
                    });
                } else {
                    // slb
                    $scope.showslbAdvanced = true;
                    if (slbops.length == 0) {
                        $scope.updateGroupPaths = [{value: ''}];
                    }
                }
            }
            $('#weightAdjustDialog .operation-result').hide();
            $('#weightAdjustDialog').modal('show');


            if (soaops.length > 0) {
                $('.updateadvanced').click();
            }
            if (slbops.length > 0) {
                $('.updateadvanced').click();
            }
        }
    };

    $scope.canUserChangePolicy = function () {
        var id = $scope.policyId;
        var can = A.canDo('Policy', 'UPDATE', id);
        return !can;
    };
    $scope.getUserChangePolicyTitle = function () {
        var id = $scope.policyId;
        var can = A.canDo('Policy', 'UPDATE', id);
        if (!can) {
            return (angular.equals(resource, {}) ? '您还没有更新当前策略的权限': resource.userTrafficpolicy.js.msg59);
        }
    };
    $scope.getAppHtml = function (language, group) {
        var languageCss = (language == 'Java' ? 'fa fa-coffee status-red' : 'fa fa-windows status-green');
        var newlanguage = language;

        if (group.soaops.length > 0) {
            newlanguage = language + '(SOA)';
        }
        if (group)
            var result = '<div class="panel group-item">' +
                '<div class="panel-body" style="padding-top: 0;"> ' +
                '<div class="media-main"> ' +
                '<div class="row">  ' +
                '<div class=""><div class="pull-left"><span class="app-type"> ' + newlanguage + ' <i class="' + languageCss + '"></i></span></div><div class="pull-right"><p class="status-red ' + language + '">' + group.weight + '%</p></div> ' +
                '</div> ' +
                '<div class="row"> ' +
                '<div class="col-md-12"><span class="text-black">Group:</span><a href="/portal/group#?env=' + $scope.env + '&groupId=' + group.id + '" target="_blank">' + group.name + '</a> ' +
                '</div></div><div class="row"><div class="col-md-12">' +
                '<p class="text-black">App: <a href="/portal/app#?env=' + $scope.env + '&appId=' + group.appId + '" target="_blank">' + group.app + '</a> ' +
                '</p> </div></div>';

        if (group.soaops.length > 0) {
            result += '<p class="text-black">SOA操作: ' + group.soaops.join(',') + '</p>'
        }

        if (group.slbops.length > 0) {
            result += '<p class="text-black">SLB访问入口: ' + group.slbops.join(',') + '</p>'
        }

        result += '</div> ';

        result += '<div class="row"> <p class="text-black">IDC: ' + group.idc + '</p>' +
            '</div> ' +
            '</div> ' +
            '<div class="clearfix"></div> ' +
            '</div> ' +
            '</div></div>';

        return result;
    };
    $scope.getPolicyAuthLink = function (target) {
        var t = target.currentTarget;
        if ($(t).is("a[disabled]")) {
            alert((angular.equals(resource, {}) ? '您已经申请过权限了，请等待SLB Team批准。': resource.userTrafficpolicy.js.msg61));
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
                alert((angular.equals(resource, {}) ? '权限申请已经发出，请等待SLB Team审批通过。': resource.userTrafficpolicy.js.msg62));
                $(t).attr('disabled', 'disabled');
            }
        );
    };


    $scope.canUserSaveWeight = function () {
        var read = $scope.read;

        return !read;
    };

    $scope.policyChanged = false;

    $scope.initWeight = function () {
        $scope.firstshow = false;
        $scope.validate = undefined;
        $scope.validate2 = undefined;
        $scope.validate3 = undefined;
        $scope.showSoaError = false;
        $scope.clickedornot = false;
        $scope.showsoaAdvanced = false;
        $scope.showslbAdvanced = false;
        $scope.showSoaUpdateError = false;
        $scope.soaUpdateError = '';
        $scope.showSlbUpdateError = false;
        $scope.slbUpdateError = '';
        $scope.isSoaValide = true;
        $scope.isUpdateSoaValide = true;
        $scope.selectedops = [];
        $scope.updateNetOps = [];
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
        var advance = $scope.clickedornot;
        var groupPath = $scope.getGroupPath($scope.groups[left.id]);
        if (advance) {
            if ($scope.showsoaAdvanced) {
                //groupPath = $scope.updateNetPath;
                if (left.soaops.length == 0) {
                    $scope.showSoaUpdateError = true;
                    $scope.soaUpdateError = (angular.equals(resource, {}) ? '验证错误: 高级模式下，请至少选择一个SOA Operation': resource.userTrafficpolicy.js.msg63);
                    return;
                } else {
                    var newops = left.soaops;
                    var currentOps = $scope.updateNetOps;

                    $.each(p['policy-virtual-servers'], function (i, item) {
                        item.path = $scope.unionServiceOps($scope.updateNetPath, groupPath, newops, currentOps);
                    });
                }
            }
            if ($scope.showslbAdvanced) {
                if (left.slbops.length == 0) {
                    $scope.showSlbUpdateError = true;
                    $scope.slbUpdateError = (angular.equals(resource, {}) ? '验证错误: 高级模式下，请至少添加一个SLB 子入口': resource.userTrafficpolicy.js.msg64);
                    return;
                } else {
                    var passed = true;
                    var newslbPath = '';

                    var t = _.unique(_.pluck($scope.updateGroupPaths, 'value'));
                    if (t.length != $('.path-update-text').length) {
                        $scope.showSlbUpdateError = true;
                        $scope.slbUpdateError = (angular.equals(resource, {}) ? '验证错误: slb子入口不能重复!': resource.userTrafficpolicy.js.msg65);
                        return;
                    }
                    $.each(t, function (i, item) {
                        if (item == '' || item.indexOf('[') != -1 || item.indexOf(']') != -1 || item.indexOf('*') != -1 || item.indexOf(' ') != -1 || item.indexOf('{') != -1 || item.indexOf('}') != -1) {
                            passed = false;
                            passed = passed && passed;
                        } else {
                            if (i != $('.path-update-text').length - 1) {
                                newslbPath += item + '|';
                            } else {
                                newslbPath += item;
                            }
                        }
                    });
                    if (!passed) {
                        $scope.showSlbUpdateError = true;
                        $scope.slbUpdateError = (angular.equals(resource, {}) ? '校验错：子目录中不能包含以下字符*,[,],{,},[,] 以及空字符!': resource.userTrafficpolicy.js.msg66);
                        return;
                    } else {
                        newslbPath = '(' + newslbPath + ')';
                    }
                    $.each(p['policy-virtual-servers'], function (i, item) {
                        if (groupPath == '/') {
                            item.path = '~* ^/' + newslbPath + '($|/|\\?)';
                        } else {
                            item.path = groupPath + '/' + newslbPath + '($|/|\\?)';
                        }
                    });
                }
            }
        }
        else {
            $.each(p['policy-virtual-servers'], function (i, item) {
                if (groupPath == "" || groupPath == '/') {
                    item.path = "~* ^/";
                } else {
                    item.path = groupPath + '($|/|\\?)';
                }
            });
        }

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
        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在更新": resource.userTrafficpolicy.js.msg67);
        $('#operationConfrimModel').find(".modal-body").html(loading);
        $('#operationConfrimModel').find(".modal-title").html((angular.equals(resource, {}) ? "正在更新权重": resource.userTrafficpolicy.js.msg68));

        $scope.do(request, function () {
                $('#operationConfrimModel').modal('show');
            },
            function (policy) {
                $('#operationConfrimModel').modal('hide');
                $('.operation-result').slideDown();
                $scope.savePolicyToSoa(policy);
            }, function (message) {
                $('#operationConfrimModel').modal('hide');
                swal({
                    title: (angular.equals(resource, {}) ? "更新失败，是否强制更新?": resource.userTrafficpolicy.js.msg69),
                    text: (angular.equals(resource, {}) ? "更新失败，错误信息：": resource.userTrafficpolicy.js.msg71) + message,
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: (angular.equals(resource, {}) ? "强制更新!": resource.userTrafficpolicy.js.msg72),
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
                                title: (angular.equals(resource, {}) ? "强制操作仍然失败!": resource.userTrafficpolicy.js.msg26),
                                text: (angular.equals(resource, {}) ? "强制操作失败。错误信息：" : resource.userTrafficpolicy.js.msg41)+ message + (angular.equals(resource, {}) ? ',请联系SLB Team寻求帮助！': resource.userTrafficpolicy.js.msg28),
                                imageUrl: "/static/img/fail.png"
                            });
                            $('#operationConfrimModel').modal('hide');
                        });
                    }
                });
            });
    };
    $('#weightAdjustDialog, #newTrafficPolicyDialog, #deactivateTrafficPolicyDialog').on('hidden.bs.modal', function (a, b, c) {
        $('.first-step-bt, .second-step-bt, .third-step-bt, .fast-apply, .deactivate-apply').attr('disabled', false);
        $.each($('#newTrafficPolicyDialog').find('input[type="checkbox"]'), function (i, item) {
            $(item).attr('checked', false);
        });
        $.each($('#newTrafficPolicyDialog').find('.create-ops-label'), function (i, item) {
            $(item).removeClass('system-color');
        });
        $scope.firstshow = false;
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
    $scope.searchtext = '';
    $scope.loaded = false;
    $scope.loadData = function (hashData, owner) {
        $scope.ownedTraffics = [];
        $scope.focusedTraffics = [];
        $scope.existingPolicies = [];

        var result = {};

        var trafficsOwnerString = G.baseUrl + '/api/policies?type=extended&anyProp=target:.NET 转 Java&anyTag=owner_' + owner;
        var trafficsFocusedString = G.baseUrl + '/api/policies?type=extended&anyProp=target:.NET 转 Java&anyTag=user_' + owner;
        var existingString = G.baseUrl + '/api/policies?type=extended';

        var vsesStr = G.baseUrl + '/api/vses?type=extended';
        var groupsStr = G.baseUrl + '/api/groups?type=extended&groupType=all';
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

            var targetJavaAppId;
            var type = hashData.type;

            targetJavaAppId = hashData.appId;

            if ((!type || type.toLowerCase() == 'new') && hashData.appId && !$scope.loaded) {
                // is this appid available

                if ($scope.apps[targetJavaAppId]) {
                    $scope.query.appId = targetJavaAppId;
                    $('#appIdSelector_value').val(targetJavaAppId);
                    $scope.changeAppIdSelection();
                    $('#newTrafficPolicyDialog').modal('show');
                } else {
                    $('#errorConfrimModel').modal('show').find('.modal-body').html(targetJavaAppId + (angular.equals(resource, {}) ? ', 在SLB 上找不到，可能不存在或者尚未接入SLB. 请联系 <a href="mailto:slb@test.com">SLB Team</a>需求帮助': resource.userTrafficpolicy.js.msg76));
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
                    $('#errorConfrimModel').modal('show').find('.modal-body').html(targetJavaAppId + (angular.equals(resource, {}) ? ', 在SLB 上找不到，可能不存在或者尚未接入SLB. 请联系 <a href="mailto:slb@test.com">SLB Team</a>需求帮助': resource.userTrafficpolicy.js.msg76));
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
            }

            var summary = userPolicyApp.getSummaryData(c, $scope.searchtext);
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

    $scope.getGraphicData = function (policy) {
        return userPolicyApp.getGraphicData(policy, $scope.vses, $scope.groups, $scope.apps);
    };
    $scope.navigateTo = function (item) {
        var url = '';
        switch (item) {
            case 'activated': {
                url = '/portal/policies#?env=' + G.env + '&policyTarget=.NET 转 Java&policyStatus=已激活&policyTags=user_' + $scope.loginUserInfo.user + ',owner_' + $scope.loginUserInfo.user;
                break;
            }
            case 'tobeactivated': {
                url = '/portal/policies#?env=' + G.env + '&policyTarget=.NET 转 Java&policyStatus=有变更&policyTags=user_' + $scope.loginUserInfo.user + ',owner_' + $scope.loginUserInfo.user;
                break;
            }
            case 'deactivated': {
                url = '/portal/policies#?env=' + G.env + '&policyTarget=.NET 转 Java&policyStatus=未激活&policyTags=user_' + $scope.loginUserInfo.user + ',owner_' + $scope.loginUserInfo.user;
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
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.policyChanged = false;
        $('#diagrams-area').html('');

        $scope.selecteIndex = 0;
        $scope.hasDecide = false;
        $scope.init();

        $scope.env = 'pro';
        if (hashData.env) $scope.env = hashData.env;

        // get current login user
        var user;
        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/current/user'
        };


        userPolicyApp = new UserPolicyApp(hashData, $http, $q, $scope.env, $scope.resource['policynew']["policynew_trafficEditApp_diagram"]);

        userPolicyApp.sendRequest(request, function (response, code) {
            $scope.loginUserInfo = {
                user: response.name,
                displayName: response['display-name']
            };
            $scope.query.login = response.name;
            var user = hashData.userId || response.name;
            $scope.query.userId = user;
            if (!hashData.userId) {
                var pairs = {};
                pairs['userId'] = user;
                pairs.timeStamp = new Date().getTime();
                H.setData(pairs);
            }
            $scope.loadData(hashData, user);
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
