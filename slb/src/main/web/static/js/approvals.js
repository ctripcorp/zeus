var headerInfoApp = angular.module('headerInfoApp', ["angucomplete-alt"]);
headerInfoApp.controller('headerInfoController', function ($scope, $http) {
    $scope.query = {};
    $scope.generateLink = function (a) {
        var link = '';
        switch (a) {
            case 'config': {
                link = "/portal/backend/config#?env=" + G.env;
                break;
            }
            case 'rights': {
                link = "/portal/backend/rights#?env=" + G.env;
                break;
            }
            case 'users': {
                link = "/portal/backend/users#?env=" + G.env;
                break;
            }
            case 'role': {
                link = "/portal/backend/userroles#?env=" + G.env;
                break;
            }
            case 'access': {
                link = "/portal/backend/useraccess#?env=" + G.env;
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
        if (hashData.slbId) {
            $scope.query.slbId = hashData.slbId;
        }
    };
    H.addListener("infoLinksApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("header-area"), ['headerInfoApp']);

var approvalsInfoApp = angular.module('approvalsInfoApp', ["http-auth-interceptor"]);
approvalsInfoApp.controller('approvalsInfoController', function ($scope, $http, $q) {
    $scope.data = {
        category: [
            '未审批',
            '已审批'
        ],
        approved: [],
        notapproved: []
    };
    $scope.query = {
        category: $scope.data.category[0]
    };

    $scope.toggleCategory = function (category) {
        if (category == $scope.query.category) return;

        var pair = {
            timeStamp: new Date().getTime(),
            category: category
        };
        H.setData(pair)
    };
    $scope.isSelectedCategory = function (btn) {
        if ($scope.query.category == btn) return 'btn-info';
        return 'btn-default';
    };
    $scope.showPanel = function (panel) {
        return $scope.query.category == panel;
    };
    $scope.initTable = function () {
        $('#approvals-notapproved-table').bootstrapTable({
            toolbar: "#approvals-notapproved-toolbar",
            columns: [[
                {
                    field: 'apply-time',
                    title: '提交时间',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'apply-by',
                    title: '提交人',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var users = $scope.data.users;
                        if (!users) {
                            return;
                        }
                        var userName = value.split('(')
                        userName = userName[0];
                        var mail = users[userName];
                        if (mail) {
                            mail = mail['email'];
                        }
                        return '<a href="im:<sip:' + mail + '>">' + value + '<img src="/static/img/lync.png" title="联系"></a>';
                    }
                },
                {
                    title: '操作',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        var type = row['apply-type'];
                        var targets = row['apply-targets'];

                        targets = _.map(targets, function (v) {
                            return 'targetId=' + v;
                        });

                        var ops = row['apply-ops'];
                        var apply_by = row['apply-by'];
                        if (apply_by) {
                            apply_by = apply_by.split('(');
                            apply_by = apply_by[0];
                        }
                        var env = $scope.env;

                        var url = '/portal/user/rights-approve#?userName=' + apply_by + '&op=' + ops.join(',') + '&type=' + type + '&env=' + env + '&' + targets.join('&');
                        return '<a href="' + url + '" target="_blank">去审批</a>';
                    }
                }
            ], []],
            sortName: 'request-time',
            sortOrder: 'desc',
            data: $scope.roles,
            classes: "table-bordered  table-hover table-striped table",
            search: true,
            showRefresh: false,
            showColumns: true,
            minimumCountColumns: 2,
            pagination: true,
            idField: 'request-time',
            resizable: true,
            resizeMode: 'overflow',
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 所有未审批的申请";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的 未审批的申请';
            }
        });
        $('#approvals-approved-table').bootstrapTable({
            toolbar: "#approvals-approved-toolbar",
            columns: [[
                {
                    field: 'apply-time',
                    title: '提交时间',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'apply-by',
                    title: '提交人',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'apply-type',
                    title: '申请类型',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'apply-targets',
                    title: '申请对象',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value.join(',');
                    }
                },
                {
                    field: 'apply-ops',
                    title: '申请操作',
                    align: 'left',
                    valign: 'middle',
                    sortable: true,
                    formatter: function (value, row, index) {
                        return value.join(',');
                    }
                },
                {
                    field: 'approved-by',
                    title: '审批人',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                },
                {
                    field: 'approved-time',
                    title: '通过时间',
                    align: 'left',
                    valign: 'middle',
                    sortable: true
                }
            ], []],
            sortName: 'request-time',
            sortOrder: 'desc',
            data: $scope.roles,
            classes: "table-bordered  table-hover table-striped table",
            search: true,
            showRefresh: false,
            showColumns: true,
            minimumCountColumns: 2,
            pagination: true,
            idField: 'id',
            resizable: true,
            resizeMode: 'overflow',
            pageSize: 20,
            sidePagination: 'client',
            pageList: [20, 40, 80, 200],
            formatLoadingMessage: function () {
                return "<img class='loading-img' src='/static/img/loading_100.gif' /> 正在加载 所有通过的申请";
            },
            formatNoMatches: function () {
                return '<i class="fa status-alert fa-exclamation-triangle" aria-hidden="true"></i> 没有找到匹配的通过的申请';
            }
        });
    };
    $scope.loadData = function () {
        var groupsRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/groups?type=info'
        };
        var policyRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/policies'
        };

        var notApprovedRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/approvals?approved=false'
        };

        var approvedRequest = {
            method: 'GET',
            url: G.baseUrl + '/api/approvals?approved=true'
        };

        var notApprovedHttp = $http(approvedRequest).success(
            function (resp) {
                $scope.data.approved = resp;
                $('#approvals-approved-table').bootstrapTable("load", $scope.data.approved ? $scope.data.approved : []);
                $('#approvals-approved-table').bootstrapTable("hideLoading");
            }
        );
        var approvedHttp = $http(notApprovedRequest).success(
            function (resp) {
                $scope.data.notapproved = resp;
            }
        );

        var groupsHttp = $http(groupsRequest).success(function (response) {
            $scope.data.groups = _.pluck(response['groups'], 'id');
        });

        var policyHttp = $http(policyRequest).success(function (response) {
            $scope.data.policies = _.pluck(response['traffic-policies'], 'id');
        });

        $q.all([
            groupsHttp,
            policyHttp,
            approvedHttp,
            notApprovedHttp
        ]).then(function () {
            var groups = $scope.data.groups;
            var policies = $scope.data.policies;

            var filtered = _.filter($scope.data.notapproved, function (v) {
                var type = v['apply-type'];
                var targets = v['apply-targets'];

                if (!type) return false;

                if (type.toLowerCase() == 'policy') {
                    var diff = _.difference(policies, targets);
                    if (diff.length == policies.length - targets.length) {
                        // these ids exists
                        return true;
                    }
                    return false;
                }
                if (type.toLowerCase() == 'group') {
                    var diff = _.difference(groups, targets);
                    if (diff.length == groups.length - targets.length) {
                        // these ids exists
                        return true;
                    }
                    return false;
                }
            });
            $scope.data.notapproved = filtered;

            $('#approvals-notapproved-table').bootstrapTable("load", filtered ? filtered : []);
            $('#approvals-notapproved-table').bootstrapTable("hideLoading");
        });
    };
    $scope.hashChanged = function (hashData) { $scope.resource = H.resource;
        $scope.env = hashData.env || 'uat';
        $scope.query.category = hashData.category || $scope.data.category[0];

        $scope.initTable();
        $('#approvals-notapproved-table').bootstrapTable("removeAll");
        $('#approvals-notapproved-table').bootstrapTable("showLoading");
        $('#approvals-approved-table').bootstrapTable("removeAll");
        $('#approvals-approved-table').bootstrapTable("showLoading");

        var request = {
            method: 'GET',
            url: G.baseUrl + '/api/auth/users'
        };
        $http(request).success(
            function (resp) {
                $scope.data.users = _.indexBy(resp.users, 'user-name');
                $scope.loadData();
            }
        );


    };
    H.addListener("rolesInfoApp", $scope, $scope.hashChanged);
});
angular.bootstrap(document.getElementById("approvals-info-area"), ['approvalsInfoApp']);