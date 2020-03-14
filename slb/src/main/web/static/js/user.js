var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.resource = H.resource;
    var resource = $scope.resource;
    $scope.query = {
        userId: ''
    };
    $scope.cacheRequestFn = function (str) {
        return { q: str, timestamp: new Date().getTime() };
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
                messageNotify((angular.equals(resource, {}) ? "切换用户:": resource.user.js.msg1), (angular.equals(resource, {}) ? "成功切换至用户: ": resource.user.js.msg2) + toId, null);
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

var breadcrumbFollowApp = angular.module("breadcrumbFollowApp", ["angucomplete-alt"]);
breadcrumbFollowApp.controller("defaultController", function ($scope) {
    $scope.resource = H.resource;

    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
    };
    H.addListener("breadcrumbFollowApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("breadcrumbFollowArea"), ['breadcrumbFollowApp']);

//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource = H.resource;
    var resource = $scope.resource;
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
        link += '&userId=' + $scope.query.userId;
        return link;
    };
    $scope.loadData = function () {
        if ($scope.query.userId) return;
        var url = '/api/auth/current/user';
        var request = {
            method: 'GET',
            url: url
        };
        $http(request).success(
            function (res) {
                $scope.query.userId = res.name;
            }
        );
    };
    $scope.hashChanged = function (hashData) {
    $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.userId) {
            $scope.query.userId = hashData.userId;
        } else {
            $scope.loadData();
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);


var selfInfoApp = angular.module('selfInfoApp', ["angucomplete-alt", "http-auth-interceptor"]);
selfInfoApp.controller('selfInfoController', function ($scope, $http, $q) {
    $scope.resource = H.resource;
    var resource = $scope.resource;
    $scope.groups = {};
    $scope.vses = {};
    $scope.apps = {};

    $scope.ownedTraffics = [];
    $scope.focusedTraffics = [];
    $scope.ownGroup = false;
    $scope.userInfo = {};
    $scope.loginUserInfo = {
        slbs: {},
        vses: {},
        groups: {},
        apps: {},
        policies: {}
    };
    $scope.apps = {};
    $scope.query = {
        tab: '',
        userId: '',
        loginUserId: ''
    };
    $scope.tableOps = {
        policy: {
            showMoreColumns: false,
            showOperations: false
        },
        policyfocus: {
            showMoreColumns: false,
            showOperations: false
        },
        slbfocus: {
            showMoreColumns: false,
            showOperations: false
        },
        vsfocus: {
            showMoreColumns: false,
            showOperations: false
        },
        groupfocus: {
            showMoreColumns: false,
            showOperations: false
        },
        appfocus: {
            showMoreColumns: false,
            showOperations: false
        },
        app: {
            showMoreColumns: false,
            showOperations: false
        },
        group: {
            showMoreColumns: false,
            showOperations: false
        }
    };
    // Partial show
    $scope.disableOpen = function (type) {
        var can = false;
        switch (type) {
            case 'policy':
                can = A.canDo('Policy', 'PROPERTY', '*');
                break;

            case 'slb-f':
                can = A.canDo('Slb', 'PROPERTY', '*');
                break;

            case 'vs-f':
                can = A.canDo('Vs', 'PROPERTY', '*');
                break;
            case 'group-f':
                can = A.canDo('Group', 'PROPERTY', '*');
                break;
            case 'app-f':
                can = A.canDo('Group', 'PROPERTY', '*');
                break;
            case 'app':
                can = A.canDo('Group', 'PROPERTY', '*');
                break;
            case 'group':
                can = A.canDo('Group', 'PROPERTY', '*');
                break;
            default:
                break;
        }
        return !can;
    };
    $scope.getShowMore = function (type) {
        switch (type) {
            case 'policy':
                return $scope.tableOps.policy.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';

            case 'policy-f':
                return $scope.tableOps.policyfocus.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';

            case 'slb-f':
                return $scope.tableOps.slbfocus.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';

            case 'vs-f':
                return $scope.tableOps.vsfocus.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';

            case 'group-f':
                return $scope.tableOps.groupfocus.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
            case 'app-f':
                return $scope.tableOps.appfocus.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
            case 'app':
                return $scope.tableOps.app.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';
            case 'group':
                return $scope.tableOps.group.showMoreColumns ? 'fa fa-list-alt text-info' : 'fa fa-list-alt font-gray';

            default:
                break;
        }
    };
    $scope.getShowOperation = function (type) {
        switch (type) {
            case 'policy':
                return $scope.tableOps.policy.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
            case 'policy-f':
                return $scope.tableOps.policyfocus.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
            case 'slb-f':
                return $scope.tableOps.slbfocus.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
            case 'vs-f':
                return $scope.tableOps.vsfocus.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
            case 'group-f':
                return $scope.tableOps.groupfocus.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
            case 'app-f':
                return $scope.tableOps.appfocus.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
            case 'app':
                return $scope.tableOps.app.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';
            case 'group':
                return $scope.tableOps.group.showOperations ? 'fa fa-wrench text-info' : 'fa fa-wrench font-gray';

            default:
                break;
        }

    };
    $scope.toggleShowMoreColumns = function (type) {
        switch (type) {
            case 'policy': {
                $scope.tableOps.policy.showMoreColumns = !$scope.tableOps.policy.showMoreColumns;
                if ($scope.tableOps.policy.showMoreColumns) {
                    $('#owned-traffic-table').bootstrapTable('showColumn', 'paths');
                    $('#owned-traffic-table').bootstrapTable('showColumn', 'target');
                    $('#owned-traffic-table').bootstrapTable('showColumn', 'controls1');
                    $('#owned-traffic-table').bootstrapTable('hideColumn', 'controls2');
                } else {
                    $('#owned-traffic-table').bootstrapTable('hideColumn', 'paths');
                    $('#owned-traffic-table').bootstrapTable('hideColumn', 'target');
                    $('#owned-traffic-table').bootstrapTable('hideColumn', 'controls1');
                    $('#owned-traffic-table').bootstrapTable('showColumn', 'controls2');
                }
                break;
            }
            case 'policy-f': {
                $scope.tableOps.policyfocus.showMoreColumns = !$scope.tableOps.policyfocus.showMoreColumns;
                break;
            }
            case 'slb-f': {
                $scope.tableOps.slbfocus.showMoreColumns = !$scope.tableOps.slbfocus.showMoreColumns;
                break;
            }
            case 'vs-f': {
                $scope.tableOps.vsfocus.showMoreColumns = !$scope.tableOps.vsfocus.showMoreColumns;
                break;
            }
            case 'group-f': {
                $scope.tableOps.groupfocus.showMoreColumns = !$scope.tableOps.groupfocus.showMoreColumns;
                break;
            }
            case 'app-f': {
                $scope.tableOps.appfocus.showMoreColumns = !$scope.tableOps.appfocus.showMoreColumns;
                break;
            }
            case 'app': {
                $scope.tableOps.app.showMoreColumns = !$scope.tableOps.app.showMoreColumns;
                break;
            }
            case 'group': {
                $scope.tableOps.group.showMoreColumns = !$scope.tableOps.group.showMoreColumns;
                break;
            }
            default:
                break;
        }
    };
    $scope.toggleShowOperations = function (type) {
        switch (type) {
            case 'policy': {
                $scope.tableOps.policy.showOperations = !$scope.tableOps.policy.showOperations;
                if ($scope.tableOps.policy.showOperations) {
                    $('#owned-traffic-table').bootstrapTable('showColumn', 'operation');
                } else {
                    $('#owned-traffic-table').bootstrapTable('hideColumn', 'operation');
                }
                break;
            }
            case 'policy-f': {
                $scope.tableOps.policyfocus.showOperations = !$scope.tableOps.policyfocus.showOperations;
                if ($scope.tableOps.policyfocus.showOperations) {
                    $('#focused-traffic-table').bootstrapTable('showColumn', 'operation');
                } else {
                    $('#focused-traffic-table').bootstrapTable('hideColumn', 'operation');
                }
                break;
            }
            case 'slb-f': {
                $scope.tableOps.slbfocus.showOperations = !$scope.tableOps.slbfocus.showOperations;
                if ($scope.tableOps.slbfocus.showOperations) {
                    $('#user-focusSlb-table').bootstrapTable('showColumn', 'operation');
                } else {
                    $('#user-focusSlb-table').bootstrapTable('hideColumn', 'operation');
                }
                break;
            }
            case 'vs-f': {
                $scope.tableOps.vsfocus.showOperations = !$scope.tableOps.vsfocus.showOperations;
                if ($scope.tableOps.vsfocus.showOperations) {
                    $('#user-focusVs-table').bootstrapTable('showColumn', 'operation');
                } else {
                    $('#user-focusVs-table').bootstrapTable('hideColumn', 'operation');
                }
                break;
            }
            case 'group-f': {
                $scope.tableOps.groupfocus.showOperations = !$scope.tableOps.groupfocus.showOperations;
                if ($scope.tableOps.groupfocus.showOperations) {
                    $('#user-focusGroup-table').bootstrapTable('showColumn', 'operation');
                } else {
                    $('#user-focusGroup-table').bootstrapTable('hideColumn', 'operation');
                }
                break;
            }
            case 'app-f': {
                $scope.tableOps.appfocus.showOperations = !$scope.tableOps.appfocus.showOperations;
                if ($scope.tableOps.appfocus.showOperations) {
                    $('#user-focusApp-table').bootstrapTable('showColumn', 'operation');
                } else {
                    $('#user-focusApp-table').bootstrapTable('hideColumn', 'operation');
                }
                break;
            }
            case 'app': {
                $scope.tableOps.app.showOperations = !$scope.tableOps.app.showOperations;
                if ($scope.tableOps.app.showOperations) {
                    $('#user-ownApp-table').bootstrapTable('showColumn', 'operation');
                } else {
                    $('#user-ownApp-table').bootstrapTable('hideColumn', 'operation');
                }
                break;
            }
            case 'group': {
                $scope.tableOps.group.showOperations = !$scope.tableOps.group.showOperations;
                if ($scope.tableOps.group.showOperations) {
                    $('#user-ownGroup-table').bootstrapTable('showColumn', 'operation');
                } else {
                    $('#user-ownGroup-table').bootstrapTable('hideColumn', 'operation');
                }
                break;
            }
            default:
                break;
        }
    };
    $scope.getOperationTitle = function (type) {
        switch (type) {
            case 'policy':
                return $scope.tableOps.policy.showOperations ? (angular.equals(resource, {}) ? '关闭操作': resource.user.js.msg3) : (angular.equals(resource, {}) ? '打开操作': resource.user.js.msg4);
            case 'policy-f':
                return $scope.tableOps.policyfocus.showOperations ? (angular.equals(resource, {}) ? '关闭操作': resource.user.js.msg3) : (angular.equals(resource, {}) ? '打开操作': resource.user.js.msg4);
            case 'slb-f':
                return $scope.tableOps.slbfocus.showOperations ? (angular.equals(resource, {}) ? '关闭操作': resource.user.js.msg3) : (angular.equals(resource, {}) ? '打开操作': resource.user.js.msg4);
            case 'group-f':
                return $scope.tableOps.slbfocus.showOperations ? (angular.equals(resource, {}) ? '关闭操作': resource.user.js.msg3) : (angular.equals(resource, {}) ? '打开操作': resource.user.js.msg4);
            case 'app-f':
                return $scope.tableOps.appfocus.showOperations ? (angular.equals(resource, {}) ? '关闭操作': resource.user.js.msg3) : (angular.equals(resource, {}) ? '打开操作': resource.user.js.msg4);
            case 'app':
                return $scope.tableOps.app.showOperations ? (angular.equals(resource, {}) ? '关闭操作': resource.user.js.msg3) : (angular.equals(resource, {}) ? '打开操作': resource.user.js.msg4);
            case 'group':
                return $scope.tableOps.app.showOperations ? (angular.equals(resource, {}) ? '关闭操作': resource.user.js.msg3) : (angular.equals(resource, {}) ? '打开操作': resource.user.js.msg4);
            default:
                break;
        }
    };

    $scope.canUserTagging = function () {
        var right = A.canDo('Group', 'PROPERTY', '*');
        return right;
    };

    $scope.batchUnfocusApps = function () {

    };
    $scope.batchFocusApps = function () {

    };

    $scope.disableUnFocusApp = function (type) {
        var selected = $('#user-focusApp-table').bootstrapTable('getSelections').length > 0;

        switch (type) {
            case 'focus':

                break;
        }
    };
    $scope.disableFocusApp = function () {

    };

    $scope.navigateTo = function (type) {
        var url = '';
        switch (type) {
            case 'slb': {
                url = '/portal/slbs#?env=' + G.env + '&tags=user_' + $scope.userInfo['name'];
                break;
            }
            case 'vs': {
                url = '/portal/vses#?env=' + G.env + '&tags=user_' + $scope.userInfo['name'];
                break;
            }
            case 'app': {
                url = '/portal/apps#?env=' + G.env + '&tags=user_' + $scope.userInfo['name'];
                break;
            }
            case 'group': {
                url = '/portal/groups#?env=' + G.env + '&groupTags=user_' + $scope.userInfo['name'] + ',owner_' + $scope.userInfo['name'];
                break;
            }
            case 'activated': {
                url = '/portal/groups#?env=' + G.env + '&groupTags=user_' + $scope.userInfo['name'] + ',owner_' + $scope.userInfo['name'] + '&groupStatus=已激活';
                break;
            }
            case 'tobeactivated': {
                url = '/portal/groups#?env=' + G.env + '&groupTags=user_' + $scope.userInfo['name'] + ',owner_' + $scope.userInfo['name'] + '&groupStatus=有变更';
                break;
            }
            case 'deactivated': {
                url = '/portal/groups#?env=' + G.env + '&groupTags=user_' + $scope.userInfo['name'] + ',owner_' + $scope.userInfo['name'] + '&groupStatus=未激活';
                break;
            }
            case 'healthy': {
                url = '/portal/groups#?env=' + G.env + '&groupTags=user_' + $scope.userInfo['name'] + ',owner_' + $scope.userInfo['name'] + '&groupHealthy=healthy:healthy&groupType=Group,V-Group';
                break;
            }
            case 'unhealthy': {
                url = '/portal/groups#?env=' + G.env + '&groupTags=user_' + $scope.userInfo['name'] + ',owner_' + $scope.userInfo['name'] + '&groupHealthy=healthy:unhealthy&groupType=Group,V-Group';
                break;
            }
            case 'broken': {
                url = '/portal/groups#?env=' + G.env + '&groupTags=user_' + $scope.userInfo['name'] + ',owner_' + $scope.userInfo['name'] + '&groupHealthy=healthy:Broken&groupType=Group,V-Group';
                break;
            }
            default: {
                break;
            }
        }
        return url;
    };
    $scope.isSelectedTab = function (tab) {
        if (tab == $scope.query.tab) return 'active';
    };
    $scope.showEmployeeId = function () {
        if ($scope.query.userId.length > 0) {
            return false;
        } else {
            return true;
        }
    };

    // Slb area
    $scope.initFocusSlbTable = function (paging) {
            var resource = $scope.resource;
        if (!paging) paging = false;
        $('#user-focusSlb-table').bootstrapTable({
            toolbar: "#userInfo-focusSlb-toolbar",
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
                    rowspan: 2,
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    width: '100px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '"href="/portal/slb#?env=' + G.env + '&slbId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'SLB',
                    align: 'left',
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '"href="/portal/slb#?env=' + G.env + '&slbId=' + row.id + '"><span style="word-break: break-all">' + value + '</span></a>';
                    }
                },
                {
                    field: 'qps',
                    title: 'QPS',
                    align: 'left',
                    valign: 'middle',
                    width: '270px',
                    sortable: true,
                    formatter: function (v, row, index) {
                        return v ? '<a target="_blank" href="/portal/slb/traffic' + H.generateHashStr({
                            env: G.env,
                            slbId: row.id
                        }) + '">' + Math.floor(v) + '</a>' : '-';
                    }
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    width: '250px',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var slbStatusCss = "";
                        if (!value)
                            return "-";
                        else {
                            switch (value) {
                                case '已激活':
                                    slbStatusCss = 'status-green';
                                    break;
                                case '未激活':
                                    slbStatusCss = 'status-gray';
                                    break;
                                case '有变更':
                                    slbStatusCss = 'status-yellow';
                                    break;
                                default:
                                    slbStatusCss = "status-gray";
                            }
                        }
                        if (value == '有变更')
                            return (angular.equals(resource, {}) ? '<span class="diffSLB status-yellow">有变更(<a data-toggle="modal" data-target="#diffModal">Diff</a>)</span>': resource.user.js.msg72);
                        else if (value === "已激活") {
                                                    return '<span class="' + groupStatusCss + '" ">' + (angular.equals(resource, {}) ? value : resource.user.js.msg81) + '</span>';
                                                } else {
                                                    return '<span class="' + groupStatusCss + '" ">' + (angular.equals(resource, {}) ? value : resource.user.js.msg108) + '</span>';
                                                }
                    }
                },
                {
                    field: 'operation',
                    title: 'Operation',
                    align: 'right',
                    valign: 'middle',
                    width: '60px',
                    visible: false,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        if ($scope.query.userId) {
                            if ($scope.loginUserInfo.slbs && $scope.loginUserInfo.slbs[row.id]) {
                                return (angular.equals(resource, {}) ? '<span class="unFocusSlb" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg73);
                            } else {
                                return (angular.equals(resource, {}) ? '<span class="focusSlb" title="关注" aria-label="Left Align"><a class="fa fa-eye"> 关注</a></span>': resource.user.js.msg74);
                            }
                        } else {
                            return (angular.equals(resource, {}) ? '<span class="unFocusSlb" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg73);
                        }
                    }
                },
                {
                    title: 'Links',
                    align: 'left',
                    valign: 'middle',
                    width: '50px',
                    formatter: function (value, row, index) {
                        var slbId = row.id;
                        var query = "slb_id%3D'" + slbId + "'";
                        var es = userApp.getEsHtml(query);
                        var str = '<div class="">' + es + '</div>'

                        return str;
                    }
                }

            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            data: $scope.userInfo.focusedSlbs,
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            sidePagination: 'client',
            pagination: paging,
            pageSize: 10,
            pageList: [10, 30, 50, 100],
            formatLoadingMessage: function () {
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你关注的SLB": resource.user.js.msg5);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 你还没有关注任何SLB': resource.user.js.msg6);
            }
        });
    };
    $scope.batchFocusSlbs = function () {
        var selected = $('#user-focusSlb-table').bootstrapTable('getSelections');
        var slbids = _.pluck(selected, 'id');
        $scope.showFocusDialog('Slb', slbids);
    };
    $scope.batchUnFocusSlbs = function () {
        var selected = $('#user-focusSlb-table').bootstrapTable('getSelections');
        var slbids = _.pluck(selected, 'id');
        $scope.showUnFocusDialog('Slb', slbids);
    };
    $('#user-focusSlb-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
            var selected = $('#user-focusSlb-table').bootstrapTable('getSelections');
            var s = selected.length > 0;
            var countBy = _.countBy(selected, function (r) {
                var v = $scope.loginUserInfo.slbs[r.id];
                if (v) return 'focused';
                return 'unfocused';
            });

            var f = false;
            var u = false;

            var v = _.keys(countBy);
            if (v.length == 1) {
                if (v[0] == 'focused') {
                    u = true;
                    f = false;
                } else {
                    u = false;
                    f = true;
                }

            }
            $('#user-focus-slb').prop('disabled', !(s && f));
            $('#user-unfocus-slb').prop('disabled', !(s && u));
        });

    // Vs area
    $scope.initFocusVsTable = function (paging) {
        var resource = $scope.resource;
        if (!paging) paging = false;

        $('#user-focusVs-table').bootstrapTable({
            toolbar: "#userInfo-focusVs-toolbar",
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
                    rowspan: 2,
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    width: '100px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '"href="/portal/vs#?env=' + G.env + '&vsId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'domains',
                    title: (angular.equals(resource, {}) ? 'Domain列表': resource.user.js.msg7),
                    align: 'left',
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var domains = "";
                        $.each(value, function (i, val) {
                            domains += '<a class="editVS" target="_blank" title="' + val.name + '" href="/portal/vs#?env=' + G.env + '&vsId=' + row.id + '" style="text-decoration: none; margin-left: 5px">' + val.name + '</a><br>'
                        });
                        return domains;
                    }
                },
                {
                    field: 'ssl',
                    title: 'SSL',
                    align: 'left',
                    valign: 'middle',
                    width: '200px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value)
                            return "Https";
                        else
                            return "Http";
                    }
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    width: '170px',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var vsStatusCss = "";
                        if (!value)
                            return "-";
                        else {
                            switch (value) {
                                case "已激活":
                                    vsStatusCss = 'status-green';
                                    break;
                                case "未激活":
                                    vsStatusCss = 'status-gray';
                                    break;
                                case "有变更":
                                    vsStatusCss = 'status-yellow';
                                    break;
                                default:
                                    vsStatusCss = "status-gray";
                            }
                        }

                        if (value == '有变更')
                            return (angular.equals(resource, {}) ? '<span class="diffVS status-yellow">有变更(<a data-toggle="modal" data-target="#diffModal">Diff</a>)</span>': resource.user.js.msg8);
                        else if (value === "已激活") {
                            return '<span class="' + vsStatusCss + '" ">' + (angular.equals(resource, {}) ? value : resource.user.js.msg81) + '</span>';
                        } else {
                            return '<span class="' + vsStatusCss + '" ">' + (angular.equals(resource, {}) ? value : resource.user.js.msg108) + '</span>';
                        }
                    }
                },
                {
                    field: 'qps',
                    title: 'QPS',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (v, row, index) {
                        return v ? '<a target="_blank" href="/portal/vs/traffic' + H.generateHashStr({
                            env: G.env,
                            vsId: row.id
                        }) + '">' + Math.floor(v) + '</a>' : '-';
                    }
                },
                {
                    field: 'operation',
                    title: 'Operation',
                    align: 'right',
                    valign: 'middle',
                    width: '60px',
                    visible: false,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        if ($scope.query.userId) {
                            if ($scope.loginUserInfo.vses && $scope.loginUserInfo.vses[row.id]) {
                                return (angular.equals(resource, {}) ? '<span class="unFocusVs" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg11);
                            } else {
                                return (angular.equals(resource, {}) ? '<span class="focusVs" title="关注" aria-label="Left Align"><a class="fa fa-eye"> 关注</a></span>': resource.user.js.msg12);
                            }
                        } else {
                            return (angular.equals(resource, {}) ? '<span class="unFocusVs" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg11);
                        }
                    }
                },
                {
                    title: 'Links',
                    align: 'left',
                    valign: 'middle',
                    width: '80px',
                    formatter: function (value, row, index) {
                        var domains = row['domains'];

                        var idc = row.idc;
                        var domainarray = _.map(_.pluck(domains, 'name'), function (v) {
                            return "'" + v + "'";
                        });
                        var query = 'domain%3D(' + domainarray.join('%20OR%20') + ')';
                        var idc_english = userApp.convertIDC(idc);
                        query += '%20AND%20idc:' + idc_english;

                        var es = userApp.getEsHtml(query);
                        var str = '<div class="">' + es + '</div>'
                        return str;
                    }
                }
            ], []],

            search: true,
            showRefresh: true,
            showColumns: true,
            data: $scope.userInfo.focusedVses,
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            sidePagination: 'client',
            pagination: paging,
            pageSize: 10,
            pageList: [10, 30, 50, 100],
            formatLoadingMessage: function () {
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你关注的VS": resource.user.js.msg15);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 你还没有关注任何VS': resource.user.js.msg13);
            }
        });
    };
    $scope.batchFocusVs = function () {
        var selected = $('#user-focusVs-table').bootstrapTable('getSelections');
        var vsids = _.pluck(selected, 'id');
        $scope.showFocusDialog('Vs', vsids);
    };
    $scope.batchUnFocusVs = function () {
        var selected = $('#user-focusVs-table').bootstrapTable('getSelections');
        var vsids = _.pluck(selected, 'id');
        $scope.showUnFocusDialog('Vs', vsids);
    };
    $('#user-focusVs-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
            var selected = $('#user-focusVs-table').bootstrapTable('getSelections');
            var s = selected.length > 0;
            var countBy = _.countBy(selected, function (r) {
                var v = $scope.loginUserInfo.vses[r.id];
                if (v) return 'focused';
                return 'unfocused';
            });

            var f = false;
            var u = false;

            var v = _.keys(countBy);
            if (v.length == 1) {
                if (v[0] == 'focused') {
                    u = true;
                    f = false;
                } else {
                    u = false;
                    f = true;
                }
            }
            $('#user-focus-vs').prop('disabled', !(s && f));
            $('#user-unfocus-vs').prop('disabled', !(s && u));
        });

    // Group area
    $scope.initOwnGroupTable = function (name, toolbar, type, paging) {
            var resource = $scope.resource;

        if (!paging) paging = false;

        $(name).bootstrapTable({
            toolbar: toolbar,
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
                    rowspan: 2,
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    width: '100px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '"href="/portal/group#?env=' + G.env + '&groupId=' + value + '">' + value + '</a>';
                    }
                }, {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '" href="/portal/group#?env=' + G.env + '&groupId=' + row.id + '"><div style="word-break: break-all">' + value + '</div></a>';
                    }
                },
                {
                    field: 'app',
                    title: 'App',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value == 'VirtualGroup(-)') {
                            return 'VirtualGroup(-)';
                        }
                        return '<a target="_blank" title="' + value + '" href="/portal/app#?env=' + G.env + '&appId=' + row['app-id'] + '"><div style="word-break: break-all">' + value + '</div></a>';
                    }
                },
                {
                    field: 'idc',
                    title: 'IDC',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value) return value;
                    }
                },
                {
                    field: 'qps',
                    title: 'QPS',
                    align: 'left',
                    valign: 'middle',
                    width: '150px',
                    sortable: true,
                    formatter: function (v, row, index) {
                        return v ? '<a target="_blank" href="/portal/group/traffic' + H.generateHashStr({
                            env: G.env,
                            groupId: row.id
                        }) + '">' + Math.floor(v) + '</a>' : '-';
                    }
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var groupStatusCss = "";
                        if (!value)
                            return "-";
                        else {
                            switch (value) {
                                case '已激活':
                                    groupStatusCss = 'status-green';
                                    break;
                                case '未激活':
                                    groupStatusCss = 'status-gray';
                                    break;
                                case '有变更':
                                    groupStatusCss = 'status-yellow';
                                    break;
                                default:
                                    groupStatusCss = "status-gray";
                            }
                        }
                        if (value == '有变更')
                            return (angular.equals(resource, {}) ? '<span class="diffGroup status-yellow">有变更(<a data-toggle="modal" data-target="#diffModal">Diff</a>)</span>': resource.user.js.msg29);
                        else if (value === "已激活") {
                            return '<span class="' + groupStatusCss + '" ">' + (angular.equals(resource, {}) ? value : resource.user.js.msg81) + '</span>';
                        } else {
                            return '<span class="' + groupStatusCss + '" ">' + (angular.equals(resource, {}) ? value : resource.user.js.msg108) + '</span>';
                        }
                    }
                },
                {
                    field: 'grouphealthy',
                    title: 'Healthy',
                    valign: 'middle',
                    align: 'center',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var str = '';
                        switch (value) {
                            case "healthy":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + (angular.equals(resource, {}) ? '"><span class="fa fa-circle status-green" title="完全健康"></span></a>': resource.user.js.msg17);
                                break;
                            case "broken":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + (angular.equals(resource, {}) ? '"><span class="fa fa-circle status-red" title="全部失败"></span></a>': resource.user.js.msg18);
                                break;
                            case "unhealthy":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + (angular.equals(resource, {}) ? '"><span class="fa fa-circle status-yellow" title="存在部分问题Server"></span></a>': resource.user.js.msg19);
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    }
                },
                {
                    field: 'operation',
                    title: 'Operation',
                    align: 'right',
                    valign: 'middle',
                    width: '60px',
                    visible: false,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        if ($scope.query.userId) {
                            if ($scope.loginUserInfo.groups && $scope.loginUserInfo.groups[row.id]) {
                                return (angular.equals(resource, {}) ? '<span class="unFocusGroup" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg21);
                            } else {
                                return (angular.equals(resource, {}) ? '<span class="focusGroup" title="关注" aria-label="Left Align"><a class="fa fa-eye"> 关注</a></span>': resource.user.js.msg22);
                            }
                        } else {
                            return (angular.equals(resource, {}) ? '<span class="unFocusGroup" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg21);
                        }
                    }
                },
                {
                    title: 'Links',
                    align: 'left',
                    valign: 'middle',
                    width: '80px',
                    formatter: function (value, row, index) {
                        var appId = row['id'];
                        var query = "group_id%3D'" + appId + "'";
                        var es = userApp.getEsHtml(query);
                        var str = '<div class="">' + es + '</div>'
                        return str;
                    }
                }

            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            sidePagination: 'client',
            pagination: paging,
            pageSize: 10,
            pageList: [10, 30, 50, 100],
            formatLoadingMessage: function () {
                if (type == 'focus')
                    return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你关注的Group": resource.user.js.msg25);
                if (type == 'own')
                    return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你的Group": resource.user.js.msg26);

            },
            formatNoMatches: function () {
                if (type == 'focus')
                    return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i>你还没有关注任何Group': resource.user.js.msg28);
                if (type == 'own')
                    return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i>你还没有任何Group': resource.user.js.msg27);
            }
        });
    };
    $scope.batchFocusOwnGroup = function () {
        var selected = $('#user-ownGroup-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'id');
        $scope.showFocusDialog('Group', ids);
    };
    $scope.batchUnFocusOwnGroup = function () {
        var selected = $('#user-ownGroup-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'id');
        $scope.showUnFocusDialog('Group', ids);
    };
    $('#user-ownGroup-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
            var selected = $('#user-ownGroup-table').bootstrapTable('getSelections');
            var s = selected.length > 0;
            var countBy = _.countBy(selected, function (r) {
                var v = $scope.loginUserInfo.groups[r.id];
                if (v) return 'focused';
                return 'unfocused';
            });

            var f = false;
            var u = false;

            var v = _.keys(countBy);
            if (v.length == 1) {
                if (v[0] == 'focused') {
                    u = true;
                    f = false;
                } else {
                    u = false;
                    f = true;
                }

            }
            $('#user-focus-own-group').prop('disabled', !(s && f));
            $('#user-unfocus-own-group').prop('disabled', !(s && u));
        });

    $scope.initFocusGroupTable = function (name, toolbar, type, paging) {
            var resource = $scope.resource;

        if (!paging) paging = false;
        $(name).bootstrapTable({
            toolbar: toolbar,
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
                    rowspan: 2,
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    width: '100px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '"href="/portal/group#?env=' + G.env + '&groupId=' + value + '">' + value + '</a>';
                    }
                }, {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '" href="/portal/group#?env=' + G.env + '&groupId=' + row.id + '"><div style="word-break: break-all">' + value + '</div></a>';
                    }
                },
                {
                    field: 'app',
                    title: 'App',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value == 'VirtualGroup(-)') {
                            return 'VirtualGroup(-)';
                        }
                        return '<a target="_blank" title="' + value + '" href="/portal/app#?env=' + G.env + '&appId=' + row['app-id'] + '"><div style="word-break: break-all">' + value + '</div></a>';
                    }
                },
                {
                    field: 'idc',
                    title: 'IDC',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value) return value;
                    }
                },
                {
                    field: 'qps',
                    title: 'QPS',
                    align: 'left',
                    valign: 'middle',
                    width: '150px',
                    sortable: true,
                    formatter: function (v, row, index) {
                        return v ? '<a target="_blank" href="/portal/group/traffic' + H.generateHashStr({
                            env: G.env,
                            groupId: row.id
                        }) + '">' + Math.floor(v) + '</a>' : '-';
                    }
                },
                {
                    field: 'status',
                    title: 'Status',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        var groupStatusCss = "";
                        if (!value)
                            return "-";
                        else {
                            switch (value) {
                                case '已激活':
                                    groupStatusCss = 'status-green';
                                    break;
                                case '未激活':
                                    groupStatusCss = 'status-gray';
                                    break;
                                case '有变更':
                                    groupStatusCss = 'status-yellow';
                                    break;
                                default:
                                    groupStatusCss = "status-gray";
                            }
                        }
                        if (value == '有变更')
                            return (angular.equals(resource, {}) ? '<span class="diffGroup status-yellow">有变更(<a data-toggle="modal" data-target="#diffModal">Diff</a>)</span>': resource.user.js.msg29);
                        else if (value === "已激活") {
                                                    return '<span class="' + groupStatusCss + '" ">' + (angular.equals(resource, {}) ? value : resource.user.js.msg81) + '</span>';
                                                } else {
                                                    return '<span class="' + groupStatusCss + '" ">' + (angular.equals(resource, {}) ? value : resource.user.js.msg108) + '</span>';
                                                }
                    }
                },
                {
                    field: 'grouphealthy',
                    title: 'Healthy',
                    valign: 'middle',
                    align: 'center',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var str = '';
                        switch (value) {
                            case "healthy":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + (angular.equals(resource, {}) ? '"><span class="fa fa-circle status-green" title="完全健康"></span></a>': resource.user.js.msg17);
                                break;
                            case "broken":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + (angular.equals(resource, {}) ? '"><span class="fa fa-circle status-red" title="全部失败"></span></a>': resource.user.js.msg18);
                                break;
                            case "unhealthy":
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + row.id + (angular.equals(resource, {}) ? '"><span class="fa fa-circle status-yellow" title="存在部分问题Server"></span></a>': resource.user.js.msg19);
                                break;
                            default:
                                str = "-";
                                break;
                        }
                        return str;
                    }
                },
                {
                    field: 'operation',
                    title: 'Operation',
                    align: 'right',
                    visible: false,
                    valign: 'middle',
                    width: '60px',
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        if ($scope.query.userId) {
                            if ($scope.loginUserInfo.groups && $scope.loginUserInfo.groups[row.id]) {
                                return (angular.equals(resource, {}) ? '<span class="unFocusGroup" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg21);
                            } else {
                                return (angular.equals(resource, {}) ? '<span class="focusGroup" title="关注" aria-label="Left Align"><a class="fa fa-eye"> 关注</a></span>': resource.user.js.msg22);
                            }
                        } else {
                            return (angular.equals(resource, {}) ? '<span class="unFocusGroup" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg21);
                        }
                    }
                },
                {
                    title: 'Links',
                    align: 'left',
                    valign: 'middle',
                    width: '80px',
                    formatter: function (value, row, index) {
                        var appId = row['id'];
                        var query = "group_id%3D'" + appId + "'";
                        var es = userApp.getEsHtml(query);
                        var str = '<div class="">' + es + '</div>'
                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            sidePagination: 'client',
            pagination: paging,
            pageSize: 10,
            pageList: [10, 30, 50, 100],
            formatLoadingMessage: function () {
                if (type == 'focus')
                    return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你关注的Group": resource.user.js.msg25);
                if (type == 'own')
                    return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你的Group": resource.user.js.msg26);

            },
            formatNoMatches: function () {
                if (type == 'focus')
                    return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i>你还没有关注任何Group': resource.user.js.msg28);
                if (type == 'own')
                    return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i>你还没有任何Group': resource.user.js.msg27);
            }
        });
    };
    $scope.batchFocusGroup = function () {
        var selected = $('#user-focusGroup-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'id');
        $scope.showFocusDialog('Group', ids);
    };
    $scope.batchUnFocusGroup = function () {
        var selected = $('#user-focusGroup-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'id');
        $scope.showUnFocusDialog('Group', ids);
    };
    $('#user-focusGroup-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
            var selected = $('#user-focusGroup-table').bootstrapTable('getSelections');
            var s = selected.length > 0;
            var countBy = _.countBy(selected, function (r) {
                var v = $scope.loginUserInfo.groups[r.id];
                if (v) return 'focused';
                return 'unfocused';
            });

            var f = false;
            var u = false;

            var v = _.keys(countBy);
            if (v.length == 1) {
                if (v[0] == 'focused') {
                    u = true;
                    f = false;
                } else {
                    u = false;
                    f = true;
                }
            }
            $('#user-focus-group').prop('disabled', !(s && f));
            $('#user-unfocus-group').prop('disabled', !(s && u));
        });

    // App area
    $scope.initOwnAppTable = function (name, toolbar, type, paging) {
            var resource = $scope.resource;

        if (!paging) paging = false;

        $(name).bootstrapTable({
            toolbar: toolbar,
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
                    rowspan: 2,
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'app-id',
                    title: 'AppId',
                    align: 'left',
                    valign: 'middle',
                    width: '100px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '"href="/portal/app#?env=' + G.env + '&appId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'chinese-name',
                    title: 'AppName',
                    align: 'left',
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="' + value + '"href="/portal/app#?env=' + G.env + '&appId=' + row['app-id'] + '">' + value + '</a>';
                    }
                },
                {
                    field: 'owner',
                    title: 'Owner',
                    align: 'left',
                    valign: 'middle',
                    width: '120px',
                    formatter: function (value, row, index) {
                        if (!value || value == "unknown")
                            return "-";
                        return '<a href=mailto:' + row['owner-email'] + '>' + value + '</a>';
                    }
                },
                {
                    field: 'groupCount',
                    title: 'Groups',
                    align: 'left',
                    valign: 'middle',
                    width: '120px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return '<a href="' + '/portal/groups#?env=' + G.env + '&appId=' + row['app-id'] + '">' + value + '</a>';
                    }
                },
                {
                    field: 'qps',
                    title: 'QPS',
                    align: 'left',
                    valign: 'middle',
                    width: '150px',
                    sortable: true,
                    formatter: function (v, row, index) {
                        return v ? '<a target="_blank" href="/portal/app/traffic' + H.generateHashStr({
                            env: G.env,
                            appId: row['app-id']
                        }) + '">' + Math.floor(v) + '</a>' : '-';
                    }
                },
                {
                    field: 'sbu',
                    title: 'SBU',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return '<a href="' + '/portal/bu#?env=' + G.env + '&buName=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'operation',
                    title: 'Operation',
                    align: 'right',
                    valign: 'middle',
                    width: '60px',
                    visible: false,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        if ($scope.query.userId) {
                            if ($scope.loginUserInfo.apps && $scope.loginUserInfo.apps[row['app-id']]) {
                                return (angular.equals(resource, {}) ? '<span class="unFocusApp" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg76);
                            } else {
                                return (angular.equals(resource, {}) ? '<span class="focusApp" title="关注" aria-label="Left Align"><a class="fa fa-eye"> 关注</a></span>': resource.user.js.msg77);
                            }
                        } else {
                            return (angular.equals(resource, {}) ? '<span class="unFocusApp" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg76);
                        }
                    }
                },
                {
                    title: 'Links',
                    align: 'left',
                    valign: 'middle',
                    width: '50px',
                    formatter: function (value, row, index) {
                        var appId = row['app-id'];
                        var query = "group_appid%3D'" + appId + "'";
                        var es = userApp.getEsHtml(query);
                        var str = '<div class="">' + es + '</div>'
                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            sidePagination: 'client',
            pagination: paging,
            pageSize: 10,
            pageList: [10, 30, 50, 100],
            formatLoadingMessage: function () {
                if (type == 'focus')
                    return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你关注的App": resource.user.js.msg35);
                if (type == 'own')
                    return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你的App": resource.user.js.msg36);

            },
            formatNoMatches: function () {
                if (type == 'focus')
                    return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i>你还没有关注任何App': resource.user.js.msg37);
                if (type == 'own')
                    return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i>你还没有任何App': resource.user.js.msg78);
            }
        });
    };
    $scope.batchFocusOwnApp = function () {
        var selected = $('#user-ownApp-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'app-id');
        $scope.showFocusDialog('App', ids);
    };
    $scope.batchUnFocusOwnApp = function () {
        var selected = $('#user-ownApp-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'app-id');
        $scope.showUnFocusDialog('App', ids);
    };
    $('#user-ownApp-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
            var selected = $('#user-ownApp-table').bootstrapTable('getSelections');
            var s = selected.length > 0;
            var countBy = _.countBy(selected, function (r) {
                var v = $scope.loginUserInfo.apps[r['app-id']];
                if (v) return 'focused';
                return 'unfocused';
            });

            var f = false;
            var u = false;

            var v = _.keys(countBy);
            if (v.length == 1) {
                if (v[0] == 'focused') {
                    u = true;
                    f = false;
                } else {
                    u = false;
                    f = true;
                }

            }
            $('#user-focus-own-app').prop('disabled', !(s && f));
            $('#user-unfocus-own-app').prop('disabled', !(s && u));
        });

    $scope.initFocusAppTable = function (name, toolbar, type, paging) {
            var resource = $scope.resource;

        if (!paging) paging = false;

        $(name).bootstrapTable({
            toolbar: toolbar,
            columns: [[
                {
                    field: 'state',
                    checkbox: true,
                    rowspan: 2,
                    align: 'center',
                    valign: 'middle'
                },
                {
                    field: 'app-id',
                    title: 'AppId',
                    align: 'left',
                    valign: 'middle',
                    width: '100px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a target="_blank" title="' + value + '"href="/portal/app#?env=' + G.env + '&appId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'chinese-name',
                    title: 'AppName',
                    align: 'left',
                    valign: 'middle',
                    width: '400px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return '<a title="' + value + '"href="/portal/app#?env=' + G.env + '&appId=' + row['app-id'] + '">' + value + '</a>';
                    }
                },
                {
                    field: 'owner',
                    title: 'Owner',
                    align: 'left',
                    valign: 'middle',
                    width: '120px',
                    formatter: function (value, row, index) {
                        if (!value || value == "unknown")
                            return "-";
                        return '<a href=mailto:' + row['owner-email'] + '>' + value + '</a>';
                    }
                },
                {
                    field: 'groupCount',
                    title: 'Groups',
                    align: 'left',
                    valign: 'middle',
                    width: '120px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return '<a href="' + '/portal/groups#?env=' + G.env + '&appId=' + row['app-id'] + '">' + value + '</a>';
                    }
                },
                {
                    field: 'qps',
                    title: 'QPS',
                    align: 'left',
                    valign: 'middle',
                    width: '150px',
                    sortable: true,
                    formatter: function (v, row, index) {
                        return v ? '<a target="_blank" href="/portal/app/traffic' + H.generateHashStr({
                            env: G.env,
                            appId: row['app-id']
                        }) + '">' + Math.floor(v) + '</a>' : '-';
                    }
                },
                {
                    field: 'sbu',
                    title: 'SBU',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (!value)
                            return '-';
                        return '<a href="' + '/portal/bu#?env=' + G.env + '&buName=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'operation',
                    title: 'Operation',
                    align: 'right',
                    valign: 'middle',
                    visible: false,
                    events: operateEvents,
                    formatter: function (value, row, index) {
                        if ($scope.query.userId) {
                            if ($scope.loginUserInfo.apps && $scope.loginUserInfo.apps[row['app-id']]) {
                                return (angular.equals(resource, {}) ? '<span class="unFocusApp" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg76);
                            } else {
                                return (angular.equals(resource, {}) ? '<span class="focusApp" title="关注" aria-label="Left Align"><a class="fa fa-eye"> 关注</a></span>': resource.user.js.msg77);
                            }
                        } else {
                            return (angular.equals(resource, {}) ? '<span class="unFocusApp" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg76);
                        }
                    }
                },
                {
                    title: 'Links',
                    align: 'left',
                    valign: 'middle',
                    width: '430px',
                    formatter: function (value, row, index) {
                        var env = $scope.env;
                        var appId = row['app-id'];
                        var query = "group_appid%3D'" + appId + "'";

                        var startTime = new Date().getTime() - 60 * 1000;
                        var endTime = new Date().getTime();
                        startTime = $.format.date(startTime, 'yyyy-MM-dd_HH:mm:ss');
                        endTime = $.format.date(endTime, 'yyyy-MM-dd_HH:mm:ss');


                        var hickwall = '';
                        if (G[env] && G[env].urls.hickwall) {
                            hickwall = '<div class="system-link">' +
                                '<a class="pull-left hickwall" title="Hickwall" target="_blank" href="' + G[G.env].urls.hickwall + '/d/CfHEabRZk/slb-app?&var-group_appid=' + appId + '">Hickwall</a>' +
                                '</div>';
                        }

                        var es = userApp.getEsHtml(query);

                        var cms = '';
                        if (G[env] && G[env].urls.cms) {
                            cms = '<div class="system-link">' +
                                '<a class="pull-left cms" title="CMS" target="_blank" href="' + G[$scope.env].urls.cms + '/#/app/?q=%7B%22appId$wildcard$%22:%22' + appId + '%22%7D">CMS</a>' +
                                '</div>';
                        }

                        var cat = '';
                        if (G[env] && G[env].urls.cat) {
                            cat = '<div class="system-link">' +
                                '<a class="pull-right cat" target="_blank" href="' + G[$scope.env].urls.cat + '/cat/r/t?op=view&domain=' + appId + '">CAT</a>' +
                                '</div>';
                        }

                        var clog = '';
                        if (G[env] && G[env].urls.clog) {
                            clog = '<div class="system-link">' +
                                '<a class="pull-right clog" target="_blank" href="' + G[$scope.env].urls.clog + '?fromDate=' + startTime + '~toDate=' + endTime + '~app=' + appId + '">Clog</a>' +
                                '</div>';
                        }

                        var str = '<div class="">' + hickwall + cms + cat + es + clog + '</div>';
                        return str;
                    }
                }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortOrder: 'desc',
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            idField: 'id',
            sidePagination: 'client',
            pagination: paging,
            pageSize: 10,
            pageList: [10, 30, 50, 100],
            formatLoadingMessage: function () {
                if (type == 'focus')
                    return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你关注的App": resource.user.js.msg35);
                if (type == 'own')
                    return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载你的App": resource.user.js.msg36);

            },
            formatNoMatches: function () {
                if (type == 'focus')
                    return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i>你还没有关注任何App': resource.user.js.msg37);
                if (type == 'own')
                    return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i>你还没有任何App': resource.user.js.msg78);
            }
        });
    };
    $scope.batchFocusFocusApp = function () {
        var selected = $('#user-focusApp-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'app-id');
        $scope.showFocusDialog('App', ids);
    };
    $scope.batchUnFocusFocusApp = function () {
        var selected = $('#user-focusApp-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'app-id');
        $scope.showUnFocusDialog('App', ids);
    };
    $('#user-focusApp-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
            var selected = $('#user-focusApp-table').bootstrapTable('getSelections');
            var s = selected.length > 0;
            var countBy = _.countBy(selected, function (r) {
                var v = $scope.loginUserInfo.apps[r['app-id']];
                if (v) return 'focused';
                return 'unfocused';
            });

            var f = false;
            var u = false;

            var v = _.keys(countBy);
            if (v.length == 1) {
                if (v[0] == 'focused') {
                    u = true;
                    f = false;
                } else {
                    u = false;
                    f = true;
                }
            }
            $('#user-focus-app').prop('disabled', !(s && f));
            $('#user-unfocus-app').prop('disabled', !(s && u));
        });

    // Policy area
    $scope.initPolicyTable = function (size) {
            var resource = $scope.resource;

        $('#owned-traffic-table').bootstrapTable({
            toolbar: "#toolbar",
            columns: [[{
                field: 'state',
                checkbox: true,
                rowspan: 2,
                align: 'center',
                valign: 'middle'
            },
            {
                field: 'id',
                title: 'ID',
                align: 'left',
                valign: 'middle',
                sortable: true,
                formatter: function (value, row, index) {
                    var trafficPageUrl = "";
                    trafficPageUrl = '/portal/policy#?env=' + G.env + '&policyId=' + value;
                    return '<a title="' + value + '" href="' + trafficPageUrl + '">' + value + '</a>';
                }
            },
            {
                field: 'name',
                title: 'Name',
                align: 'left',
                resetWidth: true,
                valign: 'middle',
                width: '400px',
                sortable: true,
                formatter: function (value, row, index) {
                    var trafficPageUrl = '/portal/policy#?env=' + G.env + '&policyId=' + row.id;
                    return '<a title="' + value + '" href="' + trafficPageUrl + '"><div style="word-break: break-all">' + value + '</div></a>';
                }
            },
            {
                field: 'paths',
                title: 'Path | Priority | SSL | VS | SLB',
                width: '600px',
                align: 'left',
                valign: 'middle',
                sortName: 'pathOrder',
                sortable: true,
                visible: false,
                formatter: function (value, row, index) {
                    var result = '';
                    $.each(value, function (i, v) {
                        var p = v.path;
                        if (v.path.length > 25) {
                            p = v.path.substring(0, 22);
                            p = p + "...";
                        }
                        var pathStr = '';
                        var priorityStr = '';
                        var sslStr = '';
                        var vsIdStr = '';
                        var slbIdStr = '';
                        if (v.path) {
                            pathStr = '<span class="">' + p + '</span>';
                            priorityStr = '&nbsp;<span class="">' + v.priority + '</span>';
                            sslStr = '&nbsp;<span class="">' + (v.ssl ? 'https' : 'http') + '</span>';
                            vsIdStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/vs#?env=' + G.env + '&vsId=' + v.vsId + '" title="' + v.domain + '">' + v.vsId + '</a>';
                            slbIdStr += '(';
                            $.each(v.slbId, function (i, slbId) {
                                if (i == v.slbId.length - 1) {
                                    slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>';
                                } else {
                                    slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>,';
                                }
                            });
                            slbIdStr += ')';

                            var tempStr = "" +
                                "<div class='col-md-6' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;color:#196eaa;font-weight:bold;' title='" + v.path + "'>" +
                                pathStr +
                                "</div>" +
                                '<div class="col-md-6" style="padding:0;line-height:34px;">' +
                                '<div class="col-md-3" style="padding:0;line-height:34px;">' + priorityStr + '</div>' +
                                '<div class="col-md-3" style="padding:0;line-height:34px;">' + sslStr + '</div>' +
                                '<div class="col-md-3" style="padding:0;line-height:34px;">' + vsIdStr + '</div>' +
                                '<div class="col-md-3" style="padding:0;line-height:34px;">' + slbIdStr + '</div>' +
                                '</div>';
                            result = result + '<div class="row" style="margin:0;">' + tempStr + '</div>';
                        }
                    });
                    result = '<div class="row" style="margin:0">' + result + '</div>';
                    return result;
                }
            },
            {
                field: 'controls2',
                title: 'Group | App',
                align: 'left',
                valign: 'middle',
                events: operateEvents,
                visible: true,
                formatter: function (value, row, index) {
                    var result = '';

                    $.each(value, function (i, v) {
                        var p = v.groupName;
                        /* if (v.groupName.length > 25) {
                         p = v.groupName.substring(0, 22);
                         p = p + "...";
                         }*/
                        var appStr = '';
                        var appId = $scope.groups[v.group.id]['app-id'];
                        appStr += appId;
                        var app = $scope.apps[appId];
                        if (app) {
                            appStr += '(' + app['chinese-name'] + ')';
                        } else {
                            appStr += '(-)';
                        }
                        var link = '/portal/group#?env=' + $scope.env + '&groupId=' + v.group.id;
                        var appLink = '/portal/app#?env=' + $scope.env + '&appId=' + appId;

                        result += '<div style="padding-top: 10px"><a href="' + link + '">' + v.group.id + '</a>' + '/' + '<a href="' + link + '">' + p + '</a>' + '/' + '<a href="' + appLink + '">' + appStr + '</a></div>';

                    });
                    result = '<div class="row" style="margin:0">' + result + '</div>';
                    return result;
                },
                sortable: true
            },
            {
                field: 'controls1',
                title: 'Group ID | Group Name | IDC | Status | Owner',
                align: 'left',
                width: '600px',
                valign: 'middle',
                events: operateEvents,
                visible: false,
                formatter: function (value, row, index) {
                    var result = '';
                    $.each(value, function (i, v) {
                        var p = v.groupName;
                        if (v.groupName.length > 25) {
                            p = v.groupName.substring(0, 22);
                            p = p + "...";
                        }
                        var groupStr = '<span class="">' + p + '</span>';
                        var idcStr = '&nbsp;<span class="">' + v.idc + '</span>';
                        var statusStr = '&nbsp;<span class="">' + v.status + '</span>';
                        var ownerStr = 'N/A';
                        if (v.owner != 'N/A')
                            ownerStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/user#?env=' + G.env + '&userId=' + v.owner + '" title="' + v.owner + '">' + v.owner + '</a>';

                        var tempStr =
                            "<div class='col-md-6' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;' title='" + v.groupName + "'><a title='" + v.groupName + "' target='blank' href='/portal/group#?env=" + G.env + "&groupId=" + v.group.id + "'>" +
                            "<div class='col-md-3'>" + v.group.id + "</div>" +
                            "<div class='col-md-9'>" + groupStr + "</div>" +
                            "</a></div>" +
                            '<div class="col-md-6" style="padding:0;line-height:34px;">' +
                            '<div class="col-md-4" style="padding:0;line-height:34px;">' + idcStr + '</div>' +
                            '<div class="col-md-4" style="padding:0;line-height:34px;">' + statusStr + '</div>' +
                            '<div class="col-md-4" style="padding:0;line-height:34px;">' + ownerStr + '</div>' +
                            '</div>';
                        result = result + '<div class="row" style="margin:0;">' + tempStr + '</div>';
                    });
                    result = '<div class="row" style="margin:0">' + result + '</div>';
                    return result;
                },
                sortable: true
            },
            {
                field: 'target',
                title: 'Type',
                align: 'left',
                valign: 'middle',
                width: '200px',
                visible: false,
                sortable: true
            },
            {
                field: 'idc',
                title: 'IDC',
                align: 'left',
                valign: 'middle',
                sortable: true
            },
            {
                field: 'status',
                title: 'Status',
                align: 'left',
                events: operateEvents,
                valign: 'middle',
                width: '152px',
                formatter: function (value, row, index) {
                    var str = "";
                    switch (value) {
                        case "deactivated":
                            str = (angular.equals(resource, {}) ? "<span class='status-gray'>未激活</span>": resource.user.js.msg39);
                            break;
                        case "activated":
                            str = (angular.equals(resource, {}) ? "<span class='status-green'>已激活</span>": resource.user.js.msg42);
                            break;
                        case "toBeActivated":
                            str = (angular.equals(resource, {}) ? "<span class='diffTraffic status-yellow'>有变更(<a data-toggle='modal' data-target='#diffModal'>Diff</a>)</span>": resource.user.js.msg41);
                            break;
                        default:
                            str = "-";
                            break;
                    }
                    return str;
                },
                events: operateEvents,
                sortable: true
            },
            {
                field: 'operation',
                title: 'Operation',
                align: 'right',
                valign: 'middle',
                width: '60px',
                visible: false,
                events: operateEvents,
                formatter: function (value, row, index) {
                    if ($scope.query.userId) {
                        if ($scope.loginUserInfo.policies && $scope.loginUserInfo.policies[row.id]) {
                            return (angular.equals(resource, {}) ? '<span class="unFocusPolicy" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg79);
                        } else {
                            return (angular.equals(resource, {}) ? '<span class="focusPolicy" title="关注" aria-label="Left Align"><a class="fa fa-eye"> 关注</a></span>': resource.user.js.msg80);
                        }
                    } else {
                        return (angular.equals(resource, {}) ? '<span class="unFocusPolicy" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg79);
                    }
                }
            }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'id',
            sortOrder: 'desc',
            data: $scope.ownedTraffics,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pagination: $scope.ownedTraffics.length > size,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Traffic Policies": resource.user.js.msg43);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Traffic Policies': resource.user.js.msg44);
            }
        });
        $('#focused-traffic-table').bootstrapTable({
            toolbar: "#toolbar2",
            columns: [[{
                field: 'state',
                checkbox: true,
                rowspan: 2,
                align: 'center',
                valign: 'middle'
            },
            {
                field: 'id',
                title: 'ID',
                align: 'left',
                valign: 'middle',
                sortable: true,
                formatter: function (value, row, index) {
                    var trafficPageUrl = "";
                    trafficPageUrl = '/portal/policy#?env=' + G.env + '&policyId=' + value;
                    return '<a title="' + value + '" href="' + trafficPageUrl + '">' + value + '</a>';
                }
            },
            {
                field: 'name',
                title: 'Name',
                align: 'left',
                resetWidth: true,
                valign: 'middle',
                width: '400px',
                sortable: true,
                formatter: function (value, row, index) {
                    var trafficPageUrl = '/portal/policy#?env=' + G.env + '&policyId=' + row.id;
                    return '<a title="' + value + '" href="' + trafficPageUrl + '"><div style="word-break: break-all">' + value + '</div></a>';
                }
            },
            {
                field: 'paths',
                title: 'Path | Priority | SSL | VS | SLB',
                width: '600px',
                align: 'left',
                valign: 'middle',
                sortName: 'pathOrder',
                sortable: true,
                visible: false,
                formatter: function (value, row, index) {
                    var result = '';
                    $.each(value, function (i, v) {
                        var p = v.path;
                        if (v.path.length > 25) {
                            p = v.path.substring(0, 22);
                            p = p + "...";
                        }
                        var pathStr = '';
                        var priorityStr = '';
                        var sslStr = '';
                        var vsIdStr = '';
                        var slbIdStr = '';
                        if (v.path) {
                            pathStr = '<span class="">' + p + '</span>';
                            priorityStr = '&nbsp;<span class="">' + v.priority + '</span>';
                            sslStr = '&nbsp;<span class="">' + (v.ssl ? 'https' : 'http') + '</span>';
                            vsIdStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/vs#?env=' + G.env + '&vsId=' + v.vsId + '" title="' + v.domain + '">' + v.vsId + '</a>';
                            slbIdStr += '(';
                            $.each(v.slbId, function (i, slbId) {
                                if (i == v.slbId.length - 1) {
                                    slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>';
                                } else {
                                    slbIdStr += '<a class="" target="_blank" href="' + '/portal/slb#?env=' + G.env + '&slbId=' + slbId + '">' + slbId + '</a>,';
                                }
                            });
                            slbIdStr += ')';

                            var tempStr = "" +
                                "<div class='col-md-6' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;color:#196eaa;font-weight:bold;' title='" + v.path + "'>" +
                                pathStr +
                                "</div>" +
                                '<div class="col-md-6" style="padding:0;line-height:34px;">' +
                                '<div class="col-md-3" style="padding:0;line-height:34px;">' + priorityStr + '</div>' +
                                '<div class="col-md-3" style="padding:0;line-height:34px;">' + sslStr + '</div>' +
                                '<div class="col-md-3" style="padding:0;line-height:34px;">' + vsIdStr + '</div>' +
                                '<div class="col-md-3" style="padding:0;line-height:34px;">' + slbIdStr + '</div>' +
                                '</div>';
                            result = result + '<div class="row" style="margin:0;">' + tempStr + '</div>';
                        }
                    });
                    result = '<div class="row" style="margin:0">' + result + '</div>';
                    return result;
                }
            },
            {
                field: 'controls2',
                title: 'Group | App',
                align: 'left',
                valign: 'middle',
                events: operateEvents,
                visible: true,
                formatter: function (value, row, index) {
                    var result = '';

                    $.each(value, function (i, v) {
                        var p = v.groupName;
                        /* if (v.groupName.length > 25) {
                         p = v.groupName.substring(0, 22);
                         p = p + "...";
                         }*/
                        var appStr = '';
                        var appId = $scope.groups[v.group.id]['app-id'];
                        appStr += appId;
                        var app = $scope.apps[appId];
                        if (app) {
                            appStr += '(' + app['chinese-name'] + ')';
                        } else {
                            appStr += '(-)';
                        }
                        var link = '/portal/group#?env=' + $scope.env + '&groupId=' + v.group.id;
                        var appLink = '/portal/app#?env=' + $scope.env + '&appId=' + appId;

                        result += '<div style="padding-top: 10px"><a href="' + link + '">' + v.group.id + '</a>' + '/' + '<a href="' + link + '">' + p + '</a>' + '/' + '<a href="' + appLink + '">' + appStr + '</a></div>';

                    });
                    result = '<div class="row" style="margin:0">' + result + '</div>';
                    return result;
                },
                sortable: true
            },
            {
                field: 'controls1',
                title: 'Group ID | Group Name | IDC | Status | Owner',
                align: 'left',
                width: '600px',
                valign: 'middle',
                events: operateEvents,
                visible: false,
                formatter: function (value, row, index) {
                    var result = '';
                    $.each(value, function (i, v) {
                        var p = v.groupName;
                        if (v.groupName.length > 25) {
                            p = v.groupName.substring(0, 22);
                            p = p + "...";
                        }
                        var groupStr = '<span class="">' + p + '</span>';
                        var idcStr = '&nbsp;<span class="">' + v.idc + '</span>';
                        var statusStr = '&nbsp;<span class="">' + v.status + '</span>';
                        var ownerStr = 'N/A';
                        if (v.owner != 'N/A')
                            ownerStr = '&nbsp;<a class="" target="_blank" href="' + '/portal/user#?env=' + G.env + '&userId=' + v.owner + '" title="' + v.owner + '">' + v.owner + '</a>';

                        var tempStr =
                            "<div class='col-md-6' style='padding:0;line-height:34px;white-space: nowrap;overflow:hidden;' title='" + v.groupName + "'><a title='" + v.groupName + "' target='blank' href='/portal/group#?env=" + G.env + "&groupId=" + v.group.id + "'>" +
                            "<div class='col-md-3'>" + v.group.id + "</div>" +
                            "<div class='col-md-9'>" + groupStr + "</div>" +
                            "</a></div>" +
                            '<div class="col-md-6" style="padding:0;line-height:34px;">' +
                            '<div class="col-md-4" style="padding:0;line-height:34px;">' + idcStr + '</div>' +
                            '<div class="col-md-4" style="padding:0;line-height:34px;">' + statusStr + '</div>' +
                            '<div class="col-md-4" style="padding:0;line-height:34px;">' + ownerStr + '</div>' +
                            '</div>';
                        result = result + '<div class="row" style="margin:0;">' + tempStr + '</div>';
                    });
                    result = '<div class="row" style="margin:0">' + result + '</div>';
                    return result;
                },
                sortable: true
            },
            {
                field: 'target',
                title: 'Type',
                align: 'left',
                valign: 'middle',
                width: '200px',
                visible: false,
                sortable: true
            },
            {
                field: 'idc',
                title: 'IDC',
                align: 'left',
                valign: 'middle',
                sortable: true
            },
            {
                field: 'status',
                title: 'Status',
                align: 'left',
                events: operateEvents,
                valign: 'middle',
                width: '152px',
                formatter: function (value, row, index) {
                    var str = "";
                    switch (value) {
                        case "deactivated":
                            str = (angular.equals(resource, {}) ? "<span class='status-gray'>未激活</span>": resource.user.js.msg39);
                            break;
                        case "activated":
                            str = (angular.equals(resource, {}) ? "<span class='status-green'>已激活</span>": resource.user.js.msg42);
                            break;
                        case "toBeActivated":
                            str = (angular.equals(resource, {}) ? "<span class='diffTraffic status-yellow'>有变更(<a data-toggle='modal' data-target='#diffModal'>Diff</a>)</span>": resource.user.js.msg41);
                            break;
                        default:
                            str = "-";
                            break;
                    }
                    return str;
                },
                events: operateEvents,
                sortable: true
            },
            {
                field: 'operation',
                title: 'Operation',
                align: 'right',
                valign: 'middle',
                width: '60px',
                events: operateEvents,
                visible: false,
                formatter: function (value, row, index) {
                    if ($scope.query.userId) {
                        if ($scope.loginUserInfo.policies && $scope.loginUserInfo.policies[row.id]) {
                            return (angular.equals(resource, {}) ? '<span class="unFocusPolicy" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg79);
                        } else {
                            return (angular.equals(resource, {}) ? '<span class="focusPolicy" title="关注" aria-label="Left Align"><a class="fa fa-eye"> 关注</a></span>': resource.user.js.msg80);
                        }
                    } else {
                        return (angular.equals(resource, {}) ? '<span class="unFocusPolicy" title="取消关注" aria-label="Left Align"><a class="fa fa-eye-slash"> 取消关注</a></span>': resource.user.js.msg79);
                    }
                }
            }
            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'id',
            sortOrder: 'desc',
            data: $scope.focusedTraffics,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pagination: $scope.focusedTraffics.length > size,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return (angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Traffic Policies": resource.user.js.msg43);
            },
            formatNoMatches: function () {
                return (angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Traffic Policies': resource.user.js.msg44);
            }
        });
    };
    $scope.batchFocusPolicy = function () {
        var selected = $('#focused-traffic-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'id');
        $scope.showFocusDialog('Policy', ids);
    };
    $scope.batchUnFocusPolicy = function () {
        var selected = $('#focused-traffic-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'id');
        $scope.showUnFocusDialog('Policy', ids);
    };
    $('#focused-traffic-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
            var selected = $('#focused-traffic-table').bootstrapTable('getSelections');
            var s = selected.length > 0;
            var countBy = _.countBy(selected, function (r) {
                var v = $scope.loginUserInfo.policies[r.id];
                if (v) return 'focused';
                return 'unfocused';
            });
            var f = false;
            var u = false;

            var v = _.keys(countBy);
            if (v.length == 1) {
                if (v[0] == 'focused') {
                    u = true;
                    f = false;
                } else {
                    u = false;
                    f = true;
                }
            }
            $('#user-focus-policy').prop('disabled', !(s && f));
            $('#user-unfocus-policy').prop('disabled', !(s && u));
        });
    $scope.batchFocusOwnPolicy = function () {
        var selected = $('#owned-traffic-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'id');
        $scope.showFocusDialog('Policy', ids);
    };
    $scope.batchUnFocusOwnPolicy = function () {
        var selected = $('#owned-traffic-table').bootstrapTable('getSelections');
        var ids = _.pluck(selected, 'id');
        $scope.showUnFocusDialog('Policy', ids);
    };
    $('#owned-traffic-table').on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table', function () {
            var selected = $('#owned-traffic-table').bootstrapTable('getSelections');
            var s = selected.length > 0;
            var countBy = _.countBy(selected, function (r) {
                var v = $scope.loginUserInfo.policies[r.id];
                if (v) return 'focused';
                return 'unfocused';
            });
            var f = false;
            var u = false;

            var v = _.keys(countBy);
            if (v.length == 1) {
                if (v[0] == 'focused') {
                    u = true;
                    f = false;
                } else {
                    u = false;
                    f = true;
                }
            }
            $('#user-own-policy').prop('disabled', !(s && f));
            $('#user-unfocus-own-policy').prop('disabled', !(s && u));
        });

    // Table operations
    window.operateEvents = {
        'click .focusPolicy': function (e, value, row, index) {
            $scope.showFocusDialog("Policy", row.id);
        },
        'click .unFocusPolicy': function (e, value, row, index) {
            $scope.showUnFocusDialog("Policy", row.id);
        },
        'click .diffTraffic': function (e, value, row, index) {
            var resource= $scope.resource;
            $scope.currentPolicyId = row.id;
            $scope.confirmActivateText = (angular.equals(resource, {}) ? "线上版本与当前版本比较": resource.user.js.msg47);

            $scope.getPolicyById(row.id);

            setTimeout(function () {
                var baseText = JSON.stringify(U.sortObjectFileds($scope.onlinePolicyData), null, "\t");
                var newText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedPolicyData), null, "\t");
                var base = difflib.stringAsLines(baseText);
                var newtxt = difflib.stringAsLines(newText);

                var sm = new difflib.SequenceMatcher(base, newtxt);
                var opcodes = sm.get_opcodes();
                var diffoutputdiv = document.getElementById("diffOutput");

                diffoutputdiv.innerHTML = "";
                $scope.onlinePolicyVersion = $scope.onlinePolicyData.version;
                if (!$scope.onlinePolicyData.version)
                    $scope.onlinePolicyVersion = "无";

                diffoutputdiv.appendChild(diffview.buildView({
                    baseTextLines: base,
                    newTextLines: newtxt,
                    opcodes: opcodes,
                    baseTextName: (angular.equals(resource, {}) ? "线上Traffic Policy版本(版本号:": resource.user.js.msg45) + $scope.onlinePolicyData.version + ")",
                    newTextName: (angular.equals(resource, {}) ? "更新后Traffic Policy 版本(版本号:": resource.user.js.msg46) + $scope.tobeActivatedPolicyData.version + ")",
                    viewType: 0
                }));

            }, 500);
        },
        'click .focusSlb': function (e, value, row, index) {
            $scope.showFocusDialog("Slb", row.id);
        },
        'click .unFocusSlb': function (e, value, row, index) {
            $scope.showUnFocusDialog("Slb", row.id);
        },
        'click .focusVs': function (e, value, row, index) {
            $scope.showFocusDialog("Vs", row.id);
        },
        'click .unFocusVs': function (e, value, row, index) {
            $scope.showUnFocusDialog("Vs", row.id);
        },
        'click .focusGroup': function (e, value, row, index) {
            $scope.showFocusDialog("Group", row.id);
        },
        'click .unFocusGroup': function (e, value, row, index) {
            $scope.showUnFocusDialog("Group", row.id);
        },
        'click .focusApp': function (e, value, row, index) {
            $scope.showFocusDialog("App", row['app-id']);
        },
        'click .unFocusApp': function (e, value, row, index) {
            $scope.showUnFocusDialog("App", row['app-id']);
        },
        'click .diffSLB': function (e, value, row, index) {
            $scope.confirmActivateText = (angular.equals(resource, {}) ? "线上版本与当前版本比较": resource.user.js.msg47);

            $q.all(
                [
                    $http.get(G.baseUrl + "/api/slb?slbId=" + row.id + "&mode=online").then(
                        function (res) {
                            if (res.status == 200 || res.status == 202) {
                                $scope.onlineSLBData = _.omit(res.data, 'virtual-servers');
                            } else {
                                if (res.status == 400 && res.data.message == "Slb cannot be found.") {
                                    $scope.onlineSLBData = "No online version!!!";
                                }
                            }
                        }
                    ),
                    $http.get(G.baseUrl + "/api/slb?slbId=" + row.id).success(
                        function (res) {
                            $scope.tobeActivatedSLBData = _.omit(res, 'virtual-servers');
                        }
                    )
                ]
            ).then(
                function () {
                    var baseText = JSON.stringify(U.sortObjectFileds($scope.onlineSLBData), null, "\t");
                    var newText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedSLBData), null, "\t");

                    var base = difflib.stringAsLines(baseText);
                    var newTxt = difflib.stringAsLines(newText);

                    var sm = new difflib.SequenceMatcher(base, newTxt);
                    var opcodes = sm.get_opcodes();
                    var diffoutputdiv = document.getElementById("diffOutput");

                    diffoutputdiv.innerHTML = "";
                    $scope.onlineSlbVersion = $scope.onlineSLBData.version;
                    if (!$scope.onlineSLBData.version) {
                        $scope.onlineSlbVersion = "无";
                    }

                    diffoutputdiv.appendChild(diffview.buildView({
                        baseTextLines: base,
                        newTextLines: newTxt,
                        opcodes: opcodes,
                        baseTextName: (angular.equals(resource, {}) ? "线上SLB版本(版本号:": resource.user.js.msg48) + $scope.onlineSlbVersion + ")",
                        newTextName: (angular.equals(resource, {}) ? "更新后SLB版本(版本号:": resource.user.js.msg49) + $scope.tobeActivatedSLBData.version + ")",
                        viewType: 0
                    }));
                }
            );
        },
        'click .diffVS': function (e, value, row, index) {
            $scope.confirmActivateText = (angular.equals(resource, {}) ? "线上版本与当前版本比较": resource.user.js.msg47);

            $q.all(
                [
                    $http.get(G.baseUrl + "/api/vs?vsId=" + row.id + "&mode=online").then(
                        function (res) {
                            if (res.status == 200 || res.status == 202) {
                                $scope.onlineVSData = res.data;
                            } else {
                                if (res.status == 400 && res.data.message == "Virtual server cannot be found.") {
                                    $scope.onlineVSData = "No online version!!!";
                                }
                            }
                        }
                    ),
                    $http.get(G.baseUrl + "/api/vs?vsId=" + row.id).success(
                        function (res) {
                            $scope.tobeActivatedVSData = res;
                        }
                    )
                ]
            ).then(
                function () {
                    var baseText = JSON.stringify(U.sortObjectFileds($scope.onlineVSData), null, "\t");
                    var newText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedVSData), null, "\t");

                    var base = difflib.stringAsLines(baseText);
                    var newtxt = difflib.stringAsLines(newText);

                    var sm = new difflib.SequenceMatcher(base, newtxt);
                    var opcodes = sm.get_opcodes();
                    var diffoutputdiv = document.getElementById("diffOutput");

                    diffoutputdiv.innerHTML = "";
                    $scope.onlineVsVersion = $scope.onlineVSData.version;
                    if (!$scope.onlineVSData.version)
                        $scope.onlineVsVersion = "无";

                    diffoutputdiv.appendChild(diffview.buildView({
                        baseTextLines: base,
                        newTextLines: newtxt,
                        opcodes: opcodes,
                        baseTextName: (angular.equals(resource, {}) ? "线上VS版本(版本号:": resource.user.js.msg51) + $scope.onlineVsVersion + ")",
                        newTextName: (angular.equals(resource, {}) ? "更新后VS版本(版本号:": resource.user.js.msg52) + $scope.tobeActivatedVSData.version + ")",
                        viewType: 0
                    }));
                }
            );
        },
        'click .diffGroup': function (e, value, row, index) {
            $scope.confirmActivateText = (angular.equals(resource, {}) ? "线上版本与当前版本比较": resource.user.js.msg47);
            if (row.tags && row.tags.indexOf("owner_" + $scope.userInfo['name']) != -1) {
                $scope.currentGroupId = row.id;
                $scope.ownGroup = true;
            }

            $q.all(
                [
                    $http.get(G.baseUrl + "/api/group?groupId=" + row.id + "&mode=online").then(
                        function (res) {
                            if (res.status == 200 || res.status == 202) {
                                $scope.onlineGroupData = res.data;
                            } else {
                                if (res.status == 400 && res.data.message == "Group cannot be found.") {
                                    $scope.onlineGroupData = "No online version!!!";
                                }
                            }
                        }
                    ),
                    $http.get(G.baseUrl + "/api/group?groupId=" + row.id).success(
                        function (res) {
                            $scope.tobeActivatedGroupData = res;
                        }
                    )
                ]
            ).then(
                function () {
                    var baseText = JSON.stringify(U.sortObjectFileds($scope.onlineGroupData), null, "\t");
                    var newText = JSON.stringify(U.sortObjectFileds($scope.tobeActivatedGroupData), null, "\t");

                    var base = difflib.stringAsLines(baseText);
                    var newtxt = difflib.stringAsLines(newText);

                    var sm = new difflib.SequenceMatcher(base, newtxt);
                    var opcodes = sm.get_opcodes();
                    var diffoutputdiv = document.getElementById("diffOutput");

                    diffoutputdiv.innerHTML = "";
                    $scope.onlineGroupVersion = $scope.onlineGroupData.version;
                    if (!$scope.onlineGroupData.version)
                        $scope.onlineGroupVersion = "无";

                    diffoutputdiv.appendChild(diffview.buildView({
                        baseTextLines: base,
                        newTextLines: newtxt,
                        opcodes: opcodes,
                        baseTextName: (angular.equals(resource, {}) ? "线上VS版本(版本号:": resource.user.js.msg51) + $scope.onlineGroupVersion + ")",
                        newTextName: (angular.equals(resource, {}) ? "更新后VS版本(版本号:": resource.user.js.msg52) + $scope.tobeActivatedGroupData.version + ")",
                        viewType: 0
                    }));
                }
            );
        }
    };
    $('.confirmActivateGroup').on('click', function () {
        $('#diffModal').modal('hide');
        $('#confirmActivateGroup').modal('show');
    });
    $scope.activateGroup = function () {
        $('#confirmActivateGroup').modal('hide');

        var loading = (angular.equals(resource, {}) ? "<img src='/static/img/spinner.gif' /> 正在激活": resource.user.js.msg53);
        $('#operationConfrimModel').modal('show').find(".modal-body").html(loading);

        var request = {
            method: 'GET',
            url: G.baseUrl + "/api/activate/group?groupId=" + $scope.currentGroupId
        };
        $scope.processRequest(request, $('#operationConfrimModel'), (angular.equals(resource, {}) ? "激活Group": resource.user.js.msg54), (angular.equals(resource, {}) ? "激活Group成功,GroupId:": resource.user.js.msg55) + $scope.currentGroupId);
    };
    $scope.getPolicyById = function (id) {
        $http.get(G.baseUrl + "/api/policy?policyId=" + id + "&mode=online").then(
            function (res) {
                if (res.status == 200 || res.status == 202) {
                    $scope.onlinePolicyData = res.data;
                } else {
                    $scope.onlinePolicyData = "No online version!!!";
                }
            }
        );

        $http.get(G.baseUrl + "/api/policy?policyId=" + id).success(
            function (res) {
                $scope.tobeActivatedPolicyData = res;
            }
        );
    };

    $scope.showFocusDialog = function (type, id) {
        var resource = $scope.resource;
        $scope.focusType = type;
        $scope.focusIds = [];
        if (Array.isArray(id)) {
            $scope.focusIds = id;
        } else {
            $scope.focusIds.push(id);
        }

        var info = (angular.equals(resource, {}) ? "你确定要关注": resource.user.js.msg56) + $scope.focusType + ": " + $scope.focusIds + (angular.equals(resource, {}) ? "吗?": resource.user.js.msg71);
        $("#focusDialog").modal("show").find(".modal-title").html(info);
    };
    $scope.showUnFocusDialog = function (type, id) {
        $scope.unFocusType = type;
        $scope.unFocusIds = [];
        if (Array.isArray(id)) {
            $scope.unFocusIds = id;
        } else {
            $scope.unFocusIds.push(id);
        }

        var info = (angular.equals(resource, {}) ? "你确定要取消关注": resource.user.js.msg57) + $scope.unFocusType + ": " + $scope.unFocusIds + (angular.equals(resource, {}) ? "吗?": resource.user.js.msg71);
        $("#unFocusDialog").modal("show").find(".modal-title").html(info);
    };
    $scope.unFocusOperation = function (unFocusType, unFocusIds) {
        $("#unFocusDialog").modal("hide");
        if (unFocusIds != null && unFocusIds.length > 0) {
            var targetIdStr = '';
            $.each(unFocusIds, function (i, val) {
                if (i == unFocusIds.length - 1) {
                    targetIdStr += 'targetId=' + val;
                } else {
                    targetIdStr += 'targetId=' + val + '&';
                }
            });
            var req = {
                method: 'GET',
                url: G.baseUrl + '/api/untagging?tagName=' + 'user_' + $scope.query.loginUserId + '&type=' + unFocusType.toLowerCase() + '&' + targetIdStr
            };
            $scope.processRequest(req, $('#operationConfrimModel'), "取消关注" + unFocusType, "取消关注" + unFocusType + ":" + unFocusIds + "成功");
            $('#unFocus' + unFocusType).prop('disabled', true);
        }
    };
    $scope.focusOperation = function (unFocusType, unFocusIds) {
        $("#focusDialog").modal("hide");
        if (unFocusIds != null && unFocusIds.length > 0) {
            var targetIdStr = '';
            $.each(unFocusIds, function (i, val) {
                if (i == unFocusIds.length - 1) {
                    targetIdStr += 'targetId=' + val;
                } else {
                    targetIdStr += 'targetId=' + val + '&';
                }
            });
            var req = {
                method: 'GET',
                url: G.baseUrl + '/api/tagging?tagName=' + 'user_' + $scope.query.loginUserId + '&type=' + unFocusType.toLowerCase() + '&' + targetIdStr
            };
            $scope.processRequest(req, $('#operationConfrimModel'), "关注" + unFocusType, "关注" + unFocusType + ":" + unFocusIds + "成功");
            $('#focus' + unFocusType).prop('disabled', true);
        }
    };
    $scope.processRequest = function (request, confirmDialog, operationText, tooltipText) {
        var msg = "";
        $http(request).success(
            function (res, code) {
                var errText = '';
                if (code != 200) {
                    msg = res.message;
                    if (!msg) {
                        msg = code;
                    }
                    errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + (angular.equals(resource, {}) ? "失败</span>": resource.user.js.msg62);
                    confirmDialog.modal('show').find(".modal-title").html(errText);
                    confirmDialog.modal('show').find(".modal-body").html((angular.equals(resource, {}) ? "失败原因:": resource.user.js.msg64) + msg);
                }
                else {
                    var successText = "<span class='fa fa-check'></span><span style='padding-left: 10px'>" + operationText + (angular.equals(resource, {}) ? "成功</span>": resource.user.js.msg63);
                    confirmDialog.modal('show').find(".modal-title").html(successText);
                    if (tooltipText)
                        confirmDialog.modal('show').find(".modal-body").html(tooltipText);
                    setTimeout(function () {
                        confirmDialog.modal('hide');
                        H.setData({ timestamp: new Date().getTime() });
                    }, 2000);
                }
            }
        ).error(function (reject) {
            msg = reject.message;
            var errText = "<span class='fa fa-times'></span><span style='padding-left: 10px'>" + operationText + (angular.equals(resource, {}) ? "失败</span>": resource.user.js.msg62);
            confirmDialog.modal('show').find(".modal-title").html(errText);
            confirmDialog.modal('show').find(".modal-body").html((angular.equals(resource, {}) ? "失败原因:": resource.user.js.msg64) + msg);
        }
        );
    };

    $scope.hasApprovalRight = function () {
        return A.canDo("Auth", "AUTH", '*');
    };
    $scope.initTables = function () {
        var focusSlbPaging = $scope.userInfo.focusedSlbs && $scope.userInfo.focusedSlbs.length > 15;
        var focusVsPaging = $scope.userInfo.focusedVses && $scope.userInfo.focusedVses.length > 15;
        var focusGroupPaging = $scope.userInfo.focusedGroups && $scope.userInfo.focusedGroups.length > 15;
        var focusAppPaging = $scope.userInfo.focusedApps && $scope.userInfo.focusedApps.length > 15;

        var ownGroupPaging = $scope.userInfo.ownGroups && $scope.userInfo.ownGroups.length > 15;
        var ownAppPaging = $scope.userInfo.ownApps && $scope.userInfo.ownApps.length > 15;

        $scope.initFocusSlbTable(focusSlbPaging);
        $scope.initFocusVsTable(focusVsPaging);

        $scope.initFocusGroupTable("#user-focusGroup-table", "#userInfo-focusGroup-toolbar", "focus", focusGroupPaging);
        $scope.initFocusAppTable("#user-focusApp-table", "#userInfo-focusApp-toolbar", "focus", focusAppPaging);
        $scope.initOwnGroupTable("#user-ownGroup-table", "#userInfo-ownGroup-toolbar", "own", ownGroupPaging);
        $scope.initFocusAppTable("#user-ownApp-table", "#userInfo-ownApp-toolbar", "own", ownAppPaging);
        $scope.initPolicyTable(10);
    };
    $scope.reloadTables = function () {
        $scope.initTables();

        $('#user-focusSlb-table').bootstrapTable("load", $scope.userInfo.focusedSlbs ? $scope.userInfo.focusedSlbs : []);
        $('#user-focusVs-table').bootstrapTable("load", $scope.userInfo.focusedVses ? $scope.userInfo.focusedVses : []);
        $('#user-focusGroup-table').bootstrapTable("load", $scope.userInfo.focusedGroups ? $scope.userInfo.focusedGroups : []);
        $('#user-focusApp-table').bootstrapTable("load", $scope.userInfo.focusedApps ? $scope.userInfo.focusedApps : []);

        $('#user-ownGroup-table').bootstrapTable("load", $scope.userInfo.ownGroups ? $scope.userInfo.ownGroups : []);
        $('#user-ownApp-table').bootstrapTable("load", $scope.userInfo.ownApps ? $scope.userInfo.ownApps : []);

        $('#owned-traffic-table').bootstrapTable("load", $scope.ownedTraffics ? $scope.ownedTraffics : []);
        $('#focused-traffic-table').bootstrapTable("load", $scope.focusedTraffics ? $scope.focusedTraffics : []);

        $('#user-focusApp-table').bootstrapTable("hideLoading");
        $('#user-focusGroup-table').bootstrapTable("hideLoading");
        $('#user-focusVs-table').bootstrapTable("hideLoading");
        $('#user-focusSlb-table').bootstrapTable("hideLoading");
        $('#user-ownApp-table').bootstrapTable("hideLoading");
        $('#user-ownGroup-table').bootstrapTable("hideLoading");
    };
    $scope.userInfo = {};
    $scope.loadData = function () {
        var url = '';
        if ($scope.query.userId) {
            url = G.baseUrl + '/api/auth/user?userName=' + $scope.query.userId;
        } else {
            url = '/api/auth/current/user';
        }
        var request = {
            method: 'GET',
            url: url
        };
        $http(request).success(
            function (res) {
                $scope.queryResult = res;
                $scope.userInfo = {};
                var name = '';
                var department = '';
                var cid = '';
                var mail = '';
                var chinesename = '';
                if (res) {
                    if (res['name']) {
                        name = res['name'];
                        department = res.department;
                        mail = res.mail;
                        cid = res.employee;
                        chinesename = res['chinese-name'];
                    } else {
                        name = res['user-name'];
                        department = res.bu;
                        mail = res.email;
                        cid = res.id;
                        chinesename = name;
                    }
                }
                $scope.userInfo = {
                    name: name,
                    'chinese-name': chinesename,
                    mail: mail,
                    department: department,
                    employee: cid
                };
            }
        ).then(
            function () {
                if ($scope.queryResult.code) {
                    exceptionNotify((angular.equals(resource, {}) ? "出错了!!": resource.user.js.msg65), (angular.equals(resource, {}) ? "加载当前用户信息失败了， 失败原因": resource.user.js.msg9) + $scope.queryResult.message, null);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }

                $scope.loadMeta();
            }
        );
    };
    $scope.loadMeta = function () {
        $scope.groups_temp = {};
        $scope.vses_temp = {};
        $scope.apps_temp = {};
        $scope.groups_server = {};
        $scope.members_temp = 0;
        $q.all(
            [
                $http.get('/api/apps').success(
                    function (res) {
                        if (res.total > 0) {
                            var apps = res['apps'];
                            $scope.apps = _.indexBy(apps, 'app-id');
                        }
                    }
                ),

                $http.get('/api/auth/current/user').success(
                    function (res) {
                        $scope.query.loginUserId = res['name'];
                    }
                ),
                $http.get(G.baseUrl + "/api/slbs?type=extended&tags=user_" + $scope.userInfo['name']).success(
                    function (res) {
                        $scope.queryResult = res;
                        $scope.userInfo.focusedSlbs = res.slbs;
                        if (!$scope.query.userId) {
                            $scope.loginUserInfo.slbs = _.indexBy(res.slbs, 'id');
                        }
                        if (res.slbs && res.slbs.length > 0)
                            $('#user-focusSlb-table').bootstrapTable("showLoading");
                    }
                ),
                $http.get(G.baseUrl + "/api/vses?type=extended&tags=user_" + $scope.userInfo['name']).success(
                    function (res) {
                        $scope.queryResult = res;
                        $scope.userInfo.focusedVses = res['virtual-servers'];
                        if (!$scope.query.userId) {
                            $scope.loginUserInfo.vses = _.indexBy(res['virtual-servers'], 'id');
                        }
                        if (res['virtual-servers'] && res['virtual-servers'].length > 0)
                            $('#user-focusVs-table').bootstrapTable("showLoading");
                    }
                ),

                $http.get(G.baseUrl + "/api/groups?type=extended&tags=user_" + $scope.userInfo['name']).success(
                    function (res) {
                        $scope.userInfo.focusedGroups = res['groups'];
                        if (!$scope.query.userId) {
                            $scope.loginUserInfo.groups = _.indexBy(res['groups'], 'id');
                        }

                        if (res['groups'] && res['groups'].length > 0)
                            $('#user-focusGroup-table').bootstrapTable("showLoading");
                    }
                ),
                $http.get(G.baseUrl + "/api/vgroups?type=extended&tags=user_" + $scope.userInfo['name']).success(
                    function (res) {
                        $scope.userInfo.focusedVGroups = res['groups'];
                    }
                ),

                $http.get(G.baseUrl + "/api/apps?tags=user_" + $scope.userInfo['name']).success(
                    function (res) {
                        $scope.userInfo.focusedApps = res['total'] > 0 ? res['apps'] : [];
                        if (!$scope.query.userId) {
                            $scope.loginUserInfo.apps = _.indexBy(res['apps'], 'app-id');
                        }

                        if ($scope.userInfo.focusedApps.length > 0)
                            $('#user-focusApp-table').bootstrapTable("showLoading");
                    }
                ),

                $http.get(G.baseUrl + "/api/groups?type=extended&tags=owner_" + $scope.userInfo['name']).success(
                    function (res) {
                        $scope.userInfo.ownGroups = (res['groups'] && res['groups'].length > 0) ? res['groups'] : [];
                        if (res['groups'] && res['groups'].length > 0)
                            $('#user-ownGroup-table').bootstrapTable("showLoading");
                    }
                ),
                $http.get(G.baseUrl + "/api/vgroups?type=extended&tags=owner_" + $scope.userInfo['name']).success(
                    function (res) {
                        $scope.userInfo.ownVGroups = res['groups'];
                    }
                ),

                $http.get(G.baseUrl + "/api/apps?tags=owner_" + $scope.userInfo['name']).success(
                    function (res) {
                        $scope.userInfo.ownApps = res['total'] > 0 ? res['apps'] : [];
                        if ($scope.userInfo.ownApps.length > 0)
                            $('#user-ownApp-table').bootstrapTable("showLoading");
                    }
                ),
                $http.get(G.baseUrl + "/api/statistics/slbs").success(
                    function (res) {
                        $scope.slbStatistics = res['slb-metas'];
                        $scope.slbStatisticsMap = {};
                        $.each($scope.slbStatistics, function (i, val) {
                            $scope.slbStatisticsMap[val['slb-id']] = val;
                        });
                    }
                ),
                $http.get(G.baseUrl + "/api/statistics/vses").success(
                    function (res) {
                        $scope.vsStatistics = res['vs-metas'];
                        $scope.vsStatisticsMap = {};
                        $.each($scope.vsStatistics, function (i, val) {
                            $scope.vsStatisticsMap[val['vs-id']] = val;
                        });
                    }
                ),
                $http.get(G.baseUrl + "/api/statistics/groups").success(
                    function (response) {
                        $scope.groupStatisticsMap = {};
                        $.each(response["group-metas"], function (index, meta) {
                            $scope.groupStatisticsMap[meta['group-id']] = meta;
                        });
                    }
                ),
                $http.get(G.baseUrl + "/api/statistics/app").success(
                    function (response) {
                        $scope.appStatisticsMap = {};
                        $.each(response["app-metas"], function (index, meta) {
                            $scope.appStatisticsMap[meta['app-id']] = meta;
                        });
                    }
                ),
                $http.get(G.baseUrl + '/api/policies?type=extended&anyTag=owner_' + $scope.userInfo['name']).success(
                    function (response) {
                        if (response['traffic-policies']) {
                            $scope.ownedTraffics = response['traffic-policies'];

                        } else {
                            $scope.ownedTraffics = [];
                        }
                    }
                ),
                $http.get(G.baseUrl + '/api/policies?type=extended&anyTag=user_' + $scope.userInfo['name']).success(
                    function (response) {
                        if (response['traffic-policies']) {
                            $scope.focusedTraffics = response['traffic-policies'];
                            if (!$scope.query.userId) {
                                $scope.loginUserInfo.policies = _.indexBy(response['traffic-policies'], 'id');
                            }
                        } else {
                            $scope.focusedTraffics = [];
                        }
                    }
                ),
                $http.get(G.baseUrl + '/api/vses').success(
                    function (res) {
                        var vses = res['virtual-servers'];
                        $scope.vses = _.indexBy(vses, 'id');
                    }
                ),
                $http.get(G.baseUrl + '/api/groups?type=extended&groupType=all').success(
                    function (res) {
                        var groups = res['groups'];
                        $scope.groups = _.indexBy(groups, 'id');
                    }
                ),
                $http.get(G.baseUrl + '/api/apps').success(
                    function (res) {
                        var apps = res['apps'];
                        $scope.apps = _.indexBy(apps, 'app-id');
                    }
                )
            ]
        ).then(
            function () {
                if ($scope.queryResult.code) {
                    exceptionNotify((angular.equals(resource, {}) ? "出错了!!": resource.user.js.msg65), (angular.equals(resource, {}) ? "加载当前用户信息失败了， 失败原因": resource.user.js.msg9) + $scope.queryResult.message, null);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }

                $.each($scope.userInfo.focusedSlbs, function (i, val) {
                    if (val.properties) {
                        var propertiesMap = {};
                        $.each(val.properties, function (i, val) {
                            propertiesMap[val.name] = val;
                        });

                        if (propertiesMap['status']) {
                            switch (propertiesMap['status'].value) {
                                case "activated":
                                    val.status = '已激活';
                                    break;
                                case "deactivated":
                                    val.status = '未激活';
                                    break;
                                case "toBeActivated":
                                    val.status = '有变更';
                                    break;
                                default:
                                    val.status = "unKnown";
                            }
                        }
                    }
                });
                $.each($scope.userInfo.focusedVses, function (i, val) {
                    if (val.properties) {
                        var propertiesMap = {};
                        $.each(val.properties, function (i, val) {
                            propertiesMap[val.name] = val;
                        });

                        if (propertiesMap['status']) {
                            switch (propertiesMap['status'].value) {
                                case "activated":
                                    val.status = "已激活";
                                    break;
                                case "deactivated":
                                    val.status = "未激活";
                                    break;
                                case "toBeActivated":
                                    val.status = "有变更";
                                    break;
                                default:
                                    val.status = "unKnown";
                            }
                        }
                        if (propertiesMap['idc']) {
                            val.idc = propertiesMap['idc'].value;
                        }
                    }
                });

                $scope.userInfo.focusedGroups = $scope.userInfo.focusedGroups;
                $scope.userInfo.ownGroups = $scope.userInfo.ownGroups;
                $.each($scope.userInfo.focusedGroups, function (i, val) {
                    if (val.properties) {
                        var propertiesMap = {};
                        $.each(val.properties, function (i, val) {
                            propertiesMap[val.name] = val;
                        });

                        if (propertiesMap['healthy']) {
                            val.grouphealthy = propertiesMap['healthy'].value;
                        } else {
                            val.grouphealthy = '-';
                        }
                        if (propertiesMap['status']) {
                            switch (propertiesMap['status'].value) {
                                case "activated":
                                    val.status = "已激活";
                                    break;
                                case "deactivated":
                                    val.status = "未激活";
                                    break;
                                case "toBeActivated":
                                    val.status = "有变更";
                                    break;
                                default:
                                    val.status = "unKnown";
                            }
                        }
                        if (propertiesMap['idc']) {
                            val.idc = propertiesMap['idc'].value;
                        } else {
                            val.idc = '-';
                        }
                    }
                    val['app-id'] = val['app-id'];
                    var app = $scope.apps[val['app-id']];

                    if (app) val.app = val['app-id'] + '(' + app['chinese-name'] + ')';
                    else val.app = val['app-id'] + '(-)';
                });

                $.each($scope.userInfo.ownGroups, function (i, val) {
                    if (val.properties) {
                        var propertiesMap = {};
                        $.each(val.properties, function (i, val) {
                            propertiesMap[val.name] = val;
                        });

                        if (propertiesMap['healthy']) {
                            val.grouphealthy = propertiesMap['healthy'].value;
                        }
                        if (propertiesMap['status']) {
                            switch (propertiesMap['status'].value) {
                                case "activated":
                                    val.status = "已激活";
                                    break;
                                case "deactivated":
                                    val.status = "未激活";
                                    break;
                                case "toBeActivated":
                                    val.status = "有变更";
                                    break;
                                default:
                                    val.status = "unKnown";
                            }
                        }
                        if (propertiesMap['idc']) {
                            val.idc = propertiesMap['idc'].value;
                        } else {
                            val.idc = '-';
                        }
                    }
                    val['app-id'] = val['app-id'];
                    var app = $scope.apps[val['app-id']];

                    if (app) val.app = val['app-id'] + '(' + app['chinese-name'] + ')';
                    else val.app = val['app-id'] + '(-)';
                });

                $.each($scope.userInfo.focusedSlbs, function (i, val) {
                    if ($scope.slbStatisticsMap[val.id]) {
                        val.qps = $scope.slbStatisticsMap[val.id].qps;
                    }
                });
                $.each($scope.userInfo.focusedVses, function (i, val) {
                    if ($scope.vsStatisticsMap[val.id]) {
                        val.qps = $scope.vsStatisticsMap[val.id].qps;
                    }
                });
                $.each($scope.userInfo.focusedGroups, function (i, val) {
                    if ($scope.groupStatisticsMap[val.id]) {
                        val.qps = $scope.groupStatisticsMap[val.id].qps;
                    }
                });
                $.each($scope.userInfo.ownGroups, function (i, val) {
                    if ($scope.groupStatisticsMap[val.id]) {
                        val.qps = $scope.groupStatisticsMap[val.id].qps;
                    }
                });

                $.each($scope.userInfo.focusedApps, function (i, val) {
                    if ($scope.appStatisticsMap[val['app-id']]) {
                        val.groupCount = $scope.appStatisticsMap[val['app-id']]['group-count'];
                        val.qps = $scope.appStatisticsMap[val['app-id']].qps;
                    }
                });
                $.each($scope.userInfo.ownApps, function (i, val) {
                    if ($scope.appStatisticsMap[val['app-id']]) {
                        val.groupCount = $scope.appStatisticsMap[val['app-id']]['group-count'];
                        val.qps = $scope.appStatisticsMap[val['app-id']].qps;
                    }
                });

                $scope.ownedTraffics = $scope.getTrafficInformation($scope.ownedTraffics);
                $scope.focusedTraffics = $scope.getTrafficInformation($scope.focusedTraffics);

                var myappCount = $scope.userInfo.ownApps ? $scope.userInfo.ownApps.length : 0;
                var mygroupCount = $scope.userInfo.ownGroups ? $scope.userInfo.ownGroups.length : 0;
                var myfocusSLbCount = $scope.userInfo.focusedSlbs ? $scope.userInfo.focusedSlbs.length : 0;
                var myfocusVsCount = $scope.userInfo.focusedVses ? $scope.userInfo.focusedVses.length : 0;
                var myfocusGroupCount = $scope.userInfo.focusedGroups ? $scope.userInfo.focusedGroups.length : 0;
                var myfocusAppCount = $scope.userInfo.focusedApps ? $scope.userInfo.focusedApps.length : 0;


                $('.my-apps-text').text(myappCount + myfocusAppCount);
                if (myappCount + myfocusAppCount > 0) {
                    $('.my-apps-text').prop('href', $scope.navigateTo('app'));
                }

                $('.my-groups-text').text(mygroupCount + myfocusGroupCount);
                if (mygroupCount + myfocusGroupCount > 0) {
                    $('.my-groups-text').prop('href', $scope.navigateTo('group'));
                }

                $('.my-focus-slb').text(myfocusSLbCount);
                if (myfocusSLbCount > 0) {
                    $('.my-focus-slb').prop('href', $scope.navigateTo('slb'));
                }

                $('.my-focus-vs').text(myfocusVsCount);
                if (myfocusVsCount > 0) {
                    $('.my-focus-vs').prop('href', $scope.navigateTo('vs'));
                }

                var active_count = 0;
                var changed_count = 0;
                var deactivate_count = 0;
                var qps_count = 0;

                var healthy_count = 0;
                var unhealthy_count = 0;
                var broken_count = 0;

                if (!$scope.userInfo.focusedGroups) {
                    $scope.userInfo.focusedGroups = [];
                }
                if (!$scope.userInfo.ownGroups) {
                    $scope.userInfo.ownGroups = [];
                }
                var groups = $scope.userInfo.focusedGroups.concat($scope.userInfo.ownGroups);
                var status = _.countBy(groups, function (item) {
                    return item.status;
                });
                var healthy = _.countBy(groups, function (item) {
                    return item.grouphealthy;
                });
                if (groups.length != 0) {
                    qps_count = _.reduce(_.pluck(groups, 'qps'), function (left, right) {
                        return left + right;
                    });
                }
                active_count = status['已激活'];
                changed_count = status['有变更'];
                deactivate_count = status['未激活'];

                healthy_count = healthy['healthy'];
                unhealthy_count = healthy['unhealthy'];
                broken_count = healthy['broken'];

                $('.qps-text').text(T.getText(qps_count));

                $('.activate-group-text').text(active_count);
                if (active_count > 0) {
                    $('.activate-group-text').prop('href', $scope.navigateTo('activated'));
                }
                $('.tobeactivated-group-text').text(changed_count);
                if (changed_count > 0) {
                    $('.tobeactivated-group-text').prop('href', $scope.navigateTo('tobeactivated'));
                }
                $('.deactivated-group-text').text(deactivate_count);
                if (changed_count > 0) {
                    $('.deactivated-group-text').prop('href', $scope.navigateTo('deactivated'));
                }

                $('.healthy-text').text(healthy_count);
                if (healthy_count > 0) {
                    $('.healthy-text').prop('href', $scope.navigateTo('healthy'));
                }
                $('.unhealthy-text').text(unhealthy_count);
                if (unhealthy_count > 0) {
                    $('.unhealthy-text').prop('href', $scope.navigateTo('unhealthy'));
                }
                $('.broken-text').text(broken_count);
                if (broken_count > 0) {
                    $('.broken-text').prop('href', $scope.navigateTo('broken'));
                }

                var logingUser = $scope.query.loginUserId;
                // login user
                if ($scope.query.userId) {

                    var currentRequestArray = [
                        $http.get(G.baseUrl + '/api/policies?type=extended&anyTag=user_' + logingUser).success(
                            function (res) {
                                if (res['traffic-policies']) {
                                    $scope.loginUserInfo.policies = _.indexBy(res['traffic-policies'], 'id');
                                } else {
                                    $scope.loginUserInfo.policies = {};
                                }
                            }
                        ),
                        $http.get(G.baseUrl + '/api/policies?type=extended&anyTag=owner_' + logingUser).success(
                            function (res) {
                                $scope.loginUserInfo.slbs = _.indexBy(res['slbs'], 'id');
                            }
                        ),

                        $http.get(G.baseUrl + "/api/slbs?type=info&tags=user_" + logingUser).success(
                            function (res) {
                                $scope.loginUserInfo.slbs = _.indexBy(res['slbs'], 'id');
                            }
                        ),
                        $http.get(G.baseUrl + "/api/vses?type=info&tags=user_" + logingUser).success(
                            function (res) {
                                $scope.loginUserInfo.vses = _.indexBy(res['virtual-servers'], 'id');
                            }
                        ),

                        $http.get(G.baseUrl + "/api/groups?type=info&tags=user_" + logingUser).success(
                            function (res) {
                                $scope.loginUserInfo.groups = _.indexBy(res['groups'], 'id');
                            }
                        ),
                        $http.get(G.baseUrl + "/api/vgroups?type=info&tags=user_" + logingUser).success(
                            function (res) {
                                $scope.loginUserInfo.vgroups = _.indexBy(res['groups'], 'id');
                            }
                        ),

                        $http.get(G.baseUrl + "/api/apps?tags=user_" + logingUser).success(
                            function (res) {
                                $scope.loginUserInfo.apps = _.indexBy(res['apps'], 'app-id');
                            }
                        )
                    ];

                    $q.all(currentRequestArray).then(function () {
                        $scope.reloadTables();
                    });
                } else {
                    $scope.reloadTables();
                }

            }
        )
    };
    $scope.getTrafficInformation = function (traffics) {
        $.each(traffics, function (index, current) {
            // Traffic status fields
            current.paths = [];
            current.status = '';

            var statusProperty = _.find(current.properties, function (item) {
                return item.name == 'status';
            });
            if (statusProperty) {
                current.status = statusProperty.value;
            }
            var targetProperty = _.find(current.properties, function (item) {
                return item.name == 'target';
            });
            if (targetProperty) {
                current.target = targetProperty.value;
            }
            var idcProperty = _.find(current.properties, function (item) {
                return item.name == 'idc';
            });
            if (idcProperty) {
                current.idc = idcProperty.value;
            }
            // Traffic paths
            var vs = current['policy-virtual-servers'];
            var c = {
                path: '',
                priority: '',
                ssl: '',
                vsId: '',
                slbId: []
            };
            $.each(vs, function (c_index, c_item) {
                var vsId = c_item['virtual-server'].id;
                c.path = c_item.path;
                c.priority = c_item.priority;
                c.vsId = vsId;
                if ($scope.vses[vsId]) {
                    c.ssl = $scope.vses[vsId].ssl ? 'https' : 'http';
                    c.slbId = $scope.vses[vsId]['slb-ids'] ? $scope.vses[vsId]['slb-ids'] : [];
                }
                current.paths.push(c);
                $scope.vses_temp[vsId] = vsId;
            });

            // traffic groups and apps
            var groups = current.controls;
            $.each(groups, function (index, item) {
                var id = item.group.id;
                $scope.groups_temp[id] = id;

                item.groupName = $scope.groups[id].name;
                var idc_property = _.find($scope.groups[id].properties, function (c) {
                    return c.name == 'idc';
                });
                var status_property = _.find($scope.groups[id].properties, function (c) {
                    return c.name == 'status';
                });
                if (idc_property) {
                    item.idc = idc_property.value;
                }
                if (status_property) {
                    item.status = getGroupStatus(status_property.value);
                }
                var appId = $scope.groups[id]['app-id'];
                $scope.apps_temp[appId] = appId;
                var appName = "-";
                var sbu = "-";
                var owner = "-";
                if ($scope.apps[appId]) {
                    appName = $scope.apps[appId]['chinese-name'];
                    sbu = $scope.apps[appId]['sbu'];
                    owner = $scope.apps[appId]['owner'];
                }
                item.app = appId + '(' + appName + ')';
                item.bu = sbu;
                item.owner = owner;

                var groupServers = $scope.groups[id]['group-servers'];
                $scope.members_temp += groupServers ? groupServers.length : 0;
                $.each($scope.groups[id]['group-servers'], function (i, t) {
                    $scope.groups_server[t.ip + '/' + t['host-name']] = t;
                });
            });

            current.controls1 = current.controls;
            current.controls2 = current.controls;
        });
        return traffics;
    };

    function getGroupStatus(val) {
        switch (val) {
            case 'activated':
                return '已激活';
            case 'toBeActivated':
                return '有变更';
            case 'deactivated':
                return '未激活';
            default:
                return val;
        }
    }

    var userApp;
    $scope.hashChanged = function (hashData) {
        $scope.resource = H.resource;
        $scope.userInfo = {};
        $scope.env = 'pro';
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        if (hashData.userId) {
            $scope.query.userId = hashData.userId;
        } else {
            $scope.query.userId = '';
        }
        if (hashData.tab) {
            $scope.query.tab = hashData.tab;
        } else {
            $scope.query.tab = (angular.equals(resource, {}) ? '关于我': resource.user.js.msg70);
        }

        userApp = new UserApp($scope.env)
        $('#user-focusGroup-table').bootstrapTable("removeAll");
        $('#user-focusApp-table').bootstrapTable("removeAll");
        $('#user-focusVs-table').bootstrapTable("removeAll");
        $('#user-focusSlb-table').bootstrapTable("removeAll");
        $('#user-ownGroup-table').bootstrapTable("removeAll");
        $('#user-ownApp-table').bootstrapTable("removeAll");

        // init summary information
        $.each($('.tables').find('.btn'), function (i, item) {
            $(item).attr('disabled', true);
        });
        $scope.loadData();

        $scope.reloadTables();
    };
    H.addListener("selfInfoApp", $scope, $scope.hashChanged);

    function exceptionNotify(title, message, url) {
        var notify = $.notify({
            icon: 'fa fa-exclamation-triangle',
            title: title,
            message: message,
            url: url,
            target: '_self'
        }, {
            type: 'danger',
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
            delay: 60000 * 24,
            spacing: 5,
            z_index: 1031,
            mouse_over: 'pause'
        });
    }
});
angular.bootstrap(document.getElementById("self-info-area"), ['selfInfoApp']);
