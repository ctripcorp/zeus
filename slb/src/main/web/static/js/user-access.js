/**
 * Created by ygshen on 2016/10/17.
 */
var summaryInfoApp = angular.module('summaryInfoApp', ['http-auth-interceptor', 'angucomplete-alt']);
summaryInfoApp.controller('summaryController', function ($scope, $http, $q) {
    $scope.resource= H.resource;
    var resource = $scope.resource;
    $scope.query={
        userId:''
    };
    $scope.cacheRequestFn = function (str) {
        return {q: str, timestamp: new Date().getTime()};
    };

    $scope.remoteUrl = function () {
        return  $scope.context.targetsUrl;
    };

    $scope.context = {
        targetIdName: 'userId',
        targetNameArr: ['email', 'id', 'name'],
        targetsUrl: G.baseUrl+ '/api/meta/users',
        targetsName: 'users'
    };

    $scope.target = {
        id: null,
        name: ''
    };
    $scope.targets = {};

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
                                $scope.target.name = $scope.target.id ;
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
                messageNotify(angular.equals(resource, {}) ? "切换用户:": resource.userAccess.js.msg1,angular.equals(resource, {}) ? "成功切换至用户: ": resource.userAccess.js.msg2+toId,null);
            }
        }
    };
    $scope.clickTarget= function () {
        $('#targetSelector_value').css('width','250px');
    };
    $scope.data = {
        current: '基本信息',
        links:['基本信息', '权限','操作日志'],
        hrefs: {
            '基本信息': '/portal/user',
            '操作日志': '/portal/user/log',
            '权限':'/portal/user/user-access'
        }
    };

    $scope.isCurrentInfoPage = function (link) {
        return $scope.data.current == link ? 'current' : '';
    };

    $scope.generateInfoLink = function (link) {
        var b = $scope.data.hrefs[link] + "#?env=" + G.env;
        if($scope.query.userId){
            b+='&userId='+$scope.query.userId;
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
        if(hashData.userId){
            $scope.query.userId=hashData.userId;
        }
        $scope.target = {};
        if(hashData.userId){
            $scope.target.name = hashData.userId;
        }else{
            $scope.target.name = 'Me';
        }
    };
    H.addListener("summaryInfoApp", $scope, $scope.hashChanged);
    function messageNotify(title, message, url){
        var notify = $.notify({
            icon: '',
            title: title,
            message: message,
            url:url,
            target: '_self'
        },{
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
            delay:1000,
            spacing:5,
            z_index: 1031,
            mouse_over: 'pause'
        });
    }
});
angular.bootstrap(document.getElementById("summary-area"), ['summaryInfoApp']);

//InfoLinksComponent: info links
var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.resource= H.resource;
    var resource = $scope.resource;
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'home': {
                link = "/portal/user-home#?env=" + G.env;
                break;
            }
            case 'basic':
            {

                link = "/portal/user#?env=" + G.env;
                break;
            }
            case 'access':
            {
                link = "/portal/user/user-access#?env=" + G.env;
                break;
            }
            case 'log':
            {
                link = "/portal/user/log#?env=" + G.env;

                break;
            }
            case 'policy':
            {
                link = "/portal/user/user-trafficpolicy#?env=" + G.env;
                break;
            }
            case 'AB':
            {
                link = "/portal/user/user-normalpolicy#?env=" + G.env;
                break;
            }
            case 'drs': {
                link = "/portal/user/user-drs#?env=" + G.env;
                break;
            }
            case 'unhealthy':
            {
                link = "/portal/user/user-unhealthy#?env=" + G.env;
                break;
            }
            default:
                break;
        }
        if($scope.query.userId){
            link+='&userId='+$scope.query.userId;
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

var userEditInfoApp = angular.module('userEditInfoApp', ["http-auth-interceptor", "angucomplete-alt"]);
userEditInfoApp.controller('userEditInfoController', function ($scope, $http, $q) {

    /** Group Related:
     * A:ACTIVATE
     * D: DEACTIVATE
     * U: UPDATE
     * E: DELETE
     * R: READ
     * P: PROPERTY
     * S: SYNC
     * M: OP_MEMBER
     * L: OP_PULL
     * H: OP_HEALTH_CHECK
     **/
    /** SLB Related:
     * A:ACTIVATE
     * D: DEACTIVATE
     * U: UPDATE
     * E: DELETE
     * R: READ
     * P: PROPERTY
     * S: SYNC
     * I: ADMIN_INFO
     * W: WAF
     **/
    /** VS Related:
     * A:ACTIVATE
     * D: DEACTIVATE
     * U: UPDATE
     * E: DELETE
     * R: READ
     * P: PROPERTY
     * S: SYNC
     * C: CERT
     **/
    $scope.resource= H.resource;
    var resource = $scope.resource;
    $scope.query = {
        role: {
            id: '',
            'role-name': ''
        },
        slb: {
            id: ''
        },
        vs: {
            id: ''
        },
        group: {
            id: ''
        },
        user: {
            id: '',
            'user-name': '',
            email: '',
            bu: '',
            roles: [],
            'data-resources': []
        }
    };
    $scope.data = {};
    $scope.user = {
        roles: [],
        slbs: [],
        vses: [],
        groups: [],
        confs: [],
        cleans: [],
        locks: [],
        ips: [],
        auths: [],
        syncs: []
    };
    $scope.slbs = {};
    $scope.vses = {};
    $scope.groups = {};
    // Loading Area
    $scope.initTable = function () {
        let resource = H.resource;
        $('#group-table').bootstrapTable({
            toolbar: "#user-group-toolbar",
            columns: [[

                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        return '<a target="_blank" href="/portal/group#?env=' + G.env + '&groupId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        if (row.data != '*') {
                            value = value.substring(0, value.length / 2) + '...';
                            return '<a target="_blank" href="/portal/group#?env=' + G.env + '&groupId=' + row.data + '">' + value + '</a>';
                        }
                        else {
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupReadOp', value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupUpdateOp', value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupDeleteOp', value);
                    }
                },

                {
                    field: 'P',
                    title: 'PROPERTY',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupPropertyOp', value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupSyncOp', value);
                    }
                },

                {
                    field: 'A',
                    title: 'Activate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupActivateOp', value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupDeactivateOp', value);
                    }
                },

                {
                    field: 'M',
                    title: 'Member',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupMemberOp', value);
                    }
                },
                {
                    field: 'L',
                    title: 'Pull',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupPullOp', value);
                    }
                },
                {
                    field: 'H',
                    title: 'Raise',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('groupHealthOp', value);
                    }
                }

            ], []],
            data: $scope.user.groups,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return  resource.userAccess.js.msg3;
            },
            formatNoMatches: function () {
                return  resource.userAccess.js.msg4;
            }
        });
        $('#vs-table').bootstrapTable({
            toolbar: "#user-vs-toolbar",
            columns: [[

                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        return '<a target="_blank" href="/portal/vs#?env=' + G.env + '&vsId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        if (row.data != '*') {
                            value = value.substring(0, value.length / 2) + '...';
                            return '<a target="_blank" href="/portal/vs#?env=' + G.env + '&vsId=' + row.data + '">' + value + '</a>';
                        }
                        else {
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsReadOp', value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsUpdateOp', value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsDeleteOp', value);
                    }
                },
                {
                    field: 'A',
                    title: 'Activate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsActivateOp', value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsDeactivateOp', value);
                    }
                },
                {
                    field: 'P',
                    title: 'PROPERTY',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsPropertyOp', value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsSyncOp', value);
                    }
                },

                {
                    field: 'C',
                    title: 'Cert',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('vsCertOp', value);
                    }
                }

            ], []],
            data: $scope.user.vses,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return resource.userAccess.js.msg5;
            },
            formatNoMatches: function () {
                return  resource.userAccess.js.msg6;
            }
        });
        $('#slb-table').bootstrapTable({
            toolbar: "#user-slb-toolbar",
            columns: [[

                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        return '<a target="_blank" href="/portal/vs#?env=' + G.env + '&vsId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        if (row.data != '*') {
                            value = value.substring(0, value.length / 2) + '...';
                            return '<a target="_blank" href="/portal/slb#?env=' + G.env + '&slbId=' + row.data + '">' + value + '</a>';
                        }
                        else {
                            return '-';
                        }
                    }
                },
                {
                    field: 'R',
                    title: 'Read',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbReadOp', value);
                    }
                },
                {
                    field: 'U',
                    title: 'Update',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbUpdateOp', value);
                    }
                },
                {
                    field: 'E',
                    title: 'Delete',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbDeleteOp', value);
                    }
                },
                {
                    field: 'A',
                    title: 'Activate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbActivateOp', value);
                    }
                },
                {
                    field: 'D',
                    title: 'Deactivate',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbDeactivateOp', value);
                    }
                },
                {
                    field: 'P',
                    title: 'PROPERTY',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbPropertyOp', value);
                    }
                },
                {
                    field: 'S',
                    title: 'SYNC',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbSyncOp', value);
                    }
                },

                {
                    field: 'I',
                    title: 'ADMIN',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbAdminOp', value);
                    }
                },
                {
                    field: 'W',
                    title: 'WAF',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('slbWafOp', value);
                    }
                }

            ], []],
            data: $scope.user.slbs,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return resource.userAccess.js.msg7;
            },
            formatNoMatches: function () {
                return  resource.userAccess.js.msg8;
            }
        });
        $('#user-role-table').bootstrapTable({
            toolbar: "#user-roles-toolbar",
            columns: [[

                {
                    field: 'id',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index, field) {
                        return '<a href="/portal/userrole#?env=' + G.env + '&roleId=' + value + '">' + value + '</a>';
                    }
                },
                {
                    field: 'role-name',
                    title: 'Name',
                    align: 'left',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return '<a href="/portal/userrole#?env=' + G.env + '&roleId=' + row.id + '">' + value + '</a>';
                    }
                },
                {
                    field: 'discription',
                    title: 'Description',
                    align: 'left',
                    valign: 'middle'
                }
            ], []],
            data: $scope.user.roles,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return  resource.userAccess.js.msg9;
            },
            formatNoMatches: function () {
                return angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Roles': resource.userAccess.js.msg11;
            }
        });
        $('#auth-table').bootstrapTable({
            toolbar: "#user-auth-toolbar",
            columns: [[

                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'M',
                    title: 'Auth',
                    align: 'center',
                    width: '510px',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('authOp', value);
                    }
                }

            ], []],
            data: $scope.user.auths,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Auths": resource.userAccess.js.msg12;
            },
            formatNoMatches: function () {
                return angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Auths': resource.userAccess.js.msg13;
            }
        });
        $('#clean-table').bootstrapTable({
            toolbar: "#user-clean-toolbar",
            columns: [[
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    width: '510px',

                    field: 'M',
                    title: 'Clean',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('cleanOp', value);
                    }
                }

            ], []],
            data: $scope.user.cleans,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Cleans": resource.userAccess.js.msg14;
            },
            formatNoMatches: function () {
                return angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Cleans': resource.userAccess.js.msg155;
            }
        });
        $('#conf-table').bootstrapTable({
            toolbar: "#user-conf-toolbar",
            columns: [[

                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    width: '510px',

                    field: 'M',
                    title: 'Conf',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('confOp', value);
                    }
                }

            ], []],
            data: $scope.user.confs,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Confs": resource.userAccess.js.msg156;
            },
            formatNoMatches: function () {
                return angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Confs': resource.userAccess.js.msg157;
            }
        });
        $('#lock-table').bootstrapTable({
            toolbar: "#user-lock-toolbar",
            columns: [[

                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    width: '510px',

                    field: 'M',
                    title: 'Lock',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('lockOp', value);
                    }
                }
            ], []],
            data: $scope.user.locks,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return angular.equals(resource, {}) ? "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 Lock": resource.userAccess.js.msg158;
            },
            formatNoMatches: function () {
                return angular.equals(resource, {}) ? '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 Lock': resource.userAccess.js.msg159;
            }
        });
        $('#sync-table').bootstrapTable({
            toolbar: "#user-sync-toolbar",
            columns: [[
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    width: '510px',

                    field: 'M',
                    title: 'SyncError',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('syncOp', value);
                    }
                }
            ], []],
            data: $scope.user.syncs,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return  resource.userAccess.js.msg15;
            },
            formatNoMatches: function () {
                return  resource.userAccess.js.msg25;
            }
        });
        $('#ip-table').bootstrapTable({
            toolbar: "#user-ip-toolbar",
            columns: [[
                {
                    field: 'data',
                    title: 'ID',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    width: '510px',

                    field: 'M',
                    title: 'IP',
                    align: 'center',
                    valign: 'middle',
                    formatter: function (value, row, index, field) {
                        return $scope.formatter('ipOp', value);
                    }
                }

            ], []],
            data: $scope.user.ips,
            classes: "table-bordered  table-hover table-striped table",
            showRefresh: false,
            minimumCountColumns: 2,
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            resizable: true,
            resizeMode: 'overflow',
            responseHandler: "responseHandler",
            idField: 'ip',
            formatLoadingMessage: function () {
                return  resource.userAccess.js.msg35;
            },
            formatNoMatches: function () {
                return resource.userAccess.js.msg45;
            }
        });
    };
    $scope.formatter = function (className, value) {
        var str = '';
        if (value) {
            str = "<span class='fa fa-check status-green' title='有权限'></span>";
        }
        else {
            str = "<span class='fa fa-times status-red' title='无权限'></span>";
        }
        return str;
    };
    $scope.reloadTable = function () {
        $('#group-table').bootstrapTable("load", $scope.user.groups);
        $('#vs-table').bootstrapTable("load", $scope.user.vses);
        $('#slb-table').bootstrapTable("load", $scope.user.slbs);

        $('#conf-table').bootstrapTable("load", $scope.user.confs);
        $('#clean-table').bootstrapTable("load", $scope.user.cleans);
        $('#sync-table').bootstrapTable("load", $scope.user.syncs);
        $('#lock-table').bootstrapTable("load", $scope.user.locks);
        $('#ip-table').bootstrapTable("load", $scope.user.ips);
        $('#auth-table').bootstrapTable("load", $scope.user.auths);
        $('#user-role-table').bootstrapTable("load", $scope.user.roles);

        $('#group-table').bootstrapTable("hideLoading");
        $('#vs-table').bootstrapTable("hideLoading");
        $('#slb-table').bootstrapTable("hideLoading");
        $('#conf-table').bootstrapTable("hideLoading");
        $('#clean-table').bootstrapTable("hideLoading");
        $('#sync-table').bootstrapTable("hideLoading");
        $('#lock-table').bootstrapTable("hideLoading");
        $('#ip-table').bootstrapTable("hideLoading");
        $('#auth-table').bootstrapTable("hideLoading");
        $('#user-role-table').bootstrapTable("hideLoading");
    };
    $scope.compareTwoResources= function (r1, r2) {
        var a= r1.data.trim().toLowerCase()==r2.data.trim().toLowerCase();

        var b = r1['resource-type'].trim().toLowerCase()==r2['resource-type'].trim().toLowerCase();

        var e=false;

        var c = r1.operations;
        var d = r2.operations;
        if(c.length== d.length) {
            $.each(c, function (i, item) {
                var t = _.find(d, function (item2) {
                    return item2.type.trim().toLowerCase()==item.type.trim().toLowerCase();
                });
                if(t) e=true;
                else e=false; return;
            });
        }

        return a&b&e;
    };
    $scope.loadData = function (hashData) {
        var userName = '';
        $scope.user={};
        $scope.user.roles=[];
        var param2 = {
            type: 'info'
        };
        var request={};
        if($scope.query.user.id){
            request = {
                method: 'GET',
                url: G.baseUrl + '/api/auth/user?userName='+$scope.query.user.id
            };
        }else{
            request = {
                method: 'GET',
                url: G.baseUrl + '/api/auth/user/resources'
            };
        }

        var request2 = {
            method: 'GET',
            url: G.baseUrl + '/api/slbs',
            params: param2
        };

        var request3 = {
            method: 'GET',
            url: G.baseUrl + '/api/vses',
            params: param2
        };
        var request4 = {
            method: 'GET',
            url: G.baseUrl + '/api/groups',
            params: param2
        };
        var request5 = {
            method: 'GET',
            url: '/api/auth/current/user'
        };
        $q.all(
            [
                $http(request).success(
                    function (response) {
                        if(!$scope.query.user.id){
                            $scope.user = response;
                        }else{
                            $.each(response.roles, function (i, item) {
                                $scope.user.roles.push({
                                    id: item.id,
                                    'role-name': item['role-name'],
                                    discription: item['discription']
                                });
                            });

                            // reorg the users; resources
                            var resources =[];
                            $.each(response.roles, function (i,item) {
                                resources = resources.concat(item['data-resources']);
                            });
                            if(response['data-resources'] && response['data-resources'].length>0){
                                resources = resources.concat(response['data-resources']);
                            }

                            var uniqueNames = [];
                            $.each(resources, function(i, el){
                                var f = _.find(uniqueNames, function (item) {
                                    return $scope.compareTwoResources(el,item);
                                });

                                if(!f) uniqueNames.push(el);
                            });


                            var bindingResources=[];
                            $.each(uniqueNames, function (i, item) {
                                bindingResources.push({
                                    type: item['resource-type'],
                                    'data-resources':[
                                        item
                                    ]
                                })
                            });
                            $scope.user['user-resources']=bindingResources;
                        }
                    }
                ),
                $http(request5).success(
                    function (response) {
                        userName = response.name;
                        $scope.userName=userName;

                        if(!$scope.query.user.showid){
                            $scope.query.user.showid=userName;
                        }
                    }
                )
            ]).then(
            function () {
                $scope.user.confs = [];
                $scope.user.cleans = [];
                $scope.user.locks = [];
                $scope.user.syncs = [];
                $scope.user.auths = [];
                $scope.user.ips = [];
                $scope.user.slbs = [];
                $scope.user.vses = [];
                $scope.user.groups = [];

                var slbs = _.find($scope.user['user-resources'], function (item) {
                    return item['type'].toUpperCase() == 'SLB';
                });
                if (slbs) {
                    $scope.user.slbs = slbs['data-resources'];

                    $http(request2).success(
                        function (response) {
                            $scope.slbs = response.slbs;

                            $.each($scope.user.slbs, function (i, item) {
                                item.A = _.find(item.operations, function (item2) {
                                    return item2.type == 'ACTIVATE';
                                });

                                item.D = _.find(item.operations, function (item2) {
                                    return item2.type == 'DEACTIVATE';
                                });

                                item.U = _.find(item.operations, function (item2) {
                                    return item2.type == 'UPDATE';
                                });
                                item.E = _.find(item.operations, function (item2) {
                                    return item2.type == 'DELETE';
                                });
                                item.R = _.find(item.operations, function (item2) {
                                    return item2.type == 'READ';
                                });
                                item.P = _.find(item.operations, function (item2) {
                                    return item2.type == 'PROPERTY';
                                });
                                item.S = _.find(item.operations, function (item2) {
                                    return item2.type == 'SYNC';
                                });
                                item.I = _.find(item.operations, function (item2) {
                                    return item2.type == 'ADMIN_INFO';
                                });
                                item.W = _.find(item.operations, function (item2) {
                                    return item2.type == 'WAF';
                                });
                                var t = _.find($scope.slbs, function (item3) {
                                    return item3.id == item.data;
                                });
                                if (t) item.name = t.name;
                                else item.name = '-';
                            });
                        }
                    );
                }
                var vses = _.find($scope.user['user-resources'], function (item) {
                    return item['type'].toUpperCase() == 'VS';
                });
                if (vses) {
                    $scope.user.vses = vses['data-resources'];
                    $http(request3).success(
                        function (response) {
                            $scope.vses = response['virtual-servers'];
                            $.each($scope.user.vses, function (i, item) {
                                item.A = _.find(item.operations, function (item2) {
                                    return item2.type == 'ACTIVATE';
                                });

                                item.D = _.find(item.operations, function (item2) {
                                    return item2.type == 'DEACTIVATE';
                                });

                                item.U = _.find(item.operations, function (item2) {
                                    return item2.type == 'UPDATE';
                                });
                                item.E = _.find(item.operations, function (item2) {
                                    return item2.type == 'DELETE';
                                });
                                item.R = _.find(item.operations, function (item2) {
                                    return item2.type == 'READ';
                                });
                                item.P = _.find(item.operations, function (item2) {
                                    return item2.type == 'PROPERTY';
                                });
                                item.S = _.find(item.operations, function (item2) {
                                    return item2.type == 'SYNC';
                                });
                                item.C = _.find(item.operations, function (item2) {
                                    return item2.type == 'CERT';
                                });

                                var vs = _.find($scope.vses, function (item3) {
                                    return item3.id == item.data;
                                });
                                if (vs) item.name = vs.name;
                                else item.name = '-';
                            });
                        }
                    );
                }
                var groups = _.find($scope.user['user-resources'], function (item) {
                    return item['type'].toUpperCase() == 'GROUP';
                });
                if (groups) {
                    $scope.user.groups = groups['data-resources'];

                    $http(request4).success(
                        function (response) {
                            $scope.groups = response.groups;

                            $.each($scope.user.groups, function (i, item) {
                                item.A = _.find(item.operations, function (item2) {
                                    return item2.type == 'ACTIVATE';
                                });

                                item.D = _.find(item.operations, function (item2) {
                                    return item2.type == 'DEACTIVATE';
                                });

                                item.U = _.find(item.operations, function (item2) {
                                    return item2.type == 'UPDATE';
                                });
                                item.E = _.find(item.operations, function (item2) {
                                    return item2.type == 'DELETE';
                                });
                                item.R = _.find(item.operations, function (item2) {
                                    return item2.type == 'READ';
                                });
                                item.P = _.find(item.operations, function (item2) {
                                    return item2.type == 'PROPERTY';
                                });
                                item.S = _.find(item.operations, function (item2) {
                                    return item2.type == 'SYNC';
                                });
                                item.M = _.find(item.operations, function (item2) {
                                    return item2.type == 'OP_MEMBER';
                                });
                                item.L = _.find(item.operations, function (item2) {
                                    return item2.type == 'OP_PULL';
                                });
                                item.H = _.find(item.operations, function (item2) {
                                    return item2.type == 'OP_HEALTH_CHECK';
                                });
                                var t = _.find($scope.groups, function (item3) {
                                    return item3.id == item.data;
                                });
                                if (t) item.name = t.name;
                                else item.name = '-';
                            });
                        }
                    );
                }
                var confs = _.find($scope.user['user-resources'], function (item) {
                    return item['type'].toUpperCase() == 'CONF';
                });
                if (confs) {
                    $scope.user.confs = confs['data-resources'];
                }
                $.each($scope.user.confs, function (i, item) {
                    item.M = _.find(item.operations, function (item2) {
                        return item2.type == 'MAINTENANCE';
                    });
                });
                var cleans = _.find($scope.user['user-resources'], function (item) {
                    return item['type'].toUpperCase() == 'CLEAN';
                });
                if (cleans) {
                    $scope.user.cleans = cleans['data-resources'];
                }
                $.each($scope.user.cleans, function (i, item) {
                    item.M = _.find(item.operations, function (item2) {
                        return item2.type == 'MAINTENANCE';
                    });
                });
                var syncs = _.find($scope.user['user-resources'], function (item) {
                    return item['type'].toUpperCase() == 'SYNCERROR';
                });
                if (syncs) {
                    $scope.user.syncs = syncs['data-resources'];
                }
                $.each($scope.user.syncs, function (i, item) {
                    item.M = _.find(item.operations, function (item2) {
                        return item2.type == 'MAINTENANCE';
                    });
                });
                var locks = _.find($scope.user['user-resources'], function (item) {
                    return item['type'].toUpperCase() == 'LOCK';
                });
                if (locks) {
                    $scope.user.locks = locks['data-resources'];
                }
                $.each($scope.user.locks, function (i, item) {
                    item.M = _.find(item.operations, function (item2) {
                        return item2.type == 'MAINTENANCE';
                    });
                });
                var auths = _.find($scope.user['user-resources'], function (item) {
                    return item['type'].toUpperCase() == 'AUTH';
                });
                if (auths) {
                    $scope.user.auths = auths['data-resources'];
                }
                $.each($scope.user.auths, function (i, item) {
                    item.M = _.find(item.operations, function (item2) {
                        return item2.type == 'AUTH';
                    });
                });
                var ips = _.find($scope.user['user-resources'], function (item) {
                    return item['type'].toUpperCase() == 'IP';
                });
                if (ips) {
                    $scope.user.ips = ips['data-resources'];
                }
                $.each($scope.user.ips, function (i, item) {
                    item.M = _.find(item.operations, function (item2) {
                        return item2.type == 'OP_SERVER';
                    });
                });


            }).finally(
            function () {

            }
        );
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        if (hashData.env) {
            $scope.env = hashData.env;
        } else {
           $scope.env = 'pro';
        }

        if(hashData.userId){
            $scope.query.user.id=hashData.userId;
            $scope.query.user.showid=hashData.userId;
        }

        $scope.initTable();

        $('#group-table').bootstrapTable("removeAll");
        $('#vs-table').bootstrapTable("removeAll");
        $('#slb-table').bootstrapTable("removeAll");
        $('#user-role-table').bootstrapTable("removeAll");

        $('#group-table').bootstrapTable("showLoading");
        $('#vs-table').bootstrapTable("showLoading");
        $('#slb-table').bootstrapTable("showLoading");
        $('#user-role-table').bootstrapTable("showLoading");
        $scope.loadData(hashData);
        setTimeout(function () {
            if(!$scope.query.user.id){
                var param3 = {
                    userName: $scope.userName
                };
                var request6 = {
                    method: 'GET',
                    url: G.baseUrl + '/api/auth/user',
                    params: param3
                };
                $http(request6).success(
                    function (response) {
                        $scope.user.roles = response.roles;
                        $scope.reloadTable();
                    }
                );
            }else{
                $scope.reloadTable();
            }
        },2000);

    };
    H.addListener("userEditInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("user-info-area"), ['userEditInfoApp']);
