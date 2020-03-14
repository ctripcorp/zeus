//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', []);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'hc': {
                link = "/portal/statistics/statistics-hc#?env=" + G.env;
                break;
            }
            case 'rule': {
                link = "/portal/statistics/rule#?env=" + G.env;
                break;
            }
            case 'basic': {
                link = "/portal/statistics#?env=" + G.env;
                break;
            }
            case 'pie': {
                link = "/portal/statistics/charts#?env=" + G.env;
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
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
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

//TrafficComponent: Traffic
var opsLogQueryApp = angular.module('opsLogQueryApp', ['ui.bootstrap', 'ui.bootstrap.datetimepicker', 'angucomplete-alt', 'http-auth-interceptor','ngSanitize']);
opsLogQueryApp.controller('opsLogQueryController', function ($scope, $http, $filter) {
    $scope.data = {
        slbops: {},
        vsops: {},
        groupops: {},
        policyops: {},
        opstates: {},
        builtinusers: {},
        ctripusers: {}
    };
    $scope.query = {
        startTime: '',
        endTime: '',
        slbops: {},
        vsops: {},
        groupops: {},
        policyops: {},
        drops: {},
        serverops: {},
        builtinusers: {},
        ctripusers: {},
        status: {}
    };
    $scope.queryDate = {
        startTime: '',
        endTime: ''
    };
    // Calendar area
    $scope.startTime = new Date();
    $scope.endTime = new Date();
    $scope.setDateNow = function () {
        var d = new Date();
        d = d.setTime(d.getTime() - 3 * 12 * 30 * 24 * 1000 * 60 * 60);

        H.setData({startTime: encodeURIComponent($.format.date(d, 'yyyy-MM-dd HH:mm:00'))});
        d = new Date();
        var e = d.setTime(d.getTime() + 1000 * 60 * 60);
        H.setData({endTime: encodeURIComponent($.format.date(e, 'yyyy-MM-dd HH:mm:00'))});
    };

    $('#time-zone input').datepicker({
        timepicker: true,
        autoclose: false, //这里最好设置为false
    });
    $scope.getOperationTextLanguage = function (x) {
        var resource = $scope.resource;

        if (resource) {
            return resource['log']['log_opsLogQueryApp_opmapping'][x];
        }
    };

    $scope.getStatusMapping = function (x) {
        var resource = $scope.resource;

        if (resource) {
            return resource['log']['log_opsLogQueryApp_statusmapping'][x];
        }
    };

    //Load cache
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };
    $scope.remoteSLBsUrl = function () {
        return G.baseUrl + "/api/meta/users";
    };

    $scope.addUser = function (user) {
        if (user) {
            $scope.query.ctripusers[user.originalObject.name] = user.originalObject.name;
        }
    };
    $scope.removeUser = function (user) {
        delete $scope.query.ctripusers[user];
    };
    $scope.slbOpsClear = function () {
        $scope.query.slbops = {};
    };
    $scope.vsOpsClear = function () {
        $scope.query.vsops = {};
    };
    $scope.groupOpsClear = function () {
        $scope.query.groupops = {};
    };
    $scope.policyOpsClear = function () {
        $scope.query.policyops = {};
    };
    $scope.drOpsClear = function () {
        $scope.query.drops = {};
    };
    $scope.serverOpsClear = function () {
        $scope.query.serverops = {};
    };
    $scope.userClear = function () {
        $scope.query.users = {};
    };
    $scope.builtinUserClear = function () {
        $scope.query.builtinusers = {};
    };
    $scope.statusClear = function () {
        $scope.query.status = {};
    }
    $scope.showClear = function (type) {
        if (type == "slb") {
            return _.keys($scope.query.slbops).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "policy") {
            return _.keys($scope.query.policyops).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "dr") {
            return _.keys($scope.query.drops).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "vs") {
            return _.keys($scope.query.vsops).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "group") {
            return _.keys($scope.query.groupops).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "server") {
            return _.keys($scope.query.serverops).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "user") {
            return _.keys($scope.query.users).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "builtinuser") {
            return _.keys($scope.query.builtinusers).length > 0 ? "link-show" : "link-hide";
        }
        if (type == "status") {
            return _.keys($scope.query.status).length > 0 ? "link-show" : "link-hide";
        }
    };

    // Query cretia events
    $scope.toggleBuiltInUser = function (x) {
        if ($scope.query.builtinusers[x]) {
            delete $scope.query.builtinusers[x];
        } else {
            $scope.query.builtinusers[x] = x;
        }
    };
    $scope.toggleUser = function (x) {
        if ($scope.query.ctripusers[x]) {
            delete $scope.query.ctripusers[x];
        } else {
            $scope.query.ctripusers[x] = x;
        }
    };
    $scope.toggleStatus = function (x) {
        if ($scope.query.status[x]) {
            delete $scope.query.status[x];
        } else {
            $scope.query.status[x] = x;
        }
    };

    $scope.toggleSlbOps = function (x) {
        if ($scope.query.slbops[x]) {
            if (x == 'activate') {
                delete $scope.query.slbops['activateSlb'];
            }
            if (x == 'new') {
                delete $scope.query.slbops['add'];
            }
            delete $scope.query.slbops[x];
        } else {
            var t = x;
            if (x == 'activate') {
                t += ',' + 'activateSlb';
            }
            if (x == 'new') {
                t += ',' + 'add';
            }
            $scope.query.slbops[x] = t;
        }
    };
    $scope.toggleVsOps = function (x) {
        if ($scope.query.vsops[x]) {
            if (x == 'new') {
                delete $scope.query.vsops['addVirtualServer'];
            }
            if (x == 'update') {
                delete $scope.query.vsops['updateVirtualServer'];
            }
            if (x == 'activate') {
                delete $scope.query.vsops['activateVirtualServer'];
            }
            if (x == 'deactivate') {
                delete $scope.query.vsops['deactivateVirtualServer'];
            }
            if (x == 'delete') {
                delete $scope.query.vsops['deleteVirtualServer'];
            }
            delete $scope.query.vsops[x];

        } else {
            var t = x;
            if (x == 'new') {
                t += ',addVirtualServer';
            }
            if (x == 'update') {
                t += ',updateVirtualServer';
            }
            if (x == 'activate') {
                t += ',activateVirtualServer';
            }
            if (x == 'deactivate') {
                t += ',deactivateVirtualServer';
            }
            if (x == 'delete') {
                t += ',deleteVirtualServer';
            }
            $scope.query.vsops[x] = t;
        }
    };
    $scope.toggleGroupOps = function (x) {
        if ($scope.query.groupops[x]) {
            if (x == 'new') {
                delete $scope.query.groupops['add'];
            }
            if (x == 'activate') {
                delete $scope.query.groupops['activateGroup'];
            }
            if (x == 'deactivate') {
                delete $scope.query.groupops['deactivateGroup'];
            }
            delete $scope.query.groupops[x];

        } else {
            var t = x;
            if (x == "new") {
                t += ',add';
            }
            if (x == 'activate') {
                t += ',activateGroup';
            }
            if (x == 'deactivate') {
                t += ',deactivateGroup';
            }
            $scope.query.groupops[x] = t;
        }
    };
    $scope.togglePolicyOps = function (x) {
        if ($scope.query.policyops[x]) {
            if (x == 'new') {
                delete $scope.query.policyops['add'];
            }
            if (x == 'activate') {
                delete $scope.query.policyops['activateGroup'];
            }
            if (x == 'deactivate') {
                delete $scope.query.policyops['deactivateGroup'];
            }
            delete $scope.query.policyops[x];

        } else {
            var t = x;
            if (x == "new") {
                t += ',add';
            }
            if (x == 'activate') {
                t += ',activateGroup';
            }
            if (x == 'deactivate') {
                t += ',deactivateGroup';
            }
            $scope.query.policyops[x] = t;
        }
    };
    $scope.toggleDrOps = function (x) {
        if ($scope.query.drops[x]) {
            if (x == 'new') {
                delete $scope.query.drops['add'];
            }
            if (x == 'activate') {
                delete $scope.query.drops['activateDr'];
            }
            if (x == 'deactivate') {
                delete $scope.query.drops['deactivateDr'];
            }
            delete $scope.query.drops[x];

        } else {
            var t = x;
            if (x == "new") {
                t += ',add';
            }
            if (x == 'activate') {
                t += ',activateDr';
            }
            if (x == 'deactivate') {
                t += ',deactivateDr';
            }
            $scope.query.drops[x] = t;
        }
    };

    $scope.toggleServerOps = function (x) {
        if ($scope.query.serverops[x]) {
            delete $scope.query.serverops[x];

        } else {
            var t = x;
            $scope.query.serverops[x] = t;
        }
    };

    $scope.isSelectedSlbOps = function (x) {
        if ($scope.query.slbops[x]) {
            return "label-info";
        }
    };
    $scope.isSelectedVsOps = function (x) {
        if ($scope.query.vsops[x]) {
            return "label-info";
        }
    };
    $scope.isSelectedGroupOps = function (x) {
        if ($scope.query.groupops[x]) {
            return "label-info";
        }
    };
    $scope.isSelectedGroupOps = function (x) {
        if ($scope.query.groupops[x]) {
            return "label-info";
        }
    };
    $scope.isSelectedPolicyOps = function (x) {
        if ($scope.query.policyops[x]) {
            return "label-info";
        }
    };
    $scope.isSelectedDrOps = function (x) {
        if ($scope.query.drops[x]) {
            return "label-info";
        }
    };
    $scope.isSelectedServerOps = function (x) {
        if ($scope.query.serverops[x]) {
            return "label-info";
        }
    };

    $scope.isSelectedUser = function (target) {
        if ($scope.query.ctripusers[target]) {
            return "label-info";
        }
    };
    $scope.isSelectedBuiltinUser = function (target) {
        if ($scope.query.builtinusers[target]) {
            return "label-info";
        }
    };
    $scope.isSelectedStatus = function (target) {
        if ($scope.query.status[target]) {
            return "label-info";
        }
    };

    // Toggle
    $scope.showMoreUser = false;
    $scope.showMoreGroupOps = false;

    $scope.multiTagsClass = function () {
        return $scope.showMoreUser ? '' : 'multi-tags-collapse';
    };
    $scope.collapseBtnClass = function () {
        return $scope.showMoreUser ? 'fa fa-chevron-down' : 'fa fa-chevron-left';
    };
    $scope.toggleShowMoreUser = function () {
        $scope.showMoreUser = !$scope.showMoreUser;
    };

    $scope.multiGroupsOpClass = function () {
        return $scope.showMoreGroupOps ? '' : 'multi-tags-collapse';
    };
    $scope.collapseGroupOpsBtnClass = function () {
        return $scope.showMoreGroupOps ? 'fa fa-chevron-down' : 'fa fa-chevron-left';
    };
    $scope.toggleShowMoreGroupOps = function () {
        $scope.showMoreGroupOps = !$scope.showMoreGroupOps;
    };
    $scope.dataLoaded = false;
    $scope.env = '';
    $scope.loadData = function (hashData) {
        if ($scope.env == hashData.env && $scope.dataLoaded) return;
        $scope.data.slbops = {
            'new': '新建',
            update: '更新',
            activate: '激活',
            deactivate: '下线',
            'delete': '删除',
            'addServer': '扩容',
            'removeServer': '缩容',
            'setRule': '更新规则',
            'deleteRule': '删除规则'
        };
        $scope.data.policyops = {
            'new': '新建',
            update: '更新',
            activate: '激活',
            deactivate: '下线'
        };
        $scope.data.drops = {
            'new': '新建',
            update: '更新',
            activate: '激活',
            deactivate: '下线'
        };
        $scope.data.vsops = {
            'new': '新建',
            update: '更新',
            activate: '激活',
            deactivate: '下线',
            'delete': '删除',
            addDomain: '新增Domain',
            removeDomain: '删除Domain',
            'setRule': '更新规则',
            'deleteRule': '删除规则'
        };
        $scope.data.groupops = {
            'new': '新建',
            update: '更新',
            bindVs: '绑定新的VS',
            unbindVs: '解绑VS',
            updateCheckUri: '更新健康检测地址',
            activate: '激活',
            deactivate: '下线',
            'delete': '删除',
            addMember: '新增实例',
            updateMember: '更新实例',
            removeMember: '删除实例',
            upMember: '实例拉入',
            downMember: '实例拉出',
            pullIn: '发布拉入',
            pullOut: '发布拉出',
            raise: '健康拉入',
            fall: '健康拉出',
            'setRule': '更新规则',
            'deleteRule': '删除规则'
        };
        $scope.data.serverops = {
            'downServer': '服务器拉出',
            'upServer': '服务器拉入'
        };
        $scope.data.builtinusers = ['healthChecker', 'releaseSys', 'opSys'];
        $scope.data.opstates = ['成功', '失败'];
    };
    $scope.applyHashData = function (hashData) {
        $scope.query.startTime = new Date(hashData.startTime);
        if (hashData.endTime) {
            $scope.query.endTime = new Date(hashData.endTime);
        }
        else {
            $scope.query.endTime = new Date($scope.query.startTime.getTime() + 1000 * 60 * 90);
        }

        $scope.startTime = $.format.date($scope.query.startTime, 'yyyy-MM-dd HH:mm:00');
        $scope.endTime = $.format.date($scope.query.endTime, 'yyyy-MM-dd HH:mm:00');

        $scope.query.slbops = {};
        if (hashData.slbOperations) {
            $.each(hashData.slbOperations.split(","), function (i, val) {
                $scope.query.slbops[val] = val;
            })
        }

        $scope.query.vsops = {};
        if (hashData.vsOperations) {
            $.each(hashData.vsOperations.split(","), function (i, val) {
                $scope.query.vsops[val] = val;
            })
        }
        $scope.query.groupops = {};
        if (hashData.groupOperations) {
            $.each(hashData.groupOperations.split(","), function (i, val) {
                $scope.query.groupops[val] = val;
            })
        }
        $scope.query.policyops = {};
        if (hashData.policyOperations) {
            $.each(hashData.policyOperations.split(","), function (i, val) {
                $scope.query.policyops[val] = val;
            })
        }
        $scope.query.drops = {};
        if (hashData.drOperations) {
            $.each(hashData.drOperations.split(","), function (i, val) {
                $scope.query.drops[val] = val;
            })
        }
        $scope.query.serverops = {};
        if (hashData.serverOperations) {
            $.each(hashData.serverOperations.split(","), function (i, val) {
                $scope.query.serverops[val] = val;
            })
        }

        $scope.query.status = {};
        if (hashData.logStatus) {
            $.each(hashData.logStatus.split(","), function (i, val) {
                $scope.query.status[val] = val;
            })
        }

        $scope.query.builtinusers = {};
        if (hashData.logBuiltinUser) {
            $.each(hashData.logBuiltinUser.split(","), function (i, val) {
                $scope.query.builtinusers[val] = val;
            })
        }

        $scope.query.ctripusers = {};
        if (hashData.logUsers) {
            $.each(hashData.logUsers.split(","), function (i, val) {
                $scope.query.ctripusers[val] = val;
            })
        }
    };
    $scope.clearQuery = function () {
        var start;
        var end;
        var d = new Date();
        d = d.setTime(d.getTime() - 3 * 12 * 30 * 24 * 1000 * 60 * 60);
        start = $.format.date(d, 'yyyy-MM-dd HH:mm:00');

        d = new Date();
        var e = d.setTime(d.getTime() + 1000 * 60 * 90);
        end = $.format.date(e, 'yyyy-MM-dd HH:mm:00');


        $scope.query.slbops = {};
        $scope.query.vsops = {};
        $scope.query.groupops = {};
        $scope.query.serverops = {};
        $scope.query.policyops = {};
        $scope.query.drops = {};

        $scope.query.ctripusers = {};
        $scope.query.builtinusers = {};
        $scope.query.status = {};
        $scope.startTime = start;
        $scope.endTime = end;
    };

    $scope.executeQuery = function () {
        var hashData = {};
        hashData.startTime = encodeURIComponent($.format.date(new Date($scope.startTime), 'yyyy-MM-dd HH:mm'));
        hashData.endTime = encodeURIComponent($.format.date(new Date($scope.endTime), 'yyyy-MM-dd HH:mm'));

        hashData.slbOperations = _.values($scope.query.slbops);
        hashData.vsOperations = _.values($scope.query.vsops);
        hashData.groupOperations = _.values($scope.query.groupops);
        hashData.serverOperations = _.values($scope.query.serverops);
        hashData.policyOperations = _.values($scope.query.policyops);
        hashData.drOperations = _.values($scope.query.drops);

        hashData.logStatus = _.values($scope.query.status);
        hashData.logUsers = _.values($scope.query.ctripusers);
        hashData.logBuiltinUser = _.values($scope.query.builtinusers);
        hashData.timeStamp = new Date().getTime();
        H.setData(hashData);
    };

    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        var startTime = hashData.startTime;
        var endTime = hashData.endTime;

        if (!startTime) {
            startTime = new Date().getTime() - 3 * 365 * 24 * 60 * 60 * 1000;
        } else {
            startTime = new Date(startTime).getTime();
        }
        if (!endTime) {
            endTime = new Date().getTime() + 60 * 60 * 1000;
        } else {
            endTime = new Date(endTime).getTime();
        }

        startTime = $.format.date(new Date(startTime), 'yyyy-MM-dd HH:mm:00');
        endTime = $.format.date(new Date(endTime), 'yyyy-MM-dd HH:mm:00');

        startTime = new Date(decodeURIComponent(startTime.replace(/-/g, '/')));
        endTime = new Date(decodeURIComponent(endTime.replace(/-/g, '/')));

        hashData.startTime = startTime;
        hashData.endTime = endTime;

        $scope.loadData(hashData);
        $scope.applyHashData(hashData);
    };
    H.addListener("trafficApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("ops-query-area"), ['opsLogQueryApp']);

var opsResultApp = angular.module('opsResultApp', ['ui.bootstrap', 'ui.bootstrap.datetimepicker', 'angucomplete-alt', 'http-auth-interceptor','ngSanitize']);
opsResultApp.controller('opsResultController', function ($scope, $http, $filter, $q) {
    $scope.data = [];
    var ruleMapping = {
        'REQUEST_INTERCEPT_RULE': '反爬设置',
        PROXY_READ_TIMEOUT: '请求超时时间设置',
        CLIENT_MAX_BODY_SIZE: '请求体大小设置',
        CLIENT_BODY_BUFFER_SIZE: '请求体缓冲区大小设置',
        ENABLE_HSTS: '强制HSTS设置',
        GZIP: 'Gzip设置',
        KEEP_ALIVE_TIMEOUT: '客户端KeepAlive时间设置',
        LARGE_CLIENT_HEADER: 'Header缓冲区大小设置',
        UPSTREAM_KEEP_ALIVE_TIMEOUT: '后端链接KeepAlive时间设置',
        PROTOCOL_RESPONSE_HEADER: '添加SLB协议版本响应头设置',
        PROXY_REQUEST_BUFFER_ENABLE: '代理缓存配设置',
        HIDE_HEADER: '隐藏响应头设置',
        ADD_HEADER: '新增响应头设置',
        REWRITE_BY_LUA: 'Rewrite By Lua脚本设置',
        INIT_BY_LUA: 'Init By Lua脚本设置',
        ACCESS_BY_LUAL: 'Access By Lua设置',
        SET_BY_LUA: 'SET By Lua 设置',
        REQUEST_ID_ENABLE: '开启关闭RequestID设置',
        DEFAULT_LISTEN_RULE: 'Default Server监听设置',
        SERVER_HTTP2_CONFIG_RULE: '开启HTTP2设置',
        SERVER_PROXY_BUFFER_SIZE_RULE: '代理缓存配置',
        DIRECTIVE_CONFIG: '指令Directive配置',
        SSL_CONFIG: 'SSL 配置',
        CONDITION_REDIRECT: '跳转路由配置',
        ERROR_PAGE: '开启关闭错误页',
        DEFAULT_ERROR_PAGE: '默认错误页配置',
        DEFAULT_SSL_CONFIG: '默认SSL设置',
        UPSTREAM_KEEP_ALIVE_COUNT: '后端KeepAlive设置',
        FAVICON_RULE: 'Favicon 配置'
    };
    $(document).ready(function () {
        setInterval(function () {
            $(".tooltip-msg").fadeOut(200).fadeIn(200);
        }, 5000);
    });
    $scope.initTable = function () {

        var resource = $scope.resource;

        var table = resource['log']['log_operationLogApp_table'];
        var time = table['time'];
        var targetid = table['normalid'];
        var targettype = table['type'];
        var version = table['version'];
        var operation = table['operation'];
        var targetip = table['targetip'];
        var operationmsg = table['operationmsg'];
        var user = table['user'];
        var clientip = table['clientip'];
        var links = table['links'];
        var failreason = table['failreason'];
        var status = table['status'];
        var loadingtext = table['loadingsys'];
        var norecordtext = table['norecordtextsys'];
        var successtext = table['成功'];
        var failtext = table['失败'];
        $('#logs-data-table').bootstrapTable({
            toolbar: '.op-log-toolbar',
            columns: [[
                {
                    field: 'datelong',
                    title: time,
                    align: 'left',
                    valign: 'middle',
                    width: '350px',
                    formatter: function (value, row, index) {
                        var d = new Date(value);
                        var result = parseDateTime(d, true);
                        return result;
                    },
                    sortable: true
                },
                {
                    field: 'target-id',
                    title: targetid,
                    align: 'left',
                    resetWidth: true,
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var str = '';
                        switch (row.type) {
                            case "SLB": {
                                str = '<a href="/portal/slb#?env=' + G.env + '&slbId=' + value + '">' + value + '</a>';
                                break;
                            }
                            case "VS": {
                                str = '<a href="/portal/vs#?env=' + G.env + '&vsId=' + value + '">' + value + '</a>';
                                break;
                            }
                            case "GROUP": {
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + value + '">' + value + '</a>';
                                break;
                            }
                            case "SERVER": {
                                str = '<a href="/portal/group#?env=' + G.env + '&groupId=' + value + '">' + value + '</a>';
                                break;
                            }
                            case "POLICY": {
                                str = '<a href="/portal/policy#?env=' + G.env + '&policyId=' + value + '">' + value + '</a>';
                                break;
                            }
                            case "DR": {
                                str = '<a href="/portal/dr#?env=' + G.env + '&drId=' + value + '">' + value + '</a>';
                                break;
                            }
                            default: {
                                str = '-';
                                break;
                            }
                        }
                        return str;
                    }
                },
                {
                    field: 'type',
                    title: targettype,
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'version',
                    title: version,
                    align: 'left',
                    visible: false,
                    valign: 'middle',
                    events: DiffVersionEvent,
                    formatter: function (value, row, index) {
                        if (value) {
                            if (value != '-' && value != 1) {
                                var str = '<div class="pull-left">' +
                                    '<span>' + value + '</span>';
                                if (versionChanged(row.operation)) {
                                    str += '<a class="diff-version-bt" title="对比"><span class="status-yellow" style="margin-left: 10px">(Diff)</span></a>';
                                }
                                str += '</div>';
                                return str;
                            } else {
                                return value;
                            }
                        } else {
                            return '-';
                        }

                    },
                    sortable: true
                },
                {
                    field: 'operation',
                    title: operation,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var str = '';
                        var result = $scope.getOperationText(value);
                        str += '<div>' + result + '</div>';
                        return str;
                    }
                },
                {
                    field: 'description',
                    title: operationmsg,
                    align: 'left',
                    valign: 'middle',
                    width: '200px',
                    sortable: true,
                    formatter: function (value, row, index) {
                        if (value) {
                            var streeclass = row.operation == 'fall' ? 'status-red' : '';
                            var str = '<div class="' + streeclass + '" style="word-break: break-all">' + value + '</div>';

                            var rulesOperations = ['setRule', 'deleteRule'];
                            if (rulesOperations.indexOf(row['operation']) != -1) {
                                str += '<ul style="margin-left: -20px">'
                                var data = JSON.parse(row.data);
                                var ruleData = data['rule-datas'];
                                if (ruleData && ruleData.length > 0) {
                                    _.map(_.uniq(_.pluck(ruleData, 'rule-type')), function (v) {
                                        str += '<li class="' + streeclass + '">' + ruleMapping[v] + '</li>';
                                    });
                                }
                                str += '</ul>';
                            }

                            return str;
                        }
                        else return '-';
                    }
                },
                {
                    field: 'target-ip',
                    title: targetip,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        var str = '';
                        if (Array.isArray(value)) {
                            $.each(value, function (i, item) {
                                if (item != '-') {
                                    str += '<a target="_blank" href="' + G[G.env].urls.webinfo + '/?Keyword=' + item + '">' + item + '</a></br>';
                                } else {
                                    str = '-';
                                }
                            });
                        }
                        return str;
                    },
                    sortable: true
                },
                {
                    field: 'user-name',
                    title: user,
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return "<a href='/portal/user#?env=" + $scope.env + "&userId=" + value + "'>" + value + "</a>";
                    }
                },
                {
                    field: 'client-ip',
                    title: clientip,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return value;
                    },
                    sortable: true
                },
                {
                    field: 'slb-version',
                    title: version,
                    visible: false,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        return value;
                    },
                    sortable: true
                },
                {
                    field: 'err-msg',
                    title: failreason,
                    align: 'left',
                    valign: 'middle',
                    events: ViewEventDetailEvent,
                    formatter: function (value, row, index) {
                        if (value) {
                            var str = '<div><span style="word-break: break-all" class="logs-error-message-collapse pull-left wrench-text">' +
                                value +
                                '</span><span title="" class="expander pull-right" style="cursor: pointer"><i class="status-blue fa fa-chevron-down wrench"></i></span></div>';
                            return str;
                        }
                        return '-';
                    },
                    sortable: true
                },
                {
                    field: 'logs',
                    title: links,
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index) {
                        if (row['type'] != 'GROUP') return;
                        var env = $scope.env;
                        var dashboard_env = env == 'pro' ? 'PROD' : env.toUpperCase();
                        // clog, dashboard, es,
                        var startTime = row['open-time'];

                        var log_start = new Date(startTime).getTime() - 1000 * 60 * 60;
                        var log_end = new Date(startTime).getTime() + 1000 * 60 * 60;

                        var groupId = row['target-id'];
                        var appId = $scope.groupData[groupId] ? $scope.groupData[groupId]['app-id'] : '-';

                        var clogStartTime = $.format.date(log_start, 'yyyy-MM-dd_HH:mm:ss');
                        var clogEndTime = $.format.date(log_end, 'yyyy-MM-dd_HH:mm:ss');

                        var dashboardStartTime = $.format.date(log_start, 'yyyy-MM-dd HH:mm:ss');
                        var dashboardEndTime = $.format.date(log_end, 'yyyy-MM-dd HH:mm:ss');

                        var catStartTime = $.format.date(new Date(startTime).getTime(), 'yyyyMMddHH');

                        var dashboard = G.dashboardportal + '/#env=' + dashboard_env + '&metric-name=slb.req.count&interval=3m&start-time=' + dashboardStartTime + '&end-time=' + dashboardEndTime + '&chart=line&aggregator=sum&ts=1515983913677&tags={"group_id":["' + groupId + '"]}&group-by=[]';
                        var clogLink = 'fromDate=' + clogStartTime + '~toDate=' + clogEndTime + '~app=' + appId + '~~hostSearch=' + row['target-ip'];
                        var esLink;


                        var query = "group_id%3D'" + groupId + "'%20AND%20group_server%3D'" + row['target-ip'] + "*'";
                        if (G[env] && G[env].urls.es) {
                            esLink = groupApp.getEsHtml(query);
                        }

                        var str = '<div style="width: 300px">';
                        if (G[env] && G[env].urls.dashboard) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left dashboard" title="Dashboard" target="_blank" href=\'' + dashboard + '\'>Dashboard</a>' +
                                '</div>';
                        }


                        if (G[env] && G[env].urls.hickwall) {
                            str += '<div class="system-link">' +
                                '<a class="pull-left hickwall" title="Hickwall" target="_blank"' +
                                ' href="' + G[G.env].urls.hickwall + '/dashboard/host/' + row['target-ip'] + '?from=' + log_start + '&to=' + log_end + '">Hickwall</a>' +
                                '</div>';
                        }

                        if (G[env] && G[env].urls.cat) {
                            str += '<div class="system-link">' +
                                '<a class="pull-right cat" target="_blank" href="' + G[G.env].urls.cat + '/cat/r/h?domain=' + appId + '&ip=' + row['target-ip'] + '&date=' + catStartTime + '">CAT</a>' +
                                '</div>';
                        }
                        if (esLink) {
                            str += esLink;
                        }

                        if (G[env] && G[env].urls.clog) {
                            str += '<div class="system-link">' +
                                '<a class="pull-right clog" target="_blank" href="' + G[G.env].urls.clog + clogLink + '">Clog</a>' +
                                '</div>';
                        }

                        str += '</div>';
                        return str;
                    },
                    sortable: true
                },
                {
                    field: 'success',
                    title: status,
                    align: 'center',
                    valign: 'middle',
                    events: ViewEventDetailEvent,
                    formatter: function (value, row, index) {
                        if (value) return "<button type='button'' class='btn btn-info statusclass'>"+successtext+"</button>";
                        return "<button type='button'' class='btn btn-danger statusclass'>"+failtext+"</button>";
                    },
                    sortable: true
                }

            ], []],
            search: true,
            showRefresh: true,
            showColumns: true,
            sortName: 'datelong',
            sortOrder: 'desc',
            data: $scope.data,
            classes: "table-bordered  table-hover table-striped table",
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            pageSize: 20,
            resizable: true,
            resizeMode: 'overflow',
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            responseHandler: "responseHandler",
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> "+loadingtext;
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> '+norecordtext;
            }
        });
    };
    window.DiffVersionEvent = {
        'click .diff-version-bt': function (e, value, row) {
            getGroupDataByVersion(row);
        }
    };
    window.ViewEventDetailEvent = {
        'click .expander': function (e, value, row) {
            var target = e.currentTarget;
            target = $(target);
            var current = target;
            var firstSpan = target.parent('div').find('.wrench-text');
            var wrench = current.find('.wrench');

            if (firstSpan) {
                var hasTextCollapse = $(firstSpan).hasClass('logs-error-message-collapse');
                var hasWrenchCollapse = $(wrench).hasClass('fa-chevron-down');
                if (hasTextCollapse) {
                    $(firstSpan).removeClass('logs-error-message-collapse');
                } else {
                    $(firstSpan).addClass('logs-error-message-collapse');
                }
                if (hasWrenchCollapse) {
                    $(wrench).removeClass('fa-chevron-down').addClass('fa-chevron-up')
                } else {
                    $(wrench).removeClass('fa-chevron-up').addClass('fa-chevron-down')
                }
            }
        },
        'click .statusclass': function (e, value, row) {
            var resource = $scope.resource;
            $('.operation-detail-div').html('');
            var statusText = '';
            var statusCss = '';
            var type = row.type;
            var target = row['target-id'];
            var operation = row['operation'];
            var user = row['user-name'];
            var success = row['success'];
            var userip = row['client-ip'];
            var exception = '-';
            if (row['err-msg']) {
                exception = row['err-msg'];
            }
            if (success == true) {
                statusCss = 'status-green';
                statusText = '成功';
            } else {
                statusCss = 'status-red';
                statusText = '失败';
            }

            var op_url = '-';
            var op_param = '-';
            var reason = '';
            var data = row['data'];
            if (data) {
                if (IsJsonString(data)) {
                    var t = JSON.parse(data);
                    op_url = t.uri;
                    op_param = t.query;
                    reason = t['description'];
                }
            }
            var dateTime = row['date-time'];

            var state = resource['log']['log_operationLogApp_detail']['state'];
            var typetext = resource['log']['log_operationLogApp_detail']['type'];
            var id = resource['log']['log_operationLogApp_detail']['id'];
            var op = resource['log']['log_operationLogApp_detail']['op'];
            var opurl = resource['log']['log_operationLogApp_detail']['opurl'];
            var opparam = resource['log']['log_operationLogApp_detail']['opparam'];
            var oper = resource['log']['log_operationLogApp_detail']['oper'];
            var opip = resource['log']['log_operationLogApp_detail']['opip'];
            var optime = resource['log']['log_operationLogApp_detail']['optime'];
            var failreason = resource['log']['log_operationLogApp_detail']['reason'];
            var opreason = resource['log']['log_operationLogApp_detail']['opreason'];

            var str = '<div class="" style="margin: 0 auto">' +
                '<table class="table ng-scope ng-table operation-detail-table">' +
                ' <tr><td><b>'+state+':</b></td>' +
                ' <td class="' + statusCss + '" style="font-weight: bold">' + statusText + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+typetext+':</b></td>' +
                ' <td>' + type + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+id+':</b></td>' +
                ' <td>' + target + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+op+':</b></td>' +
                ' <td>' + operation + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+opurl+':</b></td>' +
                ' <td>' + op_url + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+opparam+':</b></td>' +
                ' <td>' + op_param + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+oper+':</b></td>' +
                ' <td>' + user + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+opip+':</b></td>' +
                ' <td>' + userip + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+optime+':</b></td>' +
                ' <td>' + dateTime + '</td>' +
                ' </tr>' +
                ' <tr><td><b>'+opreason+':</b></td>' +
                ' <td><span style="word-break: break-all">' + reason + '</span></td>' +
                ' </tr>' +
                ' <tr><td style="width: 10%"><b>'+failreason+':</b></td>' +
                ' <td style="word-break: break-all">' + exception + '</td>' +
                ' </tr>' +
                '</table>' +
                '</div>';
            $('.operation-detail-div').append(str);
            $('#output-div').modal('show');
        }
    };
    $scope.getLogsHeader = function () {
        var resource = $scope.resource;
        var d = $scope.data;
        if (resource) {
            var prefix = resource['log']['log_operationLogApp_header_prefix'];
            var suffix = resource['log']['log_operationLogApp_header_suffix'];

            return '<span>' + prefix + '<b>' + d.length + '</b>' + suffix + '</span>'
        }
    };

    function versionChanged(change) {
        var result = false;
        switch (change) {
            case "deactivate":
                result = true;
                break;
            case "activate":
                result = true;
                break;
            case "update":
                result = true;
                break;
            case "bindVs":
                result = true;
                break;
            case "unbindVs":
                result = true;
                break;
            case "updateCheckUri":
                result = true;
                break;
            case "addMember":
                result = true;
                break;
            case "updateMember":
                result = true;
                break;
            case "removeMember":
                result = true;
                break;
            case "removeMember":
                result = true;
                break;
            case "setRule":
                result = true;
                break;
            case "deleteRule":
                result = true;
                break;
        }
        return result;
    }

    function getGroupDataByVersion(row) {
        var version = row['version'];
        var type = row['type'];

        var currentVersion = version;
        var id = row['target-id'];

        var c = currentVersion;
        var p = currentVersion - 1;


        var param0, param1, request0, request1;
        type = type.toLowerCase();
        switch (type) {
            case 'group': {
                if (row['operation'] == 'activate') {
                    var gd = JSON.parse(row['data']);
                    var gd_datas = gd['group-datas'];
                    var gd_sort = _.sortBy(gd_datas, 'version');
                    p = gd_sort[0].version;
                }
                param0 = {
                    groupId: id,
                    version: c
                };
                request0 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/group',
                    params: param0
                };
                param1 = {
                    groupId: id,
                    version: p
                };
                request1 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/group',
                    params: param1
                };
                break;
            }
            case 'vs': {
                if (row['operation'] == 'activate') {
                    var gd = JSON.parse(row['data']);
                    var gd_datas = gd['vs-datas'];
                    var gd_sort = _.sortBy(gd_datas, 'version');
                    p = gd_sort[0].version;
                }
                param0 = {
                    vsId: id,
                    version: c
                };
                request0 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/vs',
                    params: param0
                };
                param1 = {
                    vsId: id,
                    version: p
                };
                request1 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/vs',
                    params: param1
                };
                break;
            }
            case 'slb': {
                if (row['operation'] == 'activate') {
                    var gd = JSON.parse(row['data']);
                    var gd_datas = gd['slb-datas'];
                    var gd_sort = _.sortBy(gd_datas, 'version');
                    p = gd_sort[0].version;
                }
                param0 = {
                    slbId: id,
                    version: c
                };
                request0 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/slb',
                    params: param0
                };
                param1 = {
                    slbId: id,
                    version: p
                };
                request1 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/slb',
                    params: param1
                };
                break;
            }
            case 'policy': {
                if (row['operation'] == 'activate') {
                    var gd = JSON.parse(row['data']);
                    var gd_datas = gd['policy-datas'];
                    var gd_sort = _.sortBy(gd_datas, 'version');
                    p = gd_sort[0].version;
                }
                param0 = {
                    policyId: id,
                    version: c
                };
                request0 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/policy',
                    params: param0
                };
                param1 = {
                    policyId: id,
                    version: p
                };
                request1 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/policy',
                    params: param1
                };
                break;
            }
            case 'dr': {
                if (row['operation'] == 'activate') {
                    var gd = JSON.parse(row['data']);
                    var gd_datas = gd['dr-datas'];
                    var gd_sort = _.sortBy(gd_datas, 'version');
                    p = gd_sort[0].version;
                }
                param0 = {
                    drId: id,
                    version: c
                };
                request0 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/dr',
                    params: param0
                };
                param1 = {
                    drId: id,
                    version: p
                };
                request1 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/archive/dr',
                    params: param1
                };
                break;
            }
            default :
                break;
        }
        var compareData = {};
        $q.all(
            [
                $http(request0).success(
                    function (resp) {
                        compareData.current = resp;
                    }
                ),
                $http(request1).success(
                    function (resp) {
                        compareData.previous = resp;
                    }
                )
            ]
        ).then(
            function () {
                var baseText = JSON.stringify(U.sortObjectFileds(compareData.previous), null, "\t");
                var NewText = JSON.stringify(U.sortObjectFileds(compareData.current), null, "\t");
                var target = document.getElementById('fileDiffForm1');

                var resource = $scope.resource;

                var before = resource['log']['log_operationLogApp_diff']['before'];
                var after = resource['log']['log_operationLogApp_diff']['after'];
                var online = resource['log']['log_operationLogApp_diff']['online'];
                var offline = resource['log']['log_operationLogApp_diff']['offline'];

                var ptext = before + p;
                var ctext = after + c;
                if (row['operation'] == 'activate') {
                    ptext = online + p;
                    ctext = offline + c;
                }

                diffTwoObjects(target, baseText, NewText, ptext, ctext);
                $('#diffVSDiv').modal('show');
            }
        );
    };
    $scope.getOperationText = function (text) {
        var t = $scope.resource['log']['log_operationLogApp_opmapping'][text];
        return t;
    };
    var parseDateTime = function (date, secondBool) {
        var year = date.getFullYear();

        var month = date.getMonth() + 1;
        if (month < 10) month = '0' + month;

        var day = date.getDate();
        if (day < 10) day = '0' + day;

        var hour = date.getHours();
        if (hour < 10) hour = '0' + hour;

        var minute = date.getMinutes();
        if (minute < 10) minute = '0' + minute;

        if (!secondBool) {
            return year + '-' + month + '-' + day + '_' + hour + ':' + minute + ':' + '00';
        } else {
            var second = date.getSeconds();
            if (second < 10) second = '0' + second;

            return year + '/' + month + '/' + day + ' ' + hour + ':' + minute + ':' + second;
        }
    };
    $scope.queryLogs = function (hashData) {
        var status = hashData.logStatus;
        var ctripuser = hashData.logUsers;
        var builtinuser = hashData.logBuiltinUser;

        var slbops = hashData.slbOperations;
        var vsops = hashData.vsOperations;
        var groupops = hashData.groupOperations;
        var serverops = hashData.serverOperations;
        var policyops = hashData.policyOperations;
        var drops = hashData.drOperations;

        var start = new Date(decodeURIComponent(hashData.startTime));
        var end = new Date(decodeURIComponent(hashData.endTime));
        var slbOpsArray = [];
        var vsOpsArray = [];
        var groupOpsArray = [];
        var serverOpsArray = [];
        var policyOpsArray = [];
        var drOpsArray = [];
        var statusArray = [];
        var userArray = [];
        if (slbops) {
            slbOpsArray = slbops.split(',');
        }
        if (vsops) {
            vsOpsArray = vsops.split(',');
        }
        if (groupops) {
            groupOpsArray = groupops.split(',');
        }
        if (serverops) {
            serverOpsArray = serverops.split(',');
        }
        if (policyops) {
            policyOpsArray = policyops.split(',');
        }
        if (drops) {
            drOpsArray = drops.split(',');
        }

        if (status) {
            var s = status.split(',');
            $.each(s, function (i, item) {
                if (item == '成功') statusArray.push('true');
                else {
                    statusArray.push('false');
                }
            });
        }
        if (ctripuser) {
            var w = ctripuser.split(',');
            userArray = userArray.concat(w);
        }
        if (builtinuser) {
            var v = builtinuser.split(',');
            userArray = userArray.concat(v);
        }
        $scope.data = [];
        var param = {
            fromDate: parseDateTime(start),
            toDate: parseDateTime(end)
        };
        var slbOpsObj = {
            name: 'op',
            value: slbOpsArray
        };
        var vsOpsObj = {
            name: 'op',
            value: vsOpsArray
        };
        var groupOpsObj = {
            name: 'op',
            value: groupOpsArray
        };
        var serverOpsObj = {
            name: 'op',
            value: serverOpsArray
        };
        var policyOpsObj = {
            name: 'op',
            value: policyOpsArray
        };
        var drOpsObj = {
            name: 'op',
            value: drOpsArray
        };

        var statusObj = {
            name: 'success',
            value: statusArray
        };
        var userObj = {
            name: 'user',
            value: userArray
        };

        var union = [slbOpsObj, vsOpsObj, groupOpsObj, serverOpsObj, policyOpsObj, drOpsObj, statusObj, userObj];
        var filter = _.filter(union, function (item) {
            return item.value.length > 0;
        });

        $scope.requestArray = [];
        var httpArray = [];
        if (filter.length == 0) {
            var request = {
                method: 'GET',
                url: G.baseUrl + '/api/logs',
                params: param
            };
            httpArray = [
                $http(request).success(function (response) {
                    $scope.queryResult = response;
                    $scope.data = response['operation-log-datas']
                })
            ];
        } else {
            var temp = [];
            if (slbOpsArray.length > 0) {
                union = [slbOpsObj, statusObj, userObj];
                temp = _.filter(union, function (item) {
                    return item.value.length > 0;
                });
                httpArray = httpArray.concat($scope.getRequestArray(temp, 'SLB', start, end));
            }
            if (vsOpsArray.length > 0) {
                union = [vsOpsObj, statusObj, userObj];
                temp = _.filter(union, function (item) {
                    return item.value.length > 0;
                });
                param.type = 'VS';
                httpArray = httpArray.concat(temp, $scope.getRequestArray(temp, 'VS', start, end));
            }
            if (serverOpsArray.length > 0) {
                union = [serverOpsObj, statusObj, userObj];
                temp = _.filter(union, function (item) {
                    return item.value.length > 0;
                });
                param.type = 'SERVER';
                httpArray = httpArray.concat(temp, $scope.getRequestArray(temp, undefined, start, end));
            }
            if (groupOpsArray.length > 0) {
                union = [groupOpsObj, statusObj, userObj];
                temp = _.filter(union, function (item) {
                    return item.value.length > 0;
                });
                param.type = 'GROUP';
                httpArray = httpArray.concat(temp, $scope.getRequestArray(temp, 'GROUP', start, end));
            }
            if (policyOpsArray.length > 0) {
                union = [policyOpsObj, statusObj, userObj];
                temp = _.filter(union, function (item) {
                    return item.value.length > 0;
                });
                param.type = 'POLICY';
                httpArray = httpArray.concat(temp, $scope.getRequestArray(temp, 'POLICY', start, end));
            }
            if (drOpsArray.length > 0) {
                union = [drOpsObj, statusObj, userObj];
                temp = _.filter(union, function (item) {
                    return item.value.length > 0;
                });
                param.type = 'DR';
                httpArray = httpArray.concat(temp, $scope.getRequestArray(temp, 'DR', start, end));
            }

            if (groupOpsArray.length == 0 && vsOpsArray.length == 0 && slbOpsArray.length == 0 && serverOpsArray.length == 0 && policyOpsArray.length == 0 && drOpsArray.length == 0) {
                union = [statusObj, userObj];
                temp = _.filter(union, function (item) {
                    return item.value.length > 0;
                });
                httpArray = httpArray.concat(temp, $scope.getRequestArray(temp, undefined, start, end));
            }
        }
        var groupsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/groups'
        };

        var groupsQuery = $http(groupsRequest).success(function (response, code) {
            $scope.groupData = _.indexBy(response['groups'], function (v) {
                return v.id;
            });
        });
        httpArray.push(groupsQuery);

        $q.all(httpArray).then(
            function () {
                if ($scope.queryResult.code) {
                    exceptionNotify("出错了!!", "加载Log 失败了， 失败原因" + $scope.queryResult.message, null);
                    return;
                } else {
                    setTimeout(
                        function () {
                            $('.alert-danger').remove();
                        },
                        1000
                    );
                }
                $.each($scope.data, function (i, item) {
                    var data = item.data;
                    var isJson = IsJsonString(data);
                    var reason = '';
                    var targetIp = ['-'];
                    var version = '-';
                    var targetId = '';
                    targetId = item['target-id'];
                    if (!isJson) {
                        var regit = /ip=\[(.*)\]/g;
                        var match = regit.exec(data);
                        if (match) {
                            var v = match[1];
                            if (v) {
                                targetIp = v.split(',');
                            }
                        }
                    } else {
                        data = JSON.parse(data);
                        item['description'] = data.description || '-';
                        targetIp = data.ips;
                        if (data['group-datas'] && data['group-datas'].length > 0) {
                            version = data['group-datas'][0].version;
                            targetId = data['group-datas'][0].id;
                        }
                        if (data['slb-datas'] && data['slb-datas'].length > 0) {
                            version = data['slb-datas'][0].version;
                        }
                        if (data['vs-datas'] && data['vs-datas'].length > 0) {
                            version = data['vs-datas'][0].version;
                        }
                        if (data['policy-datas'] && data['policy-datas'].length > 0) {
                            version = data['policy-datas'][0].version;
                        }
                    }

                    item['version'] = version;
                    item['target-id'] = targetId;
                    item['target-ip'] = targetIp;
                    item['open-time'] = item['date-time'];
                    var date = item['date-time'].replace(/-/g, '/');
                    item['datelong'] = new Date(date).getTime();
                });
                if (!$scope.data) {
                    $scope.data = [];
                }
                // show/hide group columns
                $('#logs-data-table').bootstrapTable("load", $scope.data);
                $('#logs-data-table').bootstrapTable("hideLoading");
            }
        );

    };

    var groupApp;

    $scope.getRequestArray = function (filter, type, start, end) {
        var httpArray = [];
        var query = getList(filter);
        $.each(query, function (i, item) {
            var param;
            if (type) {
                param = {
                    fromDate: parseDateTime(start),
                    toDate: parseDateTime(end),
                    type: type
                };
            } else {
                param = {
                    fromDate: parseDateTime(start),
                    toDate: parseDateTime(end)
                };
            }

            var last = item.lastIndexOf('&');
            item = item.substring(0, last);
            var q = item.split('&');
            $.each(q, function (j, item2) {
                var s = item2.split('=');
                param[s[0]] = s[1];
            });
            var request = {
                method: 'GET',
                url: G.baseUrl + '/api/logs',
                params: param
            };
            httpArray.push(
                $http(request).success(function (response) {
                    $scope.queryResult = response;
                    if (response['operation-log-datas'] && response['operation-log-datas'].length > 0) {
                        $scope.data = $scope.data.concat(response['operation-log-datas']);
                    }
                })
            );
        });
        return httpArray
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        }
        groupApp = new GroupApp(hashData, $http, $q, $scope.env);
        $('#logs-data-table').bootstrapTable("removeAll");
        $('#logs-data-table').bootstrapTable("showLoading");
        $scope.queryLogs(hashData);
        $scope.initTable();
    };
    H.addListener("opsResultApp", $scope, $scope.hashChanged);

    // Common functions
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
                align: 'right'
            },
            offset: {
                x: 0,
                y: 50
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

    function IsJsonString(str) {
        try {
            JSON.parse(str);
        } catch (e) {
            return false;
        }
        return true;
    }

    function getList(a) {
        var strs = [];
        var n = 1;
        for (var i = 0; i < a.length; i++) {
            n = n * a[i].value.length;
        }

        for (var j = 0; j < n; j++) {
            var c = '';
            for (var k = 0; k < a.length; k++) {
                var index = parseInt(Math.random() * a[k].value.length);
                c += a[k].name + '=' + a[k].value[index] + '&';
            }
            if (strs.indexOf(c) != -1) {
                n++;
            } else {
                strs.push(c);
            }
        }
        return strs;
    }

    function diffTwoObjects(targetDiv, baseText, newText, baseVersion, newVersion) {
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
angular.bootstrap(document.getElementById("ops-result-area"), ['opsResultApp']);
